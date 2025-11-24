package com.example.budgeting.android.data.model

data class UserUpdateRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String,
    val password: String?,
    val budget: Double
)


