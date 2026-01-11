package com.example.budgeting.android.data.model

data class ProcessedItem(
    val name: String,
    val quantity: Double,
    val price: Double,
    val category: String,
    val keywords: List<String>
)
