package com.example.trailnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trailnote.data.local.entity.UserProfileEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile")
    suspend fun getProfiles(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile WHERE id = :id")
    suspend fun deleteUserProfileById(id: Long)
}
