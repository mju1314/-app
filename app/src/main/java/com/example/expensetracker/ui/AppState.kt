package com.example.expensetracker.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.expensetracker.R

sealed class AppDestination(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
) {
    data object Home : AppDestination("home", R.string.nav_home, Icons.Filled.Home)
    data object Records : AppDestination("records", R.string.nav_records, Icons.AutoMirrored.Filled.ReceiptLong)
    data object RecordDetail : AppDestination("record_detail/{recordId}", R.string.title_record_detail, Icons.AutoMirrored.Filled.ReceiptLong)
    data object Stats : AppDestination("stats", R.string.nav_stats, Icons.Filled.Analytics)
    data object Settings : AppDestination("settings", R.string.nav_settings, Icons.Filled.Settings)
    data object AddExpense : AppDestination("add_expense", R.string.action_add_expense, Icons.Filled.AddCircle)
}

val bottomBarDestinations = listOf(
    AppDestination.Home,
    AppDestination.Records,
    AppDestination.Stats,
    AppDestination.Settings,
)
