package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.network.AuthApiService
import com.example.budgeting.android.data.network.LoginRequest
import com.example.budgeting.android.data.network.RegisterRequest

class AuthRepository(private val apiService: AuthApiService, private val tokenDataStore: TokenDataStore) {

    // save token in a data store that persists across app launches
    suspend fun login(email: String, password: String) =
        apiService.login(LoginRequest(email, password)).also { response ->
            if (response.isSuccessful) {
                response.body()?.accessToken?.let { tokenDataStore.saveToken(it) }
            }
        }


    suspend fun register(email: String, password: String, firstName: String, lastName: String, phoneNumber: String) =
        apiService.register(RegisterRequest(email, password, firstName, lastName, phoneNumber))

}