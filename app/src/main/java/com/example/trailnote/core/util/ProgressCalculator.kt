package com.example.trailnote.core.util

import com.example.trailnote.domain.model.ShortTask

object ProgressCalculator {
    fun projectProgress(milestoneProgresses: List<Int>): Int {
        if (milestoneProgresses.isEmpty()) return 0
        return milestoneProgresses.average().toInt()
    }

    fun milestoneProgress(tasks: List<ShortTask>): Int {
        if (tasks.isEmpty()) return 0
        return tasks.count { it.isDone } * 100 / tasks.size
    }
}
