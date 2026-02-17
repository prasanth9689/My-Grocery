package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.api.ApiService
import com.skyblue.mygrocery.db.LocationDao
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.utils.Resource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

// Inside LocationRepository.kt
class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val api: ApiService
) {
    val allLocations: Flow<List<UserLocation>> = locationDao.getAllLocations()
    val currentSavedLocation: Flow<UserLocation?> = locationDao.getCurrentLocation()

    // This is the missing function
    suspend fun setCurrentLocation(location: UserLocation) {
        // If your DAO has a specific method for current location
        locationDao.insertLocation(location)
    }

    // Your existing saveFullAddress function for the Zepto-style form
    suspend fun saveFullAddress(location: UserLocation): Resource<Unit> {

        if (location.userId.isNullOrEmpty()) {
            return Resource.Error("User not logged in. Cannot sync address.")
        }

        return try {
            // 1. Save to Local Room Database (Immediate UI update)
            val localId = locationDao.insertLocation(location)

            // 2. Prepare for API (Include the local ID if your server needs it)
            val response = api.uploadUserAddress(location)

            if (response.isSuccessful) {
                // 3. Mark as 'synced' in local DB if you have a sync flag
                // locationDao.updateSyncStatus(localId, true)
                Resource.Success(Unit)
            } else {
                // Server rejected it, but it's still in Room
                Resource.Error("Saved locally. Server sync failed: ${response.message()}")
            }
        } catch (e: Exception) {
            // Network error (e.g., No Internet)
            Resource.Error("Saved to device. Will sync when online.")
        }
    }

    suspend fun toggleActiveAddress(targetId: Int) {
        locationDao.switchActiveAddress(targetId)
    }

    suspend fun deleteLocation(location: UserLocation): Resource<Unit> {
        return try {
            // 1. Delete from Room (Local)
            locationDao.deleteLocation(location)

            // 2. Sync with Server (Remote)
            val response = api.deleteAddressFromServer(location.id)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // Log this for later sync or notify user
                Resource.Error("Deleted locally, but failed to remove from server.")
            }
        } catch (e: Exception) {
            // Handle network failure
            Resource.Error("Check your internet connection to sync changes.")
        }
    }

    suspend fun updateAddress(location: UserLocation): Resource<Unit> {
        return try {
            // 1. Update Local Room
            locationDao.insertLocation(location) // @Insert(onConflict = REPLACE) handles updates

            // 2. Update Server
            val response = api.updateAddress(location.id, location)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Server update failed")
        } catch (e: Exception) {
            Resource.Error("Local update saved. Sync pending.")
        }
    }

    suspend fun deleteAddress(location: UserLocation): Resource<Unit> {
        return try {
            // 1. Delete from Local Room
            locationDao.deleteLocation(location)

            // 2. Delete from Server
            val response = api.deleteAddress(location.id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Deleted locally, but server sync failed.")
        } catch (e: Exception) {
            Resource.Error("Offline: Address removed from device only.")
        }
    }
}