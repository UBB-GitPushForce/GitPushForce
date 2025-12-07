package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class ExpensePayment(
    @Json(name = "expense_id")
    val expense_id: Int,
    
    @Json(name = "user_id")
    val user_id: Int,
    
    @Json(name = "paid_at")
    val paid_at: String? = null
)

