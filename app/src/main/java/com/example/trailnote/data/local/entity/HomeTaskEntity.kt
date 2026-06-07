package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_tasks")
data class HomeTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isDone: Boolean,
    val date: String,
    val createdAt: String,
    val completedAt: String?,
    val orderIndex: Int
)
