package com.skyblue.mygrocery.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityProductDetailBinding
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.ui.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductViewModel by viewModels()
    private var currentQuantity = 0

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

            updateCartUI() // Initial check
            observeCartStatus(item.id.toString())

            binding.btnAddToCartDetail.setOnClickListener {
                currentQuantity = 1
                viewModel.addItemToCart(item) // Your existing function
                updateCartUI()
            }

            binding.btnPlus.setOnClickListener {
                currentQuantity++
                viewModel.increaseItemQuantity(item) // Add this to your ViewModel
                updateCartUI()
            }

            binding.btnMinus.setOnClickListener {
                if (currentQuantity > 1) {
                    currentQuantity--
                    viewModel.decreaseItemQuantity(item) // Add this to your ViewModel
                    updateCartUI()
                } else {
                    currentQuantity = 0
                    viewModel.removeItemFromCart(item) // Add this to your ViewModel
                    updateCartUI()
                }
            }


            // Simple Click Listener for the new button
//            binding.btnAddToCartDetail.setOnClickListener {
//                viewModel.addItemToCart(item)
//                Snackbar.make(binding.root, "${item.name} added to cart", Snackbar.LENGTH_SHORT).show()
//            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun updateCartUI() {
        val showQuantity = currentQuantity > 0

        if (showQuantity) {
            binding.tvQuantity.text = currentQuantity.toString()
            if (binding.llQuantityControl.visibility == View.GONE) {
                // Fade out ADD button, Fade in Controller
                binding.btnAddToCartDetail.animate().alpha(0f).setDuration(100).withEndAction {
                    binding.btnAddToCartDetail.visibility = View.GONE
                    binding.llQuantityControl.visibility = View.VISIBLE
                    binding.llQuantityControl.alpha = 0f
                    binding.llQuantityControl.animate().alpha(1f).setDuration(100).start()
                }
            }
        } else {
            if (binding.btnAddToCartDetail.visibility == View.GONE) {
                binding.llQuantityControl.animate().alpha(0f).setDuration(100).withEndAction {
                    binding.llQuantityControl.visibility = View.GONE
                    binding.btnAddToCartDetail.visibility = View.VISIBLE
                    binding.btnAddToCartDetail.alpha = 0f
                    binding.btnAddToCartDetail.animate().alpha(1f).setDuration(100).start()
                }
            }
        }
    }

    private fun observeCartStatus(productId: String) {
        lifecycleScope.launch {
            // This keeps the UI synced even if you change quantity in another screen
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartItems.collect { cartList ->
                    // Find this specific product in the cart list
                    val existingItem = cartList.find { it.id.toString() == productId }

                    if (existingItem != null) {
                        currentQuantity = existingItem.quantity
                    } else {
                        currentQuantity = 0
                    }

                    // Refresh the buttons based on the new quantity
                    updateCartUI()
                }
            }
        }
    }
}