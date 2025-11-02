package com.example.budgeting.android.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/users/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/users/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

}
