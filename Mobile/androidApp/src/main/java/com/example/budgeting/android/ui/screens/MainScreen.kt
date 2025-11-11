package com.example.budgeting.android.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.budgeting.android.data.model.BottomNavItem
import kotlinx.coroutines.launch

val bottomNavItems = listOf(
    BottomNavItem("Expenses", Icons.Default.Home),
    BottomNavItem("Groups", Icons.Default.Group),
    BottomNavItem("Receipts", Icons.Default.Receipt),
    BottomNavItem("Profile", Icons.Default.Person)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    // Hold the selected group ID in main so that the group detail screen will not include the navigation bar
    // TODO: might refactor this later
    val selectedGroupId = remember { mutableStateOf<Int?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ExpensesScreen(onLogout = onLogout)
                1 -> GroupsScreen(onOpenGroup = { id -> selectedGroupId.value = id })
                2 -> ReceiptsScreen()
                3 -> ProfileScreen()
            }
        }
    }

    // Overlay Group Details when selected
    selectedGroupId.value?.let { id ->
        GroupDetailsScreen(
            groupId = id,
            onBack = { selectedGroupId.value = null }
        )
    }
}
