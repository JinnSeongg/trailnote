package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "growth_areas")
data class GrowthAreaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val level: Int,
    val exp: Int,
    val colorHex: String,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String
)
