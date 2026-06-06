package com.example.trailnote.core.design.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trailnote.core.design.theme.TrailBlack
import com.example.trailnote.core.design.theme.TrailGray300
import com.example.trailnote.core.design.theme.TrailGray500
import com.example.trailnote.core.design.theme.TrailNoteTheme
import com.example.trailnote.core.design.theme.TrailWhite

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onTabClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = TrailWhite,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabClick(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TrailWhite,
                    selectedTextColor = TrailBlack,
                    indicatorColor = TrailBlack,
                    unselectedIconColor = TrailGray500,
                    unselectedTextColor = TrailGray500
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BottomNavBarPreview() {
    TrailNoteTheme {
        BottomNavBar(
            items = emptyList(),
            currentRoute = null,
            onTabClick = {}
        )
    }
}
