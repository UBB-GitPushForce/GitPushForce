package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class GroupLog(
    @Json(name = "id")
    val id: Int,
    
    @Json(name = "group_id")
    val group_id: Int,
    
    @Json(name = "user_id")
    val user_id: Int,
    
    @Json(name = "action")
    val action: String, // "JOIN" or "LEAVE"
    
    @Json(name = "created_at")
    val created_at: String
)

