package com.example.trailnote.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trailnote.feature.growth.GrowthDetailScreen
import com.example.trailnote.feature.growth.GrowthScreen
import com.example.trailnote.feature.home.HomeScreen
import com.example.trailnote.feature.log.LogScreen
import com.example.trailnote.feature.profile.ProfileScreen
import com.example.trailnote.feature.project.ProjectDetailScreen
import com.example.trailnote.feature.project.ProjectScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = TrailNoteRoute.HOME
    ) {
        composable(TrailNoteRoute.HOME) {
            HomeScreen(contentPadding = contentPadding)
        }
        composable(TrailNoteRoute.PROJECT) {
            ProjectScreen(
                contentPadding = contentPadding,
                onProjectClick = { projectId ->
                    navController.navigate(TrailNoteRoute.projectDetail(projectId))
                }
            )
        }
        composable(TrailNoteRoute.PROJECT_DETAIL) { backStackEntry ->
            ProjectDetailScreen(
                projectId = backStackEntry.arguments?.getString("projectId").orEmpty(),
                onBackClick = navController::popBackStack
            )
        }
        composable(TrailNoteRoute.LOG) {
            LogScreen(contentPadding = contentPadding)
        }
        composable(TrailNoteRoute.GROWTH) {
            GrowthScreen(
                contentPadding = contentPadding,
                onGrowthClick = { growthAreaId ->
                    navController.navigate(TrailNoteRoute.growthDetail(growthAreaId))
                }
            )
        }
        composable(TrailNoteRoute.GROWTH_DETAIL) { backStackEntry ->
            GrowthDetailScreen(
                growthAreaId = backStackEntry.arguments?.getString("growthAreaId").orEmpty(),
                onBackClick = navController::popBackStack
            )
        }
        composable(TrailNoteRoute.PROFILE) {
            ProfileScreen(contentPadding = contentPadding)
        }
    }
}
