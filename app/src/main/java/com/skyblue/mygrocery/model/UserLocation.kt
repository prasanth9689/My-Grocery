package com.skyblue.mygrocery.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_locations")
data class UserLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isCurrentLocation: Boolean = false
)