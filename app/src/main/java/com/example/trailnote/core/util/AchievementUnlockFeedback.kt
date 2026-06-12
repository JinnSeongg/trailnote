package com.example.trailnote.core.util

import android.content.Context
import android.widget.Toast
import com.example.trailnote.domain.model.Achievement

object AchievementUnlockFeedback {
    fun show(context: Context, achievements: List<Achievement>) {
        if (achievements.isEmpty()) return
        val message = if (achievements.size == 1) {
            "업적 달성: ${achievements.first().title}"
        } else {
            "업적 ${achievements.size}개 달성"
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
