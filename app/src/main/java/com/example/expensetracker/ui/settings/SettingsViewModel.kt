package com.example.expensetracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.data.backup.AppBackupManager
import com.example.expensetracker.data.export.TransactionCsvExporter
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val appBackupManager: AppBackupManager,
) : ViewModel() {
    private val transientState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        transientState,
        userPreferencesRepository.defaultCurrencyCode,
    ) { currentState, currencyCode ->
        currentState.copy(selectedCurrencyCode = currencyCode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun selectCurrency(currencyCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultCurrencyCode(currencyCode)
            transientState.value = transientState.value.copy(
                infoMessageResId = null,
                exportMessageResId = null,
                backupMessageResId = null,
                restoreMessageResId = null,
            )
        }
    }

    fun exportCsv(
        openOutputStream: () -> OutputStream?,
        onCompleted: (Boolean) -> Unit,
    ) {
        if (transientState.value.isExportingCsv) return

        viewModelScope.launch {
            transientState.value = transientState.value.copy(
                isExportingCsv = true,
                exportMessageResId = null,
                backupMessageResId = null,
                restoreMessageResId = null,
            )

            val result = runCatching {
                val rows = transactionRepository.getAllTransactionsForExport()
                if (rows.isEmpty()) {
                    transientState.value = transientState.value.copy(
                        isExportingCsv = false,
                        exportMessageResId = R.string.settings_export_csv_empty,
                    )
                    onCompleted(false)
                    return@launch
                }

                val currencyCode = uiState.value.selectedCurrencyCode
                val outputStream = openOutputStream() ?: error("Output stream is null")
                outputStream.use {
                    TransactionCsvExporter.export(it, rows, currencyCode)
                }
                transientState.value = transientState.value.copy(
                    isExportingCsv = false,
                    exportMessageResId = R.string.settings_export_csv_success,
                )
                onCompleted(true)
            }

            if (result.isFailure) {
                transientState.value = transientState.value.copy(
                    isExportingCsv = false,
                    exportMessageResId = R.string.settings_export_csv_failed,
                )
                onCompleted(false)
            }
        }
    }

    fun backupData(
        openOutputStream: () -> OutputStream?,
        onCompleted: (Boolean) -> Unit,
    ) {
        if (transientState.value.isBackingUp) return

        viewModelScope.launch {
            transientState.value = transientState.value.copy(
                isBackingUp = true,
                backupMessageResId = null,
                infoMessageResId = null,
                exportMessageResId = null,
                restoreMessageResId = null,
            )

            val result = runCatching {
                val outputStream = openOutputStream() ?: error("Output stream is null")
                outputStream.use {
                    appBackupManager.backup(it)
                }
                transientState.value = transientState.value.copy(
                    isBackingUp = false,
                    backupMessageResId = R.string.settings_backup_success,
                )
                onCompleted(true)
            }

            if (result.isFailure) {
                transientState.value = transientState.value.copy(
                    isBackingUp = false,
                    backupMessageResId = R.string.settings_backup_failed,
                )
                onCompleted(false)
            }
        }
    }

    fun restoreData(
        openInputStream: () -> java.io.InputStream?,
        onCompleted: (Boolean) -> Unit,
    ) {
        if (transientState.value.isRestoring) return

        viewModelScope.launch {
            transientState.value = transientState.value.copy(
                isRestoring = true,
                restoreMessageResId = null,
                infoMessageResId = null,
                exportMessageResId = null,
                backupMessageResId = null,
            )

            val result = runCatching {
                val inputStream = openInputStream() ?: error("Input stream is null")
                inputStream.use {
                    appBackupManager.restore(it)
                }
                transientState.value = transientState.value.copy(
                    isRestoring = false,
                    restoreMessageResId = R.string.settings_restore_success,
                )
                onCompleted(true)
            }

            if (result.isFailure) {
                transientState.value = transientState.value.copy(
                    isRestoring = false,
                    restoreMessageResId = R.string.settings_restore_failed,
                )
                onCompleted(false)
            }
        }
    }

    fun clearAllData() {
        if (transientState.value.isClearingData) return

        viewModelScope.launch {
            transientState.value = transientState.value.copy(
                isClearingData = true,
                infoMessageResId = null,
                exportMessageResId = null,
                backupMessageResId = null,
                restoreMessageResId = null,
            )
            val messageResId = runCatching {
                transactionRepository.clearAll()
                userPreferencesRepository.clearLastUsedPaymentMethodId()
                R.string.settings_clear_data_success
            }.getOrDefault(R.string.settings_clear_data_failed)

            transientState.value = transientState.value.copy(
                isClearingData = false,
                infoMessageResId = messageResId,
            )
        }
    }
}
