package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_exposure_bags")
data class RoutineExposureBagEntity(
    @PrimaryKey val id: Long = 1L,
    val remainingRoutineIds: String,
    val candidateRoutineIds: String,
    val updatedAt: String
)
