package com.skyblue.mygrocery.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "products") // Add this if it's missing!
@Parcelize
data class Product(
    @PrimaryKey val id: Int,
    val name: String,
    val price: String,
    val image: String,
    val description: String
) : Parcelable