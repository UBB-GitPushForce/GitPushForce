package com.example.budgeting.android.data.model

data class CategoryTotal (
    val category: String,
    val total: Float
)

data class CategoryCount (
    val category: String,
    val count: Int
)

data class MonthlyTotal(
    val month: String,
    val total: Float
)
