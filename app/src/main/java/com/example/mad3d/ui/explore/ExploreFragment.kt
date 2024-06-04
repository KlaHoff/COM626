package com.example.mad3d.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mad3d.R
import com.example.mad3d.data.Poi
import com.example.mad3d.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = PoisAdapter(
            //dummy text just for trying it out
            pois = listOf(
                Poi(name = "First POI", place = "Some poi place", featureType = "Some feature type", lat = 1.0, lon = 1.0, x = 1.0, y = 1.0, osmId = 2222222),
                Poi(name = "Another POI", place = "Some poi place", featureType = "Pub", lat = 1.0, lon = 1.0, x = 1.0, y = 1.0, osmId = 223),
                Poi(name = "And another one", place = "Some poi place", featureType = "Restaurant", lat = 1.0, lon = 1.0, x = 1.0, y = 1.0, osmId = 3231)
            )
        )
    }

}