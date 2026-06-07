package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routines",
    foreignKeys = [
        ForeignKey(
            entity = GrowthTopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["growthTopicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("growthTopicId")]
)
data class RoutineEntity(
    @PrimaryKey val id: String,
    val growthTopicId: String,
    val title: String,
    val description: String,
    val isFixed: Boolean,
    val isActive: Boolean,
    val repeatType: String,
    val isDoneToday: Boolean,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
    val lastCompletedDate: String?
)
