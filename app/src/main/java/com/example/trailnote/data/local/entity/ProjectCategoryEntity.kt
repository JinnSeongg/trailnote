package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_categories")
data class ProjectCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
)
