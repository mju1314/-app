package com.example.expensetracker.ui.common

object CategoryIcons {
    private val iconToEmoji = mapOf(
        // 支出分类
        "restaurant" to "🍜",
        "commute" to "🚗",
        "shopping_bag" to "🛒",
        "home" to "🏠",
        "sports_esports" to "🎮",
        "healing" to "💊",
        "more_horiz" to "📦",
        // 收入分类
        "payments" to "💰",
        "work" to "💼",
        "trending_up" to "📈",
        "attach_money" to "💵",
    )

    fun getEmoji(iconName: String): String = iconToEmoji[iconName] ?: "📌"
}
