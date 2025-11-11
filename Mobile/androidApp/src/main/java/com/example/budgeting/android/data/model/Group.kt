package com.example.budgeting.android.data.model

import com.squareup.moshi.Json

data class Group(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "created_at")
    val createdAt: String? = null
) {
    // Helper property to check if group is valid
    val isValid: Boolean
        get() = id != null && id > 0 && !name.isNullOrBlank()
}

data class CreateGroupRequest(
    val name: String,
    val description: String? = null
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null
)

