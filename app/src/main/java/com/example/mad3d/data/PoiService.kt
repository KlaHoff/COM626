package com.example.mad3d.data

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query

interface PoiService {
    @GET("map")
    fun getPois(@Query("bbox") bbox: String, @Query("layers") layers: String, @Query("outProj") outProj: String): Call<FeatureCollection>
}