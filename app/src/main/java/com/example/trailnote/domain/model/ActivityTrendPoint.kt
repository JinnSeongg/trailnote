package com.example.trailnote.domain.model

data class ActivityTrendPoint(
    val date: String,
    val todoCompletionRate: Int,
    val fixedRoutineCompletionRate: Int,
    val dailyRoutineCompletionRate: Int,
    val averageCompletionRate: Int
)
