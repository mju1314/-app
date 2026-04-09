package com.example.expensetracker.data.seed

import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.PaymentMethodEntity

object DefaultSeedData {
    fun categories(now: Long): List<CategoryEntity> = listOf(
        CategoryEntity(name = "餐饮", icon = "restaurant", sortOrder = 0, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "交通", icon = "commute", sortOrder = 1, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "购物", icon = "shopping_bag", sortOrder = 2, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "日用", icon = "home", sortOrder = 3, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "娱乐", icon = "sports_esports", sortOrder = 4, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "医疗", icon = "healing", sortOrder = 5, isDefault = true, createdAt = now, updatedAt = now),
        CategoryEntity(name = "其他", icon = "more_horiz", sortOrder = 6, isDefault = true, createdAt = now, updatedAt = now),
    )

    fun paymentMethods(now: Long): List<PaymentMethodEntity> = listOf(
        PaymentMethodEntity(name = "现金", sortOrder = 0, isDefault = true, createdAt = now, updatedAt = now),
        PaymentMethodEntity(name = "支付宝", sortOrder = 1, createdAt = now, updatedAt = now),
        PaymentMethodEntity(name = "微信支付", sortOrder = 2, createdAt = now, updatedAt = now),
        PaymentMethodEntity(name = "银行卡", sortOrder = 3, createdAt = now, updatedAt = now),
        PaymentMethodEntity(name = "其他", sortOrder = 4, createdAt = now, updatedAt = now),
    )
}
