package com.skyblue.mygrocery.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartItem(
    @PrimaryKey val id: Int,
    val name: String,
    val price: String,
    val image: String,
    val quantity: Int = 1
)