package com.skyblue.mygrocery.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.skyblue.mygrocery.model.ProfileResponse
import com.skyblue.mygrocery.model.UserStatusResponse
import com.skyblue.mygrocery.repository.AuthRepository
import com.skyblue.mygrocery.utils.Resource
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _profileResponse = MutableStateFlow<Resource<ProfileResponse>>(Resource.Idle)
    val profileResponse: StateFlow<Resource<ProfileResponse>> = _profileResponse

    private val _syncState = MutableStateFlow<Resource<UserStatusResponse>>(Resource.Idle)
    val syncState: StateFlow<Resource<UserStatusResponse>> = _syncState

    private val _authState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val authState: StateFlow<Resource<String>> = _authState

    private var verificationId: String? = null

    fun setVerificationId(id: String) {
        this.verificationId = id
    }

//    fun checkUserRegistration(uid: String) {
//        viewModelScope.launch {
//            _syncState.value = Resource.Loading
//            val result = repository.syncUserProfile(uid)
//            _syncState.value = result
//        }
//    }

    fun verifyOtp(otp: String) {
        if (verificationId == null) {
            _authState.value = Resource.Error("Session expired. Please try again.")
            return
        }

        _authState.value = Resource.Loading

        // Create the credential using the stored verificationId and user-entered OTP
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)

        signInWithPhoneAuthCredential(credential)
    }

    fun syncUserWithServer(uid: String, phone: String) {
        viewModelScope.launch {
            _syncState.value = Resource.Loading
            val result = repository.verifyUser(uid, phone)
            _syncState.value = result
        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    _authState.value = Resource.Success(firebaseUser?.uid ?: "")
                } else {
                    _authState.value = Resource.Error(task.exception?.message ?: "Invalid OTP")
                }
            }
    }

    fun saveProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            // Accessing the object directly (no parentheses)
            _profileResponse.value = Resource.Loading

            try {
                val response = repository.saveUserProfile(name, email, phone)
                Log.d("PROFILE_REQ", "Name: $name")
                Log.d("PROFILE_REQ", "Email: $email")
                Log.d("PROFILE_REQ", "Phone: $phone")
                if (response.isSuccessful && response.body() != null) {
                    // Success is a data class, so it needs parentheses
                    _profileResponse.value = Resource.Success(response.body()!!)
                } else {
                    _profileResponse.value = Resource.Error("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _profileResponse.value = Resource.Error(e.message ?: "Network Check Failed", true)
            }
        }
    }
}