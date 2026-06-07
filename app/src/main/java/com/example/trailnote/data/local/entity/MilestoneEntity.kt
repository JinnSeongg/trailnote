package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val targetDate: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
)
