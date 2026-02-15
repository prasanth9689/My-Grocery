package com.skyblue.mygrocery.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ItemProductBinding
import com.skyblue.mygrocery.model.Product

class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("CheckResult", "SetTextI18n")
        fun bind(product: Product, onItemClick: (Product) -> Unit) {
            binding.tvName.text = product.name
            binding.tvPrice.text = binding.root.context.getString(R.string.rupees) + product.price

            val fullImageUrl = "https://test2.skyblue.co.in/images/" + product.image
            Glide.with(binding.imgProduct.context)
                .load(fullImageUrl)
                .addListener(object : RequestListener<Drawable> {

                    @SuppressLint("CheckResult")
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        TODO("Not yet implemented")
                    }
                })

            binding.root.setOnClickListener {
                Log.d("CLICK_TEST", "Standard click for: ${product.name}")
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}