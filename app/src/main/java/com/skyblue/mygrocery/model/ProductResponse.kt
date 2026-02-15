package com.skyblue.mygrocery.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("tenant")
    val tenant: String,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("results")
    val results: List<Product> // This must match your existing Product model
)