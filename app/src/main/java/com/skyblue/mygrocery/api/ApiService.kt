package com.skyblue.mygrocery.api

import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.model.ProductResponse
import com.skyblue.mygrocery.model.SimpleResponse
import com.skyblue.mygrocery.model.UserLocation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    @POST("api/user/save-address")
    suspend fun uploadLocation(@Body location: UserLocation): Response<Unit>

    @DELETE("api/user/delete-address/{id}")
    suspend fun deleteAddressFromServer(@Path("id") addressId: Int): Response<Unit>

    @POST("/api/addresses")
    suspend fun uploadUserAddress(
        @Body location: UserLocation
        // Retrofit will now include the userId inside the JSON automatically
    ): Response<SimpleResponse>

    @PUT("api/address/update/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: Int,
        @Body location: UserLocation
    ): Response<SimpleResponse>
}