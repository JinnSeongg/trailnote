package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = LogTopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class LogEntryEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val content: String,
    val tags: String,
    val createdAt: String,
    val updatedAt: String
)
