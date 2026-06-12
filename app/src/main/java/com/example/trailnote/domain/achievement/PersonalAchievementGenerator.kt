package com.example.trailnote.domain.achievement

import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.Routine

class PersonalAchievementGenerator {
    fun generateProjectCompletionAchievements(
        completedProjects: List<Project>,
        existingAchievements: List<Achievement>,
        unlockedAt: String
    ): List<Achievement> {
        val existingAchievementsById = existingAchievements.associateBy { it.id }
        return completedProjects.map { project ->
            val id = projectCompletionAchievementId(project.id)
            val existing = existingAchievementsById[id]
            Achievement(
                id = id,
                title = "${project.title} 완료",
                description = "${project.title} 프로젝트를 완료했습니다.",
                isUnlocked = true,
                category = "프로젝트",
                grade = "희귀",
                iconText = "P",
                unlockedAt = existing?.unlockedAt ?: unlockedAt
            )
        }
    }

    fun generateRoutineCompletionAchievements(
        routines: List<Routine>,
        completionCountByRoutineId: Map<String, Int>,
        existingAchievements: List<Achievement>,
        unlockedAt: String
    ): List<Achievement> {
        val existingAchievementsById = existingAchievements.associateBy { it.id }
        return routines.flatMap { routine ->
            val completedCount = completionCountByRoutineId[routine.id] ?: 0
            ROUTINE_COMPLETION_THRESHOLDS
                .filter { count -> completedCount >= count }
                .map { count ->
                    val id = routineCompletionAchievementId(routine.id, count)
                    val existing = existingAchievementsById[id]
                    Achievement(
                        id = id,
                        title = "${routine.title} ${count}회 완료",
                        description = "${routine.title} 루틴을 누적 ${count}회 완료했습니다.",
                        isUnlocked = true,
                        category = "루틴",
                        grade = milestoneGrade(count),
                        iconText = "R",
                        unlockedAt = existing?.unlockedAt ?: unlockedAt
                    )
                }
        }
    }

    fun generateLogCategoryAchievements(
        categories: List<LogCategory>,
        logCountByCategory: Map<String, Int>,
        existingAchievements: List<Achievement>,
        unlockedAt: String
    ): List<Achievement> {
        val existingAchievementsById = existingAchievements.associateBy { it.id }
        return categories.flatMap { category ->
            val logCount = logCountByCategory[category.id] ?: 0
            LOG_CATEGORY_THRESHOLDS
                .filter { count -> logCount >= count }
                .map { count ->
                    val id = logCategoryAchievementId(category.id, count)
                    val existing = existingAchievementsById[id]
                    Achievement(
                        id = id,
                        title = "${category.name} 기록 ${count}개 작성",
                        description = "${category.name} 카테고리에 기록을 ${count}개 작성했습니다.",
                        isUnlocked = true,
                        category = "기록",
                        grade = milestoneGrade(count),
                        iconText = "L",
                        unlockedAt = existing?.unlockedAt ?: unlockedAt
                    )
                }
        }
    }

    fun generateGrowthTopicLevelAchievements(
        topics: List<GrowthTopic>,
        existingAchievements: List<Achievement>,
        unlockedAt: String
    ): List<Achievement> {
        val existingAchievementsById = existingAchievements.associateBy { it.id }
        return topics.flatMap { topic ->
            GROWTH_TOPIC_LEVEL_THRESHOLDS
                .filter { level -> topic.level >= level }
                .map { level ->
                    val id = growthTopicLevelAchievementId(topic.id, level)
                    val existing = existingAchievementsById[id]
                    Achievement(
                        id = id,
                        title = "${topic.title} 레벨 ${level} 달성",
                        description = "${topic.title} 지점의 레벨 ${level}을 달성했습니다.",
                        isUnlocked = true,
                        category = "성장",
                        grade = milestoneGrade(level),
                        iconText = "G",
                        unlockedAt = existing?.unlockedAt ?: unlockedAt
                    )
                }
        }
    }

    fun isPersonalAchievement(id: String): Boolean {
        return isPersonalProjectCompletionAchievement(id) ||
            isPersonalRoutineCompletionAchievement(id) ||
            isPersonalLogCategoryAchievement(id) ||
            isPersonalGrowthTopicLevelAchievement(id)
    }

    fun isRetiredPersonalAchievement(id: String): Boolean {
        return id.startsWith(ROUTINE_COMPLETION_PREFIX) && id.endsWith("-1")
    }

    private fun isPersonalProjectCompletionAchievement(id: String): Boolean {
        return id.startsWith(PROJECT_COMPLETION_PREFIX)
    }

    private fun isPersonalRoutineCompletionAchievement(id: String): Boolean {
        return id.startsWith(ROUTINE_COMPLETION_PREFIX)
    }

    private fun isPersonalLogCategoryAchievement(id: String): Boolean {
        return id.startsWith(LOG_CATEGORY_PREFIX)
    }

    private fun isPersonalGrowthTopicLevelAchievement(id: String): Boolean {
        return id.startsWith(GROWTH_TOPIC_LEVEL_PREFIX)
    }

    private fun projectCompletionAchievementId(projectId: String): String {
        return "$PROJECT_COMPLETION_PREFIX$projectId"
    }

    private fun routineCompletionAchievementId(routineId: String, count: Int): String {
        return "$ROUTINE_COMPLETION_PREFIX$routineId-$count"
    }

    private fun logCategoryAchievementId(categoryId: String, count: Int): String {
        return "$LOG_CATEGORY_PREFIX$categoryId-$count"
    }

    private fun growthTopicLevelAchievementId(topicId: String, level: Int): String {
        return "$GROWTH_TOPIC_LEVEL_PREFIX$topicId-$level"
    }

    private fun milestoneGrade(value: Int): String {
        return when {
            value >= 200 -> "유니크"
            value >= 50 -> "희귀"
            else -> "일반"
        }
    }

    private companion object {
        const val PROJECT_COMPLETION_PREFIX = "project-complete-personal-"
        const val ROUTINE_COMPLETION_PREFIX = "routine-complete-personal-"
        const val LOG_CATEGORY_PREFIX = "log-category-personal-"
        const val GROWTH_TOPIC_LEVEL_PREFIX = "growth-topic-level-personal-"

        val ROUTINE_COMPLETION_THRESHOLDS = listOf(10, 20, 30, 50, 100, 200, 300, 500)
        val LOG_CATEGORY_THRESHOLDS = listOf(10, 30, 50, 100, 300, 500, 1000)
        val GROWTH_TOPIC_LEVEL_THRESHOLDS = listOf(5, 10, 20, 30, 50, 100)
    }
}
