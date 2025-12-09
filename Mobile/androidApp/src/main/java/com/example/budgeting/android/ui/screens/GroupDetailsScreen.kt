package com.example.budgeting.android.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgeting.android.data.model.Expense
import com.example.budgeting.android.ui.viewmodels.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val qrImage by vm.qrImage.collectAsState()
    val qrIsLoading by vm.qrIsLoading.collectAsState()
    val qrError by vm.qrError.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showShareDialog, group?.id) {
        val id = group?.id
        if (showShareDialog && id != null) {
            vm.loadGroupInviteQr(id)
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
            
            // Error message
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Content: Loading or expenses list
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
                    val expensesWithDates = remember(expenses) {
                        expenses.map { expense ->
                            val date = parseDateFromString(expense.expense.created_at)
                            Triple(expense, date, date?.let { formatDateForDisplay(it) })
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        expensesWithDates.forEachIndexed { index, (groupExpense, expenseDate, displayDate) ->
                            val showDateHeader = index == 0 || 
                                expenseDate != expensesWithDates.getOrNull(index - 1)?.second
                            
                            if (showDateHeader && expenseDate != null) {
                                item(key = "date_${expenseDate}_$index") {
                                    DateHeader(displayDate ?: "Unknown Date")
                                }
                            }
                            
                            item(key = "expense_${groupExpense.expense.id}") {
                                ExpenseBubble(
                                    expense = groupExpense.expense,
                                    userName = groupExpense.userName,
                                    description = groupExpense.description
                                )
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
            onShareCode = { code ->
                shareGroupInvite(context, group!!.name ?: "Group", code)
            }
        )
    }
}

@Composable
private fun GroupMetaRow(
    memberCount: Int,
    onShareClick: () -> Unit,
    invitationCodeAvailable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$memberCount members",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Active members",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        FilledTonalButton(
            onClick = onShareClick,
            enabled = invitationCodeAvailable,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share group"
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
        }
    }
}

@Composable
private fun GroupShareDialog(
    groupName: String,
    invitationCode: String?,
    qrBytes: ByteArray?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onShareCode: (String) -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(qrBytes) {
        qrBytes?.let {
            runCatching { BitmapFactory.decodeByteArray(it, 0, it.size) }.getOrNull()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("✕", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Share \"${groupName}\"",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Share this QR code or invitation code to invite others.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Invitation code",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = invitationCode ?: "Not available",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator()
                        }
                        qrBitmap != null -> {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Group QR",
                                modifier = Modifier.size(220.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = "QR code not available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                error?.let { errorText ->
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        invitationCode?.let { code ->
                            onShareCode(code)
                        } ?: run {
                            Toast.makeText(
                                context,
                                "Invitation code unavailable",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = invitationCode != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Share code")
                }
            }
        }
    }
}

private fun shareGroupInvite(context: Context, groupName: String, invitationCode: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Join $groupName")
        putExtra(
            Intent.EXTRA_TEXT,
            "Join \"$groupName\" using this invitation code: $invitationCode"
        )
    }
    try {
        context.startActivity(
            Intent.createChooser(shareIntent, "Share group invite")
        )
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Unable to open share options",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}


private fun parseDateFromString(dateString: String?): LocalDate? {
    if (dateString == null) return null
    
    return try {
        try {
            val zonedDateTime = java.time.ZonedDateTime.parse(dateString)
            return zonedDateTime.toLocalDate()
        } catch (e: Exception) {
        }
        
        try {
            val dateTime = java.time.LocalDateTime.parse(dateString.take(19))
            return dateTime.toLocalDate()
        } catch (e: Exception) {
        }
        
        try {
            return LocalDate.parse(dateString.take(10))
        } catch (e: Exception) {
        }
        
        try {
            val cleaned = dateString.replace("T", " ").take(19)
            val dateTime = java.time.LocalDateTime.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            return dateTime.toLocalDate()
        } catch (e: Exception) {
        }
        
        null
    } catch (e: Exception) {
        null
    }
}

private fun formatDateForDisplay(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    
    return when {
        date == today -> "Today"
        date == yesterday -> "Yesterday"
        date.year == today.year -> date.format(DateTimeFormatter.ofPattern("MMM d"))
        else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

private fun isExpenseAlreadyInGroup(
    expense: Expense,
    groupExpenses: List<GroupExpense>
): Boolean {
    return groupExpenses.any { groupExpense ->
        groupExpense.expense.title == expense.title &&
        groupExpense.expense.amount == expense.amount &&
        groupExpense.expense.categoryId == expense.categoryId &&
        groupExpense.expense.user_id == expense.user_id
    }
}

@Composable
private fun ExpenseBubble(
    expense: Expense,
    userName: String,
    description: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // User name row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = userName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Expense bubble
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = "${expense.title} - $${String.format("%.2f", expense.amount)}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomAddExpenseBar(
    vm: GroupDetailsViewModel
) {
    val context = LocalContext.current
    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(context)
    )
    
    LaunchedEffect(Unit) { 
        expenseViewModel.loadExpenses() 
    }
    
    val personalExpenses by expenseViewModel.expenses.collectAsState()
    val groupExpenses by vm.expenses.collectAsState()
    val personalExpensesOnly = remember(personalExpenses, groupExpenses) {
        val existingExpenseIds = groupExpenses.map { it.expense.id }.toSet()
        personalExpenses.filter { expense ->
            expense.group_id == null && 
            !existingExpenseIds.contains(expense.id) &&
            !isExpenseAlreadyInGroup(expense, groupExpenses)
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
            onDismiss = { showPicker = false },
            onConfirm = { selected ->
                vm.addExpensesFromPersonal(selected, description, "You")
                description = ""
                showPicker = false
            }
        )
    }
}

@Composable
private fun ExpensePickerDialog(
    expenses: List<Expense>,
    onDismiss: () -> Unit,
    onConfirm: (List<Expense>) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("✕", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Choose expenses",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                }

                // Expense list
                val selected = remember { mutableStateListOf<Int>() }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(expenses) { index, expense ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected.contains(index)) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selected.contains(index)) {
                                        selected.remove(index)
                                    } else {
                                        selected.add(index)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.title,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = expense.categoryId.toString(), // TODO pretty print the category id
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "$${"%.2f".format(expense.amount)}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val items = selected.map { expenses[it] }
                            onConfirm(items)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
