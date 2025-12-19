package com.example.budgeting.android.data.model

data class ReceiptProcessResponse(
    val items: List<ProcessedItem>,
    val total: Double
)
