package com.example.trailnote.data.repository

import android.content.Context
import com.example.trailnote.data.local.db.AppDatabaseProvider

object RepositoryProvider {
    @Volatile
    private var repository: TrailNoteRepository? = null

    fun getRepository(context: Context): TrailNoteRepository {
        return repository ?: synchronized(this) {
            repository ?: AppDatabaseProvider.getDatabase(context).let { database ->
                TrailNoteRepository(
                    projectDao = database.projectDao(),
                    logDao = database.logDao(),
                    growthDao = database.growthDao(),
                    homeDao = database.homeDao(),
                    profileDao = database.profileDao(),
                    achievementDao = database.achievementDao(),
                    activityRecordDao = database.activityRecordDao()
                )
            }.also { repository = it }
        }
    }
}
