package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_home_states")
data class DailyHomeStateEntity(
    @PrimaryKey val date: String,
    val selectedRoutineIds: String,
    val lastResetAt: String
)
