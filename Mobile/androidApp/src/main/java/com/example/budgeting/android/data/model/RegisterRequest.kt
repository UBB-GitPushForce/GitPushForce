package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class RegisterRequest(
    val email: String,
    val password: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "phone_number") val phoneNumber: String
)
