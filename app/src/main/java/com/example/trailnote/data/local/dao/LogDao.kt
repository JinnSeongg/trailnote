package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.LogCategoryEntity
import com.example.trailnote.data.local.entity.LogEntryEntity
import com.example.trailnote.data.local.entity.LogTopicEntity

@Dao
interface LogDao {
    @Query("SELECT * FROM log_categories ORDER BY orderIndex ASC")
    suspend fun getLogCategories(): List<LogCategoryEntity>

    @Query("SELECT * FROM log_categories WHERE id = :id")
    suspend fun getLogCategoryById(id: String): LogCategoryEntity?

    @Query("SELECT * FROM log_topics ORDER BY orderIndex ASC")
    suspend fun getLogTopics(): List<LogTopicEntity>

    @Query("SELECT * FROM log_topics WHERE id = :id")
    suspend fun getLogTopicById(id: String): LogTopicEntity?

    @Query("SELECT * FROM log_topics WHERE categoryId = :categoryId ORDER BY orderIndex ASC")
    suspend fun getLogTopicsByCategoryId(categoryId: String): List<LogTopicEntity>

    @Query("SELECT * FROM log_entries ORDER BY updatedAt DESC")
    suspend fun getLogEntries(): List<LogEntryEntity>

    @Query("SELECT * FROM log_entries WHERE id = :id")
    suspend fun getLogEntryById(id: String): LogEntryEntity?

    @Query("SELECT * FROM log_entries WHERE topicId = :topicId ORDER BY updatedAt DESC")
    suspend fun getLogEntriesByTopicId(topicId: String): List<LogEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogCategory(category: LogCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogCategories(categories: List<LogCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogTopic(topic: LogTopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogTopics(topics: List<LogTopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(entry: LogEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntries(entries: List<LogEntryEntity>)

    @Update
    suspend fun updateLogCategory(category: LogCategoryEntity)

    @Update
    suspend fun updateLogTopic(topic: LogTopicEntity)

    @Update
    suspend fun updateLogEntry(entry: LogEntryEntity)

    @Delete
    suspend fun deleteLogCategory(category: LogCategoryEntity)

    @Delete
    suspend fun deleteLogTopic(topic: LogTopicEntity)

    @Delete
    suspend fun deleteLogEntry(entry: LogEntryEntity)

    @Query("DELETE FROM log_categories WHERE id = :id")
    suspend fun deleteLogCategoryById(id: String)

    @Query("DELETE FROM log_topics WHERE id = :id")
    suspend fun deleteLogTopicById(id: String)

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun deleteLogEntryById(id: String)

    @Query("UPDATE log_entries SET topicId = :topicId, updatedAt = :updatedAt WHERE id IN (:entryIds)")
    suspend fun moveLogEntriesToTopic(entryIds: Collection<String>, topicId: String, updatedAt: String)
}
