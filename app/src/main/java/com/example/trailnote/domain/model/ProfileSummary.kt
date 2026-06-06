package com.example.trailnote.domain.model

data class ProfileSummary(
    val name: String,
    val level: Int,
    val exp: Int,
    val completedTaskCount: Int,
    val activeProjectCount: Int,
    val logCount: Int,
    val completedRoutineCount: Int,
    val featuredAchievementId: String? = null,
    val avatarVariant: Int = 0
)
