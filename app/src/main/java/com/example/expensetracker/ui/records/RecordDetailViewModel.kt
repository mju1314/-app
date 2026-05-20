package com.example.expensetracker.ui.records

import com.example.expensetracker.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.repository.AccountRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.ui.add.SelectOptionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val transactionId: Long = checkNotNull(savedStateHandle["recordId"])
    private val formState = MutableStateFlow(RecordDetailUiState())
    private var originalEntity: TransactionEntity? = null
    private var hasInitializedForm = false

    private val categoriesFlow = formState
        .map { it.transactionType }
        .flatMapLatest { type ->
            categoryRepository.observeActiveCategoriesByType(type).map { categories ->
                categories.map { SelectOptionUiModel(id = it.id, label = it.name) }
            }
        }

    val uiState: StateFlow<RecordDetailUiState> = combine(
        formState,
        transactionRepository.observeTransactionDetail(transactionId),
        categoriesFlow,
        accountRepository.observeAll().map { accounts ->
            accounts.map { SelectOptionUiModel(id = it.id, label = it.name) }
        },
    ) { currentState, detail, categoryOptions, accountOptions ->
        if (detail == null) {
            return@combine currentState.copy(
                transactionId = transactionId,
                categoryOptions = categoryOptions,
                accountOptions = accountOptions,
                isLoading = false,
                errorMessageResId = currentState.errorMessageResId ?: R.string.error_record_not_found,
            )
        }

        originalEntity = TransactionEntity(
            id = detail.id,
            type = detail.type,
            amount = detail.amount,
            categoryId = detail.categoryId,
            accountId = detail.accountId,
            note = detail.note,
            spentAt = detail.spentAt,
            createdAt = detail.createdAt,
            updatedAt = detail.updatedAt,
        )

        if (!hasInitializedForm) {
            hasInitializedForm = true
            formState.value = currentState.copy(
                transactionId = detail.id,
                transactionType = detail.type,
                amount = detail.amount.toPlainAmount(),
                selectedCategoryId = detail.categoryId,
                selectedAccountId = detail.accountId,
                note = detail.note.orEmpty(),
                spentAtMillis = detail.spentAt,
                spentAtText = DateFormats.formatDateTime(detail.spentAt),
                isLoading = false,
                errorMessageResId = null,
            )
        }

        currentState.copy(
            transactionId = detail.id,
            categoryOptions = categoryOptions,
            accountOptions = accountOptions,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordDetailUiState(),
    )

    fun updateTransactionType(type: Int) {
        formState.value = formState.value.copy(
            transactionType = type,
            selectedCategoryId = null,
            errorMessageResId = null,
        )
    }

    fun updateAmount(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }.let { text ->
            val firstDot = text.indexOf('.')
            if (firstDot < 0) text else text.substring(0, firstDot + 1) + text.substring(firstDot + 1).replace(".", "").take(2)
        }
        formState.value = formState.value.copy(amount = sanitized, errorMessageResId = null)
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(note = value, errorMessageResId = null)
    }

    fun updateSpentAt(timestamp: Long) {
        formState.value = formState.value.copy(
            spentAtMillis = timestamp,
            spentAtText = DateFormats.formatDateTime(timestamp),
            errorMessageResId = null,
        )
    }

    fun selectCategory(categoryId: Long) {
        formState.value = formState.value.copy(selectedCategoryId = categoryId, errorMessageResId = null)
    }

    fun selectAccount(accountId: Long?) {
        formState.value = formState.value.copy(selectedAccountId = accountId, errorMessageResId = null)
    }

    fun saveChanges(
        onSuccess: () -> Unit,
        onBalanceWarning: () -> Unit = {},
        onBudgetExceeded: (String) -> Unit = {},
    ) {
        val currentState = uiState.value
        val source = originalEntity ?: return
        val amountInCent = currentState.amount.toAmountInCent()

        val errorMessageResId = when {
            amountInCent == null || amountInCent <= 0L -> R.string.error_invalid_amount
            currentState.selectedCategoryId == null -> R.string.error_missing_category
            else -> null
        }

        if (errorMessageResId != null) {
            formState.value = formState.value.copy(errorMessageResId = errorMessageResId)
            return
        }

        viewModelScope.launch {
            formState.value = formState.value.copy(isSaving = true, errorMessageResId = null)
            val validatedAmountInCent = checkNotNull(amountInCent)
            val newAccountId = currentState.selectedAccountId
            val newType = currentState.transactionType
            val isExpense = newType == TransactionEntity.TYPE_EXPENSE

            val oldAccountId = source.accountId
            val oldType = source.type
            val needsBalanceAdjust = oldAccountId != newAccountId ||
                source.amount != validatedAmountInCent ||
                oldType != newType

            // Restore old balance
            if (oldAccountId != null && needsBalanceAdjust) {
                if (oldType == TransactionEntity.TYPE_EXPENSE) {
                    accountRepository.restoreBalance(oldAccountId, source.amount)
                } else {
                    accountRepository.deductBalance(oldAccountId, source.amount)
                }
            }

            // Apply new balance
            var balanceWillBeNegative = false
            if (newAccountId != null && needsBalanceAdjust) {
                if (isExpense) {
                    val currentBalance = accountRepository.getBalanceById(newAccountId)
                    if (currentBalance != null && currentBalance - validatedAmountInCent < 0) {
                        balanceWillBeNegative = true
                    }
                    accountRepository.deductBalance(newAccountId, validatedAmountInCent)
                } else {
                    accountRepository.restoreBalance(newAccountId, validatedAmountInCent)
                }
            }

            transactionRepository.update(
                source.copy(
                    type = newType,
                    amount = validatedAmountInCent,
                    categoryId = currentState.selectedCategoryId!!,
                    accountId = newAccountId,
                    note = currentState.note.trim().ifBlank { null },
                    spentAt = currentState.spentAtMillis,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            formState.value = formState.value.copy(isSaving = false)
            if (balanceWillBeNegative) onBalanceWarning()
            if (isExpense) {
                checkBudgetExceeded(currentState.selectedCategoryId!!, onBudgetExceeded)
            }
            onSuccess()
        }
    }

    private suspend fun checkBudgetExceeded(categoryId: Long, onExceeded: (String) -> Unit) {
        val categoryBudget = budgetRepository.getByCategoryId(categoryId)
        if (categoryBudget != null) {
            val categoryTotal = transactionRepository.getMonthCategoryTotal(categoryId)
            if (categoryTotal > categoryBudget.amount) {
                val categoryName = uiState.value.categoryOptions
                    .firstOrNull { it.id == categoryId }?.label.orEmpty()
                onExceeded(categoryName)
                return
            }
        }
        val totalBudget = budgetRepository.getByCategoryId(null)
        if (totalBudget != null) {
            val monthTotal = transactionRepository.getMonthTotal()
            if (monthTotal > totalBudget.amount) {
                onExceeded("")
            }
        }
    }

    fun deleteRecord(onSuccess: (TransactionEntity) -> Unit) {
        val source = originalEntity ?: return
        viewModelScope.launch {
            formState.value = formState.value.copy(isDeleting = true, errorMessageResId = null)
            onSuccess(source)
        }
    }
}

private fun Long.toPlainAmount(): String =
    BigDecimal(this).divide(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).toPlainString()

private fun String.toAmountInCent(): Long? {
    if (isBlank()) return null
    return runCatching {
        BigDecimal(this)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}
