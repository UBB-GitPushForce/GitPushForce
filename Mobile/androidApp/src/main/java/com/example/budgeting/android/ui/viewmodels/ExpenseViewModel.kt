package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.ExpenseFilters
import com.example.budgeting.android.data.model.SortOption
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.ExpenseRepository
import com.example.budgeting.android.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExpenseMode {
    PERSONAL,
    ALL,
    GROUP
}

class ExpenseViewModel(context: Context) : ViewModel() {

    private val tokenStore = TokenDataStore(context)
    private val repository = ExpenseRepository(RetrofitClient.expenseInstance, tokenStore)
    private val groupRepository = GroupRepository(RetrofitClient.groupInstance, RetrofitClient.expenseInstance, tokenStore)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filters = MutableStateFlow(ExpenseFilters())
    val filters: StateFlow<ExpenseFilters> = _filters.asStateFlow()

    private val _mode = MutableStateFlow(ExpenseMode.ALL)
    val mode: StateFlow<ExpenseMode> = _mode.asStateFlow()

    private val _groupId = MutableStateFlow<Int?>(null)
    val groupId: StateFlow<Int?> = _groupId.asStateFlow()

    private val _groupIds = MutableStateFlow<List<Int>>(emptyList())
    val groupIds: StateFlow<List<Int>> = _groupIds.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    init {
        loadCurrentUserId()
        loadUserGroups()
    }

    /** ----------------------------------------------------------
     *  MAIN: Load expenses using backend filtering
     * ---------------------------------------------------------- */
    fun loadCurrentUserId() {
        viewModelScope.launch {
            _currentUserId.value = tokenStore.getUserId()
        }
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                _categories.value = listOf("All")
                val f = _filters.value
                val data = when (_mode.value) {

                    ExpenseMode.PERSONAL -> {
                        val response = repository.getPersonalExpenses(
                            search = f.search,
                            category = if (f.category == "All" || f.category.isBlank()) null else f.category,
                            sortBy = f.sortOption.sortBy,
                            order = f.sortOption.order
                        )
                        val filtered = response.filter { it.title.contains(f.search, ignoreCase = true) }
                        filtered
                    }

                    ExpenseMode.ALL -> {
                        val response = repository.getAllExpenses(
                            category = if (f.category == "All" || f.category.isBlank()) null else f.category,
                            sortBy = f.sortOption.sortBy,
                            order = f.sortOption.order
                        )
                        val filtered = response.filter { it.title.contains(f.search, ignoreCase = true) }
                        filtered
                    }

                    ExpenseMode.GROUP -> {
                        // fetch expenses from all groups the user is part of
                        val allExpenses = mutableListOf<Expense>()
                        _groupIds.value.forEach { groupId ->
                            val response = repository.getGroupExpenses(
                                groupId = groupId,
                                category = if (f.category == "All" || f.category.isBlank()) null else f.category,
                                sortBy = f.sortOption.sortBy,
                                order = f.sortOption.order
                            )
                            val filtered = response.filter { it.title.contains(f.search, ignoreCase = true) }
                            allExpenses.addAll(filtered)
                        }
                        allExpenses
                    }
                }

                _expenses.value = data
                updateCategories(data)
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateCategories(expenses: List<Expense>) {
        val newCategories = expenses
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        // Merge with existing categories, keep All at top
        val merged = listOf("All") + (_categories.value - "All" + newCategories).distinct()
        _categories.value = merged
    }

    /** ----------------------------------------------------------
     *  MODES
     * ---------------------------------------------------------- */
    fun setMode(newMode: ExpenseMode) {
        _mode.value = newMode
        loadExpenses()
    }

    /** ----------------------------------------------------------
     *  GROUPS
     * ---------------------------------------------------------- */
    private fun loadUserGroups() {
        viewModelScope.launch {
            try {
                val userId = tokenStore.getUserId()
                val response = groupRepository.getGroupsByUser(userId!!)
                if (response.isSuccessful && response.body() != null) {
                    _groupIds.value = response.body()!!.map { it.id!! } // store all group IDs
                } else {
                    _error.value = "Error loading user groups"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun setGroup(groupId: Int) {
        _groupId.value = groupId
        _mode.value = ExpenseMode.GROUP
        loadExpenses()
    }

    /** ----------------------------------------------------------
     *  FILTERS
     * ---------------------------------------------------------- */
    fun setSearchQuery(query: String) {
        _filters.value = _filters.value.copy(search = query)
        loadExpenses()
    }

    fun setCategoryFilter(category: String) {
        _filters.value = _filters.value.copy(category = category)
        loadExpenses()
    }

    fun setSortOption(option: SortOption) {
        _filters.value = _filters.value.copy(sortOption = option)
        loadExpenses()
    }

    /** ----------------------------------------------------------
     *  CRUD
     * ---------------------------------------------------------- */
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                expense.user_id = tokenStore.getUserId()
                repository.addExpense(expense)
                loadExpenses() // refresh
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                expense.user_id = tokenStore.getUserId()
                repository.updateExpense(expense.id!!, expense)
                loadExpenses()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.deleteExpense(expense.id!!)
                loadExpenses()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
