package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import android.util.Log
import com.example.budgeting.android.data.model.Expense
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.ExpenseFilters
import com.example.budgeting.android.data.model.SortOption
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val expenseRepository = ExpenseRepository(RetrofitClient.expenseInstance, tokenDataStore)
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filters = MutableStateFlow(ExpenseFilters(category = "All"))
    val filters: StateFlow<ExpenseFilters> = _filters.asStateFlow()

    val categories = _expenses.map { list ->
        list.map { it.category }
            .distinct()
            .sorted()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val filteredExpenses: StateFlow<List<Expense>> =
        combine(_expenses, _filters) { expenses, filters ->

            var result = expenses

            // Search filter
            if (filters.search.isNotBlank()) {
                result = result.filter {
                    it.title.contains(filters.search, ignoreCase = true)
                }
            }

            // Category filter
            if (filters.category != "All") {
                result = result.filter { it.category == filters.category }
            }

            // Sorting
            result = when (filters.sortOption) {
                SortOption.AMOUNT_ASC -> result.sortedBy { it.amount }
                SortOption.AMOUNT_DESC -> result.sortedByDescending { it.amount }
                SortOption.TITLE_ASC -> result.sortedBy { it.title }
                SortOption.TITLE_DESC -> result.sortedByDescending { it.title }
            }

            result
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    fun loadExpenses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = expenseRepository.getExpenses()

                if (response.isSuccessful && response.body() != null) {
                    _expenses.value = response.body()!!
                } else {
                    _error.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addExpense(expense: Expense) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                expense.user_id = tokenDataStore.getUserId()
                val response = expenseRepository.addExpense(expense)

                if (response.isSuccessful) {
                    _expenses.value += response.body()!!
                } else {
                    _error.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExpense(expense: Expense) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = expenseRepository.updateExpense(id = expense.id!!, expense = expense)

                if (response.isSuccessful) {
                    _expenses.value = _expenses.value.map { if (it.id == expense.id) expense else it }
                } else {
                    _error.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = expenseRepository.deleteExpense(expense.id!!)

                if (response.isSuccessful) {
                    _expenses.value -= expense
                } else {
                    _error.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _filters.value = _filters.value.copy(search = query)
    }

    fun setCategoryFilter(category: String) {
        _filters.value = _filters.value.copy(category = category)
    }

    fun setSortOption(option: SortOption) {
        _filters.value = _filters.value.copy(sortOption = option)
    }

}
