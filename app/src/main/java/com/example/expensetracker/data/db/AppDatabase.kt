package com.example.expensetracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.dao.AccountDao
import com.example.expensetracker.data.dao.BudgetDao
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.TransactionDao
import com.example.expensetracker.data.entity.AccountEntity
import com.example.expensetracker.data.entity.BudgetEntity
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
        const val DB_VERSION = 7

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bank_cards` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `balance` INTEGER NOT NULL DEFAULT 0,
                        `sort_order` INTEGER NOT NULL DEFAULT 0,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_bank_cards_name` ON `bank_cards` (`name`)"
                )
                db.execSQL(
                    "ALTER TABLE `transactions` ADD COLUMN `bank_card_id` INTEGER DEFAULT NULL REFERENCES `bank_cards`(`id`) ON DELETE SET NULL"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_bank_card_id` ON `transactions` (`bank_card_id`)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE `payment_methods` SET `is_archived` = 1 WHERE `name` IN ('现金', '其他')"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `category_id` INTEGER NOT NULL,
                        `payment_method_id` INTEGER DEFAULT NULL,
                        `bank_card_id` INTEGER DEFAULT NULL,
                        `note` TEXT,
                        `spent_at` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE RESTRICT,
                        FOREIGN KEY (`bank_card_id`) REFERENCES `bank_cards`(`id`) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `transactions_new` (`id`, `amount`, `category_id`, `payment_method_id`, `bank_card_id`, `note`, `spent_at`, `created_at`, `updated_at`) SELECT `id`, `amount`, `category_id`, `payment_method_id`, `bank_card_id`, `note`, `spent_at`, `created_at`, `updated_at` FROM `transactions`"
                )
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category_id` ON `transactions` (`category_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_payment_method_id` ON `transactions` (`payment_method_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_spent_at` ON `transactions` (`spent_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_bank_card_id` ON `transactions` (`bank_card_id`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `category_id` INTEGER DEFAULT NULL,
                        `amount` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_category_id` ON `budgets` (`category_id`)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `type` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `categories` ADD COLUMN `type` INTEGER NOT NULL DEFAULT 0")
                val now = System.currentTimeMillis()
                db.execSQL(
                    "INSERT OR IGNORE INTO `categories` (`name`, `icon`, `sort_order`, `type`, `is_default`, `is_archived`, `created_at`, `updated_at`) VALUES ('工资', 'payments', 0, 1, 1, 0, $now, $now)"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `categories` (`name`, `icon`, `sort_order`, `type`, `is_default`, `is_archived`, `created_at`, `updated_at`) VALUES ('兼职', 'work', 1, 1, 1, 0, $now, $now)"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `categories` (`name`, `icon`, `sort_order`, `type`, `is_default`, `is_archived`, `created_at`, `updated_at`) VALUES ('投资收益', 'trending_up', 2, 1, 1, 0, $now, $now)"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `categories` (`name`, `icon`, `sort_order`, `type`, `is_default`, `is_archived`, `created_at`, `updated_at`) VALUES ('其他收入', 'attach_money', 3, 1, 1, 0, $now, $now)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 重建 transactions 表，移除 payment_method_id 列
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `type` INTEGER NOT NULL DEFAULT 0,
                        `amount` INTEGER NOT NULL,
                        `category_id` INTEGER NOT NULL,
                        `bank_card_id` INTEGER DEFAULT NULL,
                        `note` TEXT,
                        `spent_at` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE RESTRICT,
                        FOREIGN KEY (`bank_card_id`) REFERENCES `bank_cards`(`id`) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `transactions_new` (`id`, `type`, `amount`, `category_id`, `bank_card_id`, `note`, `spent_at`, `created_at`, `updated_at`) SELECT `id`, `type`, `amount`, `category_id`, `bank_card_id`, `note`, `spent_at`, `created_at`, `updated_at` FROM `transactions`"
                )
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category_id` ON `transactions` (`category_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_spent_at` ON `transactions` (`spent_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_bank_card_id` ON `transactions` (`bank_card_id`)")
                // 删除 payment_methods 表
                db.execSQL("DROP TABLE IF EXISTS `payment_methods`")
            }
        }
    }
}
