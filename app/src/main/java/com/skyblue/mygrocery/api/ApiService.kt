package com.skyblue.mygrocery.api

import com.skyblue.mygrocery.model.OrderHistoryResponse
import com.skyblue.mygrocery.model.OrderRequest
import com.skyblue.mygrocery.model.OrderResponse
import com.skyblue.mygrocery.model.Product
import com.skyblue.mygrocery.model.ProductResponse
import com.skyblue.mygrocery.model.ProfileRequest
import com.skyblue.mygrocery.model.ProfileResponse
import com.skyblue.mygrocery.model.RatingRequest
import com.skyblue.mygrocery.model.SimpleResponse
import com.skyblue.mygrocery.model.UserLocation
import com.skyblue.mygrocery.model.UserProfileResponse
import com.skyblue.mygrocery.model.UserStatusResponse
import com.skyblue.mygrocery.model.VerifyUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/orders/user/{userId}/pending")
    suspend fun getUserPendingOrders(
        @Path("userId") userId: String
    ): Response<List<OrderRequest>>

    @GET("api/products") // Completed
    suspend fun getProducts(
        @Query("page") page: Int
    ): Response<List<Product>>

    @GET("api/products/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): ProductResponse

    @POST("api/profile")
    suspend fun updateProfile(@Body request: ProfileRequest): Response<ProfileResponse>

    @POST("api/verify_user") // Completed
    suspend fun verifyUser(
        @Body request: VerifyUserRequest
    ): Response<UserStatusResponse>

    @DELETE("api/user/delete-address/{id}")
    suspend fun deleteAddressFromServer(@Path("id") addressId: Int): Response<Unit>

    @POST("/api/addresses") // Completed
    suspend fun uploadUserAddress(
        @Body location: UserLocation
    ): Response<SimpleResponse>

    @PUT("api/address/update/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: Int,
        @Body location: UserLocation
    ): Response<SimpleResponse>

    @DELETE("api/address/delete/{id}")
    suspend fun deleteAddress(
        @Path("id") addressId: Int
    ): Response<SimpleResponse>

    @GET("api/user/profile/{uid}")
    suspend fun getUserProfile(
        @Path("uid") firebaseUid: String
    ): Response<UserProfileResponse>


    /*
    POST  /api/orders/place
    GET   /api/orders/user/{userId}
    GET   /api/orders/{orderId}
     */
    @POST("/api/orders/place") // Completed (GET  /api/orders/user/5)
    suspend fun placeOrder(
        @Body orderRequest: OrderRequest
    ): Response<OrderResponse>

    @GET("/api/orders/user/{userId}") // Completed
    suspend fun getOrderHistory(
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): Response<OrderHistoryResponse>

    @POST("/api/ratings")
    suspend fun submitRating(
        @Body ratingRequest: RatingRequest
    ): Response<SimpleResponse>
}