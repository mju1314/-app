package com.example.expensetracker.data.seed

import com.example.expensetracker.data.entity.CategoryEntity

object DefaultSeedData {
    fun categories(now: Long): List<CategoryEntity> = expenseCategories(now) + incomeCategories(now)

    fun expenseCategories(now: Long): List<CategoryEntity> = listOf(
        CategoryEntity(name = "餐饮", icon = "restaurant", sortOrder = 0, type = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "交通", icon = "commute", sortOrder = 1, type = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "购物", icon = "shopping_bag", sortOrder = 2, type = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "日用", icon = "home", sortOrder = 3, type = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "娱乐", icon = "sports_esports", sortOrder = 4, type = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "医疗", icon = "healing", sortOrder = 5, type = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "其他", icon = "more_horiz", sortOrder = 6, type = 0, isDefault = true, createdAt = now, updatedAt = now),
    )

    fun incomeCategories(now: Long): List<CategoryEntity> = listOf(
        CategoryEntity(name = "工资", icon = "payments", sortOrder = 0, type = 1, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "兼职", icon = "work", sortOrder = 1, type = 1, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "投资收益", icon = "trending_up", sortOrder = 2, type = 1, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "其他收入", icon = "attach_money", sortOrder = 3, type = 1, isDefault = true, createdAt = now, updatedAt = now),
    )
}
