package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class RegisterResponse(
    val message: String,
    @Json(name = "access_token") val accessToken: String? = null,
    val user: UserData
)
