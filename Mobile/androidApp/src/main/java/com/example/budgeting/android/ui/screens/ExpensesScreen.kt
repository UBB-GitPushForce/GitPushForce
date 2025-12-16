package com.example.budgeting.android.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.*
import com.example.budgeting.android.ui.component.ExpenseItem
import com.example.budgeting.android.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    onOpenCategories: () -> Unit,
    expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(LocalContext.current)
    )
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val error by expenseViewModel.error.collectAsState()
    val mode by expenseViewModel.mode.collectAsState()
    val currentUserId by expenseViewModel.currentUserId.collectAsState()
    val filters by expenseViewModel.filters.collectAsState()
    val categories by expenseViewModel.categories.collectAsState()

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
            TopAppBar(
                title = {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onOpenCategories) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "Categories"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedExpense = null
                    showDialog = true
                },
                shape = RoundedCornerShape(16.dp)
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

            Spacer(Modifier.height(8.dp))

            FilterCard(
                search = filters.search,
                onSearchChange = { expenseViewModel.setSearchQuery(it) },
                categories = expenseViewModel.categories.collectAsState().value,
                selectedCategory = filters.category,
                onCategorySelected = { expenseViewModel.setCategoryFilter(it) },
                sortOption = filters.sortOption,
                onSortSelected = { expenseViewModel.setSortOption(it) }
            )

            Spacer(Modifier.height(12.dp))

            ModeSelector(
                selected = mode,
                onSelected = { expenseViewModel.setMode(it) }
            )

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> CenterLoading()
                    error != null -> CenterError(error!!)
                    expenses.isEmpty() -> EmptyState()
                    else -> {
                        val userId = currentUserId ?: return@Box
                        ExpensesList(
                            expenses = expenses,
                            currentUserId = userId,
                            vm = expenseViewModel,
                            onClick = {
                                if (it.user_id == userId) {
                                    selectedExpense = it
                                    showDialog = true
                                }
                            },
                            onLongClick = {
                                if (it.user_id == userId) {
                                    selectedExpense = it
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            expenseViewModel.getCategoriesOfUser()
            val realCategories = categories

            AddEditExpenseDialog(
                expense = selectedExpense,
                categories = realCategories,
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
fun FilterCard(
    search: String,
    onSearchChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    sortOption: SortOption,
    onSortSelected: (SortOption) -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                placeholder = { Text("Search expenses") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            Box {
                IconButton(onClick = { showCategoryMenu = true }) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = "Filter by category",
                        tint = if (selectedCategory != "All")
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
                    SortMenu {
                        onSortSelected(it)
                        showSortMenu = false
                    }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExpenseMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                label = {
                    Text(
                        mode.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            )
        }
    }
}

@Composable
fun CategoryMenu(
    categories: List<Category>,
    selected: String,
    onSelected: (String) -> Unit
) {
    categories.forEach { category ->
        DropdownMenuItem(
            text = { Text(category.title ?: "") },
            onClick = { onSelected(category.title ?: "") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    expense: Expense?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var amount by remember { mutableStateOf(expense?.amount?.toString() ?: "") }

    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(expense, categories) {
        selectedCategoryId =
            expense?.categoryId ?: categories.firstOrNull()?.id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (expense == null) "Add expense" else "Edit expense",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("eg. Grocery shopping")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categoryName = categories
                        .find { it.id == selectedCategoryId }
                        ?.title
                        ?: ""

                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.title ?: "") },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("eg. 168.99") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && selectedCategoryId != null,
                onClick = {
                    onSave(
                        Expense(
                            id = expense?.id,
                            title = title,
                            categoryId = selectedCategoryId!!,
                            amount = amount.toDoubleOrNull() ?: 0.0
                        )
                    )
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
fun CenterLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun CenterError(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No expenses yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExpensesList(
    expenses: List<Expense>,
    currentUserId: Int,
    vm: ExpenseViewModel,
    onClick: (Expense) -> Unit,
    onLongClick: (Expense) -> Unit
) {
    LazyColumn {
        items(expenses) { expense ->
            ExpenseItem(
                expense = expense,
                categoryTitle = vm.getCategoryTitle(expense.categoryId!!),
                currentUserId = currentUserId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
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
        title = { Text("Delete expense") },
        text = { Text("Are you sure you want to delete \"${expense.title}\"?") },
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
