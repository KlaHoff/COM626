package com.example.mad3d.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.mad3d.R
import com.example.mad3d.data.POIRepository
import com.example.mad3d.data.PoiDao
import com.example.mad3d.data.PoiDatabase
import com.example.mad3d.data.proj.Algorithms
import com.example.mad3d.databinding.ActivityMainBinding
import com.example.mad3d.databinding.DialogFilterPoiBinding
import com.example.mad3d.ui.ar.ARFragment
import com.example.mad3d.ui.explore.ExploreFragment
import com.example.mad3d.ui.map.MapFragment
import com.example.mad3d.utils.FilterPreferenceHelper
import com.example.mad3d.utils.NotificationUtils
import com.example.mad3d.utils.PermissionsUtils
import com.example.mad3d.utils.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    // view binding for the activity
    private lateinit var binding: ActivityMainBinding
    // database instance
    private lateinit var database: PoiDatabase
    // data access object for POIs
    private val poiDao: PoiDao by lazy { database.getPoiDao() }
    // view model for location data
    private lateinit var locationViewModel: LocationViewModel
    // distance threshold for proximity checks in meters
    private val proximityThreshold = 100.0

    // broadcast receiver to handle location updates
    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.androidservices.LOCATION_UPDATE") {
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                locationViewModel.updateLocation(latitude, longitude)
                checkProximityToPois(latitude, longitude)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up navigation and floating action button
        binding.bottomNav.setOnItemSelectedListener(this)
        binding.fab.setOnClickListener { showFilterPOIDialog() }

        // initialize the database
        database = PoiDatabase.getDatabase(this)

        // initialize the location view model
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        // request necessary permissions
        PermissionsUtils.requestPermissions(this)
    }

    override fun onResume() {
        super.onResume()
        // register the location update receiver
        val filter = IntentFilter().apply {
            addAction("com.example.androidservices.LOCATION_UPDATE")
        }
        registerReceiver(locationUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        // unregister the location update receiver
        unregisterReceiver(locationUpdateReceiver)
    }

    // handles the result of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    // initializes the GPS service
    fun initService() {
        val intent = Intent(this, GpsService::class.java)
        startService(intent)
    }

    // shows the filter dialog for selecting POI types
    private fun showFilterPOIDialog() {
        val dialogBinding = DialogFilterPoiBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)

        // set the current filter in the dialog
        val currentFilter = FilterPreferenceHelper.getFilter(this)
        when (currentFilter) {
            "restaurant" -> dialogBinding.radioRestaurants.isChecked = true
            "pub" -> dialogBinding.radioPubs.isChecked = true
            "cafe" -> dialogBinding.radioCafes.isChecked = true
            "suburb" -> dialogBinding.radioSuburbs.isChecked = true
            "other" -> dialogBinding.radioOther.isChecked = true
            else -> dialogBinding.radioNoFilter.isChecked = true
        }

        // handle filter selection
        dialogBinding.filterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.radio_restaurants -> "restaurant"
                R.id.radio_pubs -> "pub"
                R.id.radio_cafes -> "cafe"
                R.id.radio_suburbs -> "suburb"
                R.id.radio_other -> "other"
                else -> null
            }
            applyFilter(filter)
            dialog.dismiss()
        }
        dialog.show()
    }

    // applies the selected filter
    private fun applyFilter(filter: String?) {
        FilterPreferenceHelper.saveFilter(this, filter)

        val bundle = Bundle().apply {
            putString("FILTER_TYPE", filter)
        }

        // update the current fragment with the new filter
        supportFragmentManager.findFragmentById(R.id.frame_content)?.let { fragment ->
            fragment.arguments = bundle
            supportFragmentManager.commit {
                replace(R.id.frame_content, fragment::class.java, fragment.arguments)
            }
        }
    }

    // inflates the options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    // handles menu item selections
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.download_POIs -> {
            downloadPOIs()
            true
        }

        R.id.delete_POIs -> {
            // delete all POIs in a background thread
            Thread {
                poiDao.deleteAllPois()
            }.start()
            reloadCurrentFragment()
            ToastUtils.showToast(this, "All POIs deleted")
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    // downloads POIs based on the current location
    private fun downloadPOIs() {
        locationViewModel.latLon.value?.let { location ->
            val bbox =
                "${location.lon - 0.01},${location.lat - 0.01},${location.lon + 0.01},${location.lat + 0.01}"
            val poiRepository = POIRepository(this)
            showLoading(true)
            // fetch and store POIs in the background
            poiRepository.fetchAndStorePOIs(bbox) {
                runOnUiThread {
                    showLoading(false)
                    reloadCurrentFragment()  // reload current fragment to show new POIs
                    ToastUtils.showToast(this, "POIs downloaded")
                }
            }
            ToastUtils.showToast(this, "Downloading POIs... please wait")
        } ?: run {
            ToastUtils.showToast(this, "Location not available")
        }
    }

    // shows or hides the loading indicator
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    // handles click on the map navigation bar icon
    private fun onMapClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, MapFragment())
        }
        return true
    }

    // handles click on the explore navigation bar icon
    private fun onExploreClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, ExploreFragment())
        }
        return true
    }

    // handles click on the AR navigation bar icon
    private fun onARClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, ARFragment())
        }
        return true
    }

    // handles navigation bar selections
    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_explore -> onExploreClicked()
        R.id.nav_map -> onMapClicked()
        R.id.nav_ar -> onARClicked()
        else -> false
    }

    // reloads the current fragment --> to refresh UI for example after downloading POIs
    private fun reloadCurrentFragment() {
        supportFragmentManager.findFragmentById(R.id.frame_content)?.let { fragment ->
            supportFragmentManager.commit {
                replace(R.id.frame_content, fragment::class.java, null)
            }
        }
    }

    // checks the proximity of the user to each POI
    private fun checkProximityToPois(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val pois = poiDao.getAllPois()
            for (poi in pois) {
                val distance = Algorithms.haversineDist(longitude, latitude, poi.lon, poi.lat)
                if (distance <= proximityThreshold) {
                    NotificationUtils.sendProximityNotification(this@MainActivity, poi)
                    break
                }
            }
        }
    }

    // handles configuration changes (screen orientation changes)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // hide navigation and action bars in landscape mode
            binding.bottomNav.visibility = View.GONE
            binding.fab.visibility = View.GONE
            supportActionBar?.hide()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // show navigation and action bars in portrait mode
            binding.bottomNav.visibility = View.VISIBLE
            binding.fab.visibility = View.VISIBLE
            supportActionBar?.show()
        }

        // notify ARFragment of the orientation change
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
        if (fragment is ARFragment) {
            fragment.updateOrientationMessage()
        }
    }
}
