package com.prohub.assistant.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    @Query("SELECT * FROM fitness_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<FitnessEntry>>

    @Query("SELECT * FROM fitness_entries WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getEntriesForRange(startDate: Long, endDate: Long): Flow<List<FitnessEntry>>

    @Query("SELECT SUM(steps) FROM fitness_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalSteps(startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(calories) FROM fitness_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalCalories(startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(distanceKm) FROM fitness_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalDistance(startDate: Long, endDate: Long): Float?

    @Query("SELECT SUM(activeMinutes) FROM fitness_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalActiveMinutes(startDate: Long, endDate: Long): Int?

    @Query("SELECT AVG(heartRateAvg) FROM fitness_entries WHERE date >= :startDate AND date <= :endDate AND heartRateAvg > 0")
    suspend fun getAvgHeartRate(startDate: Long, endDate: Long): Float?

    @Query("SELECT COUNT(*) FROM fitness_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getWorkoutCount(startDate: Long, endDate: Long): Int

    @Insert
    suspend fun insert(entry: FitnessEntry): Long

    @Update
    suspend fun update(entry: FitnessEntry)

    @Delete
    suspend fun delete(entry: FitnessEntry)

    @Query("DELETE FROM fitness_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
