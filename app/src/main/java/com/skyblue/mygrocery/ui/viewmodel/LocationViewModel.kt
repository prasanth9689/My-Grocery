package com.skyblue.mygrocery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.repository.LocationRepository
import com.skyblue.mygrocery.utils.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    val allLocations: StateFlow<List<UserLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun checkPermissionStatus(): Boolean {
        return locationManager.hasLocationPermission()
    }

    fun getRequiredPermissions(): Array<String> {
        return locationManager.getRequiredPermissions()
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = LocationState.Loading

            try {
                val location = locationManager.getCurrentLocation()

                if (location != null) {
                    val address = locationManager.getAddressFromLocation(
                        location.latitude,
                        location.longitude
                    ) ?: "Address not available"

                    val userLocation = UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = address,
                        isCurrentLocation = true
                    )

                    repository.setCurrentLocation(userLocation)
                    _locationState.value = LocationState.Success(userLocation)
                } else {
                    _locationState.value = LocationState.Error("Unable to fetch location")
                }
            } catch (e: Exception) {
                _locationState.value = LocationState.Error(e.message ?: "Unknown error")
            }
        }
    }

}

sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: UserLocation) : LocationState()
    data class Error(val message: String) : LocationState()
}
