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
import com.example.budgeting.android.ui.viewmodels.ExpenseMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(LocalContext.current)
    )
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val error by expenseViewModel.error.collectAsState()
    val mode by expenseViewModel.mode.collectAsState()

    val filters by expenseViewModel.filters.collectAsState()
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
                title = { Text("Expenses") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedExpense = null
                    showDialog = true
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        ) {

            // -------------------------------------------------------------------
            // MODE TOGGLE: PERSONAL | GROUP | ALL
            // -------------------------------------------------------------------
            ModeSelector(
                selected = mode,
                onSelected = { expenseViewModel.setMode(it) }
            )

            Spacer(Modifier.height(4.dp))

            // -------------------------------------------------------------------
            // FILTER BAR
            // -------------------------------------------------------------------
            FilterBar(
                search = filters.search,
                onSearchChange = { expenseViewModel.setSearchQuery(it) },

                categories = expenseViewModel.categories.collectAsState().value,
                selectedCategory = filters.category,
                onCategorySelected = { expenseViewModel.setCategoryFilter(it) },

                sortOption = filters.sortOption,
                onSortSelected = { expenseViewModel.setSortOption(it) }
            )

            Spacer(Modifier.height(8.dp))

            // -------------------------------------------------------------------
            // CONTENT AREA
            // -------------------------------------------------------------------
            Box(modifier = Modifier.fillMaxSize()) {

                when {
                    isLoading -> CenterLoading()

                    error != null -> CenterError(error!!)

                    expenses.isEmpty() -> EmptyState()

                    else -> ExpensesList(
                        expenses = expenses,
                        onClick = { expense ->
                            selectedExpense = expense
                            showDialog = true
                        },
                        onLongClick = { expense ->
                            selectedExpense = expense
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // ADD/EDIT DIALOG
        if (showDialog) {
            AddEditExpenseDialog(
                expense = selectedExpense,
                onDismiss = { showDialog = false },
                onSave = { expense ->
                    if (selectedExpense != null)
                        expenseViewModel.updateExpense(expense.copy(id = selectedExpense!!.id))
                    else
                        expenseViewModel.addExpense(expense)

                    selectedExpense = null
                    showDialog = false
                }
            )
        }

        // DELETE CONFIRMATION
        if (showDeleteDialog && selectedExpense != null) {
            DeleteExpenseDialog(
                expense = selectedExpense!!,
                onConfirm = {
                    expenseViewModel.deleteExpense(selectedExpense!!)
                    showDeleteDialog = false
                    selectedExpense = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    selectedExpense = null
                }
            )
        }
    }
}


@Composable
fun FilterBar(
    search: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    sortOption: SortOption,
    onSortSelected: (SortOption) -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            label = { Text("Search") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        // Category button
        Box {
            IconButton(onClick = { showCategoryMenu = true }) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = "Category",
                    tint =
                        if (selectedCategory != "All")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                CategoryMenu(
                    categories = categories,
                    selected = selectedCategory,
                    onSelected = {
                        onCategorySelected(it)
                        showCategoryMenu = false
                    }
                )
            }
        }

        // Sort button
        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort"
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortMenu { option ->
                    onSortSelected(option)
                    showSortMenu = false
                }
            }
        }
    }
}


@Composable
fun ModeSelector(
    selected: ExpenseMode,
    onSelected: (ExpenseMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ExpenseMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@Composable
fun CategoryMenu(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    categories.forEach { category ->
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
            text = { Text(option.label.replace("_", " ")) },
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

@Composable
fun CenterLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun CenterError(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error: $message", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No expenses found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExpensesList(
    expenses: List<Expense>,
    onClick: (Expense) -> Unit,
    onLongClick: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(expenses) { expense ->
            ExpenseItem(
                expense = expense,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .combinedClickable(
                        onClick = { onClick(expense) },
                        onLongClick = { onLongClick(expense) }
                    )
            )
        }
    }
}

@Composable
fun DeleteExpenseDialog(
    expense: Expense,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Expense") },
        text = { Text("Are you sure you want to delete '${expense.title}'?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

