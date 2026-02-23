package com.skyblue.mygrocery.model

data class ProfileRequest(
    val name: String,
    val email: String,
    val phone: String // Usually needed by backends to link the profile
)