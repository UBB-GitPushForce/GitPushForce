package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.CategoryCount
import com.example.budgeting.android.data.model.CategoryTotal
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.MonthlyTotal
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.ExpenseRepository
import com.example.budgeting.android.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AnalyticsViewModel(context: Context) : ViewModel() {

    private val tokenStore = TokenDataStore(context)
    private val repository = ExpenseRepository(RetrofitClient.expenseInstance, tokenStore)
    private val groupRepository = GroupRepository(
        RetrofitClient.groupInstance,
        RetrofitClient.expenseInstance,
        tokenStore
    )

    // --- STATE ---
    private val _mode = MutableStateFlow(ExpenseMode.ALL)
    val mode: StateFlow<ExpenseMode> = _mode.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _groupIds = MutableStateFlow<List<Int>>(emptyList())
    private val _dateFrom = MutableStateFlow<LocalDate?>(null)
    val dateFrom: StateFlow<LocalDate?> = _dateFrom.asStateFlow()
    private val _dateTo = MutableStateFlow<LocalDate?>(null)
    val dateTo: StateFlow<LocalDate?> = _dateTo.asStateFlow()

    private val _categoryAmounts = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categoryAmounts: StateFlow<List<CategoryTotal>> = _categoryAmounts.asStateFlow()

    private val _categoryCounts = MutableStateFlow<List<CategoryCount>>(emptyList())
    val categoryCounts: StateFlow<List<CategoryCount>> = _categoryCounts.asStateFlow()

    private val _monthlyTotals = MutableStateFlow<List<MonthlyTotal>>(emptyList())
    val monthlyTotals: StateFlow<List<MonthlyTotal>> = _monthlyTotals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserGroups()
    }

    // --- DATE FILTERS ---
    fun setDateFrom(date: LocalDate) {
        _dateFrom.value = date
        loadAnalytics()
    }

    fun setDateTo(date: LocalDate) {
        _dateTo.value = date
        loadAnalytics()
    }

    // --- MODE ---
    fun setMode(newMode: ExpenseMode) {
        _mode.value = newMode
        loadAnalytics()
    }

    // --- CATEGORY ---
    fun setCategory(category: String) {
        _selectedCategory.value = category
        loadAnalytics()
    }

    // --- GROUPS ---
    private fun loadUserGroups() {
        viewModelScope.launch {
            try {
                val userId = tokenStore.getUserId()
                val response = groupRepository.getGroupsByUser(userId!!)
                if (response.isSuccessful && response.body() != null) {
                    _groupIds.value = response.body()!!.map { it.id as Int }
                    loadAnalytics()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    // --- MAIN ANALYTICS ---
    fun loadAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val expenses = when (_mode.value) {
                    ExpenseMode.PERSONAL -> repository.getPersonalExpenses(
                        search = null,
                        category = if (_selectedCategory.value == "All") null else _selectedCategory.value,
                        sortBy = null,
                        order = null,
                        dateFrom = _dateFrom.value?.toString(),
                        dateTo = _dateTo.value?.toString()
                    )

                    ExpenseMode.ALL -> repository.getAllExpenses(
                        category = if (_selectedCategory.value == "All") null else _selectedCategory.value,
                        sortBy = null,
                        order = null,
                        dateFrom = _dateFrom.value?.toString(),
                        dateTo = _dateTo.value?.toString()
                    )

                    ExpenseMode.GROUP -> {
                        val all = mutableListOf<Expense>()
                        _groupIds.value.forEach { id ->
                            all.addAll(repository.getGroupExpenses(
                                groupId = id,
                                category = if (_selectedCategory.value == "All") null else _selectedCategory.value,
                                sortBy = null,
                                order = null,
                                dateFrom = _dateFrom.value?.toString(),
                                dateTo = _dateTo.value?.toString()
                            ))
                        }
                        all
                    }
                }

                // --- CATEGORY AMOUNT ---
                _categoryAmounts.value = expenses.groupBy { it.categoryTitle }
                    .map { (cat, list) -> CategoryTotal(category = cat, total = list.sumOf { it.amount }.toFloat()) }
                
                // --- CATEGORY COUNT ---
                _categoryCounts.value = expenses.groupBy { it.categoryTitle }
                    .map { (cat, list) -> CategoryCount(category = cat, count = list.size) }

                // --- MONTHLY TREND ---
                _monthlyTotals.value = expenses.groupBy { it.created_at!!.substring(0, 7) }
                    .map { (month, list) -> MonthlyTotal(month = month, total = list.sumOf { it.amount }.toFloat()) }

                // --- AVAILABLE CATEGORIES ---
                _categories.value = expenses.mapNotNull { it.category?.title }.distinct().sorted()

            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
