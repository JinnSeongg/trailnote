package com.example.trailnote.feature.home

import com.example.trailnote.core.model.CheckItem
import com.example.trailnote.core.model.HomeGoalSettings
import com.example.trailnote.core.model.Routine
import com.example.trailnote.core.sample.SampleData
import kotlin.random.Random

data class HomeUiState(
    val todayTasks: List<CheckItem> = emptyList(),
    val fixedGoals: List<Routine> = emptyList(),
    val randomTodayGoals: List<Routine> = emptyList()
)

fun buildSampleHomeUiState(
    todayDateKey: String = SampleData.homeTodayDateKey,
    settings: HomeGoalSettings = SampleData.homeGoalSettings
): HomeUiState {
    val activeRoutines = SampleData.routines.filter { it.isActive }.sortedBy { it.order }
    val fixedGoals = activeRoutines.filter { it.isFixed }
    val randomTodayGoals = activeRoutines
        .filter { !it.isFixed }
        .shuffled(Random(todayDateKey.hashCode()))
        .take(settings.randomTodayGoalCount)
        .sortedBy { it.order }

    return HomeUiState(
        todayTasks = SampleData.todayTasks,
        fixedGoals = fixedGoals,
        randomTodayGoals = randomTodayGoals
    )
}
