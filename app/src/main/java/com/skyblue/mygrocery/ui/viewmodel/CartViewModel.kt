package com.skyblue.mygrocery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.skyblue.mygrocery.db.CartDao
import com.skyblue.mygrocery.model.CartItem
import com.skyblue.mygrocery.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository,
    private val cartDao: CartDao
) : ViewModel() {

    // Observe items from Repository
    val cartItems = repository.allItems.asLiveData()

    // Function to calculate total price
    fun calculateTotal(items: List<CartItem>): Double {
        return items.sumOf { it.price.toDouble() * it.quantity }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    suspend fun deleteAll() {
        cartDao.clearCart() // This calls the function in your CartDao interface
    }
}