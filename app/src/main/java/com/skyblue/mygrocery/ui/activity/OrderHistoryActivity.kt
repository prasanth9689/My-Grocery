package com.skyblue.mygrocery.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.mygrocery.databinding.ActivityOrderHistoryBinding
import com.skyblue.mygrocery.ui.adapter.OrderHistoryAdapter
import com.skyblue.mygrocery.ui.viewmodel.OrderViewModel
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding

    // Resolved Reference: ViewModel declaration
    private val viewModel: OrderViewModel by viewModels()

    private val orderAdapter by lazy {
        OrderHistoryAdapter { order ->
            // Navigate to Details Screen
            val intent = Intent(this, OrderDetailsActivity::class.java).apply {
                putExtra("ORDER_ID", order.orderId)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupPagination()
        observeOrders()

        // Initial fetch
        val userId = SessionHandler.getUserId()
        viewModel.fetchOrders(userId)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = orderAdapter
        }
    }

    private fun setupPagination() {
        binding.rvOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // Trigger pagination when 2 items from the bottom
                if (lastVisibleItem >= totalItemCount - 2 && !viewModel.isLoadingMore) {
                    val userId = SessionHandler.getUserId()
                    viewModel.fetchOrders(userId)
                }
            }
        })
    }

    private fun observeOrders() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyList.collect { list ->
                    if (list.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvOrders.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvOrders.visibility = View.VISIBLE
                        // Resolved: type mismatch handled by Option 2 alignment
                        orderAdapter.submitList(list)
                    }
                }
            }
        }

        // Observe Loading State for UI Feedback
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collect { isLoading ->
                    binding.mainLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }
    }
}