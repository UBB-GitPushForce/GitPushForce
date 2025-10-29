package com.example.budgeting.android.data.network

import com.squareup.moshi.Json

data class UserData(
    @Json(name = "id")
    val id: Int,

    @Json(name = "first_name")
    val firstName: String,

    @Json(name = "last_name")
    val lastName: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "phone_number")
    val phoneNumber: String
)
