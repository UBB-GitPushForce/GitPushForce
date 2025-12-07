package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.ExpensePayment
import com.example.budgeting.android.data.network.ExpensePaymentApiService

class ExpensePaymentRepository(
    private val api: ExpensePaymentApiService,
    private val tokenDataStore: TokenDataStore
) {
    
    suspend fun markPaid(expenseId: Int, payerId: Int): Boolean {
        val response = api.markPaid(expenseId, payerId)
        return response.isSuccessful && response.body()?.success == true
    }
    
    suspend fun unmarkPaid(expenseId: Int, payerId: Int): Boolean {
        return try {
            val response = api.unmarkPaid(expenseId, payerId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    true
                } else {
                    body.success == true
                }
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    "Unable to read error body"
                }
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getPayments(expenseId: Int): List<ExpensePayment> {
        val response = api.getPayments(expenseId)
        return if (response.isSuccessful && response.body()?.data != null) {
            response.body()!!.data!!
        } else {
            emptyList()
        }
    }
}

