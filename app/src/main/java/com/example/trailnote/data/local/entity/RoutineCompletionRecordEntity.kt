package com.example.trailnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_completion_records",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId"),
        Index(value = ["routineId", "date"], unique = true)
    ]
)
data class RoutineCompletionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val routineId: String,
    val date: String,
    val completedAt: String
)
