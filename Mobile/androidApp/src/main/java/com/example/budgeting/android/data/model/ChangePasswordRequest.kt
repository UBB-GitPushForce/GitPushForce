package com.example.budgeting.android.data.model

class ChangePasswordRequest (
    val user_id: Int,
    val old_password: String,
    val new_password: String
)