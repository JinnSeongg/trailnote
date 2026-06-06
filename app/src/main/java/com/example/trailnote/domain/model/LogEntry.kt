package com.example.trailnote.domain.model

data class LogEntry(
    val id: String,
    val topicId: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val tags: List<String>
)
