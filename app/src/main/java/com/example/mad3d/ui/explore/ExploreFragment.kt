package com.example.mad3d.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.mad3d.data.PoiRepository
import com.example.mad3d.databinding.FragmentExploreBinding
import com.example.mad3d.ui.PoiViewModel
import com.example.mad3d.ui.PoiViewModelFactory

class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding

    private val poiViewModel: PoiViewModel by viewModels {
        PoiViewModelFactory(PoiRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        poiViewModel.pois.observe(viewLifecycleOwner, Observer { pois ->
            binding.recyclerView.adapter = PoisAdapter(pois = pois)
        })

        poiViewModel.fetchPois()
    }
}