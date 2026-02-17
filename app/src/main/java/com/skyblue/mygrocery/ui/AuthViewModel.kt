package com.skyblue.mygrocery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.skyblue.mygrocery.model.UserProfileResponse
import com.skyblue.mygrocery.repository.AuthRepository
import com.skyblue.mygrocery.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository // Hilt injects this automatically
) : ViewModel() {

    private val _syncState = MutableStateFlow<Resource<UserProfileResponse>>(Resource.Idle)
    val syncState: StateFlow<Resource<UserProfileResponse>> = _syncState

    private val _authState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val authState: StateFlow<Resource<String>> = _authState

    private var verificationId: String? = null

    fun setVerificationId(id: String) {
        this.verificationId = id
    }

    fun checkUserRegistration(uid: String) {
        viewModelScope.launch {
            _syncState.value = Resource.Loading
            val result = repository.syncUserProfile(uid)
            _syncState.value = result
        }
    }

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

    // ... rest of your code (verifyOtp, setVerificationId)
}