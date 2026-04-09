package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetracker.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT * FROM categories
        WHERE is_archived = 0
        ORDER BY sort_order ASC, id ASC
        """,
    )
    suspend fun getActiveCategories(): List<CategoryEntity>

    @Query(
        """
        SELECT * FROM categories
        WHERE is_archived = 0
        ORDER BY sort_order ASC, id ASC
        """,
    )
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)
}
