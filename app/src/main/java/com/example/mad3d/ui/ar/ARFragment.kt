package com.example.mad3d.ui.ar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mad3d.R

class ARFragment : Fragment() {

    private lateinit var orientationManager: OrientationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ar, container, false)
        orientationManager = OrientationManager(requireContext())
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orientationManager.stopListening()
    }
}