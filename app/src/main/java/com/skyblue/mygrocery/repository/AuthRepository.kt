package com.skyblue.mygrocery.repository

import com.skyblue.mygrocery.api.ApiService
import com.skyblue.mygrocery.model.ProfileRequest
import com.skyblue.mygrocery.model.UserProfileResponse
import com.skyblue.mygrocery.model.UserStatusResponse
import com.skyblue.mygrocery.model.VerifyUserRequest
import com.skyblue.mygrocery.utils.Resource
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun saveUserProfile(name: String, email: String, phone: String) =
        apiService.updateProfile(ProfileRequest(name, email, phone))

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

    suspend fun verifyUser(uid: String, phone: String): Resource<UserStatusResponse> {
        return try {
            // Create the JSON body object
            val request = VerifyUserRequest(uid, phone)

            val response = apiService.verifyUser(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Connection failed")
        }
    }
}