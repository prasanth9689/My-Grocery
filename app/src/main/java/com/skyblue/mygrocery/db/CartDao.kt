package com.skyblue.mygrocery.db

import androidx.room.*
import com.skyblue.mygrocery.model.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    // Adds an item to the cart. If it exists, it replaces it (updates)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItem)

    // Observes all items in the cart in real-time
    @Query("SELECT * FROM cart_table")
    fun getAllCartItems(): Flow<List<CartItem>>

    // Deletes a specific item
    @Delete
    suspend fun removeFromCart(item: CartItem)

    // Clears the entire cart
    @Query("DELETE FROM cart_table")
    suspend fun clearCart()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem)
}