package com.example.trailnote.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trailnote.core.design.component.AppScaffold
import com.example.trailnote.core.design.component.BottomNavItem

@Composable
fun TrailNoteApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val bottomItems = listOf(
        BottomNavItem(TrailNoteRoute.HOME, "홈", Icons.Default.Home),
        BottomNavItem(TrailNoteRoute.PROJECT, "프로젝트", Icons.Default.Folder),
        BottomNavItem(TrailNoteRoute.LOG, "기록", Icons.Default.Bookmark),
        BottomNavItem(TrailNoteRoute.GROWTH, "성장", Icons.AutoMirrored.Filled.TrendingUp),
        BottomNavItem(TrailNoteRoute.PROFILE, "프로필", Icons.Default.AccountCircle)
    )

    AppScaffold(
        modifier = modifier,
        bottomItems = bottomItems,
        currentRoute = currentRoute,
        showBottomBar = currentRoute in TrailNoteRoute.topLevelRoutes,
        onTabClick = { route ->
            navController.navigate(route) {
                popUpTo(TrailNoteRoute.HOME) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            contentPadding = innerPadding
        )
    }
}
