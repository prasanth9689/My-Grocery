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

        binding.btnBack.setOnClickListener { finish() }

        // Inside CartActivity.kt
        binding.btnCheckout.setOnClickListener {
            if (cartAdapter.currentList.isNotEmpty()) {
                // 1. Clear the Room Database
                viewModel.clearAll()

                // 2. Navigate to Success Screen
                val intent = Intent(this, SuccessActivity::class.java)
                startActivity(intent)
                finish() // Close cart so they can't go back to an empty list
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
                binding.layoutEmptyCart.root.visibility = View.VISIBLE // You can reuse your empty state layout here
                binding.tvTotalPrice.text = "$0.00"
            } else {
                binding.rvCart.visibility = View.VISIBLE
                binding.layoutEmptyCart.root.visibility = View.GONE
                cartAdapter.submitList(items)

                // Update total price
                val total = viewModel.calculateTotal(items)
                binding.tvTotalPrice.text = String.format("$%.2f", total)
            }
        }
    }

    private fun observeActiveLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelLocation.currentSavedLocation.collect { location ->
                    location?.let {
                        // Update Text
                        binding.tvActiveLocationName.text = it.locationType

                        // Update Icon based on Type
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

        // Open Saved Locations on click
        binding.layoutActiveLocation.setOnClickListener {
            startActivity(Intent(this, SavedAddressesActivity::class.java))
        }
    }
}