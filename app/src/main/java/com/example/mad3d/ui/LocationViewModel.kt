package com.example.mad3d.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class LatLon(var lat: Double = 0.0, var lon: Double = 0.0)

class LocationViewModel : ViewModel() {
    private val _latLon = MutableLiveData<LatLon>()
    val latLon: LiveData<LatLon> = _latLon

    fun updateLocation(lat: Double, lon: Double) {
        _latLon.value = LatLon(lat, lon)
        Log.d("LocationViewModel", "Location updated to lat: $lat, lon: $lon")
    }
}
