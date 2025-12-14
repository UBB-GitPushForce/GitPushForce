package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.budgeting.android.ui.components.group.*
import com.example.budgeting.android.ui.viewmodels.GroupsViewModel
import com.example.budgeting.android.ui.viewmodels.GroupsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(onOpenGroup: (Int) -> Unit) {
    val context = LocalContext.current
    val vm: GroupsViewModel = viewModel(factory = GroupsViewModelFactory(context))
    val groupsState by vm.groups.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadGroups() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Groups",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )

            Text(
                text = "My Groups",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Show error message if any
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Show loading indicator or groups list
            if (isLoading && groupsState.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading groups...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    val displayGroups = groupsState
                        .filter { it.id != null && !it.name.isNullOrBlank() }
                    
                    if (displayGroups.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No groups yet",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Create or join a group to get started!",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    } else {
                        items(displayGroups) { group ->
                            GroupRow(group, onOpenGroup)
                        }
                    }
                }
            }

            // Bottom action buttons above nav bar
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Join Group button
                Button(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Join Group")
                }

                // Create Group button
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Create Group")
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            isLoading = isLoading,
            error = error,
            onCreate = { name, description ->
                val trimmedName = name.trim()
                if (trimmedName.isNotBlank()) {
                    vm.createGroup(
                        name = trimmedName,
                        description = description?.trim()?.ifBlank { null }
                    ) { group ->
                        showCreateDialog = false
                    }
                }
            }
        )
    }

    if (showJoinDialog) {
        JoinGroupDialog(
            onDismiss = { showJoinDialog = false },
            isLoading = isLoading,
            error = error,
            onJoinByCode = { code ->
                vm.joinGroupByInvitationCode(code) {
                    showJoinDialog = false
                }
            }
        )
    }
}
