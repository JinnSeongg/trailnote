package com.example.trailnote.data.repository

import com.example.trailnote.data.local.dao.AchievementDao
import com.example.trailnote.data.local.dao.ActivityRecordDao
import com.example.trailnote.data.local.dao.GrowthDao
import com.example.trailnote.data.local.dao.HomeDao
import com.example.trailnote.data.local.dao.LogDao
import com.example.trailnote.data.local.dao.ProfileDao
import com.example.trailnote.data.local.dao.ProjectDao
import com.example.trailnote.data.local.entity.AchievementEntity
import com.example.trailnote.data.local.entity.ActivityRecordEntity
import com.example.trailnote.data.local.entity.AppPreferenceEntity
import com.example.trailnote.data.local.entity.GrowthAreaEntity
import com.example.trailnote.data.local.entity.GrowthTopicEntity
import com.example.trailnote.data.local.entity.HomeTaskEntity
import com.example.trailnote.data.local.entity.HomeGoalSettingsEntity
import com.example.trailnote.data.local.entity.DailyHomeStateEntity
import com.example.trailnote.data.local.entity.LogEntryEntity
import com.example.trailnote.data.local.entity.LogCategoryEntity
import com.example.trailnote.data.local.entity.LogTopicEntity
import com.example.trailnote.data.local.entity.ProjectEntity
import com.example.trailnote.data.local.entity.MilestoneEntity
import com.example.trailnote.data.local.entity.ProjectCategoryEntity
import com.example.trailnote.data.local.entity.RoutineCompletionRecordEntity
import com.example.trailnote.data.local.entity.RoutineEntity
import com.example.trailnote.data.local.entity.RoutineExposureBagEntity
import com.example.trailnote.data.local.entity.ShortTaskEntity
import com.example.trailnote.data.local.entity.UserProfileEntity
import com.example.trailnote.data.sample.SampleProfile
import com.example.trailnote.domain.achievement.AchievementEvaluator
import com.example.trailnote.domain.achievement.PersonalAchievementGenerator
import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthColorPalette
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.HomeGoalSettings
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogCategoryType
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic
import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.Achievement
import com.example.trailnote.domain.model.AchievementStats
import com.example.trailnote.domain.model.AchievementUnlockResult
import com.example.trailnote.domain.model.ActivityRecord
import com.example.trailnote.domain.model.ActivityStatsSummary
import com.example.trailnote.domain.model.ActivityTrendPoint
import com.example.trailnote.domain.model.ProfileSummary
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.ProjectCategory
import com.example.trailnote.domain.model.RepeatType
import com.example.trailnote.domain.model.Routine
import com.example.trailnote.domain.model.ShortTask
import com.example.trailnote.domain.model.StatsPeriod
import com.example.trailnote.domain.model.Task
import com.example.trailnote.domain.model.VisitStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import android.util.Log

class TrailNoteRepository(
    private val projectDao: ProjectDao,
    private val logDao: LogDao,
    private val growthDao: GrowthDao,
    private val homeDao: HomeDao,
    private val profileDao: ProfileDao,
    private val achievementDao: AchievementDao,
    private val activityRecordDao: ActivityRecordDao
) {
    private val achievementEvaluator = AchievementEvaluator()
    private val personalAchievementGenerator = PersonalAchievementGenerator()

    suspend fun getProjectCategories(): List<ProjectCategory> = projectDao.getProjectCategories().map { it.toDomain() }
    suspend fun getProjects(): List<Project> = projectDao.getProjects().map { it.toDomain() }
    suspend fun getProjectEntities(): List<ProjectEntity> = projectDao.getProjects()
    suspend fun getProjectById(projectId: String): Project? = projectDao.getProjectById(projectId)?.toDomain()
    suspend fun getProjectsByCategoryId(categoryId: Long): List<Project> {
        return projectDao.getProjectsByCategoryId(categoryId).map { it.toDomain() }
    }
    suspend fun getUncategorizedProjects(): List<Project> = projectDao.getUncategorizedProjects().map { it.toDomain() }
    suspend fun getMilestones(): List<Milestone> = projectDao.getMilestones().map { it.toDomain() }
    suspend fun getMilestonesByProjectId(projectId: String): List<Milestone> {
        return projectDao.getMilestonesByProjectId(projectId).map { it.toDomain() }
    }
    suspend fun getMilestoneById(milestoneId: String): Milestone? = projectDao.getMilestoneById(milestoneId)?.toDomain()
    suspend fun getShortTasks(): List<ShortTask> = projectDao.getShortTasks().map { it.toDomain() }
    suspend fun getShortTasksByMilestoneId(milestoneId: String): List<ShortTask> {
        return projectDao.getShortTasksByMilestoneId(milestoneId).map { it.toDomain() }
    }

    suspend fun insertProject(project: ProjectEntity) = projectDao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) {
        val current = projectDao.getProjectById(project.id)
        projectDao.updateProject(project)
        if (current != null) updateProjectCompletionActivity(current, project)
    }
    suspend fun deleteProjectById(id: String) = deleteProject(id)

    suspend fun addProject(title: String, category: String = DEFAULT_CATEGORY): Project {
        val categoryEntity = findOrCreateProjectCategory(category)
        return addProject(title, categoryEntity.id)
    }

    suspend fun addProject(title: String, categoryId: Long?): Project {
        val now = currentTimestamp()
        val category = categoryId?.let { projectDao.getProjectCategoryById(it)?.title } ?: DEFAULT_CATEGORY
        val project = ProjectEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "",
            category = category,
            categoryId = categoryId,
            status = DEFAULT_PROJECT_STATUS,
            targetDate = DEFAULT_TARGET_DATE,
            createdAt = now,
            updatedAt = now,
            orderIndex = projectDao.getProjects().size + 1,
            isArchived = false
        )
        projectDao.insertProject(project)
        recordCreationActivity(ActivityTypes.PROJECT_CREATED, project.id, ActivityTargetTypes.PROJECT)
        return project.toDomain()
    }

    suspend fun addProjectCategory(title: String): ProjectCategory {
        val normalized = title.trim()
        val existing = projectDao.getProjectCategories().firstOrNull { it.title == normalized }
        if (existing != null) return existing.toDomain()
        val now = currentTimestamp()
        val category = ProjectCategoryEntity(
            title = normalized,
            orderIndex = projectDao.getMaxProjectCategoryOrderIndex() + 1,
            createdAt = now,
            updatedAt = now
        )
        val id = projectDao.insertProjectCategory(category)
        return category.copy(id = id).toDomain()
    }

    suspend fun addMilestone(projectId: String, title: String): Milestone {
        val now = currentTimestamp()
        val milestone = MilestoneEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            title = title,
            description = "",
            targetDate = projectDao.getProjectById(projectId)?.targetDate ?: DEFAULT_TARGET_DATE,
            orderIndex = projectDao.getMilestonesByProjectId(projectId).size + 1,
            createdAt = now,
            updatedAt = now
        )
        projectDao.insertMilestone(milestone)
        recordCreationActivity(ActivityTypes.MILESTONE_CREATED, milestone.id, ActivityTargetTypes.MILESTONE)
        touchProject(projectId)
        return milestone.toDomain()
    }

    suspend fun addShortTask(milestoneId: String, title: String): ShortTask {
        val now = currentTimestamp()
        val shortTask = ShortTaskEntity(
            id = UUID.randomUUID().toString(),
            milestoneId = milestoneId,
            title = title,
            isDone = false,
            orderIndex = projectDao.getShortTasksByMilestoneId(milestoneId).size + 1,
            createdAt = now,
            completedAt = null
        )
        projectDao.insertShortTask(shortTask)
        recordCreationActivity(ActivityTypes.SHORT_TASK_CREATED, shortTask.id, ActivityTargetTypes.SHORT_TASK)
        touchProjectByMilestoneId(milestoneId)
        return shortTask.toDomain()
    }

    suspend fun updateProject(project: Project): Project? {
        val current = projectDao.getProjectById(project.id) ?: return null
        val updated = current.copy(
            title = project.title,
            description = project.description,
            category = project.category,
            categoryId = project.categoryId,
            status = project.status,
            targetDate = project.targetDate,
            updatedAt = currentTimestamp()
        )
        projectDao.updateProject(updated)
        updateProjectCompletionActivity(current, updated)
        return updated.toDomain()
    }

    suspend fun updateProjectTitle(projectId: String, title: String): Project? {
        val current = projectDao.getProjectById(projectId) ?: return null
        val updated = current.copy(title = title, updatedAt = currentTimestamp())
        projectDao.updateProject(updated)
        return updated.toDomain()
    }

    suspend fun updateProjectDescription(projectId: String, description: String): Project? {
        val current = projectDao.getProjectById(projectId) ?: return null
        val updated = current.copy(description = description, updatedAt = currentTimestamp())
        projectDao.updateProject(updated)
        return updated.toDomain()
    }

    suspend fun updateProjectCategoryTitle(categoryId: Long, title: String): ProjectCategory? {
        val current = projectDao.getProjectCategoryById(categoryId) ?: return null
        val normalized = title.trim()
        val updated = current.copy(title = normalized, updatedAt = currentTimestamp())
        projectDao.updateProjectCategory(updated)
        val projectIds = projectDao.getProjectsByCategoryId(categoryId).map { it.id }
        if (projectIds.isNotEmpty()) {
            projectDao.moveProjectsToCategory(projectIds, categoryId, normalized, currentTimestamp())
        }
        return updated.toDomain()
    }

    suspend fun updateMilestone(milestone: Milestone): Milestone? {
        val current = projectDao.getMilestoneById(milestone.id) ?: return null
        val updated = current.copy(
            title = milestone.title,
            description = milestone.description,
            targetDate = milestone.targetDate,
            updatedAt = currentTimestamp()
        )
        projectDao.updateMilestone(updated)
        touchProject(updated.projectId)
        return updated.toDomain()
    }

    suspend fun updateMilestoneTitle(milestoneId: String, title: String): Milestone? {
        val current = projectDao.getMilestoneById(milestoneId) ?: return null
        val updated = current.copy(title = title, updatedAt = currentTimestamp())
        projectDao.updateMilestone(updated)
        touchProject(updated.projectId)
        return updated.toDomain()
    }

    suspend fun updateMilestoneDescription(milestoneId: String, description: String): Milestone? {
        val current = projectDao.getMilestoneById(milestoneId) ?: return null
        val updated = current.copy(description = description, updatedAt = currentTimestamp())
        projectDao.updateMilestone(updated)
        touchProject(updated.projectId)
        return updated.toDomain()
    }

    suspend fun updateShortTask(shortTask: ShortTask): ShortTask? {
        val current = projectDao.getShortTaskById(shortTask.id) ?: return null
        val updated = current.copy(
            title = shortTask.title,
            isDone = shortTask.isDone,
            completedAt = if (shortTask.isDone) current.completedAt ?: currentTimestamp() else null
        )
        projectDao.updateShortTask(updated)
        when {
            !current.isDone && updated.isDone -> recordCompletionActivity(
                ActivityTypes.SHORT_TASK_COMPLETED,
                updated.id,
                ActivityTargetTypes.SHORT_TASK
            )
            current.isDone && !updated.isDone -> deleteActivity(
                ActivityTypes.SHORT_TASK_COMPLETED,
                updated.id,
                ActivityTargetTypes.SHORT_TASK,
                currentDate()
            )
        }
        touchProjectByMilestoneId(updated.milestoneId)
        return updated.toDomain()
    }

    suspend fun updateShortTaskDone(shortTaskId: String, isDone: Boolean): ShortTask? {
        val current = projectDao.getShortTaskById(shortTaskId) ?: return null
        val updated = current.copy(
            isDone = isDone,
            completedAt = if (isDone) currentTimestamp() else null
        )
        projectDao.updateShortTask(updated)
        if (isDone) {
            recordCompletionActivity(ActivityTypes.SHORT_TASK_COMPLETED, shortTaskId, ActivityTargetTypes.SHORT_TASK)
        } else {
            deleteActivity(ActivityTypes.SHORT_TASK_COMPLETED, shortTaskId, ActivityTargetTypes.SHORT_TASK, currentDate())
        }
        touchProjectByMilestoneId(updated.milestoneId)
        return updated.toDomain()
    }

    suspend fun deleteProject(projectId: String) = projectDao.deleteProjectById(projectId)
    suspend fun deleteProjectCategory(categoryId: Long) = projectDao.deleteProjectCategoryById(categoryId)
    suspend fun deleteMilestone(milestoneId: String) {
        val projectId = projectDao.getMilestoneById(milestoneId)?.projectId
        projectDao.deleteMilestoneById(milestoneId)
        if (projectId != null) touchProject(projectId)
    }
    suspend fun deleteShortTask(shortTaskId: String) {
        val milestoneId = projectDao.getShortTaskById(shortTaskId)?.milestoneId
        projectDao.deleteShortTaskById(shortTaskId)
        if (milestoneId != null) touchProjectByMilestoneId(milestoneId)
    }
    suspend fun moveProjectCategoryOrder(categoryId: Long, orderIndex: Int) {
        projectDao.moveProjectCategoryOrder(categoryId, orderIndex, currentTimestamp())
    }
    suspend fun updateProjectCategory(projectId: String, categoryId: Long?) {
        val current = projectDao.getProjectById(projectId) ?: return
        val title = categoryId?.let { projectDao.getProjectCategoryById(it)?.title } ?: DEFAULT_CATEGORY
        projectDao.updateProject(current.copy(categoryId = categoryId, category = title, updatedAt = currentTimestamp()))
    }
    suspend fun moveProjectsToCategory(projectIds: Collection<String>, categoryId: Long?) {
        val title = categoryId?.let { projectDao.getProjectCategoryById(it)?.title } ?: DEFAULT_CATEGORY
        projectDao.moveProjectsToCategory(projectIds, categoryId, title, currentTimestamp())
    }
    suspend fun moveProjectsToCategory(projectIds: Collection<String>, category: String) {
        val categoryEntity = findOrCreateProjectCategory(category)
        projectDao.moveProjectsToCategory(projectIds, categoryEntity.id, categoryEntity.title, currentTimestamp())
    }
    suspend fun moveMilestonesToProject(milestoneIds: Collection<String>, projectId: String) {
        val oldProjectIds = milestoneIds.mapNotNull { projectDao.getMilestoneById(it)?.projectId }.toSet()
        projectDao.moveMilestonesToProject(milestoneIds, projectId, currentTimestamp())
        (oldProjectIds + projectId).forEach { touchProject(it) }
    }
    suspend fun moveShortTasksToMilestone(shortTaskIds: Collection<String>, milestoneId: String) {
        val oldMilestoneIds = shortTaskIds.mapNotNull { projectDao.getShortTaskById(it)?.milestoneId }.toSet()
        projectDao.moveShortTasksToMilestone(shortTaskIds, milestoneId)
        (oldMilestoneIds + milestoneId).forEach { touchProjectByMilestoneId(it) }
    }

    suspend fun getLogCategories(): List<LogCategory> = logDao.getLogCategories().map { it.toDomain() }
    suspend fun getLogCategoryById(categoryId: String): LogCategory? = logDao.getLogCategoryById(categoryId)?.toDomain()
    suspend fun getLogTopics(): List<LogTopic> = logDao.getLogTopics().map { it.toDomain() }
    suspend fun getLogTopicsByCategoryId(categoryId: String): List<LogTopic> {
        return logDao.getLogTopicsByCategoryId(categoryId).map { it.toDomain() }
    }
    suspend fun getLogTopicById(topicId: String): LogTopic? = logDao.getLogTopicById(topicId)?.toDomain()
    suspend fun getLogEntries(): List<LogEntry> = logDao.getLogEntries().map { it.toDomain() }
    suspend fun getLogEntriesByTopicId(topicId: String): List<LogEntry> {
        return logDao.getLogEntriesByTopicId(topicId).map { it.toDomain() }
    }
    suspend fun getLogEntryById(entryId: String): LogEntry? = logDao.getLogEntryById(entryId)?.toDomain()

    suspend fun insertLogEntry(entry: LogEntryEntity) = logDao.insertLogEntry(entry)
    suspend fun updateLogEntry(entry: LogEntryEntity) = logDao.updateLogEntry(entry)
    suspend fun deleteLogEntryById(id: String) = deleteLogEntry(id)

    suspend fun addLogCategory(name: String, type: LogCategoryType = LogCategoryType.Memo): LogCategory {
        val category = LogCategoryEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type.name,
            orderIndex = logDao.getLogCategories().size + 1
        )
        logDao.insertLogCategory(category)
        return category.toDomain()
    }

    suspend fun addLogTopic(categoryId: String, title: String): LogTopic {
        val now = currentTimestamp()
        val topic = LogTopicEntity(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            title = title,
            description = "",
            orderIndex = logDao.getLogTopicsByCategoryId(categoryId).size + 1,
            createdAt = now,
            updatedAt = now
        )
        logDao.insertLogTopic(topic)
        return topic.toDomain()
    }

    suspend fun addLogEntry(topicId: String, title: String): LogEntry {
        val now = currentTimestamp()
        val entry = LogEntryEntity(
            id = UUID.randomUUID().toString(),
            topicId = topicId,
            title = title,
            content = "",
            tags = "",
            createdAt = now,
            updatedAt = now
        )
        logDao.insertLogEntry(entry)
        recordCreationActivity(ActivityTypes.LOG_CREATED, entry.id, ActivityTargetTypes.LOG)
        return entry.toDomain()
    }

    suspend fun updateLogCategory(category: LogCategory): LogCategory? {
        val current = logDao.getLogCategoryById(category.id) ?: return null
        val updated = current.copy(name = category.name, type = category.type.name)
        logDao.updateLogCategory(updated)
        return updated.toDomain()
    }

    suspend fun updateLogCategoryName(categoryId: String, name: String): LogCategory? {
        val current = logDao.getLogCategoryById(categoryId) ?: return null
        val updated = current.copy(name = name.trim())
        logDao.updateLogCategory(updated)
        return updated.toDomain()
    }

    suspend fun updateLogTopic(topic: LogTopic): LogTopic? {
        val current = logDao.getLogTopicById(topic.id) ?: return null
        val updated = current.copy(
            title = topic.title,
            description = topic.description,
            updatedAt = currentDate()
        )
        logDao.updateLogTopic(updated)
        return updated.toDomain()
    }

    suspend fun updateLogTopicTitle(topicId: String, title: String): LogTopic? {
        val current = logDao.getLogTopicById(topicId) ?: return null
        val updated = current.copy(title = title, updatedAt = currentDate())
        logDao.updateLogTopic(updated)
        return updated.toDomain()
    }

    suspend fun updateLogEntry(entry: LogEntry): LogEntry? {
        val current = logDao.getLogEntryById(entry.id) ?: return null
        val updated = current.copy(
            title = entry.title,
            content = entry.content,
            tags = entry.tags.joinToString(TAG_SEPARATOR),
            updatedAt = currentTimestamp()
        )
        logDao.updateLogEntry(updated)
        return updated.toDomain()
    }

    suspend fun updateLogEntry(entryId: String, title: String, content: String, updatedAt: String): LogEntry? {
        val current = logDao.getLogEntryById(entryId) ?: return null
        val updated = current.copy(
            title = title,
            content = content,
            updatedAt = currentTimestamp()
        )
        logDao.updateLogEntry(updated)
        return updated.toDomain()
    }

    suspend fun deleteLogCategory(categoryId: String) = logDao.deleteLogCategoryById(categoryId)
    suspend fun deleteLogTopic(topicId: String) = logDao.deleteLogTopicById(topicId)
    suspend fun deleteLogEntry(entryId: String) = logDao.deleteLogEntryById(entryId)
    suspend fun moveLogEntriesToTopic(entryIds: Collection<String>, topicId: String) {
        logDao.moveLogEntriesToTopic(entryIds, topicId, currentTimestamp())
    }

    suspend fun getGrowthAreas(): List<GrowthArea> {
        val topicsByAreaId = growthDao.getGrowthTopics().groupBy { it.growthAreaId }
        return growthDao.getGrowthAreas().map { area ->
            area.toDomain(level = calculateGrowthAreaLevel(topicsByAreaId[area.id].orEmpty()), exp = 0)
        }
    }

    suspend fun getGrowthAreaById(areaId: String): GrowthArea? {
        val area = growthDao.getGrowthAreaById(areaId) ?: return null
        return area.toDomain(level = calculateGrowthAreaLevel(areaId), exp = 0)
    }
    suspend fun getGrowthTopics(): List<GrowthTopic> = growthDao.getGrowthTopics().map { it.toDomain() }
    suspend fun getGrowthTopicsByAreaId(areaId: String): List<GrowthTopic> {
        return growthDao.getGrowthTopicsByAreaId(areaId).map { it.toDomain() }
    }
    suspend fun getGrowthTopicById(topicId: String): GrowthTopic? = growthDao.getGrowthTopicById(topicId)?.toDomain()
    suspend fun getRoutines(): List<Routine> = growthDao.getRoutines().map { it.toDomain() }
    suspend fun getRoutinesByTopicId(topicId: String): List<Routine> {
        return growthDao.getRoutinesByTopicId(topicId).map { it.toDomain() }
    }
    suspend fun getRoutineById(routineId: String): Routine? = growthDao.getRoutineById(routineId)?.toDomain()

    suspend fun insertGrowthArea(area: GrowthAreaEntity) = growthDao.insertGrowthArea(area)
    suspend fun updateGrowthArea(area: GrowthAreaEntity) = growthDao.updateGrowthArea(area)
    suspend fun deleteGrowthAreaById(id: String) = deleteGrowthArea(id)

    suspend fun addGrowthArea(title: String): GrowthArea {
        val now = currentDate()
        val area = GrowthAreaEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "",
            level = 1,
            exp = 0,
            colorHex = GrowthColorPalette.DEFAULT_COLOR,
            orderIndex = growthDao.getGrowthAreas().size + 1,
            createdAt = now,
            updatedAt = now
        )
        growthDao.insertGrowthArea(area)
        recordCreationActivity(ActivityTypes.GROWTH_AREA_CREATED, area.id, ActivityTargetTypes.GROWTH_AREA)
        return area.toDomain(level = 1, exp = 0)
    }

    suspend fun addGrowthTopic(growthAreaId: String, title: String): GrowthTopic {
        val now = currentDate()
        val topic = GrowthTopicEntity(
            id = UUID.randomUUID().toString(),
            growthAreaId = growthAreaId,
            title = title,
            description = "",
            level = 1,
            exp = 0,
            orderIndex = growthDao.getGrowthTopicsByAreaId(growthAreaId).size + 1,
            createdAt = now,
            updatedAt = now
        )
        growthDao.insertGrowthTopic(topic)
        recordCreationActivity(ActivityTypes.GROWTH_TOPIC_CREATED, topic.id, ActivityTargetTypes.GROWTH_TOPIC)
        return topic.toDomain()
    }

    suspend fun addRoutine(growthTopicId: String, title: String): Routine {
        val now = currentDate()
        val routine = RoutineEntity(
            id = UUID.randomUUID().toString(),
            growthTopicId = growthTopicId,
            title = title,
            description = "",
            isFixed = false,
            isActive = true,
            repeatType = RepeatType.Weekly.name,
            isDoneToday = false,
            orderIndex = growthDao.getRoutinesByTopicId(growthTopicId).size + 1,
            createdAt = now,
            updatedAt = now,
            lastCompletedDate = null
        )
        growthDao.insertRoutine(routine)
        recordCreationActivity(ActivityTypes.ROUTINE_CREATED, routine.id, ActivityTargetTypes.ROUTINE)
        return routine.toDomain()
    }

    suspend fun updateGrowthArea(area: GrowthArea): GrowthArea? {
        val current = growthDao.getGrowthAreaById(area.id) ?: return null
        val updated = current.copy(
            title = area.title,
            description = area.description,
            level = area.level,
            exp = area.exp,
            colorHex = area.colorHex,
            updatedAt = currentDate()
        )
        growthDao.updateGrowthArea(updated)
        return updated.toDomain()
    }

    suspend fun updateGrowthAreaTitle(areaId: String, title: String): GrowthArea? {
        val current = growthDao.getGrowthAreaById(areaId) ?: return null
        val updated = current.copy(title = title, updatedAt = currentDate())
        growthDao.updateGrowthArea(updated)
        return updated.toDomain()
    }

    suspend fun updateGrowthAreaDescription(areaId: String, description: String): GrowthArea? {
        val current = growthDao.getGrowthAreaById(areaId) ?: return null
        val updated = current.copy(description = description, updatedAt = currentDate())
        growthDao.updateGrowthArea(updated)
        return updated.toDomain()
    }

    suspend fun updateGrowthAreaColor(areaId: String, colorHex: String): GrowthArea? {
        val before = growthDao.getGrowthAreaById(areaId)?.colorHex
        val normalizedColor = GrowthColorPalette.normalize(colorHex)
        val rowCount = growthDao.updateGrowthAreaColor(areaId, normalizedColor, currentDate())
        val updated = getGrowthAreaById(areaId)
        Log.d(
            GROWTH_COLOR_DEBUG_TAG,
            "repo update areaId=$areaId before=$before requested=$colorHex normalized=$normalizedColor rows=$rowCount after=${updated?.colorHex}"
        )
        return updated
    }

    suspend fun updateGrowthTopic(topic: GrowthTopic): GrowthTopic? {
        val current = growthDao.getGrowthTopicById(topic.id) ?: return null
        val updated = current.copy(
            title = topic.title,
            description = topic.description,
            level = topic.level,
            exp = topic.exp,
            updatedAt = currentDate()
        )
        growthDao.updateGrowthTopic(updated)
        return updated.toDomain()
    }

    suspend fun updateGrowthTopicTitle(topicId: String, title: String): GrowthTopic? {
        val current = growthDao.getGrowthTopicById(topicId) ?: return null
        val updated = current.copy(title = title, updatedAt = currentDate())
        growthDao.updateGrowthTopic(updated)
        return updated.toDomain()
    }

    suspend fun updateGrowthTopicDescription(topicId: String, description: String): GrowthTopic? {
        val current = growthDao.getGrowthTopicById(topicId) ?: return null
        val updated = current.copy(description = description, updatedAt = currentDate())
        growthDao.updateGrowthTopic(updated)
        return updated.toDomain()
    }

    suspend fun updateRoutine(routine: Routine): Routine? {
        val current = growthDao.getRoutineById(routine.id) ?: return null
        val updated = current.copy(
            title = routine.title,
            description = routine.description,
            isFixed = routine.isFixed,
            isActive = routine.isActive,
            repeatType = routine.repeatType.name,
            isDoneToday = routine.isDoneToday,
            updatedAt = currentDate(),
            lastCompletedDate = if (routine.isDoneToday) current.lastCompletedDate ?: currentDate() else null
        )
        growthDao.updateRoutine(updated)
        return updated.toDomain()
    }

    suspend fun updateRoutineDoneState(routineId: String, isDoneToday: Boolean): Routine? {
        val current = growthDao.getRoutineById(routineId) ?: return null
        val today = currentDate()
        val updated = current.copy(
            isDoneToday = isDoneToday,
            updatedAt = today,
            lastCompletedDate = if (isDoneToday) today else null
        )
        if (isDoneToday) {
            val insertedId = growthDao.insertRoutineCompletionIfAbsent(
                RoutineCompletionRecordEntity(
                    routineId = routineId,
                    date = today,
                    completedAt = currentTimestamp()
                )
            )
            if (insertedId != INSERT_IGNORED) {
                addGrowthTopicProgress(current.growthTopicId, ROUTINE_COMPLETION_PROGRESS)
                recordActivity(
                    type = ActivityTypes.ROUTINE_COMPLETED,
                    date = today,
                    value = 1,
                    targetId = routineId,
                    targetType = ActivityTargetTypes.ROUTINE
                )
            }
        } else {
            // Completion history stays intact so same-day re-checks do not grant growth twice.
        }
        val latestCompletionDate = if (isDoneToday) {
            today
        } else {
            growthDao.getLatestRoutineCompletionDate(routineId)
        }
        growthDao.updateRoutine(updated)
        val finalRoutine = updated.copy(lastCompletedDate = latestCompletionDate)
        if (finalRoutine != updated) {
            growthDao.updateRoutine(finalRoutine)
        }
        return finalRoutine.toDomain()
    }

    suspend fun calculateGrowthAreaLevel(areaId: String): Int {
        return calculateGrowthAreaLevel(growthDao.getGrowthTopicsByAreaId(areaId))
    }

    suspend fun calculateUserLevelFromGrowthAreas(): Int {
        val topicsByAreaId = growthDao.getGrowthTopics().groupBy { it.growthAreaId }
        return growthDao.getGrowthAreas().sumOf { area ->
            calculateGrowthAreaLevel(topicsByAreaId[area.id].orEmpty())
        }
    }

    private fun calculateGrowthAreaLevel(topics: List<GrowthTopicEntity>): Int {
        if (topics.isEmpty()) return DEFAULT_GROWTH_AREA_LEVEL
        return topics.sumOf { it.level } / topics.size
    }

    private suspend fun addGrowthTopicProgress(topicId: String, progressDelta: Int) {
        val topic = growthDao.getGrowthTopicById(topicId) ?: return
        val totalProgress = topic.exp + progressDelta
        val updated = topic.copy(
            level = topic.level + totalProgress / GROWTH_TOPIC_LEVEL_PROGRESS,
            exp = totalProgress % GROWTH_TOPIC_LEVEL_PROGRESS,
            updatedAt = currentDate()
        )
        growthDao.updateGrowthTopic(updated)
    }

    suspend fun updateRoutineRepeatType(routineId: String, repeatType: RepeatType): Routine? {
        val current = growthDao.getRoutineById(routineId) ?: return null
        val updated = current.copy(repeatType = repeatType.name, updatedAt = currentDate())
        growthDao.updateRoutine(updated)
        return updated.toDomain()
    }

    suspend fun updateRoutineFixedState(routineId: String, isFixed: Boolean): Routine? {
        val updatedRows = growthDao.updateRoutineFixedState(routineId, isFixed, currentDate())
        if (updatedRows == 0) return null
        adjustTodayRoutineGoalsForCountChange(currentDate(), getHomeGoalSettings().randomTodayGoalCount)
        return growthDao.getRoutineById(routineId)?.toDomain()
    }

    suspend fun deleteGrowthArea(areaId: String) = growthDao.deleteGrowthAreaById(areaId)
    suspend fun deleteGrowthTopic(topicId: String) = growthDao.deleteGrowthTopicById(topicId)
    suspend fun deleteRoutine(routineId: String) = growthDao.deleteRoutineById(routineId)
    suspend fun moveGrowthTopicsToArea(topicIds: Collection<String>, growthAreaId: String) {
        growthDao.moveGrowthTopicsToArea(topicIds, growthAreaId, currentDate())
    }
    suspend fun moveRoutinesToTopic(routineIds: Collection<String>, growthTopicId: String) {
        growthDao.moveRoutinesToTopic(routineIds, growthTopicId, currentDate())
    }

    suspend fun getHomeTasks() = homeDao.getHomeTasks()
    suspend fun getTodayHomeTasks(date: String): List<Task> {
        return homeDao.getHomeTasksByDate(date).map { it.toDomain() }
    }

    suspend fun insertHomeTask(task: HomeTaskEntity) = homeDao.insertHomeTask(task)
    suspend fun updateHomeTask(task: HomeTaskEntity) = homeDao.updateHomeTask(task)
    suspend fun deleteHomeTaskById(id: String) = deleteHomeTask(id)

    suspend fun addHomeTask(title: String, date: String): Task {
        val now = currentDate()
        val task = HomeTaskEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            isDone = false,
            date = date,
            createdAt = now,
            completedAt = null,
            orderIndex = homeDao.getHomeTasksByDate(date).size + 1
        )
        homeDao.insertHomeTask(task)
        recordCreationActivity(ActivityTypes.TODO_CREATED, task.id, ActivityTargetTypes.TODO, date)
        return task.toDomain()
    }

    suspend fun updateHomeTaskDoneState(taskId: String, isDone: Boolean): Task? {
        val current = homeDao.getHomeTaskById(taskId) ?: return null
        val updated = current.copy(
            isDone = isDone,
            completedAt = if (isDone) currentDate() else null
        )
        homeDao.updateHomeTask(updated)
        if (isDone) {
            recordCompletionActivity(ActivityTypes.TODO_COMPLETED, taskId, ActivityTargetTypes.TODO, current.date)
        } else {
            deleteActivity(ActivityTypes.TODO_COMPLETED, taskId, ActivityTargetTypes.TODO, current.date)
        }
        return updated.toDomain()
    }

    suspend fun deleteHomeTask(taskId: String) = homeDao.deleteHomeTaskById(taskId)

    suspend fun getHomeGoalSettings(): HomeGoalSettings {
        val settings = homeDao.getHomeGoalSettings()
            ?: HomeGoalSettingsEntity(randomTodayGoalCount = DEFAULT_RANDOM_TODAY_GOAL_COUNT).also {
                homeDao.insertOrUpdateHomeGoalSettings(it)
            }
        return HomeGoalSettings(settings.randomTodayGoalCount.coerceIn(MIN_RANDOM_TODAY_GOAL_COUNT, MAX_RANDOM_TODAY_GOAL_COUNT))
    }

    suspend fun updateRandomTodayGoalCount(count: Int): HomeGoalSettings {
        val normalizedCount = count.coerceIn(MIN_RANDOM_TODAY_GOAL_COUNT, MAX_RANDOM_TODAY_GOAL_COUNT)
        val settings = HomeGoalSettingsEntity(randomTodayGoalCount = normalizedCount)
        homeDao.insertOrUpdateHomeGoalSettings(settings)
        adjustTodayRoutineGoalsForCountChange(currentDate(), normalizedCount)
        return HomeGoalSettings(normalizedCount)
    }

    suspend fun getFixedRoutinesForHome(): List<Routine> {
        return growthDao.getFixedActiveRoutines().map { it.toDomain() }
    }

    suspend fun getTodayRandomRoutines(date: String): List<Routine> {
        val state = getOrCreateDailyHomeState(date)
        val selectedIds = state.selectedRoutineIds.toIdList()
        if (selectedIds.isEmpty()) return emptyList()
        val routinesById = growthDao.getRoutinesByIds(selectedIds).associateBy { it.id }
        return selectedIds.mapNotNull { routinesById[it] }
            .filter { it.isActive && !it.isFixed }
            .map { it.toDomain() }
    }

    suspend fun getOrCreateDailyHomeState(date: String): DailyHomeStateEntity {
        val settings = getHomeGoalSettings()
        homeDao.getDailyHomeState(date)?.let {
            return adjustDailyHomeState(it, settings.randomTodayGoalCount)
        }
        val selectedIds = drawRoutineIdsFromBag(settings.randomTodayGoalCount)
        val state = DailyHomeStateEntity(
            date = date,
            selectedRoutineIds = selectedIds.joinToString(ID_SEPARATOR),
            lastResetAt = currentDate()
        )
        homeDao.insertOrUpdateDailyHomeState(state)
        return state
    }

    suspend fun adjustTodayRoutineGoalsForCountChange(date: String, newCount: Int): DailyHomeStateEntity {
        return homeDao.getDailyHomeState(date)
            ?.let { adjustDailyHomeState(it, newCount) }
            ?: DailyHomeStateEntity(
                date = date,
                selectedRoutineIds = drawRoutineIdsFromBag(newCount).joinToString(ID_SEPARATOR),
                lastResetAt = currentDate()
            ).also { homeDao.insertOrUpdateDailyHomeState(it) }
    }

    private suspend fun adjustDailyHomeState(state: DailyHomeStateEntity, requestedCount: Int): DailyHomeStateEntity {
        val candidateIds = getRandomCandidateRoutineIds()
        val targetCount = requestedCount.coerceIn(MIN_RANDOM_TODAY_GOAL_COUNT, MAX_RANDOM_TODAY_GOAL_COUNT).coerceAtMost(candidateIds.size)
        val selectedIds = state.selectedRoutineIds.toIdList()
        val activeRandomIds = selectedIds
            .filter { it in candidateIds }
            .distinct()
            .take(targetCount)
        val adjustedIds = if (activeRandomIds.size < targetCount) {
            activeRandomIds + drawRoutineIdsFromBag(
                count = targetCount - activeRandomIds.size,
                excludedIds = activeRandomIds.toSet()
            )
        } else {
            activeRandomIds
        }
        if (adjustedIds == selectedIds) return state
        val updated = state.copy(
            selectedRoutineIds = adjustedIds.joinToString(ID_SEPARATOR),
            lastResetAt = currentDate()
        )
        homeDao.insertOrUpdateDailyHomeState(updated)
        return updated
    }

    private suspend fun getRoutineExposureBag(): RoutineExposureBagEntity? {
        return homeDao.getRoutineExposureBag()
    }

    private suspend fun saveRoutineExposureBag(bag: RoutineExposureBagEntity) {
        homeDao.insertOrUpdateRoutineExposureBag(bag)
    }

    private suspend fun drawRoutineIdsFromBag(
        count: Int,
        excludedIds: Set<String> = emptySet()
    ): List<String> {
        if (count <= 0) return emptyList()
        val candidateIds = getRandomCandidateRoutineIds()
        val targetCount = count.coerceAtMost((candidateIds - excludedIds).size)
        if (targetCount <= 0) return emptyList()

        var remainingIds = normalizeRoutineExposureBag(candidateIds).toMutableList()
        val drawnIds = mutableListOf<String>()
        while (drawnIds.size < targetCount) {
            remainingIds = remainingIds
                .filter { it in candidateIds && it !in excludedIds && it !in drawnIds }
                .toMutableList()
            if (remainingIds.isEmpty()) {
                remainingIds = candidateIds
                    .filter { it !in excludedIds && it !in drawnIds }
                    .shuffled()
                    .toMutableList()
            }
            if (remainingIds.isEmpty()) break
            drawnIds += remainingIds.removeAt(0)
        }

        saveRoutineExposureBag(
            RoutineExposureBagEntity(
                remainingRoutineIds = remainingIds.joinToString(ID_SEPARATOR),
                candidateRoutineIds = candidateIds.joinToString(ID_SEPARATOR),
                updatedAt = currentDate()
            )
        )
        return drawnIds
    }

    private suspend fun normalizeRoutineExposureBag(candidateIds: List<String>): List<String> {
        val bag = getRoutineExposureBag()
        val storedRemainingIds = bag?.remainingRoutineIds?.toIdList().orEmpty()
        val storedCandidateIds = bag?.candidateRoutineIds?.toIdList().orEmpty()
        val newCandidateIds = candidateIds.filterNot { it in storedCandidateIds }
        val normalizedRemainingIds = (
            storedRemainingIds.filter { it in candidateIds } + newCandidateIds.shuffled()
        ).distinct()
        val shouldSave = bag == null ||
            normalizedRemainingIds != storedRemainingIds ||
            candidateIds != storedCandidateIds
        if (shouldSave) {
            saveRoutineExposureBag(
                RoutineExposureBagEntity(
                    remainingRoutineIds = normalizedRemainingIds.joinToString(ID_SEPARATOR),
                    candidateRoutineIds = candidateIds.joinToString(ID_SEPARATOR),
                    updatedAt = currentDate()
                )
            )
        }
        return normalizedRemainingIds
    }

    private suspend fun getRandomCandidateRoutineIds(): List<String> {
        return growthDao.getRandomCandidateRoutines().map { it.id }.distinct()
    }

    suspend fun getPreference(key: String): String? {
        return homeDao.getPreference(key)?.value
    }

    suspend fun setPreference(key: String, value: String) {
        homeDao.setPreference(AppPreferenceEntity(key, value))
    }

    suspend fun runDailyResetIfNeeded(today: String) {
        val lastProcessedDate = getPreference(KEY_LAST_PROCESSED_DATE)
        if (lastProcessedDate == null) {
            setPreference(KEY_LAST_PROCESSED_DATE, today)
            getOrCreateDailyHomeState(today)
            return
        }
        if (lastProcessedDate == today) return

        carryOverIncompleteHomeTasks(fromDate = lastProcessedDate, toDate = today)
        deleteCompletedHomeTasksBeforeDate(today)
        resetAllRoutineDoneState()
        getOrCreateDailyHomeState(today)
        setPreference(KEY_LAST_PROCESSED_DATE, today)
    }

    suspend fun resetAllRoutineDoneState() {
        growthDao.resetAllRoutineDoneState(currentDate())
    }

    suspend fun carryOverIncompleteHomeTasks(fromDate: String, toDate: String) {
        homeDao.carryOverIncompleteHomeTasks(fromDate, toDate)
    }

    suspend fun deleteCompletedHomeTasksBeforeDate(today: String) {
        homeDao.deleteCompletedHomeTasksBeforeDate(today)
    }

    suspend fun getUserProfile() = profileDao.getUserProfile()
    suspend fun insertUserProfile(profile: UserProfileEntity) = profileDao.insertUserProfile(profile)
    suspend fun updateUserProfile(profile: UserProfileEntity) = profileDao.updateUserProfile(profile)

    suspend fun getAchievements() = achievementDao.getAchievements()
    suspend fun insertAchievement(achievement: AchievementEntity) = achievementDao.insertAchievement(achievement)
    suspend fun updateAchievement(achievement: AchievementEntity) = achievementDao.updateAchievement(achievement)

    suspend fun getActivityRecords() = activityRecordDao.getActivityRecords()
    suspend fun insertActivityRecord(record: ActivityRecordEntity) = activityRecordDao.insertActivityRecord(record)

    suspend fun recordActivity(
        type: String,
        date: String,
        value: Int,
        targetId: String? = null,
        targetType: String? = null
    ) {
        activityRecordDao.insertActivityRecord(
            ActivityRecordEntity(
                id = UUID.randomUUID().toString(),
                type = type,
                targetId = targetId,
                targetType = targetType,
                date = date,
                value = value,
                createdAt = currentTimestamp()
            )
        )
    }

    private suspend fun recordCreationActivity(
        type: String,
        targetId: String,
        targetType: String,
        date: String = currentDate()
    ) {
        if (activityRecordDao.countActivitiesByTypeAndTarget(type, targetId) > 0) return
        recordActivity(type, date, 1, targetId, targetType)
    }

    private suspend fun recordCompletionActivity(
        type: String,
        targetId: String,
        targetType: String,
        date: String = currentDate()
    ) {
        if (activityRecordDao.countActivitiesByTypeTargetAndDate(type, targetId, date) > 0) return
        recordActivity(type, date, 1, targetId, targetType)
    }

    suspend fun recordAppVisitIfNeeded() {
        val today = currentDate()
        if (activityRecordDao.countActivitiesByTypeTargetAndDate(ActivityTypes.APP_VISIT, APP_TARGET_ID, today) > 0) {
            return
        }
        recordActivity(
            type = ActivityTypes.APP_VISIT,
            date = today,
            value = 1,
            targetId = APP_TARGET_ID,
            targetType = ActivityTargetTypes.APP
        )
    }

    suspend fun getVisitStats(): VisitStats {
        val visitDates = activityRecordDao.getActivitiesByType(ActivityTypes.APP_VISIT)
            .map { it.date }
            .distinct()
            .sorted()
        if (visitDates.isEmpty()) {
            return VisitStats(
                totalVisitDays = 0,
                currentStreakDays = 0,
                firstVisitDate = null,
                lastVisitDate = null
            )
        }
        val visitDateSet = visitDates.toSet()
        val today = LocalDate.parse(currentDate())
        // Streak is counted from today. App startup records today's visit, so missing today means current streak is 0.
        var cursor = today
        var streak = 0
        while (cursor.toString() in visitDateSet) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return VisitStats(
            totalVisitDays = visitDates.size,
            currentStreakDays = streak,
            firstVisitDate = visitDates.first(),
            lastVisitDate = visitDates.last()
        )
    }

    suspend fun getAchievementStats(): AchievementStats {
        val projects = projectDao.getProjects()
        val logCategories = logDao.getLogCategories()
        val logTopics = logDao.getLogTopics()
        val logEntries = logDao.getLogEntries()
        val routines = growthDao.getRoutines()
        val growthAreas = growthDao.getGrowthAreas()
        val growthTopics = growthDao.getGrowthTopics()
        val profile = profileDao.getUserProfile()
        val routineById = routines.associateBy { it.id }
        val routineCompletions = growthDao.getRoutineCompletions()
        val visitStats = getVisitStats()

        val entriesByTopicId = logEntries.groupBy { it.topicId }
        val topicsByCategoryId = logTopics.groupBy { it.categoryId }
        val routineCompletionCountByRoutineId = routineCompletions
            .groupingBy { it.routineId }
            .eachCount()

        return AchievementStats(
            completedTaskCount = activityRecordDao.countActivitiesByType(ActivityTypes.TODO_COMPLETED),
            completedProjectCount = projects.count { it.status.isDoneStatus() },
            logCount = logEntries.size,
            completedRoutineCount = routineCompletions.size,
            completedNormalRoutineCount = routineCompletions.count { routineById[it.routineId]?.isFixed == false },
            completedFixedRoutineCount = routineCompletions.count { routineById[it.routineId]?.isFixed == true },
            fixedRoutineCount = routines.count { it.isFixed },
            growthAreaCount = growthAreas.size,
            growthTopicCount = growthTopics.size,
            userLevel = calculateUserLevelFromGrowthAreas(),
            completedShortTaskCount = projectDao.getShortTasks().count { it.isDone },
            // MilestoneEntity has no completion state yet, so this cannot be derived accurately.
            completedMilestoneCount = 0,
            totalVisitDays = visitStats.totalVisitDays,
            currentStreakDays = visitStats.currentStreakDays,
            firstVisitDate = visitStats.firstVisitDate,
            firstTaskCreatedDate = firstActivityDate(ActivityTypes.TODO_CREATED),
            firstProjectCreatedDate = firstActivityDate(ActivityTypes.PROJECT_CREATED),
            firstLogCreatedDate = firstActivityDate(ActivityTypes.LOG_CREATED),
            firstRoutineCreatedDate = firstActivityDate(ActivityTypes.ROUTINE_CREATED),
            logCountByCategory = logCategories.associate { category ->
                val categoryTopicIds = topicsByCategoryId[category.id].orEmpty().map { it.id }.toSet()
                category.id to categoryTopicIds.sumOf { topicId -> entriesByTopicId[topicId].orEmpty().size }
            },
            logCountByTopic = logTopics.associate { topic ->
                topic.id to entriesByTopicId[topic.id].orEmpty().size
            },
            routineCompletionCountByRoutineId = routineCompletionCountByRoutineId,
            // RoutineEntity has no level field; routine levels need a model/schema source before this can be populated.
            routineLevelByRoutineId = emptyMap()
        )
    }

    suspend fun getProfileSummaryFromDb(): ProfileSummary {
        val profile = profileDao.getUserProfile()
        val completedTaskCount = activityRecordDao.countActivitiesByType(ActivityTypes.TODO_COMPLETED)
        val completedProjectCount = projectDao.getProjects().count { it.status.isDoneStatus() }
        val logCount = logDao.getLogEntries().size
        // Counts all persisted routine completion events, including fixed and random routines.
        val completedRoutineCount = growthDao.countRoutineCompletions()

        return ProfileSummary(
            name = profile?.name ?: DEFAULT_PROFILE_NAME,
            level = calculateUserLevelFromGrowthAreas(),
            exp = 0,
            completedTaskCount = completedTaskCount,
            activeProjectCount = completedProjectCount,
            logCount = logCount,
            completedRoutineCount = completedRoutineCount,
            featuredAchievementId = profile?.representativeAchievementId,
            avatarVariant = 0
        )
    }

    suspend fun updateUserProfileName(name: String) {
        val normalizedName = name.trim()
        val now = currentDate()
        val current = profileDao.getUserProfile()
        val updatedProfile = current?.copy(
            name = normalizedName.ifBlank { current.name },
            updatedAt = now
        ) ?: UserProfileEntity(
            name = normalizedName.ifBlank { DEFAULT_PROFILE_NAME },
            level = DEFAULT_PROFILE_LEVEL,
            exp = DEFAULT_PROFILE_EXP,
            representativeAchievementId = null,
            createdAt = now,
            updatedAt = now
        )
        profileDao.insertUserProfile(updatedProfile)
    }

    suspend fun getAchievementsFromDb(): List<Achievement> {
        val achievements = achievementDao.getAchievements().map { it.toDomain() }
        return achievements
            .filterNot { achievement -> isRetiredAchievementId(achievement.id) }
            .ifEmpty { activeFixedAchievementDefinitions() }
    }

    suspend fun refreshAchievementUnlocks(): AchievementUnlockResult {
        val fixedAchievements = ensureAchievementDefinitions()
        val existingAchievements = achievementDao.getAchievements().map { it.toDomain() }
        val stats = getAchievementStats()
        val personalAchievements = generatePersonalProjectCompletionAchievements(existingAchievements) +
            generatePersonalRoutineCompletionAchievements(existingAchievements) +
            generatePersonalLogCategoryAchievements(stats, existingAchievements) +
            generatePersonalGrowthTopicLevelAchievements(existingAchievements)
        if (personalAchievements.isNotEmpty()) {
            achievementDao.insertAchievements(personalAchievements.map { it.toEntity() })
        }
        val evaluatedAchievements = achievementEvaluator.evaluate(
            stats = stats,
            achievements = fixedAchievements,
            unlockedAt = currentDate()
        )
        val newlyUnlockedAchievements = evaluatedAchievements.filter { evaluated ->
            val current = fixedAchievements.firstOrNull { achievement -> achievement.id == evaluated.id }
            current?.isUnlocked == false && evaluated.isUnlocked
        }
        if (newlyUnlockedAchievements.isNotEmpty()) {
            achievementDao.insertAchievements(newlyUnlockedAchievements.map { it.toEntity() })
        }
        removeStaleAchievements((evaluatedAchievements + personalAchievements).map { it.id }.toSet())
        return AchievementUnlockResult(
            achievements = evaluatedAchievements + personalAchievements,
            newlyUnlockedAchievements = newlyUnlockedAchievements + personalAchievements.filter { generated ->
                existingAchievements.none { existing -> existing.id == generated.id && existing.isUnlocked }
            }
        )
    }

    private suspend fun ensureAchievementDefinitions(): List<Achievement> {
        val achievementsById = achievementDao.getAchievements().map { it.toDomain() }.associateBy { it.id }
        val syncedAchievements = activeFixedAchievementDefinitions().map { definition ->
            val existing = achievementsById[definition.id]
            definition.copy(
                isUnlocked = existing?.isUnlocked ?: definition.isUnlocked,
                unlockedAt = existing?.unlockedAt ?: definition.unlockedAt
            )
        }
        achievementDao.insertAchievements(syncedAchievements.map { it.toEntity() })
        return syncedAchievements
    }

    private suspend fun generatePersonalProjectCompletionAchievements(
        existingAchievements: List<Achievement>
    ): List<Achievement> {
        val completedProjects = projectDao.getProjects()
            .filter { it.status.isDoneStatus() }
            .map { it.toDomain() }
        return personalAchievementGenerator.generateProjectCompletionAchievements(
            completedProjects = completedProjects,
            existingAchievements = existingAchievements,
            unlockedAt = currentDate()
        )
    }

    private suspend fun generatePersonalRoutineCompletionAchievements(
        existingAchievements: List<Achievement>
    ): List<Achievement> {
        val completionCountByRoutineId = growthDao.getRoutineCompletions()
            .groupingBy { it.routineId }
            .eachCount()
        return personalAchievementGenerator.generateRoutineCompletionAchievements(
            routines = growthDao.getRoutines().map { it.toDomain() },
            completionCountByRoutineId = completionCountByRoutineId,
            existingAchievements = existingAchievements,
            unlockedAt = currentDate()
        )
    }

    private suspend fun generatePersonalLogCategoryAchievements(
        stats: AchievementStats,
        existingAchievements: List<Achievement>
    ): List<Achievement> {
        return personalAchievementGenerator.generateLogCategoryAchievements(
            categories = logDao.getLogCategories().map { it.toDomain() },
            logCountByCategory = stats.logCountByCategory,
            existingAchievements = existingAchievements,
            unlockedAt = currentDate()
        )
    }

    private suspend fun generatePersonalGrowthTopicLevelAchievements(
        existingAchievements: List<Achievement>
    ): List<Achievement> {
        return personalAchievementGenerator.generateGrowthTopicLevelAchievements(
            topics = growthDao.getGrowthTopics().map { it.toDomain() },
            existingAchievements = existingAchievements,
            unlockedAt = currentDate()
        )
    }

    private suspend fun removeStaleAchievements(validAchievementIds: Set<String>) {
        val staleIds = achievementDao.getAchievements()
            .map { it.id }
            .filter { id -> id !in validAchievementIds || isRetiredAchievementId(id) }
        if (staleIds.isNotEmpty()) {
            achievementDao.deleteAchievementsByIds(staleIds)
        }
    }

    private fun activeFixedAchievementDefinitions(): List<Achievement> {
        return SampleProfile.achievements.filterNot { achievement -> isRetiredAchievementId(achievement.id) }
    }

    private fun isRetiredAchievementId(id: String): Boolean {
        return RETIRED_ACHIEVEMENT_IDS.contains(id) ||
            RETIRED_ACHIEVEMENT_PREFIXES.any { prefix -> id.startsWith(prefix) } ||
            personalAchievementGenerator.isRetiredPersonalAchievement(id)
    }

    suspend fun getUnlockedAchievementsForDisplay(limit: Int? = null): List<Achievement> {
        return getUnlockedAchievementsForDisplay(refreshAchievementUnlocks().achievements, limit)
    }

    fun getUnlockedAchievementsForDisplay(
        achievements: List<Achievement>,
        limit: Int? = null
    ): List<Achievement> {
        val sortedAchievements = achievements
            .withIndex()
            .filter { (_, achievement) -> achievement.isUnlocked }
            .sortedWith(
                compareByDescending<IndexedValue<Achievement>> { (_, achievement) -> achievement.unlockedAt.orEmpty() }
                    .thenByDescending { indexedAchievement -> indexedAchievement.index }
            )
            .map { (_, achievement) -> achievement }
        return if (limit == null) sortedAchievements else sortedAchievements.take(limit)
    }

    suspend fun getAchievementById(id: String): Achievement? {
        return getAchievementsFromDb().firstOrNull { achievement -> achievement.id == id }
    }

    suspend fun updateRepresentativeAchievement(achievementId: String) {
        val now = currentDate()
        val profile = profileDao.getUserProfile()
        val updatedProfile = profile?.copy(
            representativeAchievementId = achievementId,
            updatedAt = now
        ) ?: UserProfileEntity(
            name = DEFAULT_PROFILE_NAME,
            level = DEFAULT_PROFILE_LEVEL,
            exp = DEFAULT_PROFILE_EXP,
            representativeAchievementId = achievementId,
            createdAt = now,
            updatedAt = now
        )
        profileDao.insertUserProfile(updatedProfile)
    }

    suspend fun getActivityRecordsFromDb(): List<ActivityRecord> {
        // Activity records are only partially connected; routine completion is recorded first.
        return activityRecordDao.getActivityRecords()
            .sortedBy { it.date }
            .map { it.toDomain() }
    }

    suspend fun getActivityStats(period: StatsPeriod): ActivityStatsSummary {
        return getActivityStatsForDate(period, currentDate())
    }

    suspend fun getActivityStatsForDate(period: StatsPeriod, todayString: String): ActivityStatsSummary {
        val (startDate, endDate) = getStatsDateRange(period, todayString)
        val routinesById = growthDao.getRoutines().associateBy { it.id }
        val completions = growthDao.getRoutineCompletionsBetween(startDate, endDate)
        val normalRoutineCompletedCount = completions.count { completion ->
            routinesById[completion.routineId]?.isFixed == false
        }
        val fixedRoutineAverageCompletionRate = calculateFixedRoutineAverageCompletionRate(
            startDate = startDate,
            endDate = endDate,
            completionsByDate = completions
                .filter { routinesById[it.routineId]?.isFixed == true }
                .groupBy { it.date }
        )

        return ActivityStatsSummary(
            period = period,
            startDate = startDate,
            endDate = endDate,
            todoCreatedCount = activityRecordDao.countByTypeBetween(ActivityTypes.TODO_CREATED, startDate, endDate),
            logCreatedCount = activityRecordDao.countByTypeBetween(ActivityTypes.LOG_CREATED, startDate, endDate),
            normalRoutineCompletedCount = normalRoutineCompletedCount,
            fixedRoutineAverageCompletionRate = fixedRoutineAverageCompletionRate
        )
    }

    suspend fun getRecentActivityTrend(days: Int = DEFAULT_TREND_DAYS): List<ActivityTrendPoint> {
        if (days <= 0) return emptyList()
        val today = LocalDate.parse(currentDate())
        val dates = (days - 1 downTo 0).map { today.minusDays(it.toLong()).toString() }
        val startDate = dates.first()
        val endDate = dates.last()

        val homeTasksByDate = homeDao.getHomeTasks().groupBy { it.date }
        val routinesById = growthDao.getRoutines().associateBy { it.id }
        val fixedRoutineIds = growthDao.getFixedActiveRoutines().map { it.id }.toSet()
        val completionsByDate = growthDao.getRoutineCompletionsBetween(startDate, endDate).groupBy { it.date }
        val dailyStatesByDate = homeDao.getDailyHomeStates().associateBy { it.date }

        return dates.map { date ->
            // Home task history is incomplete when completed past tasks are deleted; this uses only currently persisted rows.
            val tasks = homeTasksByDate[date].orEmpty()
            val todoRate = completionRateOrNull(
                completedCount = tasks.count { it.isDone },
                totalCount = tasks.size
            )
            // Historical fixed-routine membership is not snapshotted; current active fixed routines are used as the denominator.
            val fixedRoutineRate = completionRateOrNull(
                completedCount = completionsByDate[date].orEmpty()
                    .map { it.routineId }
                    .distinct()
                    .count { it in fixedRoutineIds },
                totalCount = fixedRoutineIds.size
            )
            val selectedDailyRoutineIds = dailyStatesByDate[date]
                ?.selectedRoutineIds
                ?.toIdList()
                .orEmpty()
                .filter { routinesById[it]?.isFixed == false }
                .toSet()
            val dailyRoutineRate = completionRateOrNull(
                completedCount = completionsByDate[date].orEmpty()
                    .map { it.routineId }
                    .distinct()
                    .count { it in selectedDailyRoutineIds },
                totalCount = selectedDailyRoutineIds.size
            )
            // Average excludes components whose denominator is unavailable; if none are calculable, it returns 0.
            val calculableRates = listOfNotNull(todoRate, fixedRoutineRate, dailyRoutineRate)
            ActivityTrendPoint(
                date = date,
                todoCompletionRate = todoRate ?: 0,
                fixedRoutineCompletionRate = fixedRoutineRate ?: 0,
                dailyRoutineCompletionRate = dailyRoutineRate ?: 0,
                averageCompletionRate = if (calculableRates.isEmpty()) 0 else calculableRates.sum() / calculableRates.size
            )
        }
    }

    private fun ProjectEntity.toDomain(): Project {
        return Project(id, title, description, category, status, targetDate, categoryId, updatedAt)
    }

    private fun AchievementEntity.toDomain(): Achievement {
        return Achievement(
            id = id,
            title = title,
            description = description,
            isUnlocked = isUnlocked,
            category = category,
            grade = rarity,
            iconText = iconKey.ifBlank { DEFAULT_ACHIEVEMENT_ICON },
            unlockedAt = unlockedAt
        )
    }

    private fun Achievement.toEntity(): AchievementEntity {
        return AchievementEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            rarity = grade,
            iconKey = iconText,
            isUnlocked = isUnlocked,
            unlockedAt = unlockedAt,
            progress = if (isUnlocked) 1 else 0,
            target = 1
        )
    }

    private fun ActivityRecordEntity.toDomain(): ActivityRecord {
        return ActivityRecord(id, date, value)
    }

    private fun ProjectCategoryEntity.toDomain(): ProjectCategory {
        return ProjectCategory(id, title, orderIndex, createdAt)
    }

    private fun MilestoneEntity.toDomain(): Milestone {
        return Milestone(id, projectId, title, description, orderIndex, targetDate)
    }

    private fun ShortTaskEntity.toDomain(): ShortTask {
        return ShortTask(id, milestoneId, title, isDone, orderIndex)
    }

    private fun LogCategoryEntity.toDomain(): LogCategory {
        return LogCategory(
            id = id,
            name = name,
            type = runCatching { LogCategoryType.valueOf(type) }.getOrDefault(LogCategoryType.Memo)
        )
    }

    private fun LogTopicEntity.toDomain(): LogTopic {
        return LogTopic(id, categoryId, title, description, orderIndex)
    }

    private fun LogEntryEntity.toDomain(): LogEntry {
        return LogEntry(
            id = id,
            topicId = topicId,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags.split(TAG_SEPARATOR).filter { it.isNotBlank() }
        )
    }

    private fun GrowthAreaEntity.toDomain(
        level: Int = this.level,
        exp: Int = this.exp
    ): GrowthArea {
        return GrowthArea(id, title, description, level, exp, GrowthColorPalette.normalize(colorHex))
    }

    private fun GrowthTopicEntity.toDomain(): GrowthTopic {
        return GrowthTopic(id, growthAreaId, title, description, level, exp, orderIndex)
    }

    private fun RoutineEntity.toDomain(): Routine {
        return Routine(
            id = id,
            growthTopicId = growthTopicId,
            title = title,
            description = description,
            isFixed = isFixed,
            isActive = isActive,
            repeatType = runCatching { RepeatType.valueOf(repeatType) }.getOrDefault(RepeatType.Weekly),
            isDoneToday = isDoneToday,
            order = orderIndex
        )
    }

    private fun HomeTaskEntity.toDomain(): Task {
        return Task(id, title, isDone)
    }

    private fun String.toIdList(): List<String> {
        return split(ID_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun String.isDoneStatus(): Boolean {
        return trim().lowercase() in setOf("\uC644\uB8CC", "?꾨즺", "done", "completed")
    }

    private suspend fun firstActivityDate(type: String): String? {
        return activityRecordDao.getActivitiesByType(type)
            .map { it.date }
            .minOrNull()
    }

    private fun completionRateOrNull(completedCount: Int, totalCount: Int): Int? {
        if (totalCount <= 0) return null
        return completedCount.coerceAtLeast(0) * 100 / totalCount
    }

    private suspend fun calculateFixedRoutineAverageCompletionRate(
        startDate: String,
        endDate: String,
        completionsByDate: Map<String, List<RoutineCompletionRecordEntity>>
    ): Int {
        // Historical fixed-routine membership is not snapshotted yet; current active fixed routines are used as the denominator.
        val fixedRoutineCount = growthDao.getFixedActiveRoutines().size
        if (fixedRoutineCount == 0) return 0
        val dates = datesBetween(startDate, endDate)
        if (dates.isEmpty()) return 0
        val totalRate = dates.sumOf { date ->
            val completedFixedRoutineCount = completionsByDate[date]
                .orEmpty()
                .map { it.routineId }
                .distinct()
                .size
            completedFixedRoutineCount * 100 / fixedRoutineCount
        }
        return totalRate / dates.size
    }

    private fun getStatsDateRange(period: StatsPeriod, todayString: String): Pair<String, String> {
        val today = LocalDate.parse(todayString)
        val start = when (period) {
            StatsPeriod.DAILY -> today
            StatsPeriod.WEEKLY -> today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
            StatsPeriod.MONTHLY -> today.withDayOfMonth(1)
        }
        return start.toString() to today.toString()
    }

    private fun datesBetween(startDate: String, endDate: String): List<String> {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        if (start.isAfter(end)) return emptyList()
        return (0L..ChronoUnit.DAYS.between(start, end))
            .map { start.plusDays(it).toString() }
    }

    private suspend fun updateProjectCompletionActivity(current: ProjectEntity, updated: ProjectEntity) {
        val wasDone = current.status.isDoneStatus()
        val isDone = updated.status.isDoneStatus()
        when {
            !wasDone && isDone -> recordCompletionActivity(
                ActivityTypes.PROJECT_COMPLETED,
                updated.id,
                ActivityTargetTypes.PROJECT
            )
            wasDone && !isDone -> deleteActivity(
                ActivityTypes.PROJECT_COMPLETED,
                updated.id,
                ActivityTargetTypes.PROJECT,
                currentDate()
            )
        }
    }

    private suspend fun deleteActivity(type: String, targetId: String?, targetType: String?, date: String) {
        activityRecordDao.deleteActivity(type, targetId, targetType, date)
    }

    private suspend fun touchProject(projectId: String) {
        projectDao.touchProject(projectId, currentTimestamp())
    }

    private suspend fun touchProjectByMilestoneId(milestoneId: String) {
        projectDao.getProjectIdByMilestoneId(milestoneId)?.let { touchProject(it) }
    }

    private suspend fun findOrCreateProjectCategory(title: String): ProjectCategoryEntity {
        val normalized = title.trim().ifEmpty { DEFAULT_CATEGORY }
        projectDao.getProjectCategories().firstOrNull { it.title == normalized }?.let { return it }
        val now = currentTimestamp()
        val category = ProjectCategoryEntity(
            title = normalized,
            orderIndex = projectDao.getMaxProjectCategoryOrderIndex() + 1,
            createdAt = now,
            updatedAt = now
        )
        val id = projectDao.insertProjectCategory(category)
        return category.copy(id = id)
    }

    private fun currentDate(): String = LocalDate.now().toString()
    private fun currentTimestamp(): String = LocalDateTime.now().toString()

    private companion object {
        const val DEFAULT_CATEGORY = "기본"
        const val DEFAULT_PROJECT_STATUS = "진행중"
        const val DEFAULT_TARGET_DATE = "미정"
        const val TAG_SEPARATOR = "\n"
        const val ID_SEPARATOR = ","
        const val DEFAULT_RANDOM_TODAY_GOAL_COUNT = 3
        const val MIN_RANDOM_TODAY_GOAL_COUNT = 1
        const val MAX_RANDOM_TODAY_GOAL_COUNT = 20
        const val KEY_LAST_PROCESSED_DATE = "last_processed_date"
        const val DEFAULT_PROFILE_NAME = "TrailNote"
        const val DEFAULT_PROFILE_LEVEL = 1
        const val DEFAULT_PROFILE_EXP = 0
        const val DEFAULT_ACHIEVEMENT_ICON = "\u25A0"
        const val INSERT_IGNORED = -1L
        const val DEFAULT_TREND_DAYS = 30
        const val APP_TARGET_ID = "app"
        const val DEFAULT_GROWTH_AREA_LEVEL = 1
        const val ROUTINE_COMPLETION_PROGRESS = 5
        const val GROWTH_TOPIC_LEVEL_PROGRESS = 100
        const val GROWTH_COLOR_DEBUG_TAG = "GrowthColorDebug"
        val RETIRED_ACHIEVEMENT_IDS = setOf(
            "fixed-routine-1",
            "fixed-routine-3",
            "fixed-routine-5",
            "fixed-routine-10",
            "fixed-routine-20",
            "growth-area-1",
            "growth-area-3",
            "growth-area-5",
            "growth-area-10",
            "growth-topic-1",
            "growth-topic-5",
            "growth-topic-10",
            "growth-topic-20",
            "growth-topic-50"
        )
        val RETIRED_ACHIEVEMENT_PREFIXES = listOf(
            "log-memo-",
            "log-idea-",
            "log-resource-",
            "log-review-",
            "achieve-"
        )
    }
}
