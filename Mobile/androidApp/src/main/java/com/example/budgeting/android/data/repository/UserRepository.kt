package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.UserResponse
import com.example.budgeting.android.data.model.UserUpdateRequest
import com.example.budgeting.android.data.network.UserApiService

class UserRepository(
    private val api: UserApiService,
    private val tokenDataStore: TokenDataStore
) {

    // GET USER BY ID
    suspend fun getUserById(id: Int): UserResponse{
        return api.getUserById(id).body() ?: throw Exception("Failed to get user")
    }

    // UPDATE USER
    suspend fun updateUser(id: Int, user: UserUpdateRequest) {
        val response = api.updateUser(id, user)

        if (!response.isSuccessful) {
            throw Exception("Failed to update user")
        }
    }

    // DELETE USER
    suspend fun deleteUser(id: Int){
        val response = api.deleteUser(id)

        if (!response.isSuccessful) {
            throw Exception("Failed to delete user")
        }

    }
}