package com.example.trailnote.feature.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.trailnote.core.design.component.ProgressCard
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailGreen
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailOrange
import com.example.trailnote.core.design.theme.TrailPurple
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.sample.SampleData

@Composable
fun GrowthScreen(
    contentPadding: PaddingValues,
    onGrowthClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColors = listOf(TrailPurple, TrailGreen, TrailOrange)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrailWhite)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TrailNoteSpacing.xl)
            .padding(bottom = TrailNoteSpacing.xxl)
    ) {
        TopBar(title = "성장")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
        Text(
            text = "오늘의 동기",
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = "이번 주 관심사는 '독서'",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "나의 재정착")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.md)) {
            SampleData.growthAreas.forEachIndexed { index, area ->
                ProgressCard(
                    title = area.title,
                    description = area.description,
                    progress = area.progress,
                    meta = "Lv.${area.level}",
                    accentColor = accentColors[index % accentColors.size],
                    onClick = { onGrowthClick(area.id) }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GrowthScreenPreview() {
    TrailNoteTheme {
        GrowthScreen(
            contentPadding = PaddingValues(),
            onGrowthClick = {}
        )
    }
}
