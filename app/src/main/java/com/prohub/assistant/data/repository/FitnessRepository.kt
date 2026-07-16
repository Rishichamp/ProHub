package com.prohub.assistant.data.repository

import com.prohub.assistant.data.db.FitnessDao
import com.prohub.assistant.data.db.FitnessEntry
import com.prohub.assistant.data.db.FitnessGoal
import com.prohub.assistant.data.db.FitnessGoalDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessRepository @Inject constructor(
    private val fitnessDao: FitnessDao,
    private val goalDao: FitnessGoalDao
) {
    fun getAllEntries(): Flow<List<FitnessEntry>> = fitnessDao.getAllEntries()

    fun getTodayEntries(): Flow<List<FitnessEntry>> {
        val (start, end) = getTodayRange()
        return fitnessDao.getEntriesForRange(start, end)
    }

    fun getWeekEntries(): Flow<List<FitnessEntry>> {
        val (start, end) = getWeekRange()
        return fitnessDao.getEntriesForRange(start, end)
    }

    suspend fun getTodayStats(): FitnessStats {
        val (start, end) = getTodayRange()
        return FitnessStats(
            steps = fitnessDao.getTotalSteps(start, end) ?: 0,
            calories = fitnessDao.getTotalCalories(start, end) ?: 0,
            distanceKm = fitnessDao.getTotalDistance(start, end) ?: 0f,
            activeMinutes = fitnessDao.getTotalActiveMinutes(start, end) ?: 0,
            avgHeartRate = fitnessDao.getAvgHeartRate(start, end) ?: 0f,
            workoutCount = fitnessDao.getWorkoutCount(start, end)
        )
    }

    suspend fun getWeekStats(): FitnessStats {
        val (start, end) = getWeekRange()
        return FitnessStats(
            steps = fitnessDao.getTotalSteps(start, end) ?: 0,
            calories = fitnessDao.getTotalCalories(start, end) ?: 0,
            distanceKm = fitnessDao.getTotalDistance(start, end) ?: 0f,
            activeMinutes = fitnessDao.getTotalActiveMinutes(start, end) ?: 0,
            avgHeartRate = fitnessDao.getAvgHeartRate(start, end) ?: 0f,
            workoutCount = fitnessDao.getWorkoutCount(start, end)
        )
    }

    suspend fun insertEntry(entry: FitnessEntry) = fitnessDao.insert(entry)
    suspend fun updateEntry(entry: FitnessEntry) = fitnessDao.update(entry)
    suspend fun deleteEntry(entry: FitnessEntry) = fitnessDao.delete(entry)

    fun getActiveGoals(): Flow<List<FitnessGoal>> = goalDao.getActiveGoals()
    fun getAllGoals(): Flow<List<FitnessGoal>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: FitnessGoal) = goalDao.insert(goal)
    suspend fun updateGoal(goal: FitnessGoal) = goalDao.update(goal)
    suspend fun deleteGoal(goal: FitnessGoal) = goalDao.delete(goal)
    suspend fun deactivateGoal(id: Long) = goalDao.deactivateGoal(id)

    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}

data class FitnessStats(
    val steps: Int = 0,
    val calories: Int = 0,
    val distanceKm: Float = 0f,
    val activeMinutes: Int = 0,
    val avgHeartRate: Float = 0f,
    val workoutCount: Int = 0
)
