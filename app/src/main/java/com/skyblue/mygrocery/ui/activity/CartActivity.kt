package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityCartBinding
import com.skyblue.mygrocery.ui.adapter.CartAdapter
import com.skyblue.mygrocery.ui.viewmodel.CartViewModel
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: CartViewModel by viewModels()
    private val viewModelLocation: LocationViewModel by viewModels()

    private val cartAdapter by lazy {
        CartAdapter { item -> viewModel.removeItem(item) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeCart()
        observeActiveLocation() // Added this back as it was missing from your onCreate

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCheckout.setOnClickListener {
            val currentItems = cartAdapter.currentList
            if (currentItems.isNotEmpty()) {
                // 1. Convert List to JSON String using GSON
                val gson = Gson()
                val cartJson = gson.toJson(currentItems)

                // 2. Calculate subtotal
                val subtotal = viewModel.calculateTotal(currentItems)

                // 3. Navigate to PlaceOrderActivity (NOT SuccessActivity yet)
                val intent = Intent(this, PlaceOrderActivity::class.java).apply {
                    putExtra("cart_json", cartJson)
                    putExtra("subtotal", subtotal)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun observeCart() {
        viewModel.cartItems.observe(this) { items ->
            if (items.isNullOrEmpty()) {
                binding.rvCart.visibility = View.GONE
                binding.layoutEmptyCart.root.visibility = View.VISIBLE
                binding.tvTotalPrice.text = "₹0.00"
                binding.btnCheckout.isEnabled = false // Disable checkout if empty
                binding.btnCheckout.alpha = 0.5f
            } else {
                binding.rvCart.visibility = View.VISIBLE
                binding.layoutEmptyCart.root.visibility = View.GONE
                binding.btnCheckout.isEnabled = true
                binding.btnCheckout.alpha = 1.0f
                cartAdapter.submitList(items)

                val total = viewModel.calculateTotal(items)
                binding.tvTotalPrice.text = String.format("₹%.2f", total)
            }
        }
    }

    private fun observeActiveLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelLocation.currentSavedLocation.collect { location ->
                    location?.let {
                        binding.tvActiveLocationName.text = it.locationType
                        val iconRes = when (it.locationType) {
                            "Home" -> R.drawable.ic_home_location
                            "Work" -> R.drawable.ic_work_location
                            else -> R.drawable.ic_other_location
                        }
                        binding.imgLocationIcon.setImageResource(iconRes)
                    }
                }
            }
        }

        binding.layoutActiveLocation.setOnClickListener {
            startActivity(Intent(this, SavedAddressesActivity::class.java))
        }
    }
}