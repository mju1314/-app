package com.example.expensetracker.ui

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
fun ExpenseTrackerApp(appViewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val fullScreenRoutes = setOf(
        AppDestination.AddExpense.route,
        AppDestination.RecordDetail.route,
    )
    val showFab = currentDestination?.route !in fullScreenRoutes
    val showBottomBar = currentDestination?.route !in fullScreenRoutes

    val snackbarHostState = remember { SnackbarHostState() }
    val deleteMessage = stringResource(id = R.string.delete_undo_message)
    val undoLabel = stringResource(id = R.string.undo_action)

    // FAB 滚动隐藏/显示
    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10f) {
                    isFabVisible = false
                } else if (available.y > 10f) {
                    isFabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.deleteEvent.collect {
            val result = snackbarHostState.showSnackbar(
                message = deleteMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> appViewModel.undoDelete()
                SnackbarResult.Dismissed -> appViewModel.confirmDelete()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            AnimatedVisibility(
                visible = showFab && isFabVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
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
            bottom = paddingValues.calculateBottomPadding(),
        )

        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300),
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300),
                ) + fadeOut(animationSpec = tween(300))
            },
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
                    onDeleteRequest = { entity -> appViewModel.requestDelete(entity) },
                )
            }
            composable(
                route = AppDestination.RecordDetail.route,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
            ) {
                RecordDetailRoute(
                    contentPadding = contentPadding,
                    onNavigateBack = { navController.popBackStack() },
                    onDeleteRequest = { entity -> appViewModel.requestDelete(entity) },
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
