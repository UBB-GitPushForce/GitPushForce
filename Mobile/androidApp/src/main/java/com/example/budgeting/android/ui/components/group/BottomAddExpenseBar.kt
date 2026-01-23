package com.example.budgeting.android.ui.components.group

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.ui.utils.GroupUtils
import com.example.budgeting.android.ui.viewmodels.CategoryViewModel
import com.example.budgeting.android.ui.viewmodels.CategoryViewModelFactory
import com.example.budgeting.android.ui.viewmodels.ExpenseMode
import com.example.budgeting.android.ui.viewmodels.ExpenseViewModel
import com.example.budgeting.android.ui.viewmodels.ExpenseViewModelFactory
import com.example.budgeting.android.ui.viewmodels.GroupDetailsViewModel
import com.example.budgeting.android.ui.viewmodels.GroupExpense

@Composable
fun BottomAddExpenseBar(
    vm: GroupDetailsViewModel
) {
    val context = LocalContext.current
    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(context)
    )
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(context)
    )
    
    LaunchedEffect(Unit) { 
        expenseViewModel.setMode(ExpenseMode.PERSONAL)
        expenseViewModel.loadExpenses()
    }
    
    val personalExpenses by expenseViewModel.expenses.collectAsState()
    val currentUserId by expenseViewModel.currentUserId.collectAsState()
    val groupExpenses by vm.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    
    val categoryMap = remember(categories) {
        categories.associate { it.id to it.title }
    }
    val personalExpensesOnly = remember(personalExpenses, groupExpenses, currentUserId) {
        if (currentUserId == null) return@remember emptyList()
        
        val existingExpenseIds = groupExpenses.map { it.expense.id }.toSet()
        personalExpenses.filter { expense ->
            expense.group_id == null && 
            expense.user_id != null &&
            expense.user_id == currentUserId &&
            !existingExpenseIds.contains(expense.id) &&
            !GroupUtils.isExpenseAlreadyInGroup(expense, groupExpenses)
        }
    }
    var showPicker by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    BackHandler(enabled = showPicker) {
        showPicker = false
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add description (optional)") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Button(
                onClick = { showPicker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text("+")
            }
        }
    }

    if (showPicker) {
        ExpensePickerDialog(
            expenses = personalExpensesOnly,
            categoryMap = categoryMap,
            onDismiss = { showPicker = false },
            onConfirm = { selected ->
                vm.addExpensesFromPersonal(selected, description, "You")
                description = ""
                showPicker = false
            }
        )
    }
}

