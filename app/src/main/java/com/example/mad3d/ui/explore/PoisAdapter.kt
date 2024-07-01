package com.example.mad3d.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mad3d.R
import com.example.mad3d.data.Poi
import com.example.mad3d.databinding.ItemPoiBinding
import com.example.mad3d.data.proj.Algorithms

// used to display a list of POIs in a RecyclerView
class PoisAdapter(
    private var pois: List<Poi>, // list of POIs
    private var currentLat: Double, // current latitude of the user
    private var currentLon: Double // current longitude of the user
) : RecyclerView.Adapter<PoisAdapter.ViewHolder>() {

    // returns the number of items in the list
    override fun getItemCount() = pois.size

    // creates a new ViewHolder to hold the view for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemPoiBinding.inflate(inflater, parent, false))
    }

    // binds the data to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pois[position], currentLat, currentLon)
    }

    // ViewHolder class to hold the view for each item
    inner class ViewHolder(private val binding: ItemPoiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // binds the data of a single POI to the views
        fun bind(poi: Poi, lat: Double, lon: Double) {
            binding.textViewName.text = poi.name
            binding.textViewFeatureType.text = poi.featureType

            // calculates and sets the distance from the current location to the POI
            val distance = Algorithms.haversineDist(lon, lat, poi.lon, poi.lat)
            binding.textViewDistance.text = "${distance.toInt()} meters" // displays the distance

            // sets the icon based on the feature type
            val iconResId = when (poi.featureType) {
                "restaurant" -> R.drawable.icon_restaurant
                "pub" -> R.drawable.icon_pub
                "cafe" -> R.drawable.icon_cafe
                "suburb" -> R.drawable.icon_suburb
                else -> R.drawable.icon_other
            }
            binding.iconView.setImageResource(iconResId) // sets the icon
        }
    }

    // updates the list of POIs and notifies the adapter to refresh the views
    fun updateData(newPois: List<Poi>) {
        pois = newPois
        notifyDataSetChanged()
    }

    // updates the current location of the user
    fun updateLocation(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
    }
}
