package com.example.mad3d.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mad3d.data.Poi
import com.example.mad3d.databinding.ItemPoiBinding

class PoisAdapter(private val pois: List<Poi>) : RecyclerView.Adapter<PoisAdapter.ViewHolder>() {

    override fun getItemCount() = pois.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemPoiBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pois[position])
    }

    inner class ViewHolder(private val binding: ItemPoiBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: Poi) {
            binding.textViewName.text = poi.name
            binding.textViewFeatureType.text = poi.featureType
        }
    }
}