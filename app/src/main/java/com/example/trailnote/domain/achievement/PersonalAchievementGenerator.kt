package com.example.trailnote.domain.achievement

import com.example.trailnote.domain.model.Achievement
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
                description = "${project.title} 프로젝트를 완료하면 획득합니다.",
                isUnlocked = true,
                category = "프로젝트",
                grade = "희귀",
                iconText = "■",
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
            ROUTINE_MILESTONES
                .filter { milestone -> completedCount >= milestone.count }
                .map { milestone ->
                    val id = routineCompletionAchievementId(routine.id, milestone.count)
                    val existing = existingAchievementsById[id]
                    Achievement(
                        id = id,
                        title = "${routine.title} ${milestone.titleSuffix}",
                        description = "${routine.title} 루틴을 누적 ${milestone.count}회 완료하면 획득합니다.",
                        isUnlocked = true,
                        category = "루틴",
                        grade = milestone.grade,
                        iconText = "▲",
                        unlockedAt = existing?.unlockedAt ?: unlockedAt
                    )
                }
        }
    }

    fun isPersonalAchievement(id: String): Boolean {
        return isPersonalProjectCompletionAchievement(id) || isPersonalRoutineCompletionAchievement(id)
    }

    fun isPersonalProjectCompletionAchievement(id: String): Boolean {
        return id.startsWith(PROJECT_COMPLETION_PREFIX)
    }

    fun isPersonalRoutineCompletionAchievement(id: String): Boolean {
        return id.startsWith(ROUTINE_COMPLETION_PREFIX)
    }

    private fun projectCompletionAchievementId(projectId: String): String {
        return "$PROJECT_COMPLETION_PREFIX$projectId"
    }

    private fun routineCompletionAchievementId(routineId: String, count: Int): String {
        return "$ROUTINE_COMPLETION_PREFIX$routineId-$count"
    }

    private data class RoutineMilestone(
        val count: Int,
        val titleSuffix: String,
        val grade: String
    )

    private companion object {
        const val PROJECT_COMPLETION_PREFIX = "project-complete-personal-"
        const val ROUTINE_COMPLETION_PREFIX = "routine-complete-personal-"

        val ROUTINE_MILESTONES = listOf(
            RoutineMilestone(1, "시작", "일반"),
            RoutineMilestone(10, "습관화", "일반"),
            RoutineMilestone(20, "익숙함", "일반"),
            RoutineMilestone(30, "숙련", "희귀"),
            RoutineMilestone(50, "꾸준함", "희귀"),
            RoutineMilestone(100, "장인", "희귀"),
            RoutineMilestone(200, "전문가", "유니크"),
            RoutineMilestone(300, "생활화", "유니크"),
            RoutineMilestone(500, "마스터", "유니크")
        )
    }
}
