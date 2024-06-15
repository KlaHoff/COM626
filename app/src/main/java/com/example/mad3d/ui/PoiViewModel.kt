package com.example.mad3d.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad3d.data.PoiRepository
import com.example.mad3d.data.Poi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PoiViewModel(private val repository: PoiRepository) : ViewModel() {

    private val _pois = MutableLiveData<List<Poi>>()
    val pois: LiveData<List<Poi>> get() = _pois

    fun fetchPois() {
        viewModelScope.launch(Dispatchers.IO) {
            val pois = repository.getAllPois()
            _pois.postValue(pois)
        }
    }

    fun fetchAndStorePois(bbox: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchAndStorePois(bbox)
            val pois = repository.getAllPois()
            _pois.postValue(pois)
        }
    }

    fun deleteAllPois() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllPOIs()
            _pois.postValue(emptyList()) // live data list will be cleared after deletion
        }
    }
}
