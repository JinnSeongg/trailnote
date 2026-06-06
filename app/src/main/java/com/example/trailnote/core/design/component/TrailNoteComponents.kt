package com.example.trailnote.core.design.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trailnote.core.design.theme.TrailBlack
import com.example.trailnote.core.design.theme.TrailGray100
import com.example.trailnote.core.design.theme.TrailGray200
import com.example.trailnote.core.design.theme.TrailGray300
import com.example.trailnote.core.design.theme.TrailGray500
import com.example.trailnote.core.design.theme.TrailGray700
import com.example.trailnote.core.design.theme.TrailNoteSpacing
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailPurple
import com.example.trailnote.core.design.theme.TrailWhite

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = "+",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null) {
            Text(
                modifier = Modifier.clickable { onActionClick?.invoke() },
                text = actionText,
                style = MaterialTheme.typography.titleMedium,
                color = TrailBlack
            )
        }
    }
}

@Composable
fun ProgressCard(
    title: String,
    description: String,
    progress: Float,
    modifier: Modifier = Modifier,
    meta: String? = null,
    accentColor: Color = TrailBlack,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        color = TrailWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, TrailGray300)
    ) {
        Row(
            modifier = Modifier.padding(TrailNoteSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (meta != null) {
                        Spacer(modifier = Modifier.width(TrailNoteSpacing.sm))
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelMedium,
                            color = TrailGray500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(TrailNoteSpacing.xs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrailGray700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(TrailNoteSpacing.sm))
                SimpleProgressBar(
                    progress = progress,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.width(TrailNoteSpacing.sm))
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "상세"
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TaskCheckItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tag: String? = null,
    accentColor: Color = TrailPurple
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = TrailBlack,
                uncheckedColor = accentColor,
                checkmarkColor = TrailWhite
            )
        )
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) TrailGray500 else TrailBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (tag != null) {
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(TrailGray200)
                    .padding(horizontal = TrailNoteSpacing.sm, vertical = TrailNoteSpacing.xs),
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                color = TrailGray700
            )
        }
    }
}

@Composable
fun FilterChipRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TrailNoteSpacing.sm)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filter == selectedFilter,
                    borderColor = Color.Transparent,
                    selectedBorderColor = TrailBlack
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TrailGray200,
                    labelColor = TrailGray700,
                    selectedContainerColor = TrailBlack,
                    selectedLabelColor = TrailWhite
                )
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String = "표시할 항목이 없습니다."
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TrailGray100)
            .padding(TrailNoteSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xs))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TrailGray500
        )
    }
}

@Composable
fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        modifier = modifier,
        containerColor = TrailBlack,
        contentColor = TrailWhite,
        onClick = onClick
    ) {
        Icon(Icons.Default.Add, contentDescription = "추가")
    }
}

@Composable
fun AddButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, TrailGray300, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = TrailNoteSpacing.lg, vertical = TrailNoteSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TrailGray700
        )
        Spacer(modifier = Modifier.width(TrailNoteSpacing.sm))
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Default.Add,
            contentDescription = "추가"
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String = ""
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TrailGray100)
            .padding(TrailNoteSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(TrailNoteSpacing.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TrailGray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (caption.isNotBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = TrailGray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SimpleProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = TrailBlack
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        drawLine(
            color = TrailGray300,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width * progress.coerceIn(0f, 1f), size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ComponentsPreview() {
    TrailNoteTheme {
        Column(
            modifier = Modifier.padding(TrailNoteSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(TrailNoteSpacing.md)
        ) {
            SectionHeader(title = "오늘 할 일")
            StatCard(title = "오늘 목표", value = "4", modifier = Modifier.fillMaxWidth())
            ProgressCard(
                title = "플래너 앱",
                description = "화면 구조 설계",
                progress = 0.6f,
                meta = "목표 5/8"
            )
            TaskCheckItem(title = "체크리스트", checked = false, onCheckedChange = {})
            FilterChipRow(listOf("전체", "진행중", "완료"), "전체", {})
            EmptyState(title = "비어 있음")
        }
    }
}
