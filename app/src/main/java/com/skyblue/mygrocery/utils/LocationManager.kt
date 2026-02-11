package com.skyblue.mygrocery.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getShortAddress(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            continuation.resume(formatShortAddress(address))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    formatShortAddress(addresses?.firstOrNull())
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCurrentLocation(): Location? = suspendCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null)
            return@suspendCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    fun hasLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ (API 33+)
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            continuation.resume(formatAddress(address))
                        }
                    }
                } else {
                    // Below Android 13
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    formatAddress(addresses?.firstOrNull())
                }
            } catch (e: IOException) {
                null
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Format address to readable string
    private fun formatAddress(address: Address?): String? {
        return address?.let {
            buildString {
                // Street address
                if (!it.thoroughfare.isNullOrEmpty()) {
                    append(it.thoroughfare)
                    append(", ")
                }

                // Locality (city)
                if (!it.locality.isNullOrEmpty()) {
                    append(it.locality)
                    append(", ")
                }

                // State
                if (!it.adminArea.isNullOrEmpty()) {
                    append(it.adminArea)
                    append(" ")
                }

                // Postal code
                if (!it.postalCode.isNullOrEmpty()) {
                    append(it.postalCode)
                }
            }.trim().takeIf { it.isNotEmpty() } ?: "Unknown location"
        }
    }

    private fun formatShortAddress(address: Address?): String? {
        return address?.let {
            "${it.locality ?: it.subAdminArea ?: "Unknown"}, ${it.adminArea ?: ""}"
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
