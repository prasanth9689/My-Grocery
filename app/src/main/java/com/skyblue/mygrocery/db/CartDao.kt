package com.skyblue.mygrocery.db

import androidx.room.*
import com.skyblue.mygrocery.model.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItem)

    @Query("SELECT * FROM cart_table")
    fun getAllCartItems(): Flow<List<CartItem>>

    @Delete
    suspend fun removeFromCart(item: CartItem)

    @Query("DELETE FROM cart_table")
    suspend fun clearCart()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem)

    @Query("UPDATE user_locations SET isCurrentLocation = 0")
    suspend fun clearActiveStatus()

    @Query("UPDATE user_locations SET isCurrentLocation = 1 WHERE id = :targetId")
    suspend fun setActiveStatus(targetId: Int)

    @Transaction
    suspend fun switchActiveAddress(targetId: Int) {
        clearActiveStatus()
        setActiveStatus(targetId)
    }
}