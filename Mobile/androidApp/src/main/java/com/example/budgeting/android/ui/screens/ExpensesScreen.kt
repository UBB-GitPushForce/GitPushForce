package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.ui.component.ExpenseItem
import com.example.budgeting.android.ui.viewmodels.ExpenseViewModelFactory
import com.example.budgeting.android.ui.viewmodels.ExpenseViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    onLogout: () -> Unit,
    expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory (LocalContext.current)
    )
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val isLoading by expenseViewModel.isLoading.collectAsState()
    val error by expenseViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        expenseViewModel.loadExpenses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Expenses") },
                actions = {
                    TextButton(
                        onClick = {
                            // Clear token and trigger navigation
                            expenseViewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
                            .height(36.dp)
                    ) {
                        Text(
                            "Logout",
                        )
                    }
                }
            )
        }
    ) { padding ->
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

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(expenses) { expense ->
                    ExpenseItem(expense)
                }
            }
        }
    }
}
