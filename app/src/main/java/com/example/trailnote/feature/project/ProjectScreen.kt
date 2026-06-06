package com.example.trailnote.feature.project

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.trailnote.core.design.component.EmptyState
import com.example.trailnote.core.design.component.FilterChipRow
import com.example.trailnote.core.design.component.ProgressCard
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailBlack
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.sample.SampleData

@Composable
fun ProjectScreen(
    contentPadding: PaddingValues,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("전체") }
    val filters = listOf("전체", "진행중", "완료")
    val categories = listOf("게임", "웹페이지", "모바일 앱")
    val filteredProjects = SampleData.projects.filter {
        selectedFilter == "전체" || it.status == selectedFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrailWhite)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TrailNoteSpacing.xl)
            .padding(bottom = TrailNoteSpacing.xxl)
    ) {
        TopBar(title = "프로젝트")
        FilterChipRow(
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
        categories.forEach { category ->
            SectionHeader(title = category)
            Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
            val categoryProjects = filteredProjects.filter { it.category == category }
            if (categoryProjects.isEmpty()) {
                EmptyState(
                    title = "$category 프로젝트 없음",
                    description = "필터 조건에 맞는 프로젝트가 없습니다."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)) {
                    categoryProjects.forEach { project ->
                        ProgressCard(
                            title = project.title,
                            description = project.description,
                            progress = project.progress,
                            meta = project.meta,
                            accentColor = TrailBlack,
                            onClick = { onProjectClick(project.id) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
        }
        // TODO: 실제 프로젝트 생성, 편집, 정렬 기능 연결
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ProjectScreenPreview() {
    TrailNoteTheme {
        ProjectScreen(
            contentPadding = PaddingValues(),
            onProjectClick = {}
        )
    }
}
