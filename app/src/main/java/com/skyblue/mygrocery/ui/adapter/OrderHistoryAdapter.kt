package com.skyblue.mygrocery.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ItemOrderHistoryBinding // Ensure this matches your model name
import com.skyblue.mygrocery.model.OrderSummary
import com.skyblue.mygrocery.utils.formatDate

class OrderHistoryAdapter(private val onItemClick: (OrderSummary) -> Unit) :
    ListAdapter<OrderSummary, OrderHistoryAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderSummary) {
            binding.tvOrderId.text = "Order #${order.orderId}"
            binding.tvOrderPrice.text = String.format("₹%.2f", order.total)
            binding.tvOrderDate.text = formatDate(order.createdAt)

            // Uses the list size directly or the itemCount variable
            binding.tvItemsCount.text = "${order.items.size} Items"

            // Your existing status logic
            setupStatusUI(order.status)

            binding.root.setOnClickListener { onItemClick(order) }
        }

        private fun setupStatusUI(status: String) {
            when (status) {
                "OUT_FOR_DELIVERY" -> {
                    binding.tvStatusTitle.text = "Your order is on the way!"
                    binding.tvStatusTitle.setTextColor(Color.parseColor("#E65100")) // Dark Orange
                    binding.tvOrderStatus.text = "Out for Delivery"
                    binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                "DELIVERED" -> {
                    binding.tvStatusTitle.text = "Order Delivered"
                    binding.tvStatusTitle.setTextColor(Color.parseColor("#2E7D32")) // Green
                    binding.tvOrderStatus.text = "Completed"
                    binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_delivered)
                }
                "CANCELLED" -> {
                    binding.tvStatusTitle.text = "Order Cancelled"
                    binding.tvStatusTitle.setTextColor(Color.RED)
                    binding.tvOrderStatus.text = "Cancelled"
                    binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                }
                else -> {
                    binding.tvStatusTitle.text = "Processing your order"
                    binding.tvStatusTitle.setTextColor(Color.BLACK)
                    binding.tvOrderStatus.text = "Confirmed"
                }
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderSummary>() {
        override fun areItemsTheSame(oldItem: OrderSummary, newItem: OrderSummary): Boolean =
            oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(oldItem: OrderSummary, newItem: OrderSummary): Boolean =
            oldItem == newItem
    }
}