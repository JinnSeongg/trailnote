package com.example.trailnote.domain.model

data class LogTopic(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val order: Int
)
