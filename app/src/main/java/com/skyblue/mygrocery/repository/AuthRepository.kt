package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.api.ApiService
import com.skyblue.mygrocery.model.UserProfileResponse
import com.skyblue.mygrocery.utils.Resource
import jakarta.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun syncUserProfile(uid: String): Resource<UserProfileResponse> {
        return try {
            val response = apiService.getUserProfile(uid)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("User not registered yet")
            }
        } catch (e: Exception) {
            Resource.Error("Network failure: ${e.message}")
        }
    }
}