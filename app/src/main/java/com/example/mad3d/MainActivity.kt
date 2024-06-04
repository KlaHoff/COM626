package com.example.mad3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.mad3d.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonMap.setOnClickListener { onMapClicked() }
        binding.buttonExplore.setOnClickListener { onExploreClicked() }
        binding.buttonAR.setOnClickListener { onARClicked() }

        }

        private fun onMapClicked() {
            supportFragmentManager.commit {
                replace(R.id.frame_content, MapFragment())
            }
        }

        private fun onExploreClicked() {
            supportFragmentManager.commit {
                replace(R.id.frame_content, ExploreFragment())
            }
        }

        private fun onARClicked() {
            supportFragmentManager.commit {
                replace(R.id.frame_content, ARFragment())
            }
        }
}