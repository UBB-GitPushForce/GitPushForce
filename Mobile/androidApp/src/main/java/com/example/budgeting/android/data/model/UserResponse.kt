package com.example.budgeting.android.data.model

data class UserResponse(
    val id: Int,
    val email: String,
    val phone_number: String,
    val first_name: String,
    val last_name: String,
    val budget: Double
)