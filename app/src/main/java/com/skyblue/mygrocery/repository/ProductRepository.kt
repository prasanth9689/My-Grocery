package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.api.ApiService // Ensure this matches your API interface name
import com.skyblue.mygrocery.db.CartDao
import com.skyblue.mygrocery.model.CartItem
import com.skyblue.mygrocery.model.Product
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

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
}