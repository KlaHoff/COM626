package com.example.mad3d.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var adapter: PoisAdapter

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

        adapter = PoisAdapter(emptyList(), 0.0, 0.0)
        binding.recyclerView.adapter = adapter

        locationViewModel.latLon.observe(viewLifecycleOwner, Observer { latLon ->
            adapter.updateLocation(latLon.lat, latLon.lon)
            adapter.notifyDataSetChanged() // Notify adapter to refresh the view
        })

        thread {
            val pois = poiDao.getAllPois()
            requireActivity().runOnUiThread {
                adapter.updateData(pois)
            }
        }
    }
}
