package com.example.budgeting.android.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgeting.android.data.local.TokenDataStore
import com.example.budgeting.android.data.model.Category
import com.example.budgeting.android.data.model.CategoryBody
import com.example.budgeting.android.data.network.RetrofitClient
import com.example.budgeting.android.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(context: Context): ViewModel() {
    val repository = CategoryRepository(RetrofitClient.categoryInstance)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCategories(null, null)
    }

    fun loadCategories(sortBy: String?, order: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = repository.getCategories(sortBy = sortBy, order = order)
                _categories.value = response
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun addCategory(category: CategoryBody) {
        viewModelScope.launch {
            _error.value = null

            try {
                repository.addCategory(category)
                loadCategories(null, null) // refresh
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun updateCategory(id: Int, category: CategoryBody) {
        viewModelScope.launch {
            _error.value = null

            try {
                repository.updateCategory(id, category)
                loadCategories(null, null) // refresh
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _error.value = null

            try {
                repository.deleteCategory(id)
                loadCategories(null, null) // refresh
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun getTitle(id: Int?): String {
        var title: String = ""
        viewModelScope.launch {
            _error.value = null

            try {
                title = repository.getTitle(id)
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
        return title
    }

}