package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.network.AuthApiService
import com.example.budgeting.android.data.model.LoginRequest
import com.example.budgeting.android.data.model.RegisterRequest

class AuthRepository(private val apiService: AuthApiService, private val tokenDataStore: TokenDataStore) {

    // save token and user ID in data store that persists across app launches
    suspend fun login(email: String, password: String) =
        apiService.login(LoginRequest(email, password)).also { response ->
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.data?.accessToken?.let { token ->
                        TokenHolder.token = token
                        tokenDataStore.saveToken(token)
                    }
                    tokenDataStore.saveUserId(body.data?.user?.id)
                }
            }
        }

    suspend fun register(firstName: String, lastName: String, email: String, password: String, phoneNumber: String) =
        apiService.register(RegisterRequest(email, password, firstName, lastName, phoneNumber)).also { response ->
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.data?.accessToken?.let { token ->
                        TokenHolder.token = token
                        tokenDataStore.saveToken(token)
                    }
                    tokenDataStore.saveUserId(body.data?.user?.id)
                }
            }
        }

}