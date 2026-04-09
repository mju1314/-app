package com.example.expensetracker.data.repository

import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.entity.CategoryEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) {
    suspend fun getActiveCategories(): List<CategoryEntity> = categoryDao.getActiveCategories()
    fun observeActiveCategories(): Flow<List<CategoryEntity>> = categoryDao.observeActiveCategories()
    suspend fun countAll(): Int = categoryDao.countAll()
    suspend fun insertAll(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)
}
