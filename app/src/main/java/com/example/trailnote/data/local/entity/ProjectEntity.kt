package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = ProjectCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val categoryId: Long? = null,
    val status: String,
    val targetDate: String,
    val createdAt: String,
    val updatedAt: String,
    val orderIndex: Int,
    val isArchived: Boolean
)
