package com.example.mad3d.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.mad3d.R
import com.example.mad3d.data.POIRepository
import com.example.mad3d.data.PoiDao
import com.example.mad3d.data.PoiDatabase
import com.example.mad3d.databinding.ActivityMainBinding
import com.example.mad3d.databinding.DialogFilterPoiBinding
import com.example.mad3d.ui.explore.ExploreFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: PoiDatabase
    private val poiDao: PoiDao by lazy { database.getPoiDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener(this)
        binding.fab.setOnClickListener { showFilterPOIDialog() }

        database = PoiDatabase.createDatabase(this)

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