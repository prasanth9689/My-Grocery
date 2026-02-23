package com.skyblue.mygrocery.model

data class Order(
    val orderId: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: String,
    val items: List<CartItem> // This allows the itemCount logic to work
)