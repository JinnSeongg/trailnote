package com.example.trailnote.core.design.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppScaffold(
    bottomItems: List<BottomNavItem>,
    currentRoute: String?,
    showBottomBar: Boolean,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = bottomItems,
                    currentRoute = currentRoute,
                    onTabClick = onTabClick
                )
            }
        },
        content = content
    )
}
