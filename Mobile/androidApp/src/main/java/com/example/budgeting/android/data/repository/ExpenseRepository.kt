package com.example.budgeting.android.data.repository

import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.network.ExpenseApiService

class ExpenseRepository(
    private val api: ExpenseApiService,
    private val tokenDataStore: TokenDataStore
) {

    // ---------------------------
    // PERSONAL EXPENSES
    // ---------------------------
    suspend fun getPersonalExpenses(
        search: String?,
        category: String?,
        sortBy: String?,
        order: String?
    ): List<Expense> {
        return api.getPersonalExpenses(
            search = search,
            category = category,
            sortBy = sortBy,
            order = order
        ).body()?.data ?: throw Exception("Failed to fetch expenses")
    }

    // ---------------------------
    // ALL EXPENSES
    // ---------------------------
    suspend fun getAllExpenses(
        category: String?,
        sortBy: String?,
        order: String?
    ): List<Expense> {
        return api.getAllExpenses(
            category = category,
            sortBy = sortBy,
            order = order
        ).body()?.data ?: throw Exception("Failed to fetch expenses")
    }

    // ---------------------------
    // GROUP EXPENSES
    // ---------------------------
    suspend fun getGroupExpenses(
        groupId: Int,
        category: String?,
        sortBy: String?,
        order: String?
    ): List<Expense> {
        return api.getGroupExpenses(
            groupId = groupId,
            category = category,
            sortBy = sortBy,
            order = order
        ).body()?.data ?: throw Exception("Failed to fetch expenses")
    }

    // ---------------------------
    // CRUD
    // ---------------------------
    suspend fun getExpense(id: Int): Expense {
        return api.getExpenseById(id).body()?.data ?: throw Exception("Failed to fetch expense")
    }

    suspend fun addExpense(expense: Expense): Int {
        return api.addExpense(expense).body()?.data?.id ?: throw Exception("Failed to add expense")
    }

    suspend fun updateExpense(id: Int, expense: Expense): Int {
        return api.updateExpense(id, expense).body()?.data?.id ?: throw Exception("Failed to update expense")
    }

    suspend fun deleteExpense(id: Int): Int {
        return api.deleteExpense(id).body()?.data?.id ?: throw Exception("Failed to delete expense")
    }
}
