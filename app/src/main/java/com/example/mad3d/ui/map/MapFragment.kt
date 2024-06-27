package com.example.mad3d.ui.map

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mad3d.R
import com.example.mad3d.data.Poi
import com.example.mad3d.data.PoiDatabase
import com.example.mad3d.ui.LocationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var poiDatabase: PoiDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

        mapView = view.findViewById(R.id.map1)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(GeoPoint(0.0, 0.0))

        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        mapView.overlays.add(myLocationOverlay)

        val compassOverlay = CompassOverlay(context, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        locationViewModel.latLon.observe(viewLifecycleOwner, Observer { latLon ->
            updateMapView(latLon.lat, latLon.lon)
        })

        poiDatabase = PoiDatabase.getDatabase(requireContext())
        fetchAndDisplayPois()

        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }

    private fun updateMapView(latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        mapView.controller.setCenter(geoPoint)
        myLocationOverlay.enableFollowLocation()
    }

    private fun fetchAndDisplayPois() {
        CoroutineScope(Dispatchers.IO).launch {
            val poiList = poiDatabase.getPoiDao().getAllPois()
            CoroutineScope(Dispatchers.Main).launch {
                addMarkersToMap(poiList)
            }
        }
    }

    private fun addMarkersToMap(poiList: List<Poi>) {
        for (poi in poiList) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(poi.lat, poi.lon)
            marker.title = poi.name ?: "POI"
            marker.snippet = poi.featureType
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }
}
