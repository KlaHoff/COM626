package com.example.mad3d.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.mad3d.R
import com.example.mad3d.data.POIRepository
import com.example.mad3d.data.PoiDao
import com.example.mad3d.data.PoiDatabase
import com.example.mad3d.databinding.ActivityMainBinding
import com.example.mad3d.databinding.DialogFilterPoiBinding
import com.example.mad3d.ui.explore.ExploreFragment
import com.example.mad3d.ui.map.MapFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: PoiDatabase
    private val poiDao: PoiDao by lazy { database.getPoiDao() }
    private lateinit var locationViewModel: LocationViewModel

    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.androidservices.LOCATION_UPDATE") {
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                locationViewModel.updateLocation(latitude, longitude)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener(this)
        binding.fab.setOnClickListener { showFilterPOIDialog() }

        database = PoiDatabase.createDatabase(this)

        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.frame_content, MapFragment())
            }
        }
        requestPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction("com.example.androidservices.LOCATION_UPDATE")
        }
        registerReceiver(locationUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationUpdateReceiver)
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            initService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initService()
        } else {
            Toast.makeText(this, "GPS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initService() {
        val intent = Intent(this, GpsService::class.java)
        startService(intent)
    }

    private fun showFilterPOIDialog() {
        val dialogBinding = DialogFilterPoiBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.download_POIs -> {
            downloadPOIs()
            true
        }

        R.id.delete_POIs -> {
            Thread {
                poiDao.deleteAllPois()
            }.start()
            Toast.makeText(this, "All POIs deleted", Toast.LENGTH_SHORT).show()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun downloadPOIs() {
        val bbox = "-1.424401,50.896821,-1.404401,50.916821"
        val poiRepository = POIRepository(this)
        poiRepository.fetchAndStorePOIs(bbox)
        Toast.makeText(this, "Downloading POIs...", Toast.LENGTH_SHORT).show()
    }

    private fun onMapClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, MapFragment())
        }
        return true
    }

    private fun onExploreClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, ExploreFragment())
        }
        return true
    }

    private fun onARClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, ARFragment())
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_explore -> onExploreClicked()
        R.id.nav_map -> onMapClicked()
        R.id.nav_ar -> onARClicked()
        else -> false
    }
}