package com.example.expensetracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.R
import com.example.expensetracker.common.CurrencyFormatter
import com.example.expensetracker.data.backup.AppBackupManager
import com.example.expensetracker.data.backup.IncompatibleBackupException
import com.example.expensetracker.data.entity.AccountEntity
import com.example.expensetracker.data.export.TransactionCsvExporter
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.repository.AccountRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.CategoryRepository
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
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val transientState = MutableStateFlow(SettingsUiState())

    private val categoriesFlow = categoryRepository.observeActiveCategoriesByType(0)

    val uiState: StateFlow<SettingsUiState> = combine(
        transientState,
        accountRepository.observeAll(),
        budgetRepository.observeAll(),
    ) { currentState, accounts, budgets ->
        Triple(currentState, accounts, budgets)
    }.combine(categoriesFlow) { (currentState, accounts, budgets), categories ->
        val categoryMap = categories.associate { it.id to it.name }
        val accountModels = accounts.map { account ->
            AccountUiModel(
                id = account.id,
                name = account.name,
                balanceText = CurrencyFormatter.formatCent(account.balance),
                balanceInCent = account.balance,
            )
        }
        val budgetModels = budgets.map { budget ->
            BudgetUiModel(
                id = budget.id,
                categoryId = budget.categoryId,
                categoryName = budget.categoryId?.let { categoryMap[it] },
                amountText = CurrencyFormatter.formatCent(budget.amount),
                amountInCent = budget.amount,
            )
        }
        currentState.copy(
            accounts = accountModels,
            totalBalanceText = CurrencyFormatter.formatCent(accounts.sumOf { it.balance }),
            budgets = budgetModels,
            categoryOptions = categories.map { CategoryOptionUiModel(id = it.id, name = it.name) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun addAccount(name: String, balanceInCent: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            accountRepository.insert(
                AccountEntity(
                    name = name,
                    balance = balanceInCent,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
    }

    fun updateAccount(id: Long, name: String, balanceInCent: Long) {
        viewModelScope.launch {
            val existing = accountRepository.getById(id) ?: return@launch
            accountRepository.update(
                existing.copy(
                    name = name,
                    balance = balanceInCent,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            val existing = accountRepository.getById(id) ?: return@launch
            accountRepository.delete(existing)
        }
    }

    fun saveBudget(categoryId: Long?, amountInCent: Long) {
        if (amountInCent <= 0) return
        viewModelScope.launch {
            budgetRepository.upsert(categoryId, amountInCent)
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            val budgets = uiState.value.budgets
            val budget = budgets.firstOrNull { it.id == budgetId } ?: return@launch
            budgetRepository.delete(
                com.example.expensetracker.data.entity.BudgetEntity(
                    id = budget.id,
                    categoryId = budget.categoryId,
                    amount = budget.amountInCent,
                    createdAt = 0,
                    updatedAt = 0,
                ),
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

                val outputStream = openOutputStream() ?: error("Output stream is null")
                outputStream.use {
                    TransactionCsvExporter.export(it, rows)
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
                val messageResId = if (result.exceptionOrNull() is IncompatibleBackupException) {
                    R.string.settings_restore_incompatible
                } else {
                    R.string.settings_restore_failed
                }
                transientState.value = transientState.value.copy(
                    isRestoring = false,
                    restoreMessageResId = messageResId,
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
                userPreferencesRepository.clearAll()
                R.string.settings_clear_data_success
            }.getOrDefault(R.string.settings_clear_data_failed)

            transientState.value = transientState.value.copy(
                isClearingData = false,
                infoMessageResId = messageResId,
            )
        }
    }
}
