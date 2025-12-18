package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.*
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.CategoryRepository
import com.example.budgeting.android.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class ReceiptUiState {
    object Idle : ReceiptUiState()
    data class Loading(val message: String) : ReceiptUiState()
    object Reviewing : ReceiptUiState()
    data class Success(val message: String) : ReceiptUiState()
    data class Error(val message: String) : ReceiptUiState()
}

class ReceiptViewModel(context: Context) : ViewModel() {
    private val tokenDataStore = TokenDataStore(context.applicationContext)

    private val expenseRepository = ExpenseRepository(RetrofitClient.expenseInstance, tokenDataStore)
    private val categoryRepository = CategoryRepository(RetrofitClient.categoryInstance)
    private val receiptApi = RetrofitClient.receiptInstance

    private val _uiState = MutableStateFlow<ReceiptUiState>(ReceiptUiState.Idle)
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    private val _scannedItems = MutableStateFlow<List<ProcessedItem>>(emptyList())
    val scannedItems: StateFlow<List<ProcessedItem>> = _scannedItems.asStateFlow()

    init {
        viewModelScope.launch {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) TokenHolder.token = savedToken
        }
    }

    fun processReceiptImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = ReceiptUiState.Loading("Analyzing receipt with AI...")

            try {
                val file = File(context.cacheDir, "receipt_upload.jpg")
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output -> input.copyTo(output) }
                    }
                }

                val reqBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, reqBody)

                val response = receiptApi.processReceipt(part)

                if (response.isSuccessful && response.body() != null) {
                    _scannedItems.value = response.body()!!.items
                    _uiState.value = ReceiptUiState.Reviewing
                } else {
                    _uiState.value = ReceiptUiState.Error("AI Analysis failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ReceiptUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun saveExpenses() {
        viewModelScope.launch {
            _uiState.value = ReceiptUiState.Loading("Saving expenses...")

            try {
                var existingCategories = try {
                    categoryRepository.getCategories(null, null)
                } catch (e: Exception) { emptyList() }

                val itemsToSave = _scannedItems.value
                var successCount = 0

                itemsToSave.forEach { item ->
                    var targetCategory = existingCategories.find {
                        it.title.equals(item.category, ignoreCase = true)
                    }

                    if (targetCategory == null) {
                        try {
                            categoryRepository.addCategory(
                                CategoryBody(title = item.category, keywords = item.keywords)
                            )
                            existingCategories = categoryRepository.getCategories(null, null)
                            targetCategory = existingCategories.find {
                                it.title.equals(item.category, ignoreCase = true)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (targetCategory != null) {
                        try {
                            expenseRepository.addExpense(
                                Expense(
                                    id = null,
                                    title = item.name,
                                    amount = item.price,
                                    categoryId = targetCategory.id!!,
                                    description = "Scanned Receipt Item",
                                    user_id = null,
                                    group_id = null,
                                    created_at = null
                                )
                            )
                            successCount++
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                _uiState.value = ReceiptUiState.Success("Successfully saved $successCount expenses!")
                _scannedItems.value = emptyList()

            } catch (e: Exception) {
                _uiState.value = ReceiptUiState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun dismissError() { _uiState.value = ReceiptUiState.Idle }
    fun cancelReview() {
        _scannedItems.value = emptyList()
        _uiState.value = ReceiptUiState.Idle
    }
}
