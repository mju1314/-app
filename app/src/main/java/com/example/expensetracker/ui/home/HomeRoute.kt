package com.example.expensetracker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.ui.components.SectionCard

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
    onRecordClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onRecordClick = onRecordClick,
    )
}

@Composable
private fun HomeScreen(
    contentPadding: PaddingValues,
    uiState: HomeUiState,
    onRecordClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(
                title = stringResource(id = R.string.label_today_total),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = uiState.todayTotalText,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            SectionCard(
                title = stringResource(id = R.string.label_month_total),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = uiState.monthTotalText,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        SectionCard(title = stringResource(id = R.string.label_recent_records)) {
            if (uiState.recentRecords.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.empty_recent_records),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.recentRecords.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecordClick(item.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = stringResource(
                                        id = item.subtitleResId,
                                        *item.subtitleArgs.toTypedArray(),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = item.amountText,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
