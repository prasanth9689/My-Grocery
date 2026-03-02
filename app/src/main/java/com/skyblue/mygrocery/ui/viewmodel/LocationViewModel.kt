package com.skyblue.mygrocery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.repository.LocationRepository
import com.skyblue.mygrocery.utils.LocationManager
import com.skyblue.mygrocery.utils.Resource
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
    private val locationManager: LocationManager
) : ViewModel() {
    val currentUserId = SessionHandler.getUserId()
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    val allLocations: StateFlow<List<UserLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveState = MutableStateFlow<Resource<Unit>>(Resource.Idle) // Default or Idle
    val saveState: StateFlow<Resource<Unit>> = _saveState

    private val _updateState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updateState: StateFlow<Resource<Unit>> = _updateState

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
                        userId = currentUserId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = address,
                        locationType = "Home", // Default type for auto-pings
                        receiverName = "",
                        receiverPhone = "",
                        floorDetail = "",
                        areaLandmark = "",
                        isCurrentLocation = true,
                        isSavedAddress = false
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

    fun updateCurrentLocation(userLocation: UserLocation) {
        viewModelScope.launch {
            repository.setCurrentLocation(userLocation)
        }
    }

    val currentSavedLocation: StateFlow<UserLocation?> = repository.currentSavedLocation
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveAddress(location: UserLocation) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading

            val result = repository.saveFullAddress(location)
            _saveState.value = result
        }
    }

    fun updateActiveAddress(location: UserLocation) {
        viewModelScope.launch {
            repository.toggleActiveAddress(location.id)
        }
    }

    fun deleteLocation(location: UserLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    fun updateAddress(location: UserLocation) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            _updateState.value = repository.updateAddress(location)
        }
    }

    fun deleteAddress(location: UserLocation) {
        viewModelScope.launch {
            repository.deleteAddress(location)
        }
    }
}

sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: UserLocation) : LocationState()
    data class Error(val message: String) : LocationState()
}
