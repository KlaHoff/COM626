package com.example.mad3d.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Poi(
    @PrimaryKey val osmId: Long,
    val name: String?,
    val place: String?,
    val featureType: String?,
    val lat: Double,
    val lon: Double,
    val x: Double,
    val y: Double
)