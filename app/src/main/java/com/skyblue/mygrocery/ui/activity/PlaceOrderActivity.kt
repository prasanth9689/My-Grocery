package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityPlaceOrderBinding
import com.skyblue.mygrocery.model.CartItem
import com.skyblue.mygrocery.model.OrderRequest
import com.skyblue.mygrocery.ui.viewmodel.LocationViewModel
import com.skyblue.mygrocery.ui.viewmodel.OrderViewModel
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PlaceOrderActivity : AppCompatActivity() {
    private var cartItems: List<CartItem> = listOf()
    private lateinit var binding: ActivityPlaceOrderBinding
    private val viewModel: OrderViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    private var selectedAddressId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cartJson = intent.getStringExtra("cart_json")
        val subtotal = intent.getDoubleExtra("subtotal", 0.0)

        if (cartJson != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            cartItems = Gson().fromJson(cartJson, type)
        }

        // Update UI with the subtotal received
        binding.tvItemTotal.text = "₹$subtotal"

        setupUI()
        observeOrderState()
        observeActiveAddress()

        binding.btnPlaceOrder.setOnClickListener {
            performOrderPlacement()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnChangeAddress.setOnClickListener {
            val intent = Intent(this, SavedAddressesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeActiveAddress() {
        lifecycleScope.launch {
            // Observe the current saved/active location from the database
            locationViewModel.currentSavedLocation.collect { location ->
                if (location != null) {
                    // Update UI
                    binding.tvAddress.text =
                        "${location.locationType}: ${location.address}\n${location.floorDetail}"
                    binding.tvAddress.setTextColor(resources.getColor(R.color.black, null))

                    // Store ID for the order placement
                    selectedAddressId = location.id.toString()

                    // Enable button if address is present
                    binding.btnPlaceOrder.isEnabled = true
                } else {
                    binding.tvAddress.text = "Select an address to continue"
                    binding.btnPlaceOrder.isEnabled = false
                }
            }
        }
    }

    private fun setupUI() {
        // pass these values via Intent or a CartViewModel
        val subtotal = intent.getDoubleExtra("subtotal", 0.0)
        val deliveryFee = 40.0
        val total = subtotal + deliveryFee

        binding.tvItemTotal.text = "₹$subtotal"
        binding.tvDeliveryFee.text = "₹$deliveryFee"
        binding.tvTotalPayable.text = "₹$total"
    }

    private fun performOrderPlacement() {

        Log.d("Place_Order", selectedAddressId.toString())

        if (selectedAddressId == null) {
            Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionHandler.getUserId()
        val totalAmount = binding.tvTotalPayable.text.toString().replace("₹", "").toDouble()

        val request = OrderRequest(
            userId = userId,
            items = cartItems,
            totalAmount = totalAmount,
            addressId = selectedAddressId!!, // Use the real ID
            paymentMethod = "COD"
        )

        viewModel.placeOrder(request)
    }

    private fun observeOrderState() {
        lifecycleScope.launch {
            viewModel.orderState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.btnPlaceOrder.isEnabled = false
                        binding.btnPlaceOrder.text = "Placing..."
                    }
                    is Resource.Success -> {
                        val intent = Intent(this@PlaceOrderActivity, SuccessActivity::class.java)
                        intent.putExtra("order_id", state.data.orderId)
                        startActivity(intent)
                        finish()
                    }
                    is Resource.Error -> {
                        binding.btnPlaceOrder.isEnabled = true
                        binding.btnPlaceOrder.text = "Place Order"
                        Toast.makeText(this@PlaceOrderActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}