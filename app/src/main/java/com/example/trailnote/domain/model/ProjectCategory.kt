package com.example.trailnote.domain.model

data class ProjectCategory(
    val id: Long,
    val title: String,
    val orderIndex: Int,
    val createdAt: String = ""
)
