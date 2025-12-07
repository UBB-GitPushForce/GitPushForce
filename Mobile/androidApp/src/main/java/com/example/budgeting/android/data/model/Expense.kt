package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class Expense(
    @Json(name = "id")
    val id: Int? = null,
    
    @Json(name = "user_id")
    var user_id: Int? = null,
    
    @Json(name = "group_id")
    val group_id: Int? = null,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "category_id")
    val category_id: Int? = null,
    
    @Json(name = "category")
    val category: Category? = null,
    
    @Json(name = "amount")
    val amount: Double,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "created_at")
    val created_at: String? = null
) {
    val categoryTitle: String
        get() = category?.title ?: "Uncategorized"
}

data class ExpenseIdResponse(
    @Json(name = "id")
    val id: Int
)

data class ExpenseCreateRequest(
    @Json(name = "title")
    val title: String,
    
    @Json(name = "amount")
    val amount: Double,
    
    @Json(name = "category_id")
    val category_id: Int,
    
    @Json(name = "group_id")
    val group_id: Int? = null,
    
    @Json(name = "description")
    val description: String? = null
)