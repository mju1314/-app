package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.AccountDao
import com.example.expensetracker.data.entity.AccountEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
) {
    fun observeAll(): Flow<List<AccountEntity>> = accountDao.observeAll()

    suspend fun getById(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun insert(account: AccountEntity): Long = accountDao.insert(account)

    suspend fun update(account: AccountEntity) = accountDao.update(account)

    suspend fun delete(account: AccountEntity) = accountDao.delete(account)

    suspend fun getBalanceById(id: Long): Long? = accountDao.getBalanceById(id)

    suspend fun deductBalance(id: Long, amount: Long) {
        accountDao.deductBalance(id, amount, System.currentTimeMillis())
    }

    suspend fun restoreBalance(id: Long, amount: Long) {
        accountDao.restoreBalance(id, amount, System.currentTimeMillis())
    }
}
