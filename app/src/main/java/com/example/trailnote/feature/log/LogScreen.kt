package com.example.trailnote.feature.log

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trailnote.core.design.component.FilterChipRow
import com.example.trailnote.core.design.component.FloatingAddButton
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailBlack
import com.example.trailnote.core.design.theme.TrailGray100
import com.example.trailnote.core.design.theme.TrailGray300
import com.example.trailnote.core.design.theme.TrailGray500
import com.example.trailnote.core.design.theme.TrailGray700
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.sample.SampleData

@Composable
fun LogScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("전체") }
    val filters = listOf("전체", "메모", "아이디어", "자료", "회고")
    val logs = SampleData.logs.filter {
        selectedFilter == "전체" || it.category == selectedFilter
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrailWhite)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TrailNoteSpacing.xl)
                .padding(bottom = TrailNoteSpacing.xxl)
        ) {
            TopBar(
                title = "기록",
                actions = {
                    IconButton(onClick = { /* TODO: Search records */ }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
            )
            FilterChipRow(
                filters = filters,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
            logs.forEach { log ->
                SectionHeader(title = log.title)
                Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
                LogCard(
                    category = log.category,
                    notes = log.notes
                )
                Spacer(modifier = Modifier.height(TrailNoteSpacing.xl))
            }
        }
        FloatingAddButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(TrailNoteSpacing.xl),
            onClick = { /* TODO: Add record */ }
        )
    }
}

@Composable
private fun LogCard(
    category: String,
    notes: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = TrailWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, TrailGray300)
    ) {
        Column(modifier = Modifier.padding(TrailNoteSpacing.md)) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = TrailGray500,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
            notes.forEach { note ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrailBlack
                    )
                    Text(
                        modifier = Modifier.padding(start = TrailNoteSpacing.sm),
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrailGray700
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LogScreenPreview() {
    TrailNoteTheme {
        LogScreen(contentPadding = PaddingValues())
    }
}
