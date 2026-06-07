package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_records",
    indices = [Index("targetId"), Index("date")]
)
data class ActivityRecordEntity(
    @PrimaryKey val id: String,
    val type: String,
    val targetId: String?,
    val targetType: String?,
    val date: String,
    val value: Int,
    val createdAt: String
)
