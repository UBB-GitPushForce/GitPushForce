package com.example.budgeting.android.data.network

import com.squareup.moshi.Json

data class RegisterResponse(
    val message: String,
    @Json(name = "access_token") val accessToken: String,
    val user: UserData
)
