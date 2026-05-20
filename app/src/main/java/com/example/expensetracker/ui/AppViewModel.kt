package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.repository.AccountRepository
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.data.seed.DefaultSeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private var pendingDeleteTransaction: TransactionEntity? = null

    private val _deleteEvent = MutableSharedFlow<TransactionEntity>(extraBufferCapacity = 1)
    val deleteEvent: SharedFlow<TransactionEntity> = _deleteEvent.asSharedFlow()

    fun bootstrap() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (categoryRepository.countAll() == 0) {
                categoryRepository.insertAll(DefaultSeedData.categories(now))
            }
        }
    }

    fun requestDelete(transaction: TransactionEntity) {
        // 如果已有待删除记录，立即确认删除旧的
        pendingDeleteTransaction?.let { old ->
            viewModelScope.launch { transactionRepository.delete(old) }
        }

        pendingDeleteTransaction = transaction

        viewModelScope.launch {
            // 先反转账户余额
            if (transaction.accountId != null) {
                if (transaction.type == TransactionEntity.TYPE_EXPENSE) {
                    accountRepository.restoreBalance(transaction.accountId, transaction.amount)
                } else {
                    accountRepository.deductBalance(transaction.accountId, transaction.amount)
                }
            }
            _deleteEvent.emit(transaction)
        }
    }

    fun confirmDelete() {
        val transaction = pendingDeleteTransaction ?: return
        pendingDeleteTransaction = null
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    fun undoDelete() {
        val transaction = pendingDeleteTransaction ?: return
        pendingDeleteTransaction = null
        viewModelScope.launch {
            // 恢复账户余额
            if (transaction.accountId != null) {
                if (transaction.type == TransactionEntity.TYPE_EXPENSE) {
                    accountRepository.deductBalance(transaction.accountId, transaction.amount)
                } else {
                    accountRepository.restoreBalance(transaction.accountId, transaction.amount)
                }
            }
        }
    }
}
