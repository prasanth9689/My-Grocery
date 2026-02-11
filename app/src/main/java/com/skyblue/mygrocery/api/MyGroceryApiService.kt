package com.skyblue.mygrocery.api

import com.skyblue.mygrocery.model.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyGroceryApiService {
    @GET("recipes")
        suspend fun getProducts(
        @Query("limit") limit: Int = 10,
        @Query("skip") skip: Int = 0
    ): Response<ProductsResponse>
}