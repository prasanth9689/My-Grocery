package com.skyblue.mygrocery.model

data class RatingRequest(
    val orderId: String,
    val rating: Float,
    val feedback: String
)