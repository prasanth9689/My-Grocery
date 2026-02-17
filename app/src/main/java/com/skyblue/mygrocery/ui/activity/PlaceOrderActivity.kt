package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skyblue.mygrocery.databinding.ActivityPlaceOrderBinding
import com.skyblue.mygrocery.model.CartItem
import com.skyblue.mygrocery.model.OrderRequest
import com.skyblue.mygrocery.ui.viewmodel.OrderViewModel
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaceOrderActivity : AppCompatActivity() {
    private var cartItems: List<CartItem> = listOf()
    private lateinit var binding: ActivityPlaceOrderBinding
    private val viewModel: OrderViewModel by viewModels()

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

        binding.btnPlaceOrder.setOnClickListener {
            performOrderPlacement()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupUI() {
        // In a real app, you'd pass these values via Intent or a CartViewModel
        val subtotal = intent.getDoubleExtra("subtotal", 0.0)
        val deliveryFee = 40.0
        val total = subtotal + deliveryFee

        binding.tvItemTotal.text = "₹$subtotal"
        binding.tvDeliveryFee.text = "₹$deliveryFee"
        binding.tvTotalPayable.text = "₹$total"
    }

    private fun performOrderPlacement() {
        val userId = SessionHandler.getUserId()
        val totalAmount = binding.tvTotalPayable.text.toString().replace("₹", "").toDouble()

        // Dummy request - adjust based on your OrderRequest data class
        val request = OrderRequest(
            userId = userId,
            items = cartItems, // Retrieve your cart items here
            totalAmount = totalAmount,
            addressId = "default_1",
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
                        // Navigate to Success Screen
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