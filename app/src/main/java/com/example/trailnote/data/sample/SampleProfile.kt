package com.example.trailnote.data.sample

import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.ActivityRecord
import com.example.trailnote.domain.model.ProfileSummary

object SampleProfile {
    val summary = ProfileSummary(
        name = "\uC9C4\uC131",
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
        Achievement(
            id = "achieve-1",
            title = "[\uB05D\uB0B4\uB7EC\uB2DD] 100%",
            description = "\uD504\uB85C\uC81D\uD2B8\uB97C \uB05D\uAE4C\uC9C0 \uC644\uB8CC",
            isUnlocked = true,
            category = "\uD504\uB85C\uC81D\uD2B8",
            grade = "\uC720\uB2C8\uD06C",
            iconText = "\u25A0",
            unlockedAt = "2026.05.17"
        ),
        Achievement(
            id = "achieve-2",
            title = "\uC644\uB8CC\uD55C \uD504\uB85C\uC81D\uD2B8 3",
            description = "\uD504\uB85C\uC81D\uD2B8 3\uAC1C \uC644\uB8CC",
            isUnlocked = true,
            category = "\uD504\uB85C\uC81D\uD2B8",
            grade = "\uD76C\uADC0",
            iconText = "\u25C6",
            unlockedAt = "2026.04.28"
        ),
        Achievement(
            id = "achieve-3",
            title = "\uC791\uC131\uD55C \uAE30\uB85D 100",
            description = "\uAE30\uB85D 100\uAC1C \uC791\uC131",
            isUnlocked = true,
            category = "\uAE30\uB85D",
            grade = "\uC720\uB2C8\uD06C",
            iconText = "\u25CF",
            unlockedAt = "2026.05.09"
        ),
        Achievement(
            id = "achieve-4",
            title = "\uC544\uC774\uB514\uC5B4 30",
            description = "\uC544\uC774\uB514\uC5B4 \uAE30\uB85D 30\uAC1C \uC791\uC131",
            isUnlocked = false,
            category = "\uAE30\uB85D",
            grade = "\uC77C\uBC18",
            iconText = "\u2713"
        ),
        Achievement(
            id = "achieve-5",
            title = "\uB8E8\uD2F4 \uC644\uB8CC 50",
            description = "\uB8E8\uD2F4 50\uD68C \uC644\uB8CC",
            isUnlocked = true,
            category = "\uC131\uC7A5",
            grade = "\uD76C\uADC0",
            iconText = "\u25B2",
            unlockedAt = "2026.05.21"
        ),
        Achievement(
            id = "achieve-6",
            title = "\uC131\uC7A5 \uC138\uBD80 \uBD84\uC57C 5",
            description = "\uC131\uC7A5 \uC138\uBD80 \uBD84\uC57C 5\uAC1C \uC791\uC131",
            isUnlocked = false,
            category = "\uC131\uC7A5",
            grade = "\uC77C\uBC18",
            iconText = "\u25B3"
        )
    )
}
