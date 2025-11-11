package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.CreateGroupRequest
import com.example.budgeting.android.data.model.UpdateGroupRequest
import com.example.budgeting.android.data.network.GroupApiService
import retrofit2.Response

class GroupRepository(
    private val apiService: GroupApiService,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun getGroup(id: Int) = apiService.getGroup(id)

    suspend fun createGroup(name: String, description: String? = null) = 
        apiService.createGroup(CreateGroupRequest(name, description))

    suspend fun updateGroup(groupId: Int, name: String? = null, description: String? = null) = 
        apiService.updateGroup(groupId, UpdateGroupRequest(name, description))

    suspend fun deleteGroup(groupId: Int) = apiService.deleteGroup(groupId)

    suspend fun addUserToGroup(groupId: Int, userId: Int) = 
        apiService.addUserToGroup(groupId, userId)

    suspend fun getGroupsByUser(userId: Int) = apiService.getGroupsByUser(userId)

    suspend fun getUsersByGroup(groupId: Int) = apiService.getUsersByGroup(groupId)
}


