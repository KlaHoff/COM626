package com.example.mad3d.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mad3d.data.PoiDao
import com.example.mad3d.data.PoiDatabase
import com.example.mad3d.databinding.FragmentExploreBinding
import kotlin.concurrent.thread

class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    private val poiDao: PoiDao by lazy {
        PoiDatabase.getDatabase(requireContext()).getPoiDao()
    }

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

        // Set an empty adapter initially
        binding.recyclerView.adapter = PoisAdapter(emptyList())

        // Load data in a background thread
        thread {
            val pois = poiDao.getAllPois()
            requireActivity().runOnUiThread {
                // Update the adapter with actual data
                (binding.recyclerView.adapter as PoisAdapter).updateData(pois)
            }
        }
    }
}