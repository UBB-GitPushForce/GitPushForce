package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ApiResponse
import com.example.budgeting.android.data.model.BudgetResponse
import com.example.budgeting.android.data.model.ChangePasswordRequest
import com.example.budgeting.android.data.model.UserResponse
import com.example.budgeting.android.data.model.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @GET("/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<ApiResponse<UserResponse>>

    @PUT("/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserUpdateRequest): Response<ApiResponse<Unit>>

    @DELETE("/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @PUT("/users/password/change")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @GET("/users/{id}/remaining-budget")
    suspend fun getRemainingBudget(@Path("id") id: Int): Response<ApiResponse<BudgetResponse>>
}