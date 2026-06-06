package com.example.trailnote.data

import com.example.trailnote.data.sample.SampleGrowth
import com.example.trailnote.data.sample.SampleHome
import com.example.trailnote.data.sample.SampleLogs
import com.example.trailnote.data.sample.SampleProfile
import com.example.trailnote.data.sample.SampleProjects
import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.ActivityRecord
import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogCategoryType
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic
import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.ProfileSummary
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
    private var profileSummary = SampleProfile.summary.copy(
        featuredAchievementId = SampleProfile.achievements.firstOrNull { it.isUnlocked }?.id
    )

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
    fun getProfileSummary(): ProfileSummary = profileSummary
    fun getAchievements(): List<Achievement> = SampleProfile.achievements
    fun getActivityRecords(): List<ActivityRecord> = SampleProfile.activityRecords

    fun updateProfile(name: String, featuredAchievementId: String?, avatarVariant: Int): ProfileSummary {
        profileSummary = profileSummary.copy(
            name = name,
            featuredAchievementId = featuredAchievementId,
            avatarVariant = avatarVariant
        )
        return profileSummary
    }

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

    fun addProject(title: String, category: String): Project {
        val project = addProject(title)
        val index = projects.indexOfFirst { it.id == project.id }
        if (index == -1) return project
        val categorizedProject = project.copy(category = category)
        projects[index] = categorizedProject
        return categorizedProject
    }

    fun updateProjectDescription(projectId: String, description: String): Project? {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return null
        val updatedProject = projects[index].copy(description = description)
        projects[index] = updatedProject
        return updatedProject
    }

    fun updateProjectTitle(projectId: String, title: String): Project? {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return null
        val updatedProject = projects[index].copy(title = title)
        projects[index] = updatedProject
        return updatedProject
    }

    fun deleteProject(projectId: String) {
        val milestoneIds = milestones.filter { it.projectId == projectId }.map { it.id }
        shortTasks.removeAll { it.milestoneId in milestoneIds }
        milestones.removeAll { it.projectId == projectId }
        projects.removeAll { it.id == projectId }
    }

    fun moveProjectsToCategory(projectIds: Collection<String>, category: String) {
        projects.replaceAll { project ->
            if (project.id in projectIds) project.copy(category = category) else project
        }
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

    fun updateMilestoneDescription(milestoneId: String, description: String): Milestone? {
        val index = milestones.indexOfFirst { it.id == milestoneId }
        if (index == -1) return null
        val updatedMilestone = milestones[index].copy(description = description)
        milestones[index] = updatedMilestone
        return updatedMilestone
    }

    fun updateMilestoneTitle(milestoneId: String, title: String): Milestone? {
        val index = milestones.indexOfFirst { it.id == milestoneId }
        if (index == -1) return null
        val updatedMilestone = milestones[index].copy(title = title)
        milestones[index] = updatedMilestone
        return updatedMilestone
    }

    fun deleteMilestone(milestoneId: String) {
        shortTasks.removeAll { it.milestoneId == milestoneId }
        milestones.removeAll { it.id == milestoneId }
    }

    fun moveMilestonesToProject(milestoneIds: Collection<String>, projectId: String) {
        milestones.replaceAll { milestone ->
            if (milestone.id in milestoneIds) milestone.copy(projectId = projectId) else milestone
        }
    }

    fun deleteShortTask(shortTaskId: String) {
        shortTasks.removeAll { it.id == shortTaskId }
    }

    fun moveShortTasksToMilestone(shortTaskIds: Collection<String>, milestoneId: String) {
        shortTasks.replaceAll { shortTask ->
            if (shortTask.id in shortTaskIds) shortTask.copy(milestoneId = milestoneId) else shortTask
        }
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

    fun addLogTopic(categoryId: String, title: String): LogTopic {
        val topic = LogTopic(
            id = nextId(logTopics.map { it.id }),
            categoryId = categoryId,
            title = title,
            description = "",
            order = logTopics.count { it.categoryId == categoryId } + 1
        )
        logTopics.add(topic)
        return topic
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

    fun updateLogTopicTitle(topicId: String, title: String): LogTopic? {
        val index = logTopics.indexOfFirst { it.id == topicId }
        if (index == -1) return null
        val updatedTopic = logTopics[index].copy(title = title)
        logTopics[index] = updatedTopic
        return updatedTopic
    }

    fun moveLogTopicsToCategory(topicIds: Collection<String>, categoryId: String) {
        logTopics.replaceAll { topic ->
            if (topic.id in topicIds) topic.copy(categoryId = categoryId) else topic
        }
    }

    fun moveLogEntriesToTopic(entryIds: Collection<String>, topicId: String) {
        logEntries.replaceAll { entry ->
            if (entry.id in entryIds) entry.copy(topicId = topicId) else entry
        }
    }

    fun deleteLogTopic(topicId: String) {
        logEntries.removeAll { it.topicId == topicId }
        logTopics.removeAll { it.id == topicId }
    }

    fun deleteLogEntry(entryId: String) {
        logEntries.removeAll { it.id == entryId }
    }

    fun addGrowthArea(title: String): GrowthArea {
        val area = GrowthArea(nextId(growthAreas.map { it.id }), title, "", 1, 0)
        growthAreas.add(area)
        return area
    }

    fun updateGrowthAreaDescription(growthAreaId: String, description: String): GrowthArea? {
        val index = growthAreas.indexOfFirst { it.id == growthAreaId }
        if (index == -1) return null
        val updatedArea = growthAreas[index].copy(description = description)
        growthAreas[index] = updatedArea
        return updatedArea
    }

    fun updateGrowthAreaTitle(growthAreaId: String, title: String): GrowthArea? {
        val index = growthAreas.indexOfFirst { it.id == growthAreaId }
        if (index == -1) return null
        val updatedArea = growthAreas[index].copy(title = title)
        growthAreas[index] = updatedArea
        return updatedArea
    }

    fun deleteGrowthArea(growthAreaId: String) {
        val topicIds = growthTopics.filter { it.growthAreaId == growthAreaId }.map { it.id }
        routines.removeAll { it.growthTopicId in topicIds }
        growthTopics.removeAll { it.growthAreaId == growthAreaId }
        growthAreas.removeAll { it.id == growthAreaId }
    }

    fun moveGrowthTopicsToArea(topicIds: Collection<String>, growthAreaId: String) {
        growthTopics.replaceAll { topic ->
            if (topic.id in topicIds) topic.copy(growthAreaId = growthAreaId) else topic
        }
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

    fun updateGrowthTopicDescription(topicId: String, description: String): GrowthTopic? {
        val index = growthTopics.indexOfFirst { it.id == topicId }
        if (index == -1) return null
        val updatedTopic = growthTopics[index].copy(description = description)
        growthTopics[index] = updatedTopic
        return updatedTopic
    }

    fun updateGrowthTopicTitle(topicId: String, title: String): GrowthTopic? {
        val index = growthTopics.indexOfFirst { it.id == topicId }
        if (index == -1) return null
        val updatedTopic = growthTopics[index].copy(title = title)
        growthTopics[index] = updatedTopic
        return updatedTopic
    }

    fun deleteGrowthTopic(topicId: String) {
        routines.removeAll { it.growthTopicId == topicId }
        growthTopics.removeAll { it.id == topicId }
    }

    fun moveRoutinesToTopic(routineIds: Collection<String>, topicId: String) {
        routines.replaceAll { routine ->
            if (routine.id in routineIds) routine.copy(growthTopicId = topicId) else routine
        }
    }

    fun addRoutine(topicId: String, title: String): Routine {
        val routine = Routine(
            id = "routine-${routines.size + 1}",
            growthTopicId = topicId,
            title = title,
            description = "",
            isFixed = false,
            isActive = true,
            repeatType = RepeatType.Weekly,
            isDoneToday = false,
            order = routines.count { it.growthTopicId == topicId } + 1
        )
        routines.add(routine)
        return routine
    }

    fun updateRoutineDone(routineId: String, isDoneToday: Boolean): Routine? {
        val index = routines.indexOfFirst { it.id == routineId }
        if (index == -1) return null
        val updatedRoutine = routines[index].copy(isDoneToday = isDoneToday)
        routines[index] = updatedRoutine
        return updatedRoutine
    }

    fun updateRoutineRepeatType(routineId: String, repeatType: RepeatType): Routine? {
        val index = routines.indexOfFirst { it.id == routineId }
        if (index == -1) return null
        val updatedRoutine = routines[index].copy(repeatType = repeatType)
        routines[index] = updatedRoutine
        return updatedRoutine
    }

    fun deleteRoutine(routineId: String) {
        routines.removeAll { it.id == routineId }
    }

    private fun nextId(ids: List<String>): String {
        val next = (ids.mapNotNull { it.toLongOrNull() }.maxOrNull() ?: 0L) + 1L
        return next.toString()
    }
}
