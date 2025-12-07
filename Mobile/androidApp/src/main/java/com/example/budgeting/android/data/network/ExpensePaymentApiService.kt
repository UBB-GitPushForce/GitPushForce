package com.example.budgeting.android.data.network

import com.example.budgeting.android.data.model.ApiResponse
import com.example.budgeting.android.data.model.ExpensePayment
import retrofit2.Response
import retrofit2.http.*

interface ExpensePaymentApiService {
    
    @POST("/expenses_payments/{expense_id}/pay/{payer_id}")
    suspend fun markPaid(
        @Path("expense_id") expenseId: Int,
        @Path("payer_id") payerId: Int
    ): Response<ApiResponse<ExpensePayment>>
    
    @DELETE("/expenses_payments/{expense_id}/pay/{payer_id}")
    suspend fun unmarkPaid(
        @Path("expense_id") expenseId: Int,
        @Path("payer_id") payerId: Int
    ): Response<ApiResponse<Any?>>
    
    @GET("/expenses_payments/{expense_id}/payments")
    suspend fun getPayments(
        @Path("expense_id") expenseId: Int
    ): Response<ApiResponse<List<ExpensePayment>>>
}

