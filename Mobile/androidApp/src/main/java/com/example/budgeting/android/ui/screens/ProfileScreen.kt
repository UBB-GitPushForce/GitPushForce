package com.example.budgeting.android.ui.screens

import android.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.UserUpdateRequest
import com.example.budgeting.android.ui.viewmodels.ProfileViewModel
import com.example.budgeting.android.ui.viewmodels.ProfileViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(LocalContext.current)
    )
) {
    val uiState by profileViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                actionColor = MaterialTheme.colorScheme.onPrimary,
                dismissActionContentColor = MaterialTheme.colorScheme.onPrimary,
                snackbarData = data
            )
        } },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    TextButton(
                        onClick = {
                            profileViewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        uiState.error?.let { err ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error: $err", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        val user = uiState.user ?: return@Scaffold

        // Editable field states
        var firstName by remember { mutableStateOf(user.first_name) }
        var lastName by remember { mutableStateOf(user.last_name) }
        var email by remember { mutableStateOf(user.email) }
        var phone by remember { mutableStateOf(user.phone_number) }
        var password by remember { mutableStateOf("") }
        var budget by remember { mutableStateOf(user.budget.toString()) }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ================= HEADER WITH AVATAR AND MENU ==================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = user.first_name.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(20.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "${user.first_name} ${user.last_name}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                var menuExpanded by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }
                var showPasswordDialog by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Profile") },
                            onClick = {
                                menuExpanded = false
                                profileViewModel.setEditing(true)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Change Password") },
                            onClick = {
                                menuExpanded = false
                                showPasswordDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // Attach dialogs to this row
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                        text = {
                            Text("This action cannot be undone.\nAll your data will be permanently deleted.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    profileViewModel.deleteUser()
                                    onLogout()
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showPasswordDialog) {
                    ChangePasswordDialog(
                        onDismiss = { showPasswordDialog = false },
                        onConfirm = { oldPassword, newPassword ->
                            showPasswordDialog = false

                            profileViewModel.changePassword(oldPassword = oldPassword, newPassword = newPassword)

                            // Show confirmation snackbar
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Password updated!",
                                    withDismissAction = true
                                )
                            }
                        }
                    )
                }
            }

            // =============== VIEW MODE ======================
            if (!uiState.isEditing) {
                InfoCard(title = "Phone Number", value = user.phone_number)
                uiState.budget?.let { budget ->
                    BudgetSummaryCard(totalBudget = budget.budget, spent = budget.spentThisMonth, remaining = budget.remainingBudget)
                }
            }

            // =============== EDIT MODE ======================
            else {

                EditField(label = "First Name", value = firstName) { firstName = it }
                EditField(label = "Last Name", value = lastName) { lastName = it }
                EditField(label = "Email", value = email) { email = it }
                EditField(label = "Phone Number", value = phone) { phone = it }
                EditField(label = "Budget", value = budget) { budget = it }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            profileViewModel.updateUser(
                                UserUpdateRequest(
                                    first_name = firstName,
                                    last_name = lastName,
                                    email = email,
                                    phone_number = phone,
                                    password = null,
                                    budget = budget.toDoubleOrNull() ?: user.budget
                                )
                            )

                            // Show confirmation snackbar
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Profile updated!",
                                    withDismissAction = true
                                )
                            }
                        }
                    ) { Text("Save") }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { profileViewModel.setEditing(false) }
                    ) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isValid = oldPassword.isNotBlank() && newPassword == confirmPassword && newPassword.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (newPassword != confirmPassword && confirmPassword.isNotEmpty()) {
                    Text(
                        "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotBlank()) {
                        onConfirm(oldPassword, newPassword)
                    }
                },
                enabled = newPassword == confirmPassword && newPassword.isNotBlank()
            ) {
                Text("Update")
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
fun BudgetSummaryCard(
    totalBudget: Double,
    spent: Double,
    remaining: Double,
    modifier: Modifier = Modifier
) {
    val realRemaining = totalBudget - spent

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header
            Text(
                text = "Budget Summary",
                style = MaterialTheme.typography.titleMedium
            )

            // Top row: Spent / Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                BudgetStat(
                    label = "Spent",
                    amount = spent,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                BudgetStat(
                    label = "Remaining",
                    amount = if(remaining > 0) remaining else realRemaining,
                    background = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            // Bottom row: Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Budget",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "$${totalBudget}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun BudgetStat(
    label: String,
    amount: Double,
    background: Color,
    modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = "$${amount.toInt()}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

