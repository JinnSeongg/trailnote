package com.example.trailnote.domain.model

enum class RepeatType {
    Daily,
    Weekly,
    Monthly
}

data class Routine(
    val id: String,
    val growthTopicId: String,
    val title: String,
    val description: String,
    val isFixed: Boolean,
    val isActive: Boolean,
    val repeatType: RepeatType,
    val isDoneToday: Boolean,
    val order: Int
)
