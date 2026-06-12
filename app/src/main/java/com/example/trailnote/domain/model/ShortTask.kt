package com.example.trailnote.domain.model

data class ShortTask(
    val id: String,
    val milestoneId: String,
    val title: String,
    val isDone: Boolean,
    val order: Int,
    val createdAt: String = "",
    val updatedAt: String = "",
    val completedAt: String? = null
)
