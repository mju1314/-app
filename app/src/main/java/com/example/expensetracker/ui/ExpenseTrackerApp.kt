package com.example.expensetracker.ui

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.R
import com.example.expensetracker.ui.add.AddExpenseRoute
import com.example.expensetracker.ui.home.HomeRoute
import com.example.expensetracker.ui.records.RecordDetailRoute
import com.example.expensetracker.ui.records.RecordsRoute
import com.example.expensetracker.ui.settings.SettingsRoute
import com.example.expensetracker.ui.stats.StatsRoute

@Composable
fun ExpenseTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val fullScreenRoutes = setOf(
        AppDestination.AddExpense.route,
        AppDestination.RecordDetail.route,
    )
    val showFab = currentDestination?.route !in fullScreenRoutes
    val showBottomBar = currentDestination?.route !in fullScreenRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarDestinations.forEach { destination ->
                        val label = stringResource(id = destination.labelResId)
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(AppDestination.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(AppDestination.AddExpense.route) },
                ) {
                    Text(text = stringResource(id = R.string.action_add_expense))
                }
            }
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding() + if (showFab) {
                FloatingActionButtonClearance
            } else {
                0.dp
            },
        )

        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
        ) {
            composable(AppDestination.Home.route) {
                HomeRoute(
                    contentPadding = contentPadding,
                    onRecordClick = { recordId ->
                        navController.navigate("record_detail/$recordId")
                    },
                )
            }
            composable(AppDestination.Records.route) {
                RecordsRoute(
                    contentPadding = contentPadding,
                    onRecordClick = { recordId ->
                        navController.navigate("record_detail/$recordId")
                    },
                )
            }
            composable(
                route = AppDestination.RecordDetail.route,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
            ) {
                RecordDetailRoute(
                    contentPadding = contentPadding,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Stats.route) {
                StatsRoute(contentPadding = contentPadding)
            }
            composable(AppDestination.Settings.route) {
                SettingsRoute(contentPadding = contentPadding)
            }
            composable(AppDestination.AddExpense.route) {
                AddExpenseRoute(
                    contentPadding = contentPadding,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private val FloatingActionButtonClearance = 88.dp
