package com.example.budgeting.android.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.ui.components.group.*
import com.example.budgeting.android.ui.utils.DateUtils
import com.example.budgeting.android.ui.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vm: GroupDetailsViewModel = viewModel(
        factory = GroupDetailsViewModelFactory(context)
    )

    BackHandler {
        onBack()
    }
    
    LaunchedEffect(groupId) { 
        vm.loadGroup(groupId) 
    }
    
    val group by vm.group.collectAsState()
    val expenses by vm.expenses.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    val members by vm.members.collectAsState()
    val logs by vm.logs.collectAsState()
    val currentUserId by vm.currentUserId.collectAsState()
    val qrImage by vm.qrImage.collectAsState()
    val qrIsLoading by vm.qrIsLoading.collectAsState()
    val qrError by vm.qrError.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<GroupExpense?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showShareDialog, group?.id) {
        val id = group?.id
        if (showShareDialog && id != null) {
            vm.loadGroupInviteQr(id, forceRefresh = true)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = group?.name ?: "Group",
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAddExpenseBar(vm = vm)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Group description
            group?.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            group?.let { currentGroup ->
                GroupMetaRow(
                    memberCount = members.size,
                    onShareClick = { showShareDialog = true },
                    invitationCodeAvailable = !currentGroup.invitationCode.isNullOrBlank()
                )
            }
            
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            when {
                isLoading && expenses.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                expenses.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses yet. Add one to get started!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    // Filter logs: exclude creator and current user's own JOIN messages
                    val creatorId = members.minByOrNull { it.id }?.id
                    val filteredLogs = remember(logs, currentUserId, creatorId) {
                        logs.filter { log ->
                            if (creatorId != null && log.user_id == creatorId) return@filter false
                            if (currentUserId != null && log.user_id == currentUserId && log.action.uppercase() == "JOIN") {
                                return@filter false
                            }
                            true    
                        }
                    }
                    
                    val timelineItems = remember(expenses, filteredLogs) {
                        val items = mutableListOf<TimelineItem>()
                        
                        expenses.forEach { expense ->
                            items.add(TimelineItem.ExpenseItem(expense, expense.expense.created_at))
                        }
                        
                        filteredLogs.forEach { log ->
                            items.add(TimelineItem.LogItem(log, log.created_at))
                        }
                        
                        items.sortedByDescending { item ->
                            when (item) {
                                is TimelineItem.ExpenseItem -> item.timestamp ?: ""
                                is TimelineItem.LogItem -> item.timestamp ?: ""
                            }
                        }
                    }
                    
                    val timelineWithDates = remember(timelineItems) {
                        timelineItems.map { item ->
                            val timestamp = when (item) {
                                is TimelineItem.ExpenseItem -> item.timestamp
                                is TimelineItem.LogItem -> item.timestamp
                            }
                            val date = DateUtils.parseDateFromString(timestamp)
                            Triple(item, date, date?.let { DateUtils.formatDateForDisplay(it) })
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        timelineWithDates.forEachIndexed { index, (item, itemDate, displayDate) ->
                            val showDateHeader = index == 0 || 
                                itemDate != timelineWithDates.getOrNull(index - 1)?.second
                            
                            if (showDateHeader && itemDate != null) {
                                item(key = "date_${itemDate}_$index") {
                                    DateHeader(displayDate ?: "Unknown Date")
                                }
                            }
                            
                            when (item) {
                                is TimelineItem.ExpenseItem -> {
                                    item(key = "expense_${item.expense.expense.id}") {
                                        ExpenseBubble(
                                            expense = item.expense.expense,
                                            userName = item.expense.userName,
                                            description = item.expense.description,
                                            onClick = {
                                                // Only allow expense owner to manage payments
                                                if (item.expense.expense.user_id == currentUserId) {
                                                    selectedExpense = item.expense
                                                    showPaymentDialog = true
                                                }
                                            },
                                            isClickable = item.expense.expense.user_id == currentUserId
                                        )
                                    }
                                }
                                is TimelineItem.LogItem -> {
                                    item(key = "log_${item.log.id}") {
                                        GroupLogBubble(
                                            log = item.log,
                                            members = members
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog && group != null) {
        GroupShareDialog(
            groupName = group!!.name ?: "Group",
            invitationCode = group!!.invitationCode,
            qrBytes = qrImage,
            isLoading = qrIsLoading,
            error = qrError,
            onDismiss = {
                showShareDialog = false
                vm.clearQrError()
            },
            onRetry = {
                group?.id?.let { id ->
                    vm.loadGroupInviteQr(id, forceRefresh = true)
                }
            }
        )
    }

    if (showPaymentDialog && selectedExpense != null) {
        ExpensePaymentDialog(
            expense = selectedExpense!!.expense,
            members = members,
            vm = vm,
            onDismiss = {
                showPaymentDialog = false
                selectedExpense = null
                vm.loadGroup(groupId) // Reload group to refresh expenses and payments
            }
        )
    }
}

