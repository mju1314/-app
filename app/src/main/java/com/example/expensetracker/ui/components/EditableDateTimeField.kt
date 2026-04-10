package com.example.expensetracker.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun EditableDateTimeField(
    timestamp: Long,
    valueText: String,
    onTimestampSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val is24Hour = DateFormat.is24HourFormat(context)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                context.showDateTimePicker(
                    initialTimestamp = timestamp,
                    zoneId = zoneId,
                    is24Hour = is24Hour,
                    onTimestampSelected = onTimestampSelected,
                )
            },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(id = R.string.spent_at_edit_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(id = R.string.spent_at_edit_action),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun android.content.Context.showDateTimePicker(
    initialTimestamp: Long,
    zoneId: ZoneId,
    is24Hour: Boolean,
    onTimestampSelected: (Long) -> Unit,
) {
    val initialDateTime = Instant.ofEpochMilli(initialTimestamp)
        .atZone(zoneId)
        .toLocalDateTime()

    DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedDateTime = LocalDateTime.of(
                        year,
                        month + 1,
                        dayOfMonth,
                        hourOfDay,
                        minute,
                    )
                    onTimestampSelected(
                        selectedDateTime.atZone(zoneId).toInstant().toEpochMilli(),
                    )
                },
                initialDateTime.hour,
                initialDateTime.minute,
                is24Hour,
            ).show()
        },
        initialDateTime.year,
        initialDateTime.monthValue - 1,
        initialDateTime.dayOfMonth,
    ).show()
}
