package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.UserResponse
import com.example.budgeting.android.data.model.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @GET("/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<UserResponse>

    @PUT("/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserUpdateRequest): Response<Unit>

    @DELETE("/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

}