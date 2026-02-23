package com.skyblue.mygrocery.model

data class OrderHistoryResponse(
    val status: Boolean,
    val orders: List<OrderSummary>
)