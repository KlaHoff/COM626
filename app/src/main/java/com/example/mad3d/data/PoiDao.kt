package com.example.mad3d.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PoiDao {

    @Insert
    fun createPoi(poi: List<Poi>)

    @Update
    fun updatePoi(poi: Poi)

    @Delete
    fun deletePoi(poi: Poi)

    @Query("SELECT * FROM poi")
    fun getAllPois(): List<Poi>

    @Query("DELETE FROM poi")
    fun deleteAllPois()

    @Query("SELECT * FROM poi WHERE osmId = :osmId")
    suspend fun getPoiByOsmId(osmId: Long): Poi?

    @Query("SELECT * FROM poi WHERE featureType = :type")
    fun getPoisByType(type: String): List<Poi>

    @Query("SELECT * FROM poi WHERE featureType NOT IN (:types)")
    fun getPoisExcludingTypes(types: List<String>): List<Poi>
}