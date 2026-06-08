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
                thresholdConditions("log-memo", listOf(10, 50, 100)) { stats -> stats.logCountByCategory["memo"].orZero() } +
                thresholdConditions("log-idea", listOf(1, 10, 30, 50, 100)) { stats -> stats.logCountByCategory["idea"].orZero() } +
                thresholdConditions("log-resource", listOf(10, 30, 100)) { stats -> stats.logCountByCategory["resource"].orZero() } +
                thresholdConditions("log-review", listOf(10, 50, 100)) { stats -> stats.logCountByCategory["review"].orZero() } +
                thresholdConditions("routine-complete", listOf(1, 10, 30, 50, 100, 300, 500, 1000)) { stats -> stats.completedRoutineCount } +
                thresholdConditions("fixed-routine", listOf(1, 3, 5, 10, 20)) { stats -> stats.fixedRoutineCount } +
                thresholdConditions("growth-area", listOf(1, 3, 5, 10)) { stats -> stats.growthAreaCount } +
                thresholdConditions("growth-topic", listOf(1, 5, 10, 20, 50)) { stats -> stats.growthTopicCount } +
                thresholdConditions("user-level", listOf(5, 10, 15, 20, 30, 50, 100)) { stats -> stats.userLevel } +
                thresholdConditions("visit-days", listOf(1, 3, 7, 30, 100, 365)) { stats -> stats.totalVisitDays } +
                thresholdConditions("streak-days", listOf(3, 7, 14, 30, 100)) { stats -> stats.currentStreakDays } +
                mapOf(
                    "achieve-1" to { stats: AchievementStats -> stats.completedProjectCount >= 1 },
                    "achieve-2" to { stats: AchievementStats -> stats.completedProjectCount >= 3 },
                    "achieve-3" to { stats: AchievementStats -> stats.logCount >= 100 },
                    "achieve-4" to { stats: AchievementStats -> stats.logCountByCategory["idea"].orZero() >= 30 },
                    "achieve-5" to { stats: AchievementStats -> stats.completedRoutineCount >= 50 },
                    "achieve-6" to { stats: AchievementStats -> stats.growthTopicCount >= 5 }
                )

        private fun thresholdConditions(
            idPrefix: String,
            thresholds: List<Int>,
            valueProvider: (AchievementStats) -> Int
        ): Map<String, (AchievementStats) -> Boolean> {
            return thresholds.associate { threshold ->
                "$idPrefix-$threshold" to { stats: AchievementStats -> valueProvider(stats) >= threshold }
            }
        }

        private fun Int?.orZero(): Int = this ?: 0
    }
}
