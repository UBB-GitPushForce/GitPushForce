package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ApiResponse
import com.example.budgeting.android.data.model.LoginRequest
import com.example.budgeting.android.data.model.LoginResponse
import com.example.budgeting.android.data.model.RegisterRequest
import com.example.budgeting.android.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/users/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("/users/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>

}
