package com.example.trailnote.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trailnote.domain.model.GrowthColorPalette

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
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9
                )
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

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `routine_exposure_bags` (
                    `id` INTEGER NOT NULL,
                    `remainingRoutineIds` TEXT NOT NULL,
                    `candidateRoutineIds` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `routine_completion_records` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `routineId` TEXT NOT NULL,
                    `date` TEXT NOT NULL,
                    `completedAt` TEXT NOT NULL,
                    FOREIGN KEY(`routineId`) REFERENCES `routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routine_completion_records_routineId` ON `routine_completion_records` (`routineId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_routine_completion_records_routineId_date` ON `routine_completion_records` (`routineId`, `date`)"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `growth_areas` ADD COLUMN `colorHex` TEXT NOT NULL DEFAULT '${GrowthColorPalette.DEFAULT_COLOR}'"
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            dedupeProjectCategories(db)
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_project_categories_title` ON `project_categories` (`title`)"
            )
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `short_tasks` ADD COLUMN `updatedAt` TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                """
                UPDATE `short_tasks`
                SET `updatedAt` = CASE
                    WHEN `completedAt` IS NOT NULL AND TRIM(`completedAt`) != '' THEN `completedAt`
                    ELSE `createdAt`
                END
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `routines` ADD COLUMN `homeFixedOrderIndex` INTEGER")
        }
    }

    private fun dedupeProjectCategories(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            UPDATE `projects`
            SET `categoryId` = (
                SELECT MIN(`canonical`.`id`)
                FROM `project_categories` AS `canonical`
                WHERE TRIM(`canonical`.`title`) = TRIM((
                    SELECT `current`.`title`
                    FROM `project_categories` AS `current`
                    WHERE `current`.`id` = `projects`.`categoryId`
                    LIMIT 1
                ))
            )
            WHERE `categoryId` IS NOT NULL
            """.trimIndent()
        )
        db.execSQL(
            """
            UPDATE `projects`
            SET `category` = COALESCE((
                SELECT `project_categories`.`title`
                FROM `project_categories`
                WHERE `project_categories`.`id` = `projects`.`categoryId`
                LIMIT 1
            ), `category`)
            WHERE `categoryId` IS NOT NULL
            """.trimIndent()
        )
        db.execSQL(
            """
            DELETE FROM `project_categories`
            WHERE `id` NOT IN (
                SELECT MIN(`id`)
                FROM `project_categories`
                GROUP BY TRIM(`title`)
            )
            """.trimIndent()
        )
    }
}
