package com.example.expensetracker.ui.records

import com.example.expensetracker.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.common.DateFormats
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.PaymentMethodRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.ui.add.SelectOptionUiModel
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
class RecordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    categoryRepository: CategoryRepository,
    paymentMethodRepository: PaymentMethodRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {
    private val transactionId: Long = checkNotNull(savedStateHandle["recordId"])
    private val formState = MutableStateFlow(RecordDetailUiState())
    private var originalEntity: TransactionEntity? = null
    private var hasInitializedForm = false

    val uiState: StateFlow<RecordDetailUiState> = combine(
        formState,
        transactionRepository.observeTransactionDetail(transactionId),
        categoryRepository.observeActiveCategories().map { categories ->
            categories.map { SelectOptionUiModel(id = it.id, label = it.name) }
        },
        paymentMethodRepository.observeActivePaymentMethods().map { items ->
            items.map { SelectOptionUiModel(id = it.id, label = it.name) }
        },
    ) { currentState, detail, categoryOptions, paymentMethodOptions ->
        if (detail == null) {
            return@combine currentState.copy(
                transactionId = transactionId,
                categoryOptions = categoryOptions,
                paymentMethodOptions = paymentMethodOptions,
                isLoading = false,
                errorMessageResId = currentState.errorMessageResId ?: R.string.error_record_not_found,
            )
        }

        originalEntity = TransactionEntity(
            id = detail.id,
            amount = detail.amount,
            categoryId = detail.categoryId,
            paymentMethodId = detail.paymentMethodId,
            note = detail.note,
            spentAt = detail.spentAt,
            createdAt = detail.createdAt,
            updatedAt = detail.updatedAt,
        )

        if (!hasInitializedForm) {
            hasInitializedForm = true
            formState.value = currentState.copy(
                transactionId = detail.id,
                amount = detail.amount.toPlainAmount(),
                selectedCategoryId = detail.categoryId,
                selectedPaymentMethodId = detail.paymentMethodId,
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
            paymentMethodOptions = paymentMethodOptions,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordDetailUiState(),
    )

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

    fun selectPaymentMethod(paymentMethodId: Long) {
        formState.value = formState.value.copy(selectedPaymentMethodId = paymentMethodId, errorMessageResId = null)
    }

    fun saveChanges(onSuccess: () -> Unit) {
        val currentState = uiState.value
        val source = originalEntity ?: return
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
            val validatedAmountInCent = checkNotNull(amountInCent)
            transactionRepository.update(
                source.copy(
                    amount = validatedAmountInCent,
                    categoryId = currentState.selectedCategoryId!!,
                    paymentMethodId = currentState.selectedPaymentMethodId!!,
                    note = currentState.note.trim().ifBlank { null },
                    spentAt = currentState.spentAtMillis,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            formState.value = formState.value.copy(isSaving = false)
            onSuccess()
        }
    }

    fun deleteRecord(onSuccess: () -> Unit) {
        val source = originalEntity ?: return
        viewModelScope.launch {
            formState.value = formState.value.copy(isDeleting = true, errorMessageResId = null)
            transactionRepository.delete(source)
            onSuccess()
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
