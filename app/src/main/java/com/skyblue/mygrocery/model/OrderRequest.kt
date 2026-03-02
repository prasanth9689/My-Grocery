package com.skyblue.mygrocery.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("order_id")
    val orderId: String? = null,
    val userId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val addressId: String,
    @SerializedName("status")
    val status: String? = "Pending",
    val paymentMethod: String
)