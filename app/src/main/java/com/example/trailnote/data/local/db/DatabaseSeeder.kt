package com.example.trailnote.data.local.db

import android.content.Context
import com.example.trailnote.data.local.entity.AchievementEntity
import com.example.trailnote.data.local.entity.GrowthAreaEntity
import com.example.trailnote.data.local.entity.GrowthTopicEntity
import com.example.trailnote.data.local.entity.LogCategoryEntity
import com.example.trailnote.data.local.entity.LogEntryEntity
import com.example.trailnote.data.local.entity.LogTopicEntity
import com.example.trailnote.data.local.entity.MilestoneEntity
import com.example.trailnote.data.local.entity.ProjectCategoryEntity
import com.example.trailnote.data.local.entity.ProjectEntity
import com.example.trailnote.data.local.entity.RoutineEntity
import com.example.trailnote.data.local.entity.ShortTaskEntity
import com.example.trailnote.data.sample.SampleGrowth
import com.example.trailnote.data.sample.SampleLogs
import com.example.trailnote.data.sample.SampleProfile
import com.example.trailnote.data.sample.SampleProjects
import com.example.trailnote.domain.model.GrowthColorPalette
import java.time.LocalDate

object DatabaseSeeder {
    suspend fun seedIfNeeded(context: Context) {
        val database = AppDatabaseProvider.getDatabase(context)
        seedProjectsIfNeeded(database)
        seedLogsIfNeeded(database)
        seedGrowthIfNeeded(database)
        seedAchievementsIfNeeded(database)
    }

    private suspend fun seedProjectsIfNeeded(database: AppDatabase) {
        val projectDao = database.projectDao()
        val now = LocalDate.now().toString()
        if (projectDao.getProjectCategories().isEmpty()) {
            projectDao.insertProjectCategories(
                SampleProjects.projects
                    .map { it.category.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .mapIndexed { index, title ->
                        ProjectCategoryEntity(
                            title = title,
                            orderIndex = index + 1,
                            createdAt = now,
                            updatedAt = now
                        )
                    }
            )
        }

        if (projectDao.getProjects().isNotEmpty()) return

        val categoriesByTitle = projectDao.getProjectCategories().associateBy { it.title }
        projectDao.insertProjects(
            SampleProjects.projects.mapIndexed { index, project ->
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
        )
        projectDao.insertMilestones(
            SampleProjects.milestones.map { milestone ->
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
        )
        projectDao.insertShortTasks(
            SampleProjects.shortTasks.map { task ->
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
        )
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

    private suspend fun seedAchievementsIfNeeded(database: AppDatabase) {
        val achievementDao = database.achievementDao()
        val existingAchievementsById = achievementDao.getAchievements().associateBy { it.id }
        achievementDao.insertAchievements(
            SampleProfile.achievements.map { achievement ->
                val existingAchievement = existingAchievementsById[achievement.id]
                AchievementEntity(
                    id = achievement.id,
                    title = achievement.title,
                    description = achievement.description,
                    category = achievement.category,
                    rarity = achievement.grade,
                    iconKey = achievement.iconText,
                    isUnlocked = existingAchievement?.isUnlocked ?: achievement.isUnlocked,
                    unlockedAt = existingAchievement?.unlockedAt ?: achievement.unlockedAt,
                    progress = existingAchievement?.progress ?: if (achievement.isUnlocked) 1 else 0,
                    target = existingAchievement?.target ?: 1
                )
            }
        )
    }

    private const val TAG_SEPARATOR = "\n"
}
