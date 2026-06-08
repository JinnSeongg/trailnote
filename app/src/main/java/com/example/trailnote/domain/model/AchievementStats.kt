package com.example.trailnote.domain.model

data class AchievementStats(
    val completedTaskCount: Int,
    val completedProjectCount: Int,
    val logCount: Int,
    val completedRoutineCount: Int,
    val completedNormalRoutineCount: Int,
    val completedFixedRoutineCount: Int,
    val fixedRoutineCount: Int,
    val growthAreaCount: Int,
    val growthTopicCount: Int,
    val userLevel: Int,
    val completedShortTaskCount: Int,
    val completedMilestoneCount: Int,
    val totalVisitDays: Int,
    val currentStreakDays: Int,
    val firstVisitDate: String?,
    val firstTaskCreatedDate: String?,
    val firstProjectCreatedDate: String?,
    val firstLogCreatedDate: String?,
    val firstRoutineCreatedDate: String?,
    val logCountByCategory: Map<String, Int>,
    val logCountByTopic: Map<String, Int>,
    val routineCompletionCountByRoutineId: Map<String, Int>,
    val routineLevelByRoutineId: Map<String, Int>
)
