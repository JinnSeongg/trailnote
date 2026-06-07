package com.example.trailnote.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.example.trailnote.data.local.entity.DailyHomeStateEntity
import com.example.trailnote.data.local.entity.GrowthAreaEntity
import com.example.trailnote.data.local.entity.GrowthTopicEntity
import com.example.trailnote.data.local.entity.HomeGoalSettingsEntity
import com.example.trailnote.data.local.entity.HomeTaskEntity
import com.example.trailnote.data.local.entity.LogCategoryEntity
import com.example.trailnote.data.local.entity.LogEntryEntity
import com.example.trailnote.data.local.entity.LogTopicEntity
import com.example.trailnote.data.local.entity.MilestoneEntity
import com.example.trailnote.data.local.entity.ProjectCategoryEntity
import com.example.trailnote.data.local.entity.ProjectEntity
import com.example.trailnote.data.local.entity.RoutineEntity
import com.example.trailnote.data.local.entity.RoutineExposureBagEntity
import com.example.trailnote.data.local.entity.ShortTaskEntity
import com.example.trailnote.data.local.entity.UserProfileEntity

@Database(
    entities = [
        ProjectEntity::class,
        MilestoneEntity::class,
        ShortTaskEntity::class,
        LogCategoryEntity::class,
        LogTopicEntity::class,
        LogEntryEntity::class,
        GrowthAreaEntity::class,
        GrowthTopicEntity::class,
        RoutineEntity::class,
        HomeTaskEntity::class,
        HomeGoalSettingsEntity::class,
        DailyHomeStateEntity::class,
        UserProfileEntity::class,
        AchievementEntity::class,
        ActivityRecordEntity::class,
        AppPreferenceEntity::class,
        ProjectCategoryEntity::class,
        RoutineExposureBagEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun logDao(): LogDao
    abstract fun growthDao(): GrowthDao
    abstract fun homeDao(): HomeDao
    abstract fun profileDao(): ProfileDao
    abstract fun achievementDao(): AchievementDao
    abstract fun activityRecordDao(): ActivityRecordDao

    companion object {
        const val DATABASE_NAME = "trailnote.db"
    }
}
