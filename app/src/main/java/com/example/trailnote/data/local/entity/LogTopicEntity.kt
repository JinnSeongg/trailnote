package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_topics",
    foreignKeys = [
        ForeignKey(
            entity = LogCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class LogTopicEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
)
