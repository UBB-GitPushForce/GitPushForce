package com.example.budgeting.android.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import com.example.budgeting.android.data.model.Expense
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseApiService {

    @GET("/expenses/")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("/expenses/")
    suspend fun addExpense(@Body expense: Expense): Response<Expense>

    @PUT("/expenses/{id}")
    suspend fun updateExpense(@Path("id") id: Int, @Body expense: Expense): Response<String>

    @DELETE("/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: Int): Response<String>
}