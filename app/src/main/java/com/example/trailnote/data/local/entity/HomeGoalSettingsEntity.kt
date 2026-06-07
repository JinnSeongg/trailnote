package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_goal_settings")
data class HomeGoalSettingsEntity(
    @PrimaryKey val id: Long = 1L,
    val randomTodayGoalCount: Int
)
