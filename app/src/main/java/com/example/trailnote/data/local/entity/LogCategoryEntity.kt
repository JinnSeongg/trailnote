package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_categories")
data class LogCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val orderIndex: Int
)
