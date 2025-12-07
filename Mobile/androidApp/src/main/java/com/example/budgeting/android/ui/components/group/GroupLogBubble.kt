package com.example.budgeting.android.ui.components.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgeting.android.data.model.GroupLog
import com.example.budgeting.android.data.model.UserData

@Composable
fun GroupLogBubble(
    log: GroupLog,
    members: List<UserData>
) {
    val memberMap = members.associateBy { it.id }
    val user = memberMap[log.user_id]
    val userName = user?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown User"
    
    val actionText = when (log.action.uppercase()) {
        "JOIN" -> "joined the group"
        "LEAVE" -> "left the group"
        else -> log.action.lowercase()
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "$userName $actionText",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

