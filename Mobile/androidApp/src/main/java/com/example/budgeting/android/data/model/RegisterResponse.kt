package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class RegisterResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    val id: Int,
    val user: UserData
)
