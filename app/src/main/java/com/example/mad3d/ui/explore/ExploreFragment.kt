package com.example.mad3d.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mad3d.data.PoiDao
import com.example.mad3d.data.PoiDatabase
import com.example.mad3d.databinding.FragmentExploreBinding
import com.example.mad3d.ui.LocationViewModel
import kotlin.concurrent.thread

class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding

    private val poiDao: PoiDao by lazy {
        PoiDatabase.getDatabase(requireContext()).getPoiDao()
    }

    // this gets the shared view model for location
    private val locationViewModel: LocationViewModel by activityViewModels()

    // this holds the adapter for the RecyclerView
    private lateinit var adapter: PoisAdapter

    // this method is called to create the view for the fragment
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

        // initialize the adapter with an empty list and default location
        adapter = PoisAdapter(emptyList(), 0.0, 0.0)
        // set the adapter to the RecyclerView
        binding.recyclerView.adapter = adapter

        // observe the location data and update the adapter when it changes
        locationViewModel.latLon.observe(viewLifecycleOwner) { latLon ->
            adapter.updateLocation(latLon.lat, latLon.lon)
            adapter.notifyDataSetChanged() // notify adapter to refresh the view
        }

        val filter = arguments?.getString("FILTER_TYPE")
        // fetch the POIs based on the filter in a background thread
        thread {
            val pois = when {
                filter.isNullOrEmpty() -> poiDao.getAllPois() // get all POIs if no filter
                filter == "other" -> poiDao.getPoisExcludingTypes(listOf("restaurant", "pub", "cafe", "suburb")) // get POIs excluding specified types
                else -> poiDao.getPoisByType(filter) // get POIs of the specified type
            }
            // update the adapter with the fetched POIs on the main thread
            requireActivity().runOnUiThread {
                adapter.updateData(pois)
            }
        }
    }
}
