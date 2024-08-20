package com.example.mad3d.data

import android.content.Context
import com.example.mad3d.data.proj.LonLat
import com.example.mad3d.data.proj.SphericalMercatorProjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class POIRepository(private val context: Context) {
    private val poiService: PoiService
    private val poiDao: PoiDao

    init {
        poiService = createPoiService()
        poiDao = PoiDatabase.getDatabase(context).getPoiDao()
    }

    // Initialize Retrofit for network operations
    private fun createPoiService(): PoiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hikar.org/webapp/") // base URL for the web service
            .addConverterFactory(GsonConverterFactory.create()) // for JSON conversion
            .build()
        return retrofit.create(PoiService::class.java)
    }

    // Fetch POIs from the web service and store them in the database
    fun fetchAndStorePOIs(bbox: String, callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            // Execute the network request to get POIs
            val response = poiService.getPois(bbox, "poi", "4326").execute()
            if (response.isSuccessful) {
                response.body()?.features?.let { features ->
                    // Process the features and insert new POIs into the database
                    val poisToInsert = processFeatures(features)
                    if (poisToInsert.isNotEmpty()) {
                        poiDao.createPoi(poisToInsert)
                    }
                }
            }
            // Call the callback function to signal completion
            callback()
        }
    }

    // Process the features received from the web service
    private suspend fun processFeatures(features: List<Feature>): List<Poi> {
        val poisToInsert = mutableListOf<Poi>()
        for (feature in features) {
            val poi = createPoiFromFeature(feature)
            // Check if a POI with the same osmId already exists in the database
            val existingPoi = poiDao.getPoiByOsmId(poi.osmId)
            if (existingPoi == null) {
                poisToInsert.add(poi)
            }
        }
        return poisToInsert
    }

    // Create a POI object from the feature data
    private fun createPoiFromFeature(feature: Feature): Poi {
        val coordinates = feature.geometry.coordinates
        val properties = feature.properties
        val lonLat = LonLat(coordinates[0], coordinates[1])
        // Project the LonLat to Easting/Northing
        val projection = SphericalMercatorProjection()
        val eastNorth = projection.project(lonLat)

        return Poi(
            osmId = properties.osm_id,
            name = properties.name,
            place = properties.place,
            featureType = properties.featureType,
            lat = coordinates[1],
            lon = coordinates[0],
            x = eastNorth.easting,
            y = eastNorth.northing
        )
    }
}
