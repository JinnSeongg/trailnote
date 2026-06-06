package com.example.trailnote.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.trailnote.core.design.component.AddButton
import com.example.trailnote.core.design.component.ProgressCard
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.TaskCheckItem
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.sample.SampleData

@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checks = rememberCheckStates(SampleData.projectChecklist.associate { it.id to it.checked })
    val title = if (projectId.contains("planner")) "플래너 앱" else "클래스 구조 설계"

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
                IconButton(onClick = { /* TODO: Project detail more menu */ }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "더보기")
                }
            }
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
        SectionHeader(title = "중기 목표")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)) {
            SampleData.projectGoals.forEach { goal ->
                ProgressCard(
                    title = goal.title,
                    description = goal.description,
                    progress = goal.progress,
                    meta = goal.meta
                )
            }
        }
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "단기 목표")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        SampleData.projectChecklist.forEach { item ->
            TaskCheckItem(
                title = item.title,
                checked = checks[item.id] == true,
                onCheckedChange = { checks[item.id] = it }
            )
        }
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
        AddButton(
            label = "목표 추가",
            onClick = { /* TODO: Add project checklist item */ }
        )
    }
}

@Composable
private fun rememberCheckStates(values: Map<String, Boolean>): SnapshotStateMap<String, Boolean> {
    return remember { values.toList().toMutableStateMap() }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ProjectDetailScreenPreview() {
    TrailNoteTheme {
        ProjectDetailScreen(
            projectId = "project-planner",
            onBackClick = {}
        )
    }
}
