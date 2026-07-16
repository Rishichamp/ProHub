package com.prohub.assistant.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries WHERE archived = 0 ORDER BY timestamp DESC")
    fun getActiveSummaries(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE archived = 1 ORDER BY timestamp DESC")
    fun getArchivedSummaries(): Flow<List<SummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity)

    @Query("UPDATE summaries SET read = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE summaries SET archived = 1 WHERE id = :id")
    suspend fun archive(id: String)

    @Query("DELETE FROM summaries WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
