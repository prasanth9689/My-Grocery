package com.skyblue.mygrocery.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.mygrocery.model.OrderRequest
import com.skyblue.mygrocery.model.OrderResponse
import com.skyblue.mygrocery.model.OrderSummary
import com.skyblue.mygrocery.repository.OrderRepository
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    // Main loading for the center spinner (mainLoader)
    private val _loadingState = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    // Pagination loading for the bottom spinner (bottomLoader)
    private var _isLoadingMore = false
    val isLoadingMore: Boolean get() = _isLoadingMore

    private var currentPage = 1
    private var isLastPage = false

    private val _historyList = MutableStateFlow<List<OrderSummary>>(emptyList())
    val historyList: StateFlow<List<OrderSummary>> = _historyList

    private val _orderState = MutableStateFlow<Resource<OrderResponse>>(Resource.Idle)
    val orderState: StateFlow<Resource<OrderResponse>> = _orderState

    private val _selectedOrder = MutableStateFlow<Resource<OrderSummary>>(Resource.Loading)
    val selectedOrder = _selectedOrder.asStateFlow()

    fun resetPaginationAndFetch(userId: String) {
        currentPage = 1
        isLastPage = false
        _historyList.value = emptyList()
        fetchOrders(userId)
    }

    fun fetchOrders(userId: String) {
        // Prevent calls if already loading or no more data
        if (_loadingState.value || _isLoadingMore || isLastPage) return

        viewModelScope.launch {
            // If it's the first page, show center loader. If not, show bottom loader.
            if (currentPage == 1) _loadingState.value = true else _isLoadingMore = true

            val result = repository.fetchOrderHistory(userId, currentPage)

            if (result is Resource.Success) {
                val newOrders = result.data ?: emptyList()

                if (newOrders.isEmpty()) {
                    isLastPage = true
                } else {
                    if (currentPage == 1) {
                        _historyList.value = newOrders
                    } else {
                        // Append new data to existing list
                        _historyList.value = _historyList.value + newOrders
                    }
                    currentPage++
                }
            } else if (result is Resource.Error) {
                // Optionally handle error state here
            }

            // Reset loading states
            _loadingState.value = false
            _isLoadingMore = false
        }
    }

    fun placeOrder(orderRequest: OrderRequest) {
        viewModelScope.launch {
            _orderState.value = Resource.Loading
            _orderState.value = repository.placeOrder(orderRequest)
        }
    }

    private val _ratingStatus = MutableSharedFlow<Resource<String>>()
    val ratingStatus: SharedFlow<Resource<String>> = _ratingStatus

    fun submitOrderRating(orderId: String, rating: Float, feedback: String) {
        viewModelScope.launch {
            _ratingStatus.emit(Resource.Loading)
            val result = repository.submitOrderRating(orderId, rating, feedback)
            _ratingStatus.emit(result)
        }
    }

    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _selectedOrder.value = Resource.Loading
            // Check local list first to avoid extra API call
            val localOrder = _historyList.value.find { it.orderId == orderId }

            if (localOrder != null) {
                _selectedOrder.value = Resource.Success(localOrder)
            } else {
                // Optional: Fetch from API if not found in memory
                // _selectedOrder.value = repository.getOrderDetail(orderId)
            }
        }
    }

    private val _activeOrder = MutableStateFlow<OrderRequest?>(null)
    val activeOrder: StateFlow<OrderRequest?> = _activeOrder.asStateFlow()

    // In OrderViewModel.kt
    fun fetchActiveOrder(userId: String) {
        viewModelScope.launch {
            repository.getOrders(userId).collect { resource ->
                if (resource is Resource.Success) {
                    // Now 'it.status' is recognized by the compiler
                    val latest = resource.data?.firstOrNull {
                        it.status != "Delivered" && it.status != "Cancelled"
                    }
                    Log.d("ORDER_", latest?.status.toString())
                    _activeOrder.value = latest
                }
            }
        }
    }
}