package com.example.mad3d.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mad3d.data.Poi
import com.example.mad3d.databinding.ItemPoiBinding
import com.example.mad3d.data.proj.Algorithms

class PoisAdapter(
    private var pois: List<Poi>,
    private var currentLat: Double,
    private var currentLon: Double
) : RecyclerView.Adapter<PoisAdapter.ViewHolder>() {

    override fun getItemCount() = pois.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemPoiBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pois[position], currentLat, currentLon)
    }

    inner class ViewHolder(private val binding: ItemPoiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: Poi, lat: Double, lon: Double) {
            binding.textViewName.text = poi.name
            binding.textViewFeatureType.text = poi.featureType

            val distance = Algorithms.haversineDist(lon, lat, poi.lon, poi.lat)
            binding.textViewDistance.text = "${distance.toInt()} meters" // Bind the distance here
        }
    }

    // Method to update the data in the adapter
    fun updateData(newPois: List<Poi>) {
        pois = newPois
        notifyDataSetChanged()
    }

    // Method to update the current location
    fun updateLocation(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
    }
}
