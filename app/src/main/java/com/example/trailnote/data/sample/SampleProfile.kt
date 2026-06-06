package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.ActivityRecord
import com.example.trailnote.domain.model.ProfileSummary

object SampleProfile {
    val summary = ProfileSummary(
        name = "진성",
        level = 3,
        exp = 42,
        completedTaskCount = 461,
        activeProjectCount = 3,
        logCount = 137,
        completedRoutineCount = 238
    )

    val activityRecords = listOf(
        ActivityRecord("record-1", "4/20", 25),
        ActivityRecord("record-2", "4/27", 14),
        ActivityRecord("record-3", "5/4", 74),
        ActivityRecord("record-4", "5/11", 16),
        ActivityRecord("record-5", "5/18", 40)
    )

    val achievements = listOf(
        Achievement("achieve-1", "[끝내러닝] 100%", "완료형 학습", true),
        Achievement("achieve-2", "[게임소재] 10개", "소재 수집", true),
        Achievement("achieve-3", "[색감] 30회", "반복 연습", false)
    )
}
