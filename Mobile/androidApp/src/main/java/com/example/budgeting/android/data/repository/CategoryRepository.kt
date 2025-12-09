package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.model.Category
import com.example.budgeting.android.data.model.CategoryBody
import com.example.budgeting.android.data.network.CategoryApiService

class CategoryRepository(
    val api: CategoryApiService
) {

    suspend fun getCategories(sortBy: String?, order: String?): List<Category> {
        return api.getCategories(sortBy, order).body()?.data ?: throw Exception("Failed to fetch categories")
    }

    suspend fun addCategory(category: CategoryBody) {
        val response = api.addCategory(category)
        if (!response.isSuccessful) {
            throw Exception("Failed to update category")
        }
    }

    suspend fun updateCategory(id: Int, category: CategoryBody) {
        val response = api.updateCategory(id, category)
        if (!response.isSuccessful) {
            throw Exception("Failed to update category")
        }
    }

    suspend fun deleteCategory(id: Int) {
        val response = api.deleteCategory(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete category")
        }
    }

    suspend fun getTitle(id: Int?): String {
        val categories = api.getCategories(null, null).body()?.data ?: throw Exception("Failed to fetch categories")
        return categories.find { it.id == id }?.title ?: throw Exception("Category not found")
    }

    suspend fun getCategoryById(id: Int?): Category? {
        val categories = api.getCategories(null, null).body()?.data ?: throw Exception("Failed to fetch categories")
        return categories.find { it.id == id }
    }

}
