package com.skyblue.mygrocery.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_locations")
data class UserLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isCurrentLocation: Boolean = false,

    // New Zepto-style fields
    val locationType: String = "Home", // Home, Work, Friend, Other
    val receiverName: String? = null,
    val receiverPhone: String? = null,
    val floorDetail: String? = null,   // House/Floor/Building
    val areaLandmark: String? = null,
    val isSavedAddress: Boolean = false // To distinguish between "Auto-fetched" and "User-saved"
) : Parcelable