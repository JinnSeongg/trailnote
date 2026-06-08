package com.example.trailnote.domain.model

data class ActivityStatsSummary(
    val period: StatsPeriod,
    val startDate: String,
    val endDate: String,
    val todoCreatedCount: Int,
    val logCreatedCount: Int,
    val normalRoutineCompletedCount: Int,
    val fixedRoutineAverageCompletionRate: Int
)
