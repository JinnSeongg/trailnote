package com.example.trailnote.app

object TrailNoteRoute {
    const val HOME = "home"
    const val PROJECT = "project"
    const val PROJECT_DETAIL = "projectDetail/{projectId}"
    const val LOG = "log"
    const val GROWTH = "growth"
    const val GROWTH_DETAIL = "growthDetail/{growthAreaId}"
    const val PROFILE = "profile"

    val topLevelRoutes = setOf(HOME, PROJECT, LOG, GROWTH, PROFILE)

    fun projectDetail(projectId: String) = "projectDetail/$projectId"

    fun growthDetail(growthAreaId: String) = "growthDetail/$growthAreaId"
}
