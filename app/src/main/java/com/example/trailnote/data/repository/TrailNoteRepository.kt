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
import com.example.trailnote.data.local.entity.RoutineEntity
import com.example.trailnote.data.local.entity.ShortTaskEntity
import com.example.trailnote.data.local.entity.UserProfileEntity
import com.example.trailnote.domain.model.GrowthArea
import com.example.trailnote.domain.model.GrowthTopic
import com.example.trailnote.domain.model.HomeGoalSettings
import com.example.trailnote.domain.model.LogCategory
import com.example.trailnote.domain.model.LogCategoryType
import com.example.trailnote.domain.model.LogEntry
import com.example.trailnote.domain.model.LogTopic
import com.example.trailnote.domain.model.Milestone
import com.example.trailnote.domain.model.Project
import com.example.trailnote.domain.model.ProjectCategory
import com.example.trailnote.domain.model.RepeatType
import com.example.trailnote.domain.model.Routine
import com.example.trailnote.domain.model.ShortTask
import com.example.trailnote.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class TrailNoteRepository(
    private val projectDao: ProjectDao,
    private val logDao: LogDao,
    private val growthDao: GrowthDao,
    private val homeDao: HomeDao,
    private val profileDao: ProfileDao,
    private val achievementDao: AchievementDao,
    private val activityRecordDao: ActivityRecordDao
) {
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
    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)
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

    suspend fun getGrowthAreas(): List<GrowthArea> = growthDao.getGrowthAreas().map { it.toDomain() }
    suspend fun getGrowthAreaById(areaId: String): GrowthArea? = growthDao.getGrowthAreaById(areaId)?.toDomain()
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
            orderIndex = growthDao.getGrowthAreas().size + 1,
            createdAt = now,
            updatedAt = now
        )
        growthDao.insertGrowthArea(area)
        return area.toDomain()
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
        return routine.toDomain()
    }

    suspend fun updateGrowthArea(area: GrowthArea): GrowthArea? {
        val current = growthDao.getGrowthAreaById(area.id) ?: return null
        val updated = current.copy(
            title = area.title,
            description = area.description,
            level = area.level,
            exp = area.exp,
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
        val updated = current.copy(
            isDoneToday = isDoneToday,
            updatedAt = currentDate(),
            lastCompletedDate = if (isDoneToday) currentDate() else null
        )
        growthDao.updateRoutine(updated)
        return updated.toDomain()
    }

    suspend fun updateRoutineRepeatType(routineId: String, repeatType: RepeatType): Routine? {
        val current = growthDao.getRoutineById(routineId) ?: return null
        val updated = current.copy(repeatType = repeatType.name, updatedAt = currentDate())
        growthDao.updateRoutine(updated)
        return updated.toDomain()
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
        return task.toDomain()
    }

    suspend fun updateHomeTaskDoneState(taskId: String, isDone: Boolean): Task? {
        val current = homeDao.getHomeTaskById(taskId) ?: return null
        val updated = current.copy(
            isDone = isDone,
            completedAt = if (isDone) currentDate() else null
        )
        homeDao.updateHomeTask(updated)
        return updated.toDomain()
    }

    suspend fun deleteHomeTask(taskId: String) = homeDao.deleteHomeTaskById(taskId)

    suspend fun getHomeGoalSettings(): HomeGoalSettings {
        val settings = homeDao.getHomeGoalSettings()
            ?: HomeGoalSettingsEntity(randomTodayGoalCount = DEFAULT_RANDOM_TODAY_GOAL_COUNT).also {
                homeDao.insertOrUpdateHomeGoalSettings(it)
            }
        return HomeGoalSettings(settings.randomTodayGoalCount)
    }

    suspend fun updateRandomTodayGoalCount(count: Int): HomeGoalSettings {
        val normalizedCount = count.coerceAtLeast(0)
        val settings = HomeGoalSettingsEntity(randomTodayGoalCount = normalizedCount)
        homeDao.insertOrUpdateHomeGoalSettings(settings)
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
        return selectedIds.mapNotNull { routinesById[it]?.toDomain() }
    }

    suspend fun getOrCreateDailyHomeState(date: String): DailyHomeStateEntity {
        homeDao.getDailyHomeState(date)?.let { return it }
        val settings = getHomeGoalSettings()
        val selectedIds = growthDao.getRandomCandidateRoutines()
            .shuffled()
            .take(settings.randomTodayGoalCount)
            .map { it.id }
        val state = DailyHomeStateEntity(
            date = date,
            selectedRoutineIds = selectedIds.joinToString(ID_SEPARATOR),
            lastResetAt = currentDate()
        )
        homeDao.insertOrUpdateDailyHomeState(state)
        return state
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

    private fun ProjectEntity.toDomain(): Project {
        return Project(id, title, description, category, status, targetDate, categoryId, updatedAt)
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

    private fun GrowthAreaEntity.toDomain(): GrowthArea {
        return GrowthArea(id, title, description, level, exp)
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
        const val KEY_LAST_PROCESSED_DATE = "last_processed_date"
    }
}
