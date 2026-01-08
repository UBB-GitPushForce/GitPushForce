package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.ExpenseIdResponse
import com.example.budgeting.android.data.model.ExpenseCreateRequest

interface ExpenseApiService {

    // ---------------------------
    // PERSONAL EXPENSES
    // ---------------------------
    @GET("/expenses/")
    suspend fun getPersonalExpenses(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc",
        @Query("offset") offset: Int? = 0,
        @Query("limit") limit: Int? = 100,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<ApiResponse<List<Expense>>>

    // ---------------------------
    // ALL EXPENSES
    // ---------------------------
    @GET("/expenses/all")
    suspend fun getAllExpenses(
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc",
        @Query("offset") offset: Int? = 0,
        @Query("limit") limit: Int? = 100,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<ApiResponse<List<Expense>>>

    // ---------------------------
    // GROUP EXPENSES
    // ---------------------------
    @GET("/expenses/group/{groupId}")
    suspend fun getGroupExpenses(
        @Path("groupId") groupId: Int,
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("order") order: String? = "desc",
        @Query("offset") offset: Int? = 0,
        @Query("limit") limit: Int? = 100,
        @Query("min_price") minPrice: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<ApiResponse<List<Expense>>>

    // ---------------------------
    // CRUD
    // ---------------------------
    @GET("/expenses/{id}")
    suspend fun getExpenseById(
        @Path("id") id: Int
    ): Response<ApiResponse<Expense>>

    @POST("/expenses/")
    suspend fun addExpense(@Body expense: Expense): Response<ApiResponse<ExpenseIdResponse>>

    @PUT("/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Int,
        @Body expense: Expense
    ): Response<ApiResponse<ExpenseIdResponse>>

    @DELETE("/expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: Int
    ): Response<ApiResponse<ExpenseIdResponse>>
}
