package com.example.mad3d.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// to hold latitude and longitude values
data class LatLon(var lat: Double = 0.0, var lon: Double = 0.0)

// view model to manage location data
class LocationViewModel : ViewModel() {
    // mutable live data to store the location
    private val _latLon = MutableLiveData<LatLon>()
    // live data that other classes can observe
    val latLon: LiveData<LatLon> = _latLon

    // function to update the location data
    fun updateLocation(lat: Double, lon: Double) {
        _latLon.value = LatLon(lat, lon)
    }
}
