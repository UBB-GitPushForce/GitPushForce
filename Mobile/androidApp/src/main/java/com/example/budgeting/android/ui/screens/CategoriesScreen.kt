package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.ui.viewmodels.CategoryViewModel
import com.example.budgeting.android.data.model.Category
import com.example.budgeting.android.data.model.CategoryBody
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
            CenterAlignedTopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                categories.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No categories found")
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            modifier = Modifier.padding(vertical = 4.dp),
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
                title = { Text("Delete Category") },
                text = {
                    Text("Are you sure you want to delete '${categoryToDelete!!.title}'?")
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

// ========================= ADD/EDIT CATEGORY DIALOG =========================
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
        mutableStateOf<MutableList<String>>(category?.keywords?.toMutableList() ?: mutableListOf())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (category == null) "Add Category" else "Edit Category")
        },
        text = {
            Column {

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Keyword input
                OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    label = { Text("Add keyword") },
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

                // Keyword chips
                if (keywords.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        keywords.forEach { keyword ->
                            AssistChip(
                                label = { Text(keyword) },
                                onClick = { /* maybe do nothing */ },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .clickable {
                                                keywords = keywords.toMutableList().also { it.remove(keyword) }
                                            }
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Title
            Text(
                text = category.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Keywords
            val keywords = category.keywords.orEmpty()
            if (keywords.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp),
                    verticalArrangement = Arrangement.Absolute.spacedBy(6.dp)
                ) {
                    keywords.forEach { keyword ->
                        AssistChip(
                            onClick = {},
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            label = { Text(keyword) }
                        )

                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tap to edit · Hold to delete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

