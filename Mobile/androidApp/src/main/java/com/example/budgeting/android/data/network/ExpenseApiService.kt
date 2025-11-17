package com.example.budgeting.android.data.network

import retrofit2.Response
import retrofit2.http.*
import com.example.budgeting.android.data.model.Expense

interface ExpenseApiService {

    // ---------------------------
    // PERSONAL EXPENSES
    // ---------------------------
    @GET("/expenses/")
    suspend fun getPersonalExpenses(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc"
    ): List<Expense>

    // ---------------------------
    // ALL EXPENSES
    // ---------------------------
    @GET("/expenses/all")
    suspend fun getAllExpenses(
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc"
    ): List<Expense>

    // ---------------------------
    // GROUP EXPENSES
    // ---------------------------
    @GET("/expenses/group/{groupId}")
    suspend fun getGroupExpenses(
        @Path("groupId") groupId: Int,
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc"
    ): List<Expense>

    // ---------------------------
    // CRUD
    // ---------------------------
    @POST("/expenses/")
    suspend fun addExpense(@Body expense: Expense): Response<Expense>

    @PUT("/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Int,
        @Body expense: Expense
    ): String

    @DELETE("/expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: Int
    ): String
}
