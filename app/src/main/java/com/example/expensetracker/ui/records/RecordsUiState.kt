package com.example.expensetracker.ui.records

import androidx.annotation.StringRes
import com.example.expensetracker.R

data class RecordsUiState(
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
    val title: String,
    @StringRes val subtitleResId: Int = R.string.records_item_subtitle,
    val subtitleArgs: List<String> = emptyList(),
    val amountText: String,
)
