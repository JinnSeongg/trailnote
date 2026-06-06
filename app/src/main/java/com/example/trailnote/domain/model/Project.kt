package com.example.trailnote.domain.model

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val status: String,
    val targetDate: String
)
