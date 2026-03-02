package com.skyblue.mygrocery.ui.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityProductDetailBinding
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val product = intent.getParcelableExtra<Product>("product_data")

        product?.let { item ->
            binding.tvDetailName.text = item.name
            binding.tvDetailPrice.text = "₹${item.price}"
            binding.tvDescription.text = item.description

            // Use the simple Glide call without the postponed transition listeners
            val fullImageUrl = "https://test2.skyblue.co.in/uploads/tenant_1/" + item.image
            Glide.with(this)
                .load(fullImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.detailImage)

            // Simple Click Listener for the new button
            binding.btnAddToCartDetail.setOnClickListener {
                viewModel.addItemToCart(item)
                Snackbar.make(binding.root, "${item.name} added to cart", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}