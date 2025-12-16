package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class Category(
    @Json(name = "id")
    val id: Int,
    
    @Json(name = "user_id")
    val user_id: Int,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "keywords")
    val keywords: List<String> = emptyList()
)

/**
 * Request model for creating categories
 */
data class CategoryCreateRequest(
    @Json(name = "title")
    val title: String,
    
    @Json(name = "keywords")
    val keywords: List<String> = emptyList()
)

/**
 * Request model for updating categories
 */
data class CategoryUpdateRequest(
    @Json(name = "title")
    val title: String? = null,
    
    @Json(name = "keywords")
    val keywords: List<String>? = null
)

