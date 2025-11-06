package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.network.ExpenseApiService
import kotlinx.coroutines.flow.firstOrNull

class ExpenseRepository(private val apiService: ExpenseApiService, private val tokenDataStore: TokenDataStore) {

    suspend fun getExpenses() =
        apiService.getExpenses()

}