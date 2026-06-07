package com.example.trailnote.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseProvider {
    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { database = it }
        }
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `app_preferences` (
                    `key` TEXT NOT NULL,
                    `value` TEXT NOT NULL,
                    PRIMARY KEY(`key`)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `project_categories` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `orderIndex` INTEGER NOT NULL,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `project_categories` (`title`, `orderIndex`, `createdAt`, `updatedAt`)
                SELECT `category`, 0, date('now'), date('now')
                FROM `projects`
                WHERE TRIM(`category`) != ''
                GROUP BY `category`
                """.trimIndent()
            )
            db.execSQL("UPDATE `project_categories` SET `orderIndex` = `id`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `projects_new` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `categoryId` INTEGER,
                    `status` TEXT NOT NULL,
                    `targetDate` TEXT NOT NULL,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL,
                    `orderIndex` INTEGER NOT NULL,
                    `isArchived` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`categoryId`) REFERENCES `project_categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `projects_new` (
                    `id`, `title`, `description`, `category`, `categoryId`, `status`,
                    `targetDate`, `createdAt`, `updatedAt`, `orderIndex`, `isArchived`
                )
                SELECT
                    `id`, `title`, `description`, `category`,
                    (SELECT `id` FROM `project_categories` WHERE `title` = `projects`.`category` LIMIT 1),
                    `status`, `targetDate`, `createdAt`, `updatedAt`, `orderIndex`, `isArchived`
                FROM `projects`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `projects`")
            db.execSQL("ALTER TABLE `projects_new` RENAME TO `projects`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_projects_categoryId` ON `projects` (`categoryId`)")
        }
    }
}
