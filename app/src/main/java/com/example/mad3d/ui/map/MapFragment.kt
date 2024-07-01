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

    // to get the location data
    private lateinit var locationViewModel: LocationViewModel

    private lateinit var mapView: MapView

    // overlay to show the user's location on the map
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var poiDatabase: PoiDatabase

    // variables to store the last known latitude and longitude
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // configure the map settings
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(requireContext()))

        mapView = view.findViewById(R.id.map1)
        // set the default zoom level and center of the map
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(GeoPoint(50.909698, -1.404351)) //Southampton

        // create and configure the overlay to show the user's location
        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        mapView.overlays.add(myLocationOverlay)

        // create and add a compass overlay to the map
        val compassOverlay = CompassOverlay(context, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // get the location view model
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        // observe the location data and update the map view when it changes
        locationViewModel.latLon.observe(viewLifecycleOwner) { latLon ->
            val lat = latLon.lat
            val lon = latLon.lon
            if (shouldUpdateMap(lat, lon)) {
                updateMapView(lat, lon)
            }
        }

        // get the database instance
        poiDatabase = PoiDatabase.getDatabase(requireContext())

        // get the filter type from the arguments
        val filter = arguments?.getString("FILTER_TYPE")
        // fetch and display the POIs based on the filter
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

    // this method checks if the map should be updated based on the location change
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

    // this method updates the map view to the new location
    private fun updateMapView(latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        mapView.controller.setCenter(geoPoint)
        myLocationOverlay.enableFollowLocation()
    }

    // this method fetches the POIs from the database and displays them on the map
    private fun fetchAndDisplayPois(filter: String? = null) {
        // use a background thread to fetch the data
        CoroutineScope(Dispatchers.IO).launch {
            val poiList = when {
                filter.isNullOrEmpty() -> poiDatabase.getPoiDao().getAllPois() // get all POIs if no filter
                filter == "other" -> poiDatabase.getPoiDao().getPoisExcludingTypes(listOf("restaurant", "pub", "cafe", "suburb")) // get POIs excluding specified types
                else -> poiDatabase.getPoiDao().getPoisByType(filter) // get POIs of the specified type
            }
            // update the map on the main thread
            CoroutineScope(Dispatchers.Main).launch {
                addMarkersToMap(poiList)
            }
        }
    }

    // this method adds markers to the map for each POI
    private fun addMarkersToMap(poiList: List<Poi>) {
        // clear existing markers
        mapView.overlays.clear()
        mapView.overlays.add(myLocationOverlay)
        for (poi in poiList) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(poi.lat, poi.lon)
            marker.title = poi.name ?: "POI"
            marker.snippet = poi.featureType
            mapView.overlays.add(marker)
        }
        mapView.invalidate() // refresh the map view
    }
}
