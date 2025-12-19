package com.example.budgeting.android.data.model

data class ProcessedItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val category: String,
    val keywords: List<String>
)
