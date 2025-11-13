package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.network.ExpenseApiService
import com.example.budgeting.android.ui.component.ExpenseItem
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.exp

class ExpenseRepository(private val apiService: ExpenseApiService, private val tokenDataStore: TokenDataStore) {

    suspend fun getExpenses() =
        apiService.getExpenses()

    suspend fun addExpense(expense: Expense) =
        apiService.addExpense(expense = expense)

    suspend fun updateExpense(id: Int, expense: Expense) =
        apiService.updateExpense(id = id, expense = expense)

    suspend fun deleteExpense(id: Int) =
        apiService.deleteExpense(id = id)

}