package com.skyblue.mygrocery.model

data class UserProfileResponse(
    val status: Boolean,
    val name: String?,
    val email: String?,
    val userId: String
)