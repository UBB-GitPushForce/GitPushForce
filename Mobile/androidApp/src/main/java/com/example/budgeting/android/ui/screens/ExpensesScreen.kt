package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.ui.component.ExpenseItem
import com.example.budgeting.android.ui.viewmodels.ExpenseViewModelFactory
import com.example.budgeting.android.ui.viewmodels.ExpenseViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory (LocalContext.current)
    )
) {
    val expenses by expenseViewModel.filteredExpenses.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val error by expenseViewModel.error.collectAsState()
    val focusManager = LocalFocusManager.current

    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        expenseViewModel.loadExpenses()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Expenses") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedExpense = null
                    showDialog = true },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->

        val filters by expenseViewModel.filters.collectAsState()

        Column(
            modifier = Modifier
                .padding(padding)
                .pointerInput(Unit){ detectTapGestures { focusManager.clearFocus() } }
        ) {
            if (!isLoading) {
                FilterBar(
                    search = filters.search,
                    onSearchChange = { expenseViewModel.setSearchQuery(it) },
                    categories = expenseViewModel.categories.collectAsState().value,
                    selectedCategory = filters.category,
                    onCategorySelected = { expenseViewModel.setCategoryFilter(it) },
                    sortOption = filters.sortOption,
                    onSortSelected = { expenseViewModel.setSortOption(it) }
                )
            }

            Box {
                when {
                    isLoading -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    error != null -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                    }

                    expenses.isEmpty() -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No bills yet")
                    }

                    else ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            items(expenses) { expense ->
                                ExpenseItem(
                                    expense = expense,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            selectedExpense = expense
                                            showDialog = true
                                        },
                                        onLongClick = {
                                            selectedExpense = expense
                                            showDeleteDialog = true
                                        }
                                    )
                                )
                            }
                        }
                }
            }
        }


        if (showDialog) {
            AddEditExpenseDialog(
                expense = selectedExpense,
                onDismiss = { showDialog = false },
                onSave = { expense ->
                    if (selectedExpense != null) expenseViewModel.updateExpense(expense.copy(id = selectedExpense!!.id))
                    else expenseViewModel.addExpense(expense)

                    selectedExpense = null
                    showDialog = false
                }
            )
        }

        if (showDeleteDialog && selectedExpense != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Expense") },
                text = { Text("Are you sure you want to delete '${selectedExpense!!.title}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        expenseViewModel.deleteExpense(selectedExpense!!)
                        showDeleteDialog = false
                        selectedExpense = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        selectedExpense = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}

@Composable
fun FilterBar(
    search: String,
    onSearchChange: (String) -> Unit,

    // Categories
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,

    // Sorting
    sortOption: SortOption,
    onSortSelected: (SortOption) -> Unit
) {
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // SEARCH BAR
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            label = { Text("Search") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Spacer(Modifier.width(10.dp))

        // CATEGORY ICON (Highlights when active)
        Box {
            IconButton(onClick = { categoryMenuOpen = true }) {
                Icon(
                    imageVector = Icons.Default.Category,
                    tint =
                        if (selectedCategory != "All")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Select Category"
                )
            }

            DropdownMenu(
                expanded = categoryMenuOpen,
                onDismissRequest = { categoryMenuOpen = false }
            ) {
                CategoryMenu(
                    categories = categories,
                    selected = selectedCategory,
                    onSelected = {
                        onCategorySelected(it)
                        categoryMenuOpen = false
                    }
                )
            }
        }

        // SORT ICON
        Box {
            IconButton(onClick = { sortMenuOpen = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Sort"
                )
            }

            DropdownMenu(
                expanded = sortMenuOpen,
                onDismissRequest = { sortMenuOpen = false }
            ) {
                SortMenu { option ->
                    onSortSelected(option)
                    sortMenuOpen = false
                }
            }
        }
    }
}

@Composable
fun CategoryMenu(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    val allCategories = listOf("All") + categories

    allCategories.forEach { category ->
        DropdownMenuItem(
            text = {
                Text(
                    text = category,
                    color = if (category == selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            },
            onClick = { onSelected(category) }
        )
    }
}

@Composable
fun SortMenu(onSelected: (SortOption) -> Unit) {
    SortOption.entries.forEach { option ->
        DropdownMenuItem(
            text = { Text(option.name.replace("_"," ")) },
            onClick = { onSelected(option) }
        )
    }
}

@Composable
fun AddEditExpenseDialog(
    expense: Expense?,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var category by remember { mutableStateOf(expense?.category ?: "") }
    var amount by remember { mutableStateOf(expense?.amount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expense == null) "Add Expense" else "Edit Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && category.isNotBlank()) {
                    onSave(Expense(title = title, category = category, amount = parsedAmount))
                }
            }) {
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
