package com.skyblue.mygrocery.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skyblue.mygrocery.databinding.ItemCartBinding
import com.skyblue.mygrocery.model.CartItem

class CartAdapter(private val onDeleteClick: (CartItem) -> Unit) :
    ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvCartName.text = item.name
            binding.tvCartPrice.text = "$${item.price}"
            binding.tvQuantity.text = "Qty: ${item.quantity}"

            Glide.with(binding.imgCart.context)
                .load(item.image)
                .into(binding.imgCart)

            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    object CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
}