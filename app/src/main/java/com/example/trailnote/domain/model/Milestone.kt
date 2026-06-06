package com.example.trailnote.domain.model

data class Milestone(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val order: Int,
    val targetDate: String
)
