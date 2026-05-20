package com.example.expensetracker.ui.records

import androidx.annotation.StringRes
import com.example.expensetracker.R

data class RecordsUiState(
    val keyword: String = "",
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val categoryOptions: List<RecordFilterOptionUiModel> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedAccountName: String? = null,
    val accountOptions: List<RecordFilterOptionUiModel> = emptyList(),
    val minAmountText: String = "",
    val maxAmountText: String = "",
    val selectedRange: RecordsDateRange = RecordsDateRange.ALL,
    val availableRanges: List<RecordsDateRange> = RecordsDateRange.entries,
    val groups: List<RecordDayGroupUiModel> = emptyList(),
    val isLoading: Boolean = true,
)

data class RecordDayGroupUiModel(
    val dateLabel: String,
    val totalAmountText: String,
    val items: List<RecordListItemUiModel>,
    @StringRes val titleResId: Int = R.string.records_group_title,
)

data class RecordListItemUiModel(
    val id: Long,
    val type: Int = 0,
    val title: String,
    val subtitle: String,
    val amountText: String,
)

data class RecordFilterOptionUiModel(
    val id: Long,
    val label: String,
    val icon: String = "",
)

enum class RecordsDateRange(
    @StringRes val labelResId: Int,
) {
    ALL(R.string.records_filter_range_all),
    LAST_7_DAYS(R.string.records_filter_range_last_7_days),
    THIS_MONTH(R.string.records_filter_range_this_month),
}

data class RecordsFilterRange(
    val startTime: Long?,
    val endTime: Long?,
)
