package com.example.trailnote.domain.model

data class VisitStats(
    val totalVisitDays: Int,
    val currentStreakDays: Int,
    val firstVisitDate: String?,
    val lastVisitDate: String?
)
