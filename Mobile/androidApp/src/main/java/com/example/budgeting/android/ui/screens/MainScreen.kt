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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.activity.compose.BackHandler
import android.app.Activity
import android.widget.Toast
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager

val bottomNavItems = listOf(
    BottomNavItem("Expenses", Icons.Default.Home),
    BottomNavItem("Groups", Icons.Default.Group),
    BottomNavItem("Receipts", Icons.Default.Receipt),
    BottomNavItem("Analytics", Icons.Default.BarChart),
    BottomNavItem("Profile", Icons.Default.Person)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val navController = rememberNavController()
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val lastBackPress = remember { mutableStateOf(0L) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(pagerState.currentPage) {
        focusManager.clearFocus()
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            // Handle back: go to home tab if not on it; otherwise double-press to exit
            BackHandler {
                if (pagerState.currentPage != 0) {
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                } else {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPress.value < 2000L) {
                        activity?.finish()
                    } else {
                        lastBackPress.value = now
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    }
                }
            }

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
                        0 -> ExpensesScreen()
                        1 -> GroupsScreen(onOpenGroup = { id -> navController.navigate("groupDetails/$id") })
                        2 -> ReceiptScreen()
                        3 -> AnalyticsScreen()
                        4 -> ProfileScreen(onLogout = onLogout)
                    }
                }
            }
        }

        composable("groupDetails/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: return@composable
            GroupDetailsScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
