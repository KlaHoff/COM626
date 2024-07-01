package com.example.mad3d.data

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query

// Retrofit service interface for fetching POI data from a web service
interface PoiService {
    // HTTP GET request to fetch POIs based on specified query parameters
    @GET("map")
    fun getPois(
        @Query("bbox") bbox: String, // the bounding box to specify the area for fetching POIs
        @Query("layers") layers: String,
        @Query("outProj") outProj: String
    ): Call<FeatureCollection> // the call object that returns a feature collection in the response
}
