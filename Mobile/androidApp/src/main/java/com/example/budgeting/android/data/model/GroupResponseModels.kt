package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class GroupIdResponse(
    @Json(name = "id")
    val id: Int
)

data class AddUserToGroupResponse(
    @Json(name = "group")
    val group: Int,
    @Json(name = "user")
    val user: Int
)

data class JoinGroupResponse(
    @Json(name = "group")
    val group: Group,
    @Json(name = "user")
    val user: UserData
)

