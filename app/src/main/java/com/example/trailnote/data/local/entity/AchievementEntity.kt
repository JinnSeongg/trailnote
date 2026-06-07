package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val rarity: String,
    val iconKey: String,
    val isUnlocked: Boolean,
    val unlockedAt: String?,
    val progress: Int,
    val target: Int
)
