package com.example.budgeting.android.data.model

data class Receipt(
    val id: String,
    val merchant: String,
    val category: String,
    val amount: Double,
    val thumbnailUrl: String? = null
)

