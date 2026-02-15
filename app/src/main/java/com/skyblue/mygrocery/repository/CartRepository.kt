package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.db.CartDao
import com.skyblue.mygrocery.model.CartItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartDao: CartDao
) {
    // Returns a Flow so the UI updates automatically when the DB changes
    val allItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    suspend fun insert(item: CartItem) {
        cartDao.addToCart(item)
    }

    suspend fun delete(item: CartItem) {
        cartDao.removeFromCart(item)
    }

    suspend fun deleteAll() {
        cartDao.clearCart()
    }
}