package com.example.mad3d.data

data class FeatureCollection(
    val type: String,
    val features: List<Feature>
)

data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

data class Properties(
    val osm_id: Long,
    val name: String?,
    val place: String?,
    val featureType: String?
)
