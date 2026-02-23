package com.skyblue.mygrocery.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.mygrocery.R
import com.skyblue.mygrocery.databinding.ActivityOrderDetailsBinding
import com.skyblue.mygrocery.ui.adapter.OrderProductAdapter
import com.skyblue.mygrocery.ui.viewmodel.OrderViewModel
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding
    private val viewModel: OrderViewModel by viewModels()

    // You'll need a simple adapter to show products in the order
    private lateinit var itemAdapter: OrderProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getStringExtra("ORDER_ID") ?: return

        setupRecyclerView()
        observeOrderDetails()

        binding.btnBack.setOnClickListener { finish() }

        // Fetch details for this specific order
        viewModel.getOrderById(orderId)
    }

    private fun setupRecyclerView() {
        itemAdapter = OrderProductAdapter()
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailsActivity)
            adapter = itemAdapter
        }
    }

    private fun observeOrderDetails() {
        lifecycleScope.launch {
            viewModel.selectedOrder.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val order = resource.data
                        order?.let {
                            binding.tvDetailStatusTitle.text = it.status
                            binding.tvDetailDate.text = "Placed on ${it.createdAt}"
                            binding.tvBillTotal.text = "₹${it.total}"

                            // Submit the list of CartItems/Products
                            itemAdapter.submitList(it.items)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@OrderDetailsActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // Show shimemr or loading
                    }

                    else -> {}
                }
            }
        }
    }
}