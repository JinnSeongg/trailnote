package com.example.trailnote.data

import com.example.trailnote.data.sample.SampleGrowth
import com.example.trailnote.data.sample.SampleHome
import com.example.trailnote.data.sample.SampleLogs
import com.example.trailnote.data.sample.SampleProjects
import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogCategoryType
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic
import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.RepeatType
import com.example.trailnote.domain.model.Routine
import com.example.trailnote.domain.model.ShortTask
import com.example.trailnote.domain.model.Task

object InMemoryDataStore {
    private val projects = SampleProjects.projects.toMutableList()
    private val milestones = SampleProjects.milestones.toMutableList()
    private val shortTasks = SampleProjects.shortTasks.toMutableList()
    private val logCategories = SampleLogs.categories.toMutableList()
    private val logTopics = SampleLogs.topics.toMutableList()
    private val logEntries = SampleLogs.entries.toMutableList()
    private val growthAreas = SampleGrowth.areas.toMutableList()
    private val growthTopics = SampleGrowth.topics.toMutableList()
    private val routines = SampleGrowth.routines.toMutableList()
    private val todayWorks = SampleHome.todayWorks.toMutableList()

    fun getProjects(): List<Project> = projects.toList()
    fun getProject(projectId: String): Project? = projects.firstOrNull { it.id == projectId }
    fun getMilestonesByProject(projectId: String): List<Milestone> = milestones.filter { it.projectId == projectId }
    fun getMilestone(milestoneId: String): Milestone? = milestones.firstOrNull { it.id == milestoneId }
    fun getShortTasksByMilestone(milestoneId: String): List<ShortTask> = shortTasks.filter { it.milestoneId == milestoneId }

    fun getLogCategories(): List<LogCategory> = logCategories.toList()
    fun getLogTopics(): List<LogTopic> = logTopics.toList()
    fun getLogTopic(topicId: String): LogTopic? = logTopics.firstOrNull { it.id == topicId }
    fun getLogTopicsByCategory(categoryId: String?): List<LogTopic> {
        return if (categoryId == null) getLogTopics() else logTopics.filter { it.categoryId == categoryId }
    }
    fun getLogEntry(entryId: String): LogEntry? = logEntries.firstOrNull { it.id == entryId }
    fun getLogEntriesByTopic(topicId: String): List<LogEntry> = logEntries.filter { it.topicId == topicId }

    fun getGrowthAreas(): List<GrowthArea> = growthAreas.toList()
    fun getGrowthArea(growthAreaId: String): GrowthArea? = growthAreas.firstOrNull { it.id == growthAreaId }
    fun getGrowthTopicsByArea(growthAreaId: String): List<GrowthTopic> = growthTopics.filter { it.growthAreaId == growthAreaId }
    fun getGrowthTopic(topicId: String): GrowthTopic? = growthTopics.firstOrNull { it.id == topicId }
    fun getRoutinesByTopic(topicId: String): List<Routine> = routines.filter { it.growthTopicId == topicId }

    fun getTodayWorks(): List<Task> = todayWorks.toList()
    fun getTodayLabel(): String = SampleHome.todayLabel
    fun getGoalSettings() = SampleHome.goalSettings
    fun getFixedGoals(): List<Task> = SampleHome.fixedGoals
    fun getTodayGoals(): List<Task> = SampleHome.todayGoals

    fun addTodayWork(title: String): Task {
        val task = Task(nextId(todayWorks.map { it.id }), title, false)
        todayWorks.add(task)
        return task
    }

    fun addProject(title: String): Project {
        val project = Project(
            id = nextId(projects.map { it.id }),
            title = title,
            description = "",
            category = "기본",
            status = "진행중",
            targetDate = "미정"
        )
        projects.add(project)
        return project
    }

    fun addMilestone(projectId: String, title: String): Milestone {
        val milestone = Milestone(
            id = nextId(milestones.map { it.id }),
            projectId = projectId,
            title = title,
            description = "",
            order = milestones.count { it.projectId == projectId } + 1,
            targetDate = getProject(projectId)?.targetDate ?: "미정"
        )
        milestones.add(milestone)
        return milestone
    }

    fun addShortTask(milestoneId: String, title: String): ShortTask {
        val shortTask = ShortTask(
            id = nextId(shortTasks.map { it.id }),
            milestoneId = milestoneId,
            title = title,
            isDone = false,
            order = shortTasks.count { it.milestoneId == milestoneId } + 1
        )
        shortTasks.add(shortTask)
        return shortTask
    }

    fun addLogCategory(title: String): LogCategory {
        val category = LogCategory(nextId(logCategories.map { it.id }), title, LogCategoryType.Memo)
        logCategories.add(category)
        return category
    }

    fun addLogEntry(topicId: String, title: String): LogEntry {
        val entry = LogEntry(
            id = nextId(logEntries.map { it.id }),
            topicId = topicId,
            title = title,
            content = "",
            createdAt = "오늘",
            updatedAt = "오늘",
            tags = emptyList()
        )
        logEntries.add(entry)
        return entry
    }

    fun updateLogEntry(entryId: String, title: String, content: String, updatedAt: String): LogEntry? {
        val index = logEntries.indexOfFirst { it.id == entryId }
        if (index == -1) return null
        val updatedEntry = logEntries[index].copy(
            title = title,
            content = content,
            updatedAt = updatedAt
        )
        logEntries[index] = updatedEntry
        return updatedEntry
    }

    fun addGrowthArea(title: String): GrowthArea {
        val area = GrowthArea(nextId(growthAreas.map { it.id }), title, "", 1, 0)
        growthAreas.add(area)
        return area
    }

    fun addGrowthTopic(growthAreaId: String, title: String): GrowthTopic {
        val topic = GrowthTopic(
            id = nextId(growthTopics.map { it.id }),
            growthAreaId = growthAreaId,
            title = title,
            description = "",
            level = 1,
            exp = 0,
            order = growthTopics.count { it.growthAreaId == growthAreaId } + 1
        )
        growthTopics.add(topic)
        return topic
    }

    fun addRoutine(topicId: String, title: String): Routine {
        val routine = Routine(
            id = "routine-${routines.size + 1}",
            growthTopicId = topicId,
            title = title,
            description = "",
            isFixed = false,
            isActive = true,
            repeatType = RepeatType.Daily,
            isDoneToday = false,
            order = routines.count { it.growthTopicId == topicId } + 1
        )
        routines.add(routine)
        return routine
    }

    private fun nextId(ids: List<String>): String {
        val next = (ids.mapNotNull { it.toLongOrNull() }.maxOrNull() ?: 0L) + 1L
        return next.toString()
    }
}
