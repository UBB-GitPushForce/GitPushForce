package com.example.budgeting.android.data.model

data class Expense(
    val id: Int? = null,
    var user_id: Int? = null,
    var group_id: Int? = null,
    val title: String,
    val category: String,
    val amount: Double
)
