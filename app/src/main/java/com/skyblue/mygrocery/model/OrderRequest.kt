package com.skyblue.mygrocery.model

data class OrderRequest(
    val userId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val addressId: String,
    val paymentMethod: String
)