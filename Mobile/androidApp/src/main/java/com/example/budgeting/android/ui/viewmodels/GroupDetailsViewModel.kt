package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Group
import com.example.budgeting.android.data.model.GroupLog
import com.example.budgeting.android.data.model.UserData
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.ExpensePayment
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.ExpensePaymentRepository
import com.example.budgeting.android.data.repository.ExpenseRepository
import com.example.budgeting.android.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class GroupExpense(
    val expense: Expense,
    val userName: String,
    val description: String? = null
)

class GroupDetailsViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)
    private val groupRepository = GroupRepository(
        RetrofitClient.groupInstance,
        RetrofitClient.expenseInstance,
        tokenDataStore
    )
    private val expensePaymentRepository = ExpensePaymentRepository(
        RetrofitClient.expensePaymentInstance,
        tokenDataStore
    )

    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group.asStateFlow()

    private val _members = MutableStateFlow<List<UserData>>(emptyList())
    val members: StateFlow<List<UserData>> = _members.asStateFlow()

    private val _expenses = MutableStateFlow<List<GroupExpense>>(emptyList())
    val expenses: StateFlow<List<GroupExpense>> = _expenses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _qrImage = MutableStateFlow<ByteArray?>(null)
    val qrImage: StateFlow<ByteArray?> = _qrImage.asStateFlow()

    private val _qrIsLoading = MutableStateFlow(false)
    val qrIsLoading: StateFlow<Boolean> = _qrIsLoading.asStateFlow()

    private val _qrError = MutableStateFlow<String?>(null)
    val qrError: StateFlow<String?> = _qrError.asStateFlow()

    private val _logs = MutableStateFlow<List<GroupLog>>(emptyList())
    val logs: StateFlow<List<GroupLog>> = _logs.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                TokenHolder.token = savedToken
            }
            _currentUserId.value = tokenDataStore.getUserId()
        }
    }

    fun loadGroup(groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                ensureTokenLoaded()

                // Load group details
                val groupResponse = groupRepository.getGroup(groupId)
                if (!groupResponse.isSuccessful || groupResponse.body() == null) {
                    _error.value = "Failed to load group"
                    _isLoading.value = false
                    return@launch
                }
                _group.value = groupResponse.body()
                _qrImage.value = null
                _qrError.value = null

                // Load group members
                val membersResponse = groupRepository.getUsersByGroup(groupId)
                val membersList = if (membersResponse.isSuccessful && membersResponse.body() != null) {
                    membersResponse.body()!!
                } else {
                    emptyList()
                }
                _members.value = membersList

                val expensesResponse = groupRepository.getExpensesByGroup(
                    groupId = groupId,
                    offset = 0,
                    limit = 100,
                    sortBy = "created_at",
                    order = "asc"
                )
                
                if (expensesResponse.isSuccessful) {
                    val expenses = expensesResponse.body() ?: emptyList()
                    val groupExpenses = mapExpensesToGroupExpenses(expenses, membersList)
                    _expenses.value = groupExpenses
                } else {
                    _error.value = "Error: ${expensesResponse.code()} - ${expensesResponse.message()}"
                    _expenses.value = emptyList()
                }

                // Load group logs (join/leave events)
                try {
                    val logsResponse = groupRepository.getGroupLogs(groupId)
                    if (logsResponse.isSuccessful && logsResponse.body() != null) {
                        _logs.value = logsResponse.body()!!
                    } else {
                        val errorMsg = "Failed to load group logs: ${logsResponse.code()}"
                        if (_error.value == null) {
                            _error.value = errorMsg
                        }
                        _logs.value = emptyList()
                    }
                } catch (e: Exception) {
                    _logs.value = emptyList()
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _expenses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapExpensesToGroupExpenses(
        expenses: List<Expense>, 
        members: List<UserData>
    ): List<GroupExpense> {
        val userMap = members.associateBy { it.id }
        
        return expenses.map { expense ->
            val userName = expense.user_id?.let { userId ->
                userMap[userId]?.let { user ->
                    "${user.firstName} ${user.lastName}"
                } ?: "Unknown User"
            } ?: "Group Member"
            
            GroupExpense(
                expense = expense,
                userName = userName,
                description = null
            )
        }
    }

    fun addExpenseFromPersonal(expense: Expense, description: String?, userName: String = "You") {
        addExpensesFromPersonal(listOf(expense), description, userName)
    }

    fun addExpensesFromPersonal(expenses: List<Expense>, description: String?, userName: String = "You") {
        if (expenses.isEmpty()) return
        
        val groupId = _group.value?.id
        if (groupId == null) {
            _error.value = "Group not loaded"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                ensureTokenLoaded()
                
                val userId = tokenDataStore.getUserId()
                if (userId == null) {
                    _error.value = "User not logged in"
                    _isLoading.value = false
                    return@launch
                }
                
                val existingExpenses = _expenses.value
                val skippedExpenses = mutableListOf<String>()
                val addedExpenseIds = mutableListOf<Int>()
                
                for (expense in expenses) {
                    // Check if expense is already in the group
                    val isDuplicate = existingExpenses.any { groupExpense ->
                        groupExpense.expense.title == expense.title &&
                        groupExpense.expense.amount == expense.amount &&
                        groupExpense.expense.categoryTitle == expense.categoryTitle &&
                        groupExpense.expense.user_id == userId
                    }
                    
                    if (isDuplicate) {
                        skippedExpenses.add(expense.title)
                        continue
                    }
                    
                    val expenseToCreate = expense.copy(
                        id = null,
                        user_id = userId,
                        group_id = groupId,
                        created_at = null
                    )

                    try {
                        val expenseId = groupRepository.addExpenseToGroup(expenseToCreate, description?.takeIf { it.isNotBlank() })
                        addedExpenseIds.add(expenseId)
                        
                        // Automatically mark the expense creator as having paid
                        try {
                            expensePaymentRepository.markPaid(expenseId, userId)
                        } catch (e: Exception) {
                        }
                    } catch (e: Exception) {
                        _error.value = "Failed to add expense '${expense.title}': ${e.message}"
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                if (skippedExpenses.isNotEmpty()) {
                    val skippedMessage = if (skippedExpenses.size == 1) {
                        "${skippedExpenses.first()} is already in this group"
                    } else {
                        "${skippedExpenses.size} expenses are already in this group"
                    }
                    _error.value = skippedMessage
                }
                
                if (addedExpenseIds.isNotEmpty()) {
                    try {
                        val expensesResponse = groupRepository.getExpensesByGroup(
                            groupId = groupId,
                            offset = 0,
                            limit = 100,
                            sortBy = "created_at",
                            order = "asc"
                        )
                        
                        if (expensesResponse.isSuccessful) {
                            val updatedExpenses = expensesResponse.body() ?: emptyList()
                            val membersList = _members.value
                            val groupExpenses = mapExpensesToGroupExpenses(updatedExpenses, membersList)
                            
                            val descriptionText = description?.takeIf { it.isNotBlank() }
                            val finalExpenses = if (descriptionText != null && addedExpenseIds.isNotEmpty()) {
                                groupExpenses.map { groupExpense ->
                                    if (addedExpenseIds.contains(groupExpense.expense.id)) {
                                        groupExpense.copy(description = descriptionText)
                                    } else {
                                        groupExpense
                                    }
                                }
                            } else {
                                groupExpenses
                            }
                            
                            _expenses.value = finalExpenses
                        } else {
                            _error.value = "Expenses added but failed to refresh list"
                        }
                        
                        val logsResponse = groupRepository.getGroupLogs(groupId)
                        if (logsResponse.isSuccessful && logsResponse.body() != null) {
                            _logs.value = logsResponse.body()!!
                        }
                    } catch (e: Exception) {
                        _error.value = "Expenses added but failed to refresh: ${e.message}"
                    }
                }
                
            } catch (e: Exception) {
                _error.value = "Error adding expenses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun ensureTokenLoaded() {
        if (TokenHolder.token.isNullOrBlank()) {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                TokenHolder.token = savedToken
            }
        }
    }

    fun loadGroupInviteQr(groupId: Int, forceRefresh: Boolean = false) {
        if (_qrImage.value != null && !forceRefresh) return

        viewModelScope.launch {
            _qrIsLoading.value = true
            _qrError.value = null

            try {
                ensureTokenLoaded()
                val response = groupRepository.getGroupInviteQr(groupId)
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val body = response.body()!!
                        val bytes = body.bytes()
                        if (bytes.isNotEmpty()) {
                            _qrImage.value = bytes
                            _qrError.value = null
                        } else {
                            _qrError.value = "QR code data is empty"
                        }
                    } catch (e: Exception) {
                        _qrError.value = "Failed to read QR code: ${e.message ?: "Unknown error"}"
                    }
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error (${response.code()})"
                    } catch (e: Exception) {
                        "Failed to fetch QR code: ${response.code()}"
                    }
                    _qrError.value = errorMessage
                }
            } catch (e: Exception) {
                _qrError.value = "Failed to load QR code: ${e.message ?: "Unknown error"}"
            } finally {
                _qrIsLoading.value = false
            }
        }
    }

    fun clearQrError() {
        _qrError.value = null
    }

    suspend fun getExpensePayments(expenseId: Int): List<ExpensePayment> {
        return try {
            expensePaymentRepository.getPayments(expenseId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun togglePaymentStatus(expenseId: Int, payerId: Int, isPaid: Boolean): Boolean {
        return try {
            val result = if (isPaid) {
                expensePaymentRepository.markPaid(expenseId, payerId)
            } else {
                expensePaymentRepository.unmarkPaid(expenseId, payerId)
            }
            result
        } catch (e: Exception) {
            false
        }
    }
}
