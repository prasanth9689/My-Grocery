package com.skyblue.mygrocery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.mygrocery.model.OrderRequest
import com.skyblue.mygrocery.model.OrderResponse
import com.skyblue.mygrocery.repository.OrderRepository
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<Resource<OrderResponse>>(Resource.Idle)
    val orderState: StateFlow<Resource<OrderResponse>> = _orderState

    fun placeOrder(orderRequest: OrderRequest) {
        viewModelScope.launch {
            _orderState.value = Resource.Loading
            _orderState.value = repository.placeOrder(orderRequest)
        }
    }
}