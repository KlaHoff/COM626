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
    // Retrofit service to fetch POI data from a web service
    private val poiService: PoiService
    // DAO (data access object) for POI database operations
    private val poiDao: PoiDao

    init {
        // Initialize Retrofit for network operations
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hikar.org/webapp/") // base URL for the web service
            .addConverterFactory(GsonConverterFactory.create()) // for JSON conversion
            .build()

        // Create the POI service from the Retrofit instance
        poiService = retrofit.create(PoiService::class.java)
        poiDao = PoiDatabase.getDatabase(context).getPoiDao()
    }

    // Fetch POIs from the web service and store them in the database
    fun fetchAndStorePOIs(bbox: String, callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            // Execute the network request to get POIs
            val response = poiService.getPois(bbox, "poi", "4326").execute()
            // Check if the response was successful
            if (response.isSuccessful) {
                // Get the feature collection from the response body
                val featureCollection = response.body()
                // If the feature collection is not null, process the features
                featureCollection?.features?.let { features ->
                    // List to hold new POIs to be inserted into the database
                    val poisToInsert = mutableListOf<Poi>()
                    // Loop through each feature in the collection
                    features.forEach { feature ->
                        // Get the coordinates and properties of the feature
                        val coordinates = feature.geometry.coordinates
                        val properties = feature.properties
                        // Create a LonLat object from the coordinates
                        val lonLat = LonLat(coordinates[0], coordinates[1])
                        // Project the LonLat to Easting/Northing
                        val projection = SphericalMercatorProjection()
                        val eastNorth = projection.project(lonLat)
                        // Create a POI object from the feature data
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
                        // If the POI does not exist, add it to the list of POIs to insert
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
            // Call the callback function to signal completion
            callback()
        }
    }
}
