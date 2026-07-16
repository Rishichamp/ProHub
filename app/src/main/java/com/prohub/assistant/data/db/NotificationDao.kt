package com.prohub.assistant.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE processed = 0 ORDER BY timestamp DESC")
    fun getUnprocessed(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp > :cutoff ORDER BY timestamp DESC")
    suspend fun getRecent(cutoff: Long): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("UPDATE notifications SET processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: String)

    @Query("DELETE FROM notifications WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
