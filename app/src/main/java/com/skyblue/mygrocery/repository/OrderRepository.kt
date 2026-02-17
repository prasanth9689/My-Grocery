package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.api.ApiService
import com.skyblue.mygrocery.db.CartDao
import com.skyblue.mygrocery.model.OrderRequest
import com.skyblue.mygrocery.model.OrderResponse
import com.skyblue.mygrocery.utils.Resource
import jakarta.inject.Inject

class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val cartDao: CartDao
) {
    suspend fun placeOrder(orderRequest: OrderRequest): Resource<OrderResponse> {
        return try {
            val response = apiService.placeOrder(orderRequest)
            val body = response.body() // Store body to help Kotlin smart-cast

            if (response.isSuccessful && body != null) {
                if (body.status) {
                    // Success! Clear the local cart
                    cartDao.clearCart()
                    Resource.Success(body)
                } else {
                    // Server returned status: false with a custom message
                    Resource.Error(body.message)
                }
            } else {
                // HTTP Error (4xx or 5xx)
                Resource.Error("Server Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }
}