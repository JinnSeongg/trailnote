package com.example.trailnote.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val category: String = "기타",
    val grade: String = "일반",
    val iconText: String = "★",
    val unlockedAt: String? = null
)
