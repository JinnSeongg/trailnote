package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "short_tasks",
    foreignKeys = [
        ForeignKey(
            entity = MilestoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["milestoneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("milestoneId")]
)
data class ShortTaskEntity(
    @PrimaryKey val id: String,
    val milestoneId: String,
    val title: String,
    val isDone: Boolean,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String = createdAt,
    val completedAt: String?
)
