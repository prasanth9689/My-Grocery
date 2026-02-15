package com.skyblue.mygrocery.api

import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.model.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/products")
    suspend fun getProducts(
        @Query("page") page: Int
    ): Response<List<Product>>

    @GET("api/products/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): ProductResponse
}