package com.example.trailnote.domain.model

enum class LogCategoryType {
    Memo,
    Idea,
    Resource,
    Review
}

data class LogCategory(
    val id: String,
    val name: String,
    val type: LogCategoryType
)
