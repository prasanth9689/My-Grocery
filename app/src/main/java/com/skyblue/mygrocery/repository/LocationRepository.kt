package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.db.LocationDao
import com.skyblue.mygrocery.model.UserLocation
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class LocationRepository @Inject constructor(
    private val locationDao: LocationDao
) {
    val allLocations: Flow<List<UserLocation>> = locationDao.getAllLocations()

    suspend fun setCurrentLocation(location: UserLocation) {
        locationDao.clearCurrentLocation()
        locationDao.insertLocation(location.copy(isCurrentLocation = true))
    }
}