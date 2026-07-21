package com.prohub.assistant.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_logs WHERE sessionDate = :sessionDate ORDER BY completedAt ASC")
    fun getForSession(sessionDate: Long): Flow<List<ExerciseLogEntity>>

    @Insert
    suspend fun insert(entry: ExerciseLogEntity): Long

    @Update
    suspend fun update(entry: ExerciseLogEntity)

    @Delete
    suspend fun delete(entry: ExerciseLogEntity)
}