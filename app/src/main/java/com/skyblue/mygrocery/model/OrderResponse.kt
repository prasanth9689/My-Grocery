package com.skyblue.mygrocery.model

data class OrderResponse(
    val status: Boolean,
    val message: String,
    val orderId: String?
)