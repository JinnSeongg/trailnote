package com.example.trailnote.data.local.db

import android.content.Context
import androidx.room.withTransaction
import com.example.trailnote.data.local.entity.AppPreferenceEntity
import com.example.trailnote.data.local.entity.GrowthAreaEntity
import com.example.trailnote.data.local.entity.GrowthTopicEntity
import com.example.trailnote.data.local.entity.HomeGoalSettingsEntity
import com.example.trailnote.data.local.entity.HomeTaskEntity
import com.example.trailnote.data.local.entity.LogCategoryEntity
import com.example.trailnote.data.local.entity.LogEntryEntity
import com.example.trailnote.data.local.entity.LogTopicEntity
import com.example.trailnote.data.local.entity.MilestoneEntity
import com.example.trailnote.data.local.entity.ProjectCategoryEntity
import com.example.trailnote.data.local.entity.ProjectEntity
import com.example.trailnote.data.local.entity.RoutineEntity
import com.example.trailnote.data.local.entity.ShortTaskEntity
import com.example.trailnote.data.local.entity.UserProfileEntity
import com.example.trailnote.data.sample.SampleGrowth
import com.example.trailnote.data.sample.SampleHome
import com.example.trailnote.data.sample.SampleLogs
import com.example.trailnote.data.sample.SampleProjects
import java.time.LocalDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DatabaseSeeder {
    private val seedMutex = Mutex()

    suspend fun seedIfNeeded(context: Context) {
        seedMutex.withLock {
            val database = AppDatabaseProvider.getDatabase(context)
            database.withTransaction {
                cleanupProjectCategoryDuplicates(database)
                val homeDao = database.homeDao()
                if (homeDao.getPreference(KEY_SEED_VERSION)?.value == CURRENT_SEED_VERSION) return@withTransaction

                seedHomeIfNeeded(database)
                seedProjectsIfNeeded(database)
                seedLogsIfNeeded(database)
                seedGrowthIfNeeded(database)
                seedProfileIfNeeded(database)
                cleanupProjectCategoryDuplicates(database)
                homeDao.setPreference(AppPreferenceEntity(KEY_SEED_VERSION, CURRENT_SEED_VERSION))
            }
        }
    }

    private suspend fun seedHomeIfNeeded(database: AppDatabase) {
        val homeDao = database.homeDao()
        val today = LocalDate.now().toString()
        val now = today

        if (homeDao.getHomeGoalSettings() == null) {
            homeDao.insertOrUpdateHomeGoalSettings(
                HomeGoalSettingsEntity(randomTodayGoalCount = SampleHome.goalSettings.randomTodayGoalCount.coerceIn(1, 20))
            )
        }

        if (homeDao.getHomeTasksByDate(today).isEmpty()) {
            homeDao.insertHomeTasks(
                SampleHome.todayWorks.mapIndexed { index, task ->
                    HomeTaskEntity(
                        id = task.id,
                        title = task.title,
                        isDone = task.isDone,
                        date = today,
                        createdAt = now,
                        completedAt = if (task.isDone) now else null,
                        orderIndex = index + 1
                    )
                }
            )
        }
    }

    private suspend fun seedProjectsIfNeeded(database: AppDatabase) {
        val projectDao = database.projectDao()
        val now = LocalDate.now().toString()
        val existingCategoryTitles = projectDao.getProjectCategories().map { it.title }.toSet()
        val categoryOrderBase = projectDao.getMaxProjectCategoryOrderIndex()
        val missingCategories = SampleProjects.projects
            .map { it.category.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .filterNot { it in existingCategoryTitles }
            .mapIndexed { index, title ->
                ProjectCategoryEntity(
                    title = title,
                    orderIndex = categoryOrderBase + index + 1,
                    createdAt = now,
                    updatedAt = now
                )
            }
        if (missingCategories.isNotEmpty()) {
            projectDao.insertProjectCategories(missingCategories)
        }

        val categoriesByTitle = projectDao.getProjectCategories().associateBy { it.title }
        val existingProjectIds = projectDao.getProjects().map { it.id }.toSet()
        val missingProjects = SampleProjects.projects
            .filterNot { it.id in existingProjectIds }
            .mapIndexed { index, project ->
                ProjectEntity(
                    id = project.id,
                    title = project.title,
                    description = project.description,
                    category = project.category,
                    categoryId = categoriesByTitle[project.category]?.id,
                    status = project.status,
                    targetDate = project.targetDate,
                    createdAt = now,
                    updatedAt = now,
                    orderIndex = index + 1,
                    isArchived = false
                )
            }
        if (missingProjects.isNotEmpty()) {
            projectDao.insertProjects(missingProjects)
        }

        val existingMilestoneIds = projectDao.getMilestones().map { it.id }.toSet()
        val missingMilestones = SampleProjects.milestones
            .filterNot { it.id in existingMilestoneIds }
            .map { milestone ->
                MilestoneEntity(
                    id = milestone.id,
                    projectId = milestone.projectId,
                    title = milestone.title,
                    description = milestone.description,
                    targetDate = milestone.targetDate,
                    orderIndex = milestone.order,
                    createdAt = now,
                    updatedAt = now
                )
            }
        if (missingMilestones.isNotEmpty()) {
            projectDao.insertMilestones(missingMilestones)
        }

        val existingShortTaskIds = projectDao.getShortTasks().map { it.id }.toSet()
        val missingShortTasks = SampleProjects.shortTasks
            .filterNot { it.id in existingShortTaskIds }
            .map { task ->
                ShortTaskEntity(
                    id = task.id,
                    milestoneId = task.milestoneId,
                    title = task.title,
                    isDone = task.isDone,
                    orderIndex = task.order,
                    createdAt = now,
                    completedAt = if (task.isDone) now else null
                )
            }
        if (missingShortTasks.isNotEmpty()) {
            projectDao.insertShortTasks(missingShortTasks)
        }
    }

    private suspend fun seedLogsIfNeeded(database: AppDatabase) {
        val logDao = database.logDao()
        if (logDao.getLogCategories().isNotEmpty() || logDao.getLogTopics().isNotEmpty() || logDao.getLogEntries().isNotEmpty()) return

        val now = LocalDate.now().toString()
        logDao.insertLogCategories(
            SampleLogs.categories.mapIndexed { index, category ->
                LogCategoryEntity(
                    id = category.id,
                    name = category.name,
                    type = category.type.name,
                    orderIndex = index + 1
                )
            }
        )
        logDao.insertLogTopics(
            SampleLogs.topics.map { topic ->
                LogTopicEntity(
                    id = topic.id,
                    categoryId = topic.categoryId,
                    title = topic.title,
                    description = topic.description,
                    orderIndex = topic.order,
                    createdAt = now,
                    updatedAt = now
                )
            }
        )
        logDao.insertLogEntries(
            SampleLogs.entries.map { entry ->
                LogEntryEntity(
                    id = entry.id,
                    topicId = entry.topicId,
                    title = entry.title,
                    content = entry.content,
                    tags = entry.tags.joinToString(TAG_SEPARATOR),
                    createdAt = entry.createdAt,
                    updatedAt = entry.updatedAt
                )
            }
        )
    }

    private suspend fun seedGrowthIfNeeded(database: AppDatabase) {
        val growthDao = database.growthDao()
        if (growthDao.getGrowthAreas().isNotEmpty() || growthDao.getGrowthTopics().isNotEmpty() || growthDao.getRoutines().isNotEmpty()) return

        val now = LocalDate.now().toString()
        growthDao.insertGrowthAreas(
            SampleGrowth.areas.mapIndexed { index, area ->
                GrowthAreaEntity(
                    id = area.id,
                    title = area.title,
                    description = area.description,
                    level = area.level,
                    exp = area.exp,
                    colorHex = area.colorHex,
                    orderIndex = index + 1,
                    createdAt = now,
                    updatedAt = now
                )
            }
        )
        growthDao.insertGrowthTopics(
            SampleGrowth.topics.map { topic ->
                GrowthTopicEntity(
                    id = topic.id,
                    growthAreaId = topic.growthAreaId,
                    title = topic.title,
                    description = topic.description,
                    level = topic.level,
                    exp = topic.exp,
                    orderIndex = topic.order,
                    createdAt = now,
                    updatedAt = now
                )
            }
        )
        growthDao.insertRoutines(
            SampleGrowth.routines.map { routine ->
                RoutineEntity(
                    id = routine.id,
                    growthTopicId = routine.growthTopicId,
                    title = routine.title,
                    description = routine.description,
                    isFixed = routine.isFixed,
                    isActive = routine.isActive,
                    repeatType = routine.repeatType.name,
                    isDoneToday = routine.isDoneToday,
                    orderIndex = routine.order,
                    createdAt = now,
                    updatedAt = now,
                    lastCompletedDate = if (routine.isDoneToday) now else null
                )
            }
        )
    }

    private suspend fun seedProfileIfNeeded(database: AppDatabase) {
        val profileDao = database.profileDao()
        if (profileDao.getUserProfile() != null) return

        val now = LocalDate.now().toString()
        profileDao.insertUserProfile(
            UserProfileEntity(
                name = "TrailNote 사용자",
                level = 1,
                exp = 0,
                representativeAchievementId = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun cleanupProjectCategoryDuplicates(database: AppDatabase) {
        val db = database.openHelper.writableDatabase
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

    private const val TAG_SEPARATOR = "\n"
    private const val KEY_SEED_VERSION = "seed_version"
    private const val CURRENT_SEED_VERSION = "1"
}
