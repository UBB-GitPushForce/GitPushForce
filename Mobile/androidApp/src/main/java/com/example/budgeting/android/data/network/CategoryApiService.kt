package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ApiResponse
import com.example.budgeting.android.data.model.Category
import com.example.budgeting.android.data.model.CategoryBody
import retrofit2.Response
import retrofit2.http.*

interface CategoryApiService {
    @GET("/categories")
    suspend fun getCategories(
        @Query("sort_by") sortBy: String? = null,
        @Query("order") order: String? = null
    ): Response<ApiResponse<List<Category>>>

    @POST("/categories")
    suspend fun addCategory(@Body category: CategoryBody): Response<Unit>

    @PUT("/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body category: CategoryBody): Response<Unit>

    @DELETE("/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

}