package com.example.expensetracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.ui.common.CategoryIcons
import com.example.expensetracker.ui.components.SectionCard
import com.example.expensetracker.ui.theme.Jade20
import com.example.expensetracker.ui.theme.Jade40
import com.example.expensetracker.ui.theme.Jade80

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
            .verticalScroll(rememberScrollState())
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

        // Hero Card - 本月总览
        HeroCard(
            monthExpenseText = uiState.monthExpenseText,
            monthIncomeText = uiState.monthIncomeText,
        )

        // 今日卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentCard(
                title = stringResource(id = R.string.label_today_total),
                amount = uiState.todayExpenseText,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            AccentCard(
                title = stringResource(id = R.string.label_today_income),
                amount = uiState.todayIncomeText,
                accentColor = MaterialTheme.colorScheme.tertiary,
                amountColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
        }

        // 最近记录
        SectionCard(title = stringResource(id = R.string.label_recent_records)) {
            if (uiState.recentRecords.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.empty_recent_records),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column {
                    uiState.recentRecords.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecordClick(item.id) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 分类 emoji 图标
                            Text(
                                text = CategoryIcons.getEmoji(item.categoryIcon),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
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
                                color = if (item.type == TransactionEntity.TYPE_INCOME) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                        if (index < uiState.recentRecords.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    monthExpenseText: String,
    monthIncomeText: String,
) {
    val isDark = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }
    val gradientBrush = if (isDark) {
        Brush.linearGradient(colors = listOf(Jade20, Jade40))
    } else {
        Brush.linearGradient(colors = listOf(Jade40, Jade20))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(24.dp),
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.label_month_total),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = monthExpenseText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.label_month_income),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Jade80,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthIncomeText,
                        style = MaterialTheme.typography.titleMedium,
                        color = Jade80,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentCard(
    title: String,
    amount: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 0.dp),
        ) {
            // 左侧彩色装饰条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor),
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = amount,
                    style = MaterialTheme.typography.headlineSmall,
                    color = amountColor,
                )
            }
        }
    }
}

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}
