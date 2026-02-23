package com.skyblue.mygrocery.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ItemTrackStepBinding
import com.skyblue.mygrocery.model.TrackingStep

class TimelineAdapter : ListAdapter<TrackingStep, TimelineAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrackStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = getItem(position)
        val isLast = position == itemCount - 1
        val isFirst = position == 0
        holder.bind(step, isFirst, isLast)
    }

    class ViewHolder(private val binding: ItemTrackStepBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(step: TrackingStep, isFirst: Boolean, isLast: Boolean) {
            binding.tvStepTitle.text = step.title
            binding.tvStepTime.text = step.time

            // 1. Handle Dot & Line Colors
            val colorActive = Color.parseColor("#3B5EDF") // Zepto Blue
            val colorInactive = Color.parseColor("#E0E0E0") // Light Gray

            if (step.isCompleted || step.isCurrent) {
                binding.imgStatusDot.setImageResource(R.drawable.ic_dot_checked)
                binding.imgStatusDot.setColorFilter(colorActive)
                binding.lineTop.setBackgroundColor(colorActive)
                binding.tvStepTitle.setTextColor(Color.BLACK)
            } else {
                binding.imgStatusDot.setImageResource(R.drawable.ic_dot_empty)
                binding.imgStatusDot.setColorFilter(colorInactive)
                binding.lineTop.setBackgroundColor(colorInactive)
                binding.tvStepTitle.setTextColor(colorInactive)
            }

            // 2. Manage Vertical Line Visibility
            binding.lineTop.visibility = if (isFirst) View.INVISIBLE else View.VISIBLE
            binding.lineBottom.visibility = if (isLast) View.INVISIBLE else View.VISIBLE

            // Bottom line should only be colored if the NEXT step is also completed
            binding.lineBottom.setBackgroundColor(if (step.isCompleted) colorActive else colorInactive)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TrackingStep>() {
        override fun areItemsTheSame(old: TrackingStep, new: TrackingStep) = old.title == new.title
        override fun areContentsTheSame(old: TrackingStep, new: TrackingStep) = old == new
    }
}