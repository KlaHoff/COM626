package com.example.mad3d.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
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
import kotlin.math.abs

class MapFragment : Fragment() {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var poiDatabase: PoiDatabase
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(requireContext()))

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

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        locationViewModel.latLon.observe(viewLifecycleOwner) { latLon ->
            val lat = latLon.lat
            val lon = latLon.lon
            if (shouldUpdateMap(lat, lon)) {
                updateMapView(lat, lon)
            }
        }

        poiDatabase = PoiDatabase.getDatabase(requireContext())

        val filter = arguments?.getString("FILTER_TYPE")
        fetchAndDisplayPois(filter)

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

    private fun shouldUpdateMap(lat: Double, lon: Double): Boolean {
        val threshold = 0.0001 // threshold for significant location change
        return if (lastLat == null || lastLon == null) {
            lastLat = lat
            lastLon = lon
            true
        } else {
            val latChanged = abs(lat - lastLat!!) > threshold
            val lonChanged = abs(lon - lastLon!!) > threshold
            if (latChanged || lonChanged) {
                lastLat = lat
                lastLon = lon
                true
            } else {
                false
            }
        }
    }

    private fun updateMapView(latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        mapView.controller.setCenter(geoPoint)
        myLocationOverlay.enableFollowLocation()
    }

    private fun fetchAndDisplayPois(filter: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val poiList = if (filter.isNullOrEmpty()) {
                poiDatabase.getPoiDao().getAllPois()
            } else {
                poiDatabase.getPoiDao().getPoisByType(filter)
            }
            CoroutineScope(Dispatchers.Main).launch {
                addMarkersToMap(poiList)
            }
        }
    }

    private fun addMarkersToMap(poiList: List<Poi>) {
        mapView.overlays.clear()
        mapView.overlays.add(myLocationOverlay)
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
