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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityRecord(record: ActivityRecordEntity)

    @Update
    suspend fun updateActivityRecord(record: ActivityRecordEntity)

    @Delete
    suspend fun deleteActivityRecord(record: ActivityRecordEntity)

    @Query("DELETE FROM activity_records WHERE id = :id")
    suspend fun deleteActivityRecordById(id: String)
}
