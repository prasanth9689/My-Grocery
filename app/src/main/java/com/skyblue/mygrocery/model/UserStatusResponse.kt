package com.skyblue.mygrocery.model

data class UserStatusResponse(
    val status: Boolean,
    val message: String,
    val isNewUser: Boolean, // true = go to Profile, false = go to Home
    val data: UserData? = null
)