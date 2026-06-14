package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.MilestoneEntity
import com.example.trailnote.data.local.entity.ProjectCategoryEntity
import com.example.trailnote.data.local.entity.ProjectEntity
import com.example.trailnote.data.local.entity.ShortTaskEntity

@Dao
interface ProjectDao {
    @Query("SELECT * FROM project_categories ORDER BY orderIndex ASC, createdAt ASC, id ASC")
    suspend fun getProjectCategories(): List<ProjectCategoryEntity>

    @Query("SELECT * FROM project_categories WHERE id = :id")
    suspend fun getProjectCategoryById(id: Long): ProjectCategoryEntity?

    @Query("SELECT * FROM project_categories WHERE title = :title LIMIT 1")
    suspend fun getProjectCategoryByTitle(title: String): ProjectCategoryEntity?

    @Query("SELECT COALESCE(MAX(orderIndex), 0) FROM project_categories")
    suspend fun getMaxProjectCategoryOrderIndex(): Int

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    suspend fun getProjects(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE categoryId = :categoryId ORDER BY updatedAt DESC")
    suspend fun getProjectsByCategoryId(categoryId: Long): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE categoryId IS NULL ORDER BY updatedAt DESC")
    suspend fun getUncategorizedProjects(): List<ProjectEntity>

    @Query("SELECT * FROM milestones ORDER BY orderIndex ASC")
    suspend fun getMilestones(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun getMilestoneById(id: String): MilestoneEntity?

    @Query("SELECT projectId FROM milestones WHERE id = :id")
    suspend fun getProjectIdByMilestoneId(id: String): String?

    @Query("SELECT * FROM milestones WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getMilestonesByProjectId(projectId: String): List<MilestoneEntity>

    @Query("SELECT * FROM short_tasks ORDER BY orderIndex ASC")
    suspend fun getShortTasks(): List<ShortTaskEntity>

    @Query("SELECT * FROM short_tasks WHERE id = :id")
    suspend fun getShortTaskById(id: String): ShortTaskEntity?

    @Query("SELECT * FROM short_tasks WHERE milestoneId = :milestoneId ORDER BY orderIndex ASC")
    suspend fun getShortTasksByMilestoneId(milestoneId: String): List<ShortTaskEntity>

    @Query(
        """
        SELECT short_tasks.* FROM short_tasks
        INNER JOIN milestones ON milestones.id = short_tasks.milestoneId
        WHERE milestones.projectId = :projectId
        ORDER BY short_tasks.orderIndex ASC
        """
    )
    suspend fun getShortTasksByProjectId(projectId: String): List<ShortTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProjectCategory(category: ProjectCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProjectCategories(categories: List<ProjectCategoryEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortTask(shortTask: ShortTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortTasks(shortTasks: List<ShortTaskEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Update
    suspend fun updateProjectCategory(category: ProjectCategoryEntity)

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Update
    suspend fun updateShortTask(shortTask: ShortTaskEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteShortTask(shortTask: ShortTaskEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("DELETE FROM project_categories WHERE id = :id")
    suspend fun deleteProjectCategoryById(id: Long)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: String)

    @Query("DELETE FROM short_tasks WHERE id = :id")
    suspend fun deleteShortTaskById(id: String)

    @Query("UPDATE project_categories SET orderIndex = :orderIndex, updatedAt = :updatedAt WHERE id = :categoryId")
    suspend fun moveProjectCategoryOrder(categoryId: Long, orderIndex: Int, updatedAt: String)

    @Query("UPDATE projects SET category = :category, categoryId = :categoryId, updatedAt = :updatedAt WHERE id IN (:projectIds)")
    suspend fun moveProjectsToCategory(projectIds: Collection<String>, categoryId: Long?, category: String, updatedAt: String)

    @Query("UPDATE projects SET updatedAt = :updatedAt WHERE id = :projectId")
    suspend fun touchProject(projectId: String, updatedAt: String)

    @Query("UPDATE milestones SET projectId = :projectId, updatedAt = :updatedAt WHERE id IN (:milestoneIds)")
    suspend fun moveMilestonesToProject(milestoneIds: Collection<String>, projectId: String, updatedAt: String)

    @Query("UPDATE short_tasks SET milestoneId = :milestoneId, updatedAt = :updatedAt WHERE id IN (:shortTaskIds)")
    suspend fun moveShortTasksToMilestone(shortTaskIds: Collection<String>, milestoneId: String, updatedAt: String)
}
