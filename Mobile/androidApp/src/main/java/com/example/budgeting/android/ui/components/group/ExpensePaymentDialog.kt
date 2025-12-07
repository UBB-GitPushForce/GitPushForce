package com.example.budgeting.android.ui.components.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.data.model.ExpensePayment
import com.example.budgeting.android.data.model.UserData
import com.example.budgeting.android.ui.viewmodels.GroupDetailsViewModel
import kotlinx.coroutines.launch

@Composable
fun ExpensePaymentDialog(
    expense: Expense,
    members: List<UserData>,
    vm: GroupDetailsViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var payments by remember { mutableStateOf<List<ExpensePayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Optimistic updates: track pending additions and removals
    var optimisticAdditions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var optimisticRemovals by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // Load payments when dialog opens
    LaunchedEffect(expense.id) {
        if (expense.id != null) {
            isLoading = true
            error = null
            try {
                payments = vm.getExpensePayments(expense.id!!)
                optimisticAdditions = emptySet()
                optimisticRemovals = emptySet()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    // Combine server payments with optimistic updates
    val paidUserIds = remember(payments, optimisticAdditions, optimisticRemovals) {
        val serverPaidIds = payments.map { it.user_id }.toSet()
        (serverPaidIds + optimisticAdditions) - optimisticRemovals
    }
    
    // Filter out the expense creator from the members list
    val filteredMembers = remember(members, expense.user_id) {
        members.filter { it.id != expense.user_id }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", expense.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Who paid?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Loading or error state
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // Members list with checkboxes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(filteredMembers.size) { index ->
                            val member = filteredMembers[index]
                            val isPaid = paidUserIds.contains(member.id)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                if (expense.id != null && member.id != null) {
                                                    coroutineScope.launch {
                                                        vm.togglePaymentStatus(
                                                            expense.id!!,
                                                            member.id!!,
                                                            !isPaid
                                                        )
                                                        payments = vm.getExpensePayments(expense.id!!)
                                                    }
                                                }
                                            }
                                        )
                                ) {
                                    Checkbox(
                                        checked = isPaid,
                                        onCheckedChange = { checked ->
                                            if (expense.id != null && member.id != null) {
                                                val memberId = member.id!!
                                                
                                                // Optimistic update
                                                if (checked) {
                                                    optimisticRemovals = optimisticRemovals - memberId
                                                    optimisticAdditions = optimisticAdditions + memberId
                                                } else {
                                                    optimisticAdditions = optimisticAdditions - memberId
                                                    optimisticRemovals = optimisticRemovals + memberId
                                                }
                                                
                                                coroutineScope.launch {
                                                    val success = vm.togglePaymentStatus(
                                                        expense.id!!,
                                                        memberId,
                                                        checked
                                                    )
                                                    
                                                    if (success) {
                                                        try {
                                                            payments = vm.getExpensePayments(expense.id!!)
                                                            optimisticAdditions = optimisticAdditions - memberId
                                                            optimisticRemovals = optimisticRemovals - memberId
                                                        } catch (e: Exception) {
                                                            // Keep optimistic update if reload fails
                                                        }
                                                    } else {
                                                        // Revert on failure
                                                        if (checked) {
                                                            optimisticAdditions = optimisticAdditions - memberId
                                                            optimisticRemovals = optimisticRemovals + memberId
                                                        } else {
                                                            optimisticRemovals = optimisticRemovals - memberId
                                                            optimisticAdditions = optimisticAdditions + memberId
                                                        }
                                                        error = "Failed to update payment status"
                                                    }
                                                }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                            disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                            disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                            disabledIndeterminateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${member.firstName} ${member.lastName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.Transparent)
                                        .clickable(
                                            onClick = {
                                                if (expense.id != null && member.id != null) {
                                                    val memberId = member.id!!
                                                    val newCheckedState = !isPaid
                                                    
                                                    if (newCheckedState) {
                                                        optimisticRemovals = optimisticRemovals - memberId
                                                        optimisticAdditions = optimisticAdditions + memberId
                                                    } else {
                                                        optimisticAdditions = optimisticAdditions - memberId
                                                        optimisticRemovals = optimisticRemovals + memberId
                                                    }
                                                    
                                                    coroutineScope.launch {
                                                        val success = vm.togglePaymentStatus(
                                                            expense.id!!,
                                                            memberId,
                                                            newCheckedState
                                                        )
                                                        
                                                        if (success) {
                                                            try {
                                                                payments = vm.getExpensePayments(expense.id!!)
                                                                optimisticAdditions = optimisticAdditions - memberId
                                                                optimisticRemovals = optimisticRemovals - memberId
                                                            } catch (e: Exception) {
                                                                // Keep optimistic update
                                                            }
                                                        } else {
                                                            if (newCheckedState) {
                                                                optimisticAdditions = optimisticAdditions - memberId
                                                                optimisticRemovals = optimisticRemovals + memberId
                                                            } else {
                                                                optimisticRemovals = optimisticRemovals - memberId
                                                                optimisticAdditions = optimisticAdditions + memberId
                                                            }
                                                            error = "Failed to update payment status"
                                                        }
                                                    }
                                                }
                                            },
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

