package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.CreateGroupRequest
import com.example.budgeting.android.data.model.Group
import com.example.budgeting.android.data.model.UpdateGroupRequest
import com.example.budgeting.android.data.model.UserData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GroupApiService {
    @GET("/groups/")
    suspend fun getGroups(): Response<List<Group>>

    @GET("/groups/{group_id}")
    suspend fun getGroup(@Path("group_id") groupId: Int): Response<Group>

    @POST("/groups/")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<Group>

    @PUT("/groups/{group_id}")
    suspend fun updateGroup(
        @Path("group_id") groupId: Int,
        @Body request: UpdateGroupRequest
    ): Response<Group>

    @DELETE("/groups/{group_id}")
    suspend fun deleteGroup(@Path("group_id") groupId: Int): Response<Unit>

    @POST("/groups/{group_id}/users/{user_id}")
    suspend fun addUserToGroup(
        @Path("group_id") groupId: Int,
        @Path("user_id") userId: Int
    ): Response<Unit>

    @DELETE("/groups/{group_id}/users/{user_id}")
    suspend fun removeUserFromGroup(
        @Path("group_id") groupId: Int,
        @Path("user_id") userId: Int
    ): Response<Unit>

    @GET("/groups/user/{user_id}")
    suspend fun getGroupsByUser(@Path("user_id") userId: Int): Response<List<Group>>

    @GET("/groups/{group_id}/users")
    suspend fun getUsersByGroup(@Path("group_id") groupId: Int): Response<List<UserData>>
}


