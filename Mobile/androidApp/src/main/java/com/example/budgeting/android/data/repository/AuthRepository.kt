package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.network.AuthApiService
import com.example.budgeting.android.data.network.LoginRequest
import com.example.budgeting.android.data.network.RegisterRequest

class AuthRepository(private val apiService: AuthApiService) {

    suspend fun login(email: String, password: String) =
        apiService.login(LoginRequest(email, password))

    suspend fun register(email: String, password: String, firstName: String, lastName: String, phoneNumber: String) =
        apiService.register(RegisterRequest(email, password, firstName, lastName, phoneNumber))

}