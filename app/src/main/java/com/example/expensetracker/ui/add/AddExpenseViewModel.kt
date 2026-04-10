package com.example.expensetracker.ui.add

import com.example.expensetracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.preferences.UserPreferencesRepository
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.PaymentMethodRepository
import com.example.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    paymentMethodRepository: PaymentMethodRepository,
    private val transactionRepository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(createInitialState())

    val uiState: StateFlow<AddExpenseUiState> = combine(
        formState,
        categoryRepository.observeActiveCategories().map { categories ->
            categories.map { SelectOptionUiModel(id = it.id, label = it.name) }
        },
        paymentMethodRepository.observeActivePaymentMethods().map { items ->
            items.map { SelectOptionUiModel(id = it.id, label = it.name) }
        },
        userPreferencesRepository.lastUsedPaymentMethodId,
    ) { currentState, categoryOptions, paymentMethodOptions, lastUsedPaymentMethodId ->
        val selectedCategory = currentState.selectedCategoryId
            ?.let { targetId -> categoryOptions.firstOrNull { it.id == targetId } }

        val selectedPaymentMethod = currentState.selectedPaymentMethodId
            ?.let { targetId -> paymentMethodOptions.firstOrNull { it.id == targetId } }
            ?: lastUsedPaymentMethodId
                ?.let { targetId -> paymentMethodOptions.firstOrNull { it.id == targetId } }
            ?: paymentMethodOptions.firstOrNull()

        currentState.copy(
            selectedCategoryId = selectedCategory?.id,
            selectedCategoryName = selectedCategory?.label,
            selectedPaymentMethodId = selectedPaymentMethod?.id,
            selectedPaymentMethodName = selectedPaymentMethod?.label,
            categoryOptions = categoryOptions,
            paymentMethodOptions = paymentMethodOptions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = formState.value,
    )

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

    fun selectPaymentMethod(paymentMethodId: Long) {
        formState.value = formState.value.copy(
            selectedPaymentMethodId = paymentMethodId,
            errorMessageResId = null,
        )
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val currentState = uiState.value
        val amountInCent = currentState.amount.toAmountInCent()

        val errorMessageResId = when {
            amountInCent == null || amountInCent <= 0L -> R.string.error_invalid_amount
            currentState.selectedCategoryId == null -> R.string.error_missing_category
            currentState.selectedPaymentMethodId == null -> R.string.error_missing_payment_method
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
            val selectedPaymentMethodId = currentState.selectedPaymentMethodId!!
            transactionRepository.insert(
                TransactionEntity(
                    amount = validatedAmountInCent,
                    categoryId = selectedCategoryId,
                    paymentMethodId = selectedPaymentMethodId,
                    note = currentState.note.trim().ifBlank { null },
                    spentAt = currentState.spentAtMillis,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            userPreferencesRepository.setLastUsedPaymentMethodId(selectedPaymentMethodId)
            formState.value = createInitialState()
            onSuccess()
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
