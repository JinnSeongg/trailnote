package com.example.trailnote.feature.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trailnote.core.design.component.SectionHeader
import com.example.trailnote.core.design.component.SimpleProgressBar
import com.example.trailnote.core.design.component.StatCard
import com.example.trailnote.core.design.component.TopBar
import com.example.trailnote.core.design.theme.TrailBlack
import com.example.trailnote.core.design.theme.TrailGray100
import com.example.trailnote.core.design.theme.TrailGray300
import com.example.trailnote.core.design.theme.TrailGray500
import com.example.trailnote.core.design.theme.TrailGray700
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailPurple
import com.example.trailnote.core.design.theme.TrailWhite
import com.example.trailnote.core.sample.SampleData

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrailWhite)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TrailNoteSpacing.xl)
            .padding(bottom = TrailNoteSpacing.xxl)
    ) {
        TopBar(
            title = "프로필",
            actions = {
                IconButton(onClick = { /* TODO: Profile more menu */ }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "더보기")
                }
            }
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.lg))
        UserCard()
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        ProfileStatGrid()
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "활동 통계", actionText = "이번 달")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        ActivityStatGrid()
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "활동 추이", actionText = null)
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        ActivityTrend()
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xxl))
        SectionHeader(title = "업적", actionText = "전체보기")
        Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
        AchievementList()
    }
}

@Composable
private fun UserCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = TrailWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, TrailBlack)
    ) {
        Column(modifier = Modifier.padding(TrailNoteSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "사용자"
                )
                Column(modifier = Modifier.padding(start = TrailNoteSpacing.md)) {
                    Text(
                        text = "진성",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lv.6 · 꾸준한 기록가",
                        style = MaterialTheme.typography.labelMedium,
                        color = TrailGray500
                    )
                }
            }
            Spacer(modifier = Modifier.height(TrailNoteSpacing.md))
            SimpleProgressBar(
                progress = 0.68f,
                color = TrailPurple
            )
        }
    }
}

@Composable
private fun ProfileStatGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)) {
        SampleData.profileStats.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)
            ) {
                rowItems.forEach { stat ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = stat.title,
                        value = stat.value,
                        caption = stat.caption
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityStatGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)) {
        SampleData.activityStats.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)
            ) {
                rowItems.forEach { stat ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = TrailGray100
                    ) {
                        Column(modifier = Modifier.padding(TrailNoteSpacing.md)) {
                            Text(
                                text = stat.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = TrailGray700
                            )
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stat.caption,
                                style = MaterialTheme.typography.labelMedium,
                                color = TrailGray500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityTrend() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(1.dp, TrailGray300, RoundedCornerShape(8.dp))
            .padding(TrailNoteSpacing.lg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFFEDEDF0)
            repeat(4) { index ->
                val y = size.height * (index + 1) / 5f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = listOf(0.72f, 0.58f, 0.76f, 0.84f, 0.52f, 0.46f, 0.60f)
            val path = Path()
            points.forEachIndexed { index, value ->
                val x = size.width * index / (points.lastIndex.coerceAtLeast(1))
                val y = size.height * (1f - value)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = TrailPurple,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun AchievementList() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)
    ) {
        SampleData.achievements.forEach { achievement ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(TrailGray100, RoundedCornerShape(8.dp))
                    .padding(TrailNoteSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(TrailWhite, CircleShape)
                        .border(1.dp, TrailGray300, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.caption,
                    style = MaterialTheme.typography.labelMedium,
                    color = TrailGray500
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ProfileScreenPreview() {
    TrailNoteTheme {
        ProfileScreen(contentPadding = PaddingValues())
    }
}
