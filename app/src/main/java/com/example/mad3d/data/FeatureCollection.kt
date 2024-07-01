package com.example.mad3d.data

// Data class representing a collection of geographical features
data class FeatureCollection(
    val type: String,
    val features: List<Feature>
)

data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

// Data class representing the geometry of a feature
data class Geometry(
    val type: String, // the type of geometry, e.g., "Point"
    val coordinates: List<Double> // the coordinates of the geometry, typically a list of longitude and latitude
)

// Data class representing the properties of a feature
data class Properties(
    val osm_id: Long, // the OpenStreetMap ID of the feature
    val name: String?, // the name of the feature, if available
    val place: String?, // the place type of the feature, if available
    val featureType: String? // the specific type of feature
)
