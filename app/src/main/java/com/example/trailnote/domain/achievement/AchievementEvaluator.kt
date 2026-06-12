package com.example.trailnote.domain.achievement

import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.AchievementStats

class AchievementEvaluator {
    fun evaluate(
        stats: AchievementStats,
        achievements: List<Achievement>,
        unlockedAt: String
    ): List<Achievement> {
        return achievements.map { achievement ->
            if (achievement.isUnlocked || !isConditionMet(achievement.id, stats)) {
                achievement
            } else {
                achievement.copy(isUnlocked = true, unlockedAt = achievement.unlockedAt ?: unlockedAt)
            }
        }
    }

    private fun isConditionMet(id: String, stats: AchievementStats): Boolean {
        return conditions[id]?.invoke(stats) ?: false
    }

    private companion object {
        val conditions: Map<String, (AchievementStats) -> Boolean> =
            thresholdConditions("task-complete", listOf(1, 10, 30, 50, 100, 200, 300, 500, 1000)) { stats -> stats.completedTaskCount } +
                thresholdConditions("project-complete", listOf(1, 3, 5, 10, 20, 50)) { stats -> stats.completedProjectCount } +
                thresholdConditions("short-task-complete", listOf(1, 30, 100, 300, 500, 1000)) { stats -> stats.completedShortTaskCount } +
                thresholdConditions("log-total", listOf(1, 10, 30, 100, 300, 500, 1000)) { stats -> stats.logCount } +
                thresholdConditions("routine-complete", listOf(1, 10, 30, 50, 100, 300, 500, 1000)) { stats -> stats.completedRoutineCount } +
                thresholdConditions("user-level", listOf(5, 10, 15, 20, 30, 50, 100)) { stats -> stats.userLevel } +
                thresholdConditions("visit-days", listOf(1, 3, 7, 30, 100, 365)) { stats -> stats.totalVisitDays } +
                thresholdConditions("streak-days", listOf(3, 7, 14, 30, 100)) { stats -> stats.currentStreakDays }

        private fun thresholdConditions(
            idPrefix: String,
            thresholds: List<Int>,
            valueProvider: (AchievementStats) -> Int
        ): Map<String, (AchievementStats) -> Boolean> {
            return thresholds.associate { threshold ->
                "$idPrefix-$threshold" to { stats: AchievementStats -> valueProvider(stats) >= threshold }
            }
        }
    }
}
