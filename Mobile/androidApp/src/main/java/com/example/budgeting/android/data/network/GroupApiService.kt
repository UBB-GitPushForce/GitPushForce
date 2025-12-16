package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ApiResponse
import com.example.budgeting.android.data.model.CreateGroupRequest
import com.example.budgeting.android.data.model.Group
import com.example.budgeting.android.data.model.UpdateGroupRequest
import com.example.budgeting.android.data.model.UserData
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.GroupIdResponse
import com.example.budgeting.android.data.model.AddUserToGroupResponse
import com.example.budgeting.android.data.model.JoinGroupResponse
import com.example.budgeting.android.data.model.GroupLog
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupApiService {
    @GET("/groups/")
    suspend fun getGroups(): Response<ApiResponse<List<Group>>>

    @GET("/groups/{group_id}")
    suspend fun getGroup(@Path("group_id") groupId: Int): Response<ApiResponse<Group>>

    @POST("/groups/")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<ApiResponse<GroupIdResponse>>

    @PUT("/groups/{group_id}")
    suspend fun updateGroup(
        @Path("group_id") groupId: Int,
        @Body request: UpdateGroupRequest
    ): Response<ApiResponse<GroupIdResponse>>

    @DELETE("/groups/{group_id}")
    suspend fun deleteGroup(@Path("group_id") groupId: Int): Response<ApiResponse<GroupIdResponse>>

    @POST("/groups/{group_id}/users/{user_id}")
    suspend fun addUserToGroup(
        @Path("group_id") groupId: Int,
        @Path("user_id") userId: Int
    ): Response<ApiResponse<AddUserToGroupResponse>>

    @DELETE("/groups/{group_id}/users/{user_id}")
    suspend fun removeUserFromGroup(
        @Path("group_id") groupId: Int,
        @Path("user_id") userId: Int
    ): Response<ApiResponse<Unit>>

    @GET("/groups/user/{user_id}")
    suspend fun getGroupsByUser(@Path("user_id") userId: Int): Response<ApiResponse<List<Group>>>

    @GET("/groups/{group_id}/users")
    suspend fun getUsersByGroup(@Path("group_id") groupId: Int): Response<ApiResponse<List<UserData>>>

    @GET("/groups/{group_id}/users/nr")
    suspend fun getNrOfUsersFromGroup(@Path("group_id") groupId: Int): Response<ApiResponse<Int>>

    @GET("/groups/{group_id}/expenses")
    suspend fun getExpensesByGroup(
        @Path("group_id") groupId: Int,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("order") order: String = "desc"
    ): Response<ApiResponse<List<Expense>>>

    @GET("/groups/{group_id}/invite-qr")
    suspend fun getGroupInviteQr(@Path("group_id") groupId: Int): Response<ApiResponse<String>>

    @POST("/users/join-group/{invitation_code}")
    suspend fun joinGroupByInvitationCode(
        @Path("invitation_code") invitationCode: String
    ): Response<ApiResponse<JoinGroupResponse>>

    @GET("/group_logs/{group_id}")
    suspend fun getGroupLogs(@Path("group_id") groupId: Int): Response<ApiResponse<List<GroupLog>>>
}
