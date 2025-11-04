package com.example.budgeting.android.data.network

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.GET
import com.example.budgeting.android.data.model.Expense

interface ExpenseApiService {

    @GET("/expenses/")
    suspend fun getExpenses(): Response<List<Expense>>


}