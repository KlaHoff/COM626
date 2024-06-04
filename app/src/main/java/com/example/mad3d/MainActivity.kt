package com.example.mad3d

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.mad3d.databinding.ActivityMainBinding
import com.example.mad3d.databinding.DialogFilterPoiBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener(this)
        binding.fab.setOnClickListener { showFilterPOIDialog() }
    }

    private fun showFilterPOIDialog() {
        val dialogBinding = DialogFilterPoiBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(this)
            .setTitle("Write something")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                Toast.makeText(
                    this,
                    "The text is: ${dialogBinding.editText.text}",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.download_POIs -> {
            true
        }

        R.id.delete_POIs -> {
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
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