package com.example.expensetracker.data.export

import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.model.TransactionExportRow
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object TransactionCsvExporter {
    private const val CSV_HEADER =
        "\"ID\",\"时间\",\"金额\",\"分类\",\"支付方式\",\"备注\",\"创建时间\",\"更新时间\""

    fun export(
        outputStream: OutputStream,
        rows: List<TransactionExportRow>,
        currencyCode: String,
    ) {
        OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.appendLine(CSV_HEADER)
            rows.forEach { row ->
                writer.appendLine(
                    listOf(
                        row.id.toString(),
                        DateFormats.formatDateTime(row.spentAt),
                        CurrencyFormatter.formatCent(row.amount, currencyCode),
                        row.categoryName,
                        row.paymentMethodName,
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
