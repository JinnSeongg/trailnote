package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.HomeGoalSettings
import com.example.trailnote.domain.model.Task

object SampleHome {
    const val todayLabel = "오늘"

    val goalSettings = HomeGoalSettings(randomTodayGoalCount = 3)

    val todayWorks = listOf(
        Task("sample-home-task-1", "오늘 계획 3분 정리하기", false),
        Task("sample-home-task-2", "책상 위 정리하기", false),
        Task("sample-home-task-3", "읽을 글 하나 저장하기", false)
    )

    val fixedGoals = SampleGrowth.routines
        .filter { it.isFixed }
        .map { Task(it.id, it.title, it.isDoneToday) }

    val todayGoals = SampleGrowth.routines
        .filter { !it.isFixed }
        .take(goalSettings.randomTodayGoalCount)
        .map { Task(it.id, it.title, it.isDoneToday) }
}
