package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "growth_topics",
    foreignKeys = [
        ForeignKey(
            entity = GrowthAreaEntity::class,
            parentColumns = ["id"],
            childColumns = ["growthAreaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("growthAreaId")]
)
data class GrowthTopicEntity(
    @PrimaryKey val id: String,
    val growthAreaId: String,
    val title: String,
    val description: String,
    val level: Int,
    val exp: Int,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
)
