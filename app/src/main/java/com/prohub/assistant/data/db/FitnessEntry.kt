package com.prohub.assistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_entries")
data class FitnessEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val steps: Int = 0,
    val calories: Int = 0,
    val distanceKm: Float = 0f,
    val activeMinutes: Int = 0,
    val heartRateAvg: Int = 0,
    val workoutType: String = "walking",
    val durationMinutes: Int = 0,
    val notes: String = ""
)
