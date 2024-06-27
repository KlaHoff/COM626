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
import com.example.mad3d.ui.LocationViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

        mapView = view.findViewById(R.id.map1)
        mapView.controller.setZoom(14.0)
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

        return view
    }

    private fun updateMapView(latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        mapView.controller.setCenter(geoPoint)
        myLocationOverlay.enableFollowLocation()
    }
}
