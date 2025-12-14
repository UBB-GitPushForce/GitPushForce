package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.Category
import com.example.budgeting.android.data.model.CategoryBody
import com.example.budgeting.android.ui.viewmodels.CategoryViewModel
import com.example.budgeting.android.ui.viewmodels.CategoryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(LocalContext.current)
    )
) {
    val categories by categoryViewModel.categories.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val error by categoryViewModel.error.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingCategory = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                categories.isEmpty() -> EmptyCategoriesState()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            onClick = {
                                editingCategory = category
                                showDialog = true
                            },
                            onLongClick = {
                                categoryToDelete = category
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog && categoryToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = { Text("Delete category") },
                text = {
                    Text("Are you sure you want to delete \"${categoryToDelete!!.title}\"?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        categoryViewModel.deleteCategory(categoryToDelete!!.id!!)
                        showDeleteDialog = false
                        categoryToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        categoryToDelete = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showDialog) {
        AddEditCategoryDialog(
            category = editingCategory,
            onDismiss = { showDialog = false },
            onSave = { title, keywords ->
                if (editingCategory != null) {
                    categoryViewModel.updateCategory(
                        editingCategory!!.id!!,
                        CategoryBody(title = title, keywords = keywords)
                    )
                } else {
                    categoryViewModel.addCategory(
                        CategoryBody(title = title, keywords = keywords)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf(category?.title ?: "") }
    var keywordInput by remember { mutableStateOf("") }
    var keywords by remember {
        mutableStateOf(category?.keywords ?: mutableListOf())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (category == null) "Add category" else "Edit category",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("eg. Food") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    label = { Text("Add keyword") },
                    placeholder = { Text("eg. Grocery") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            enabled = keywordInput.isNotBlank(),
                            onClick = {
                                val keyword = keywordInput.trim()
                                if (keyword.isNotEmpty() && keyword !in keywords) {
                                    keywords = (keywords + keyword).toMutableList()
                                }
                                keywordInput = ""
                            }
                        ) {
                            Text("Add")
                        }
                    }
                )

                if (keywords.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        keywords.forEach { keyword ->
                            AssistChip(
                                onClick = {},
                                label = { Text(keyword) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.clickable {
                                            keywords =
                                                keywords.toMutableList().also { it.remove(keyword) }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(title, keywords)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CategoryItem(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = category.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            val keywords = category.keywords.orEmpty()
            if (keywords.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    keywords.forEach { keyword ->
                        AssistChip(
                            onClick = {},
                            label = { Text(keyword) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            Text(
                text = "Tap to edit · Hold to delete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyCategoriesState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No categories yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
