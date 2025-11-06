package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class LoginResponse(
    val message: String,
    @Json(name = "access_token") val accessToken: String? = null,
    val user: UserData
)

