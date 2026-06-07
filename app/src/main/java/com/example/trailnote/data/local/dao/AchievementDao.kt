package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.AchievementEntity

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    suspend fun getAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE category = :category")
    suspend fun getAchievementsByCategory(category: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM achievements WHERE id = :id")
    suspend fun deleteAchievementById(id: String)
}
