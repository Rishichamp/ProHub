package com.prohub.assistant.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessGoalDao {
    @Query("SELECT * FROM fitness_goals WHERE isActive = 1")
    fun getActiveGoals(): Flow<List<FitnessGoal>>

    @Query("SELECT * FROM fitness_goals")
    fun getAllGoals(): Flow<List<FitnessGoal>>

    @Insert
    suspend fun insert(goal: FitnessGoal): Long

    @Update
    suspend fun update(goal: FitnessGoal)

    @Delete
    suspend fun delete(goal: FitnessGoal)

    @Query("UPDATE fitness_goals SET isActive = 0 WHERE id = :id")
    suspend fun deactivateGoal(id: Long)
}
