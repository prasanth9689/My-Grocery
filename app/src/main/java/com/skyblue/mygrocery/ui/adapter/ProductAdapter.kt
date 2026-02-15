package com.skyblue.mygrocery.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.skyblue.mygrocery.databinding.ItemProductBinding
import com.skyblue.mygrocery.model.Product

class ProductAdapter(
    private val onItemClick: (Product, ImageView) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

//    class ProductViewHolder(private val binding: ItemProductBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(product: Product, onItemClick: (Product, ImageView) -> Unit) {
//            binding.tvName.text = product.name
//            binding.tvPrice.text = "$${product.price}"
//
//            // Using Glide for image loading
//            val fullImageUrl = "https://test2.skyblue.co.in/images/" + product.image
//            Glide.with(binding.imgProduct.context)
//                .load(fullImageUrl)
//                .into(binding.imgProduct)
//
//            // Fix: Set the click listener on the entire item or a specific container
//            binding.root.setOnClickListener {
//                onItemClick(product, binding.imgProduct)
//            }
//        }
//    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, onItemClick: (Product, ImageView) -> Unit) {
            binding.tvName.text = product.name
            binding.tvPrice.text = "$${product.price}"

            // ⭐ SET THE TRANSITION NAME DYNAMICALLY
            binding.imgProduct.transitionName = "product_image_${product.id}"

            binding.imgProduct.transitionName = "product_image"

            // Using Glide for image loading
            val fullImageUrl = "https://test2.skyblue.co.in/images/" + product.image
            Glide.with(binding.imgProduct.context)
                .load(fullImageUrl)
                .addListener(object : RequestListener<Drawable> {
                    // Leave this empty and use the IDE's auto-fix
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

            // Set the click listener on the entire item
            binding.root.setOnClickListener {
                Log.d("ADAPTER_CLICK", "TransitionName before click: ${binding.imgProduct.transitionName}")
                onItemClick(product, binding.imgProduct)
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
        // Fix: Pass the onItemClick function here
        Log.d("CLICK_TEST", "Product clicked: ${getItem(position)}")
        holder.bind(getItem(position), onItemClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}