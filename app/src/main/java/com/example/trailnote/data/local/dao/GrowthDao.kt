package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.GrowthAreaEntity
import com.example.trailnote.data.local.entity.GrowthTopicEntity
import com.example.trailnote.data.local.entity.RoutineEntity

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_areas ORDER BY orderIndex ASC")
    suspend fun getGrowthAreas(): List<GrowthAreaEntity>

    @Query("SELECT * FROM growth_areas WHERE id = :id")
    suspend fun getGrowthAreaById(id: String): GrowthAreaEntity?

    @Query("SELECT * FROM growth_topics ORDER BY orderIndex ASC")
    suspend fun getGrowthTopics(): List<GrowthTopicEntity>

    @Query("SELECT * FROM growth_topics WHERE id = :id")
    suspend fun getGrowthTopicById(id: String): GrowthTopicEntity?

    @Query("SELECT * FROM growth_topics WHERE growthAreaId = :growthAreaId ORDER BY orderIndex ASC")
    suspend fun getGrowthTopicsByAreaId(growthAreaId: String): List<GrowthTopicEntity>

    @Query("SELECT * FROM routines ORDER BY orderIndex ASC")
    suspend fun getRoutines(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Query("SELECT * FROM routines WHERE growthTopicId = :growthTopicId ORDER BY orderIndex ASC")
    suspend fun getRoutinesByTopicId(growthTopicId: String): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE isFixed = 1 AND isActive = 1 ORDER BY orderIndex ASC")
    suspend fun getFixedActiveRoutines(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE isFixed = 0 AND isActive = 1 ORDER BY orderIndex ASC")
    suspend fun getRandomCandidateRoutines(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE id IN (:ids)")
    suspend fun getRoutinesByIds(ids: Collection<String>): List<RoutineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthArea(area: GrowthAreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthAreas(areas: List<GrowthAreaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthTopic(topic: GrowthTopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthTopics(topics: List<GrowthTopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Update
    suspend fun updateGrowthArea(area: GrowthAreaEntity)

    @Update
    suspend fun updateGrowthTopic(topic: GrowthTopicEntity)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("UPDATE routines SET isFixed = :isFixed, updatedAt = :updatedAt WHERE id = :routineId")
    suspend fun updateRoutineFixedState(routineId: String, isFixed: Boolean, updatedAt: String): Int

    @Delete
    suspend fun deleteGrowthArea(area: GrowthAreaEntity)

    @Delete
    suspend fun deleteGrowthTopic(topic: GrowthTopicEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("DELETE FROM growth_areas WHERE id = :id")
    suspend fun deleteGrowthAreaById(id: String)

    @Query("DELETE FROM growth_topics WHERE id = :id")
    suspend fun deleteGrowthTopicById(id: String)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: String)

    @Query("UPDATE growth_topics SET growthAreaId = :growthAreaId, updatedAt = :updatedAt WHERE id IN (:topicIds)")
    suspend fun moveGrowthTopicsToArea(topicIds: Collection<String>, growthAreaId: String, updatedAt: String)

    @Query("UPDATE routines SET growthTopicId = :growthTopicId, updatedAt = :updatedAt WHERE id IN (:routineIds)")
    suspend fun moveRoutinesToTopic(routineIds: Collection<String>, growthTopicId: String, updatedAt: String)

    @Query("UPDATE routines SET isDoneToday = 0, lastCompletedDate = NULL, updatedAt = :updatedAt")
    suspend fun resetAllRoutineDoneState(updatedAt: String)
}
