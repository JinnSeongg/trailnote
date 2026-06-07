package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.DailyHomeStateEntity
import com.example.trailnote.data.local.entity.HomeGoalSettingsEntity
import com.example.trailnote.data.local.entity.HomeTaskEntity
import com.example.trailnote.data.local.entity.AppPreferenceEntity
import com.example.trailnote.data.local.entity.RoutineExposureBagEntity

@Dao
interface HomeDao {
    @Query("SELECT * FROM home_tasks ORDER BY date DESC, orderIndex ASC")
    suspend fun getHomeTasks(): List<HomeTaskEntity>

    @Query("SELECT * FROM home_tasks WHERE id = :id")
    suspend fun getHomeTaskById(id: String): HomeTaskEntity?

    @Query("SELECT * FROM home_tasks WHERE date = :date ORDER BY orderIndex ASC")
    suspend fun getHomeTasksByDate(date: String): List<HomeTaskEntity>

    @Query("UPDATE home_tasks SET date = :toDate WHERE date = :fromDate AND isDone = 0")
    suspend fun carryOverIncompleteHomeTasks(fromDate: String, toDate: String)

    @Query("DELETE FROM home_tasks WHERE date < :today AND isDone = 1")
    suspend fun deleteCompletedHomeTasksBeforeDate(today: String)

    @Query("SELECT * FROM home_goal_settings WHERE id = 1")
    suspend fun getHomeGoalSettings(): HomeGoalSettingsEntity?

    @Query("SELECT * FROM daily_home_states ORDER BY date DESC")
    suspend fun getDailyHomeStates(): List<DailyHomeStateEntity>

    @Query("SELECT * FROM daily_home_states WHERE date = :date")
    suspend fun getDailyHomeState(date: String): DailyHomeStateEntity?

    @Query("SELECT * FROM routine_exposure_bags WHERE id = 1")
    suspend fun getRoutineExposureBag(): RoutineExposureBagEntity?

    @Query("SELECT * FROM app_preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): AppPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeTask(task: HomeTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeGoalSettings(settings: HomeGoalSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHomeGoalSettings(settings: HomeGoalSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHomeState(state: DailyHomeStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyHomeState(state: DailyHomeStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoutineExposureBag(bag: RoutineExposureBagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: AppPreferenceEntity)

    @Update
    suspend fun updateHomeTask(task: HomeTaskEntity)

    @Update
    suspend fun updateHomeGoalSettings(settings: HomeGoalSettingsEntity)

    @Update
    suspend fun updateDailyHomeState(state: DailyHomeStateEntity)

    @Delete
    suspend fun deleteHomeTask(task: HomeTaskEntity)

    @Delete
    suspend fun deleteDailyHomeState(state: DailyHomeStateEntity)

    @Query("DELETE FROM home_tasks WHERE id = :id")
    suspend fun deleteHomeTaskById(id: String)

    @Query("DELETE FROM daily_home_states WHERE date = :date")
    suspend fun deleteDailyHomeStateByDate(date: String)
}
