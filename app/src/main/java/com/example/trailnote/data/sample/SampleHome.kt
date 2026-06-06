package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.HomeGoalSettings
import com.example.trailnote.domain.model.Task

object SampleHome {
    const val todayLabel = "2026년 5월 21일 목요일"

    val goalSettings = HomeGoalSettings(randomTodayGoalCount = 4)

    val todayWorks = listOf(
        Task("work-1", "우선순위 제거", true),
        Task("work-2", "보조 백로그 정리", true),
        Task("work-3", "식빵", false),
        Task("work-4", "영상추", false)
    )

    val fixedGoals = SampleGrowth.routines
        .filter { it.isFixed }
        .map { Task(it.id, it.title, it.isDoneToday) }

    val todayGoals = SampleGrowth.routines
        .filter { !it.isFixed }
        .take(goalSettings.randomTodayGoalCount)
        .map { Task(it.id, it.title, it.isDoneToday) }
}
