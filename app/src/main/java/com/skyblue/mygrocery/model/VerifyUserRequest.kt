package com.skyblue.mygrocery.model

data class VerifyUserRequest(
    val uid: String,
    val phone: String
)