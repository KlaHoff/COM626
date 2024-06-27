package com.example.mad3d.data

import com.example.mad3d.data.proj.LonLat
import com.example.mad3d.data.proj.SphericalMercatorProjection
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class POIRepository(private val context: Context) {
    private val poiService: PoiService
    private val poiDao: PoiDao

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hikar.org/webapp/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        poiService = retrofit.create(PoiService::class.java)
        poiDao = PoiDatabase.createDatabase(context).getPoiDao()
    }

    fun fetchAndStorePOIs(bbox: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = poiService.getPois(bbox, "poi", "4326").execute()
            if (response.isSuccessful) {
                val featureCollection = response.body()
                featureCollection?.features?.let { features ->
                    val poisToInsert = mutableListOf<Poi>()
                    features.forEach { feature ->
                        val coordinates = feature.geometry.coordinates
                        val properties = feature.properties
                        val lonLat = LonLat(coordinates[0], coordinates[1])
                        val projection = SphericalMercatorProjection()
                        val eastNorth = projection.project(lonLat)
                        val poi = Poi(
                            osmId = properties.osm_id,
                            name = properties.name,
                            place = properties.place,
                            featureType = properties.featureType,
                            lat = coordinates[1],
                            lon = coordinates[0],
                            x = eastNorth.easting,
                            y = eastNorth.northing
                        )

                        // Check if a POI with the same osmId already exists in the database
                        val existingPoi = poiDao.getPoiByOsmId(poi.osmId)
                        if (existingPoi == null) {
                            poisToInsert.add(poi)
                        }
                    }

                    // Insert only the POIs that are not already in the database
                    if (poisToInsert.isNotEmpty()) {
                        poiDao.createPoi(poisToInsert)
                    }
                }
            }
        }
    }
}