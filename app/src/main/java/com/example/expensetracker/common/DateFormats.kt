package com.example.expensetracker.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormats {
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatMonthDay(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(monthDayFormatter)

    fun formatDateTime(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(dateTimeFormatter)

    fun formatDate(date: LocalDate): String = date.format(fullDateFormatter)
}

