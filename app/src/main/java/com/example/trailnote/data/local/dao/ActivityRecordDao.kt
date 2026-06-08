package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.ActivityRecordEntity

@Dao
interface ActivityRecordDao {
    @Query("SELECT * FROM activity_records ORDER BY date DESC")
    suspend fun getActivityRecords(): List<ActivityRecordEntity>

    @Query("SELECT * FROM activity_records WHERE targetId = :targetId ORDER BY date DESC")
    suspend fun getActivityRecordsByTargetId(targetId: String): List<ActivityRecordEntity>

    @Query("SELECT * FROM activity_records WHERE type = :type ORDER BY date DESC")
    suspend fun getActivitiesByType(type: String): List<ActivityRecordEntity>

    @Query("SELECT * FROM activity_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getActivitiesBetween(startDate: String, endDate: String): List<ActivityRecordEntity>

    @Query("SELECT COUNT(*) FROM activity_records WHERE type = :type")
    suspend fun countActivitiesByType(type: String): Int

    @Query("SELECT COUNT(*) FROM activity_records WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun countByTypeBetween(type: String, startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM activity_records WHERE type = :type AND targetId = :targetId")
    suspend fun countActivitiesByTypeAndTarget(type: String, targetId: String): Int

    @Query("SELECT COUNT(*) FROM activity_records WHERE type = :type AND targetId = :targetId AND date = :date")
    suspend fun countActivitiesByTypeTargetAndDate(type: String, targetId: String, date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityRecord(record: ActivityRecordEntity)

    @Update
    suspend fun updateActivityRecord(record: ActivityRecordEntity)

    @Delete
    suspend fun deleteActivityRecord(record: ActivityRecordEntity)

    @Query("DELETE FROM activity_records WHERE id = :id")
    suspend fun deleteActivityRecordById(id: String)

    @Query("DELETE FROM activity_records WHERE type = :type AND targetId = :targetId AND date = :date")
    suspend fun deleteActivityRecords(type: String, targetId: String?, date: String)

    @Query(
        """
        DELETE FROM activity_records
        WHERE type = :type
            AND targetId = :targetId
            AND (:targetType IS NULL OR targetType = :targetType)
            AND date = :date
        """
    )
    suspend fun deleteActivity(type: String, targetId: String?, targetType: String?, date: String)
}
