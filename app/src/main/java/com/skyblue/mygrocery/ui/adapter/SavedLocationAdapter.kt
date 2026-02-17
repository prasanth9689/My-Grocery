package com.skyblue.mygrocery.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ItemSavedLocationBinding
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.ui.activity.UpdateLocationActivity

class SavedLocationAdapter(private val onLocationClick: (UserLocation) -> Unit) :
    ListAdapter<UserLocation, SavedLocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        return LocationViewHolder(
            ItemSavedLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.bind(location)
    }

    inner class LocationViewHolder(private val binding: ItemSavedLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(location: UserLocation) {
            binding.tvType.text = location.locationType
            binding.tvName.text = "${location.receiverName} | ${location.receiverPhone}"
            binding.tvAddress.text = "${location.floorDetail}, ${location.areaLandmark}, ${location.address}"

            // Set Icon based on type
            val iconRes = when (location.locationType) {
                "Home" -> R.drawable.ic_home_location
                "Work" -> R.drawable.ic_work_location
                else -> R.drawable.ic_other_location
            }
            binding.imgType.setImageResource(iconRes)

            // Highlight if it's currently selected
            binding.root.setCardBackgroundColor(
                if (location.isCurrentLocation) binding.root.context.getColor(R.color.light_blue)
                else binding.root.context.getColor(R.color.white)
            )

            binding.root.setOnClickListener { onLocationClick(location) }

            binding.btnEdit.setOnClickListener {
                val intent = Intent(binding.root.context, UpdateLocationActivity::class.java)
                intent.putExtra("location_data", location) // Make sure UserLocation is Parcelable
                binding.root.context.startActivity(intent)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserLocation>() {
        override fun areItemsTheSame(oldItem: UserLocation, newItem: UserLocation) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserLocation, newItem: UserLocation) = oldItem == newItem
    }
}