package com.skyblue.mygrocery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.mygrocery.api.ApiService
import com.skyblue.mygrocery.db.CartDao
import com.skyblue.mygrocery.model.CartItem
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.repository.ProductRepository
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _res = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val res = _res.asStateFlow()

    private val _isPaginationLoading = MutableStateFlow(false)
    val isPaginationLoading = _isPaginationLoading.asStateFlow()

    private var currentPage = 1
    private val allProducts = mutableListOf<Product>()

    init { fetchProducts() }

    fun fetchProducts(isPagination: Boolean = false) {
        if (_isPaginationLoading.value) return

        viewModelScope.launch {
            if (isPagination) {
                _isPaginationLoading.value = true
            } else {
                _res.value = Resource.Loading
            }

            try {
                val response = repository.getProducts(currentPage)
                if (response.isSuccessful) {
                    val newItems = response.body() ?: emptyList()

                    if (newItems.isEmpty() && allProducts.isEmpty()) {
                        _res.value = Resource.Empty
                    } else {
                        // CRITICAL: You must create a NEW list for DiffUtil to detect changes
                        allProducts.addAll(newItems)
                        _res.value = Resource.Success(ArrayList(allProducts))
                        currentPage++
                    }
                } else {
                    _res.value = Resource.Error("Server Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _res.value = Resource.Error("No Internet", isNetworkError = true)
            } finally {
                _isPaginationLoading.value = false
            }
        }
    }

    fun addItemToCart(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            // Convert Product to CartItem
            val cartItem = CartItem(
                id = product.id,
                name = product.name,
                price = product.price,
                image = product.image,
                quantity = 1 // Initial quantity
            )
            repository.insertCartItem(cartItem)
        }
    }
}