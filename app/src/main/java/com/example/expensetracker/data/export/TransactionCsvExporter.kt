package com.example.expensetracker.data.export

import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.model.TransactionExportRow
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object TransactionCsvExporter {
    private const val CSV_HEADER =
        "\"ID\",\"类型\",\"时间\",\"金额\",\"分类\",\"账户\",\"备注\",\"创建时间\",\"更新时间\""

    fun export(
        outputStream: OutputStream,
        rows: List<TransactionExportRow>,
    ) {
        OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.appendLine(CSV_HEADER)
            rows.forEach { row ->
                writer.appendLine(
                    listOf(
                        row.id.toString(),
                        if (row.type == 1) "收入" else "支出",
                        DateFormats.formatDateTime(row.spentAt),
                        CurrencyFormatter.formatCent(row.amount),
                        row.categoryName,
                        row.accountName.orEmpty(),
                        row.note.orEmpty(),
                        DateFormats.formatDateTime(row.createdAt),
                        DateFormats.formatDateTime(row.updatedAt),
                    ).joinToString(separator = ",") { value ->
                        value.toCsvField()
                    },
                )
            }
            writer.flush()
        }
    }

    private fun String.toCsvField(): String =
        buildString {
            append('"')
            append(this@toCsvField.replace("\"", "\"\""))
            append('"')
        }
}
