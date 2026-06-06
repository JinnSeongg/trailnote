package com.example.trailnote.core.model

data class SummaryStat(
    val title: String,
    val value: String,
    val caption: String = ""
)

data class CheckItem(
    val id: String,
    val title: String,
    val checked: Boolean = false,
    val tag: String? = null
)

enum class RepeatType {
    Daily,
    Weekly,
    Monthly
}

data class Routine(
    val id: String,
    val growthTopicId: String,
    val title: String,
    val repeatType: RepeatType,
    val isFixed: Boolean,
    val isActive: Boolean,
    val order: Int
)

data class HomeGoalSettings(
    val randomTodayGoalCount: Int
)

data class ProjectSample(
    val id: String,
    val title: String,
    val category: String,
    val status: String,
    val description: String,
    val progress: Float,
    val meta: String
)

data class LogSample(
    val id: String,
    val category: String,
    val title: String,
    val notes: List<String>
)

data class GrowthAreaSample(
    val id: String,
    val title: String,
    val level: Int,
    val description: String,
    val progress: Float
)

data class AchievementSample(
    val title: String,
    val caption: String
)
