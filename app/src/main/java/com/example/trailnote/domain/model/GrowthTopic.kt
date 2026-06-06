package com.example.trailnote.domain.model

data class GrowthTopic(
    val id: String,
    val growthAreaId: String,
    val title: String,
    val description: String,
    val level: Int,
    val exp: Int,
    val order: Int
)
