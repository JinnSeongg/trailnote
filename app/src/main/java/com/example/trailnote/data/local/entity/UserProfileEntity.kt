package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String,
    val level: Int,
    val exp: Int,
    val representativeAchievementId: String?,
    val createdAt: String,
    val updatedAt: String
)
