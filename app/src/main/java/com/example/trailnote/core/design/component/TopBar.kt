package com.example.trailnote.core.design.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trailnote.core.design.theme.TrailNoteTheme

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showMenu: Boolean = true,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showBack) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onBackClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        } else if (showMenu) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { /* TODO: Drawer navigation */ }
            ) {
                Icon(Icons.Default.Menu, contentDescription = "메뉴")
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            content = actions
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TopBarPreview() {
    TrailNoteTheme {
        TopBar(title = "홈")
    }
}
