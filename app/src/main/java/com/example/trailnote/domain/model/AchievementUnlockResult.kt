package com.example.trailnote.domain.model

data class AchievementUnlockResult(
    val achievements: List<Achievement>,
    val newlyUnlockedAchievements: List<Achievement>
)
