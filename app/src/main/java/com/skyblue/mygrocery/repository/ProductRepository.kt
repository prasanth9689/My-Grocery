package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.api.ApiService // Ensure this matches your API interface name
import com.skyblue.mygrocery.db.CartDao
import com.skyblue.mygrocery.model.CartItem
import com.skyblue.mygrocery.model.Product
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.skyblue.mygrocery.utils.Resource
import kotlinx.coroutines.flow.Flow

@Singleton
class ProductRepository @Inject constructor(
    private val api: ApiService,
    private val cartDao: CartDao
) {
    suspend fun getProducts(page: Int): Response<List<Product>> {
        return api.getProducts(page)
    }

    suspend fun insertCartItem(cartItem: CartItem) {
        cartDao.insert(cartItem)
    }

    suspend fun searchProducts(query: String): Resource<List<Product>> {
        return try {
            val response = api.searchProducts(query)
            if (response.results.isNotEmpty()) {
                Resource.Success(response.results)
            } else {
                Resource.Error("No products found for '$query'")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateQuantity(productId: String, isIncrement: Boolean) {
        if (isIncrement) {
            cartDao.incrementQuantity(productId)
        } else {
            val currentQty = cartDao.getQuantity(productId)
            if (currentQty <= 1) {
                cartDao.removeItem(productId)
            } else {
                cartDao.decrementQuantity(productId)
            }
        }
    }

    suspend fun removeItem(productId: String) {
        cartDao.removeItem(productId)
    }

    fun getAllCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllItems()
    }
}