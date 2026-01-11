package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Category
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.ExpenseFilters
import com.example.budgeting.android.data.model.SortOption
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.CategoryRepository
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

    private val categoryRepository = CategoryRepository(RetrofitClient.categoryInstance)

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

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    private var offset = 0
    private var limit = 20
    private var endReached = false

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

    fun loadExpenses(append: Boolean = false) {
        if (_isLoading.value || endReached && append) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (!append) {
                    offset = 0
                    endReached = false
                    _expenses.value = emptyList()
                }

                _categories.value = listOf(Category(0, 0, "All", emptyList()))

                val data = when (_mode.value) {
                    ExpenseMode.PERSONAL -> loadPersonalExpenses()
                    ExpenseMode.GROUP -> {
                        endReached = true
                        loadGroupExpenses()
                    }
                    ExpenseMode.ALL -> {
                        endReached = true
                        loadAllExpenses()
                    }
                }

                if (data.size < limit) endReached = true
                offset += data.size

                _expenses.value =
                    if (append) _expenses.value + data
                    else data

                updateCategories(_expenses.value)

            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadPersonalExpenses(): List<Expense> {
        val f = _filters.value
        return repository.getPersonalExpenses(
            search = f.search,
            category = if (f.category == "All" || f.category.isBlank()) null else f.category,
            sortBy = f.sortOption.sortBy,
            order = f.sortOption.order,
            offset = offset,
            limit = limit,
            minPrice = f.minAmount,
            maxPrice = f.maxAmount,
            dateFrom = f.startDate,
            dateTo = f.endDate
        )
    }

    private suspend fun loadGroupExpenses(): List<Expense> {
        val all = mutableListOf<Expense>()
        val f = _filters.value
        _groupIds.value.forEach { groupId ->
            all += repository.getGroupExpenses(
                groupId = groupId,
                category = if (f.category == "All" || f.category.isBlank()) null else f.category,
                sortBy = f.sortOption.sortBy,
                order = f.sortOption.order,
                offset = offset,
                limit = limit,
                minPrice = f.minAmount,
                maxPrice = f.maxAmount,
                dateFrom = f.startDate,
                dateTo = f.endDate
            )
        }

        return all
    }

    private suspend fun loadAllExpenses(): List<Expense> {
        return loadPersonalExpenses() + loadGroupExpenses()
    }

    private fun updateCategories(expenses: List<Expense>) {
        viewModelScope.launch {
            val usedCategoryIds = expenses
                .map { it.categoryId }
                .distinct()

            val resolvedCategories = usedCategoryIds.mapNotNull { categoryId ->
                categoryRepository.getCategoryById(categoryId)
            }

            _categories.value =
                listOf(Category(0, 0, "All", emptyList())) +
                        resolvedCategories.sortedBy { it.title }
        }
    }

    fun getCategoriesOfUser() {
        viewModelScope.launch {
            _categories.value = categoryRepository.getCategories(null, null)
        }
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

    fun setAmountRange(min: Float?, max: Float?) {
        _filters.value = _filters.value.copy(minAmount = min, maxAmount = max)
        loadExpenses()
    }

    fun setDateRange(start: String?, end: String?) {
        _filters.value = _filters.value.copy(startDate = start, endDate = end)
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

    fun getCategoryTitle(categoryId: Int): String {
        return _categories.value.find { it.id == categoryId }?.title ?: "Unknown"
    }
}
