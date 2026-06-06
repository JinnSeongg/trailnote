package com.example.trailnote.feature.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.trailnote.core.design.component.ProgressCard
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.TaskCheckItem
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailPurple
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.sample.SampleData

@Composable
fun GrowthDetailScreen(
    growthAreaId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routines = remember(growthAreaId) {
        SampleData.routines
            .filter { it.growthTopicId == growthAreaId && it.isActive }
            .sortedBy { it.order }
    }
    val checks = rememberCheckStates(routines.associate { it.id to false })
    val title = SampleData.growthAreas.firstOrNull { it.id == growthAreaId }?.title ?: "그림"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrailWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TrailNoteSpacing.xl)
            .padding(bottom = TrailNoteSpacing.xxl)
    ) {
        TopBar(
            title = title,
            showBack = true,
            showMenu = false,
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { /* TODO: Growth detail more menu */ }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "더보기")
                }
            }
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "상세 분야")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)) {
            SampleData.growthDetails.forEach { detail ->
                ProgressCard(
                    title = detail.title,
                    description = detail.description,
                    progress = detail.progress,
                    meta = "Lv.${detail.level}",
                    accentColor = TrailPurple
                )
            }
        }
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "루틴")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        routines.forEach { routine ->
            TaskCheckItem(
                title = routine.title,
                checked = checks[routine.id] == true,
                onCheckedChange = { checks[routine.id] = it },
                accentColor = TrailPurple
            )
        }
    }
}

@Composable
private fun rememberCheckStates(values: Map<String, Boolean>): SnapshotStateMap<String, Boolean> {
    return remember(values) { values.toList().toMutableStateMap() }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GrowthDetailScreenPreview() {
    TrailNoteTheme {
        GrowthDetailScreen(
            growthAreaId = "drawing",
            onBackClick = {}
        )
    }
}
