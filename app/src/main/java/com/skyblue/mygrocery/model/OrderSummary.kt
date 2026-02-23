package com.skyblue.mygrocery.model

data class OrderSummary(
    public final val orderId: String,
    public final val total: Double,
    public final val status: String,
    public final val createdAt: String,
    public final val items: List<CartItem> = emptyList(),
    public final val itemCount: Int = items.size,
    public final val formattedDate: String = createdAt
)