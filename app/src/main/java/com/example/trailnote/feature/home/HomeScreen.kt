package com.example.trailnote.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.StatCard
import com.example.trailnote.core.design.component.TaskCheckItem
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailBlack
import com.example.trailnote.core.design.theme.TrailGray100
import com.example.trailnote.core.design.theme.TrailGray300
import com.example.trailnote.core.design.theme.TrailGray500
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.model.Routine
import com.example.trailnote.core.sample.SampleData

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val uiState = remember { buildSampleHomeUiState() }
    val taskStates = rememberCheckStates(uiState.todayTasks.associate { it.id to it.checked })
    val goalStates = rememberCheckStates((uiState.fixedGoals + uiState.randomTodayGoals).associate { it.id to false })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrailWhite)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TrailNoteSpacing.xl)
            .padding(bottom = TrailNoteSpacing.xxl)
    ) {
        TopBar(title = "홈")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        Text(
            text = "2026년",
            style = MaterialTheme.typography.labelMedium,
            color = TrailGray500
        )
        Text(
            text = "5월 21일 목요일",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)
        ) {
            SampleData.homeStats.forEach { stat ->
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stat.title,
                    value = stat.value,
                    caption = stat.caption
                )
            }
        }
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "오늘 할 일")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        uiState.todayTasks.forEach { task ->
            TaskCheckItem(
                title = task.title,
                checked = taskStates[task.id] == true,
                onCheckedChange = { taskStates[task.id] = it }
            )
        }
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        HorizontalRule()
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        GoalSection(
            title = "고정 목표",
            goals = uiState.fixedGoals,
            goalStates = goalStates
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        HorizontalRule()
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        GoalSection(
            title = "오늘 목표",
            goals = uiState.randomTodayGoals,
            goalStates = goalStates
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
        QuickInput()
    }
}

@Composable
private fun GoalSection(
    title: String,
    goals: List<Routine>,
    goalStates: SnapshotStateMap<String, Boolean>
) {
    Column {
        SectionHeader(title = title)
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        goals.forEach { goal ->
            TaskCheckItem(
                title = goal.title,
                checked = goalStates[goal.id] == true,
                tag = SampleData.growthTopicTitle(goal.growthTopicId),
                onCheckedChange = { goalStates[goal.id] = it }
            )
        }
    }
}

@Composable
private fun rememberCheckStates(values: Map<String, Boolean>): SnapshotStateMap<String, Boolean> {
    return remember(values) { values.toList().toMutableStateMap() }
}

@Composable
private fun QuickInput() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)
    ) {
        Surface(
            modifier = Modifier
                .height(44.dp)
                .weight(1f),
            shape = RoundedCornerShape(999.dp),
            color = TrailWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, TrailGray300)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = TrailNoteSpacing.lg),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "입력 필드",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrailGray500
                )
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(TrailBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "빠른 추가",
                tint = TrailWhite
            )
        }
    }
}

@Composable
private fun HorizontalRule() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TrailGray100)
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HomeScreenPreview() {
    TrailNoteTheme {
        HomeScreen(contentPadding = PaddingValues())
    }
}
