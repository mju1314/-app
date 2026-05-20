package com.example.expensetracker.ui.add

import com.example.expensetracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.repository.AccountRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.TransactionRepository
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
class AddExpenseViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(createInitialState())

    private val categoriesFlow = formState
        .map { it.transactionType }
        .flatMapLatest { type ->
            categoryRepository.observeActiveCategoriesByType(type).map { categories ->
                categories.map { SelectOptionUiModel(id = it.id, label = it.name, icon = it.icon) }
            }
        }

    val uiState: StateFlow<AddExpenseUiState> = combine(
        formState,
        categoriesFlow,
        accountRepository.observeAll().map { accounts ->
            accounts.map { SelectOptionUiModel(id = it.id, label = it.name) }
        },
    ) { currentState, categoryOptions, accountOptions ->
        val selectedCategory = currentState.selectedCategoryId
            ?.let { targetId -> categoryOptions.firstOrNull { it.id == targetId } }

        val selectedAccount = currentState.selectedAccountId
            ?.let { targetId -> accountOptions.firstOrNull { it.id == targetId } }

        currentState.copy(
            selectedCategoryId = selectedCategory?.id,
            selectedCategoryName = selectedCategory?.label,
            selectedAccountId = selectedAccount?.id,
            selectedAccountName = selectedAccount?.label,
            categoryOptions = categoryOptions,
            accountOptions = accountOptions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = formState.value,
    )

    fun updateTransactionType(type: Int) {
        formState.value = formState.value.copy(
            transactionType = type,
            selectedCategoryId = null,
            selectedCategoryName = null,
            errorMessageResId = null,
        )
    }

    fun updateAmount(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }.let { text ->
            val firstDot = text.indexOf('.')
            if (firstDot < 0) {
                text
            } else {
                val integerPart = text.substring(0, firstDot + 1)
                val decimalPart = text.substring(firstDot + 1).replace(".", "").take(2)
                integerPart + decimalPart
            }
        }
        formState.value = formState.value.copy(
            amount = sanitized,
            errorMessageResId = null,
        )
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(
            note = value,
            errorMessageResId = null,
        )
    }

    fun updateSpentAt(timestamp: Long) {
        formState.value = formState.value.copy(
            spentAtMillis = timestamp,
            spentAtText = DateFormats.formatDateTime(timestamp),
            errorMessageResId = null,
        )
    }

    fun selectCategory(categoryId: Long) {
        formState.value = formState.value.copy(
            selectedCategoryId = categoryId,
            errorMessageResId = null,
        )
    }

    fun selectAccount(accountId: Long?) {
        formState.value = formState.value.copy(
            selectedAccountId = accountId,
            errorMessageResId = null,
        )
    }

    fun saveExpense(
        onSuccess: () -> Unit,
        onBalanceWarning: () -> Unit = {},
        onBudgetExceeded: (String) -> Unit = {},
    ) {
        val currentState = uiState.value
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

            val now = System.currentTimeMillis()
            val validatedAmountInCent = checkNotNull(amountInCent)
            val selectedCategoryId = currentState.selectedCategoryId!!
            val accountId = currentState.selectedAccountId
            val isExpense = currentState.transactionType == TransactionEntity.TYPE_EXPENSE

            var balanceWillBeNegative = false
            if (accountId != null && isExpense) {
                val currentBalance = accountRepository.getBalanceById(accountId)
                if (currentBalance != null && currentBalance - validatedAmountInCent < 0) {
                    balanceWillBeNegative = true
                }
            }

            transactionRepository.insert(
                TransactionEntity(
                    type = currentState.transactionType,
                    amount = validatedAmountInCent,
                    categoryId = selectedCategoryId,
                    accountId = accountId,
                    note = currentState.note.trim().ifBlank { null },
                    spentAt = currentState.spentAtMillis,
                    createdAt = now,
                    updatedAt = now,
                ),
            )

            if (accountId != null) {
                if (isExpense) {
                    accountRepository.deductBalance(accountId, validatedAmountInCent)
                } else {
                    accountRepository.restoreBalance(accountId, validatedAmountInCent)
                }
            }

            formState.value = createInitialState()
            if (balanceWillBeNegative) onBalanceWarning()
            if (isExpense) {
                checkBudgetExceeded(selectedCategoryId, onBudgetExceeded)
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

    private fun createInitialState(now: Long = System.currentTimeMillis()): AddExpenseUiState =
        AddExpenseUiState(
            spentAtMillis = now,
            spentAtText = DateFormats.formatDateTime(now),
        )

    private fun String.toAmountInCent(): Long? {
        if (isBlank()) return null
        return runCatching {
            BigDecimal(this)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull()
    }
}
