package com.skyblue.mygrocery.model

import com.google.gson.annotations.SerializedName

data class SimpleResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("error")
    val error: Boolean = false
)