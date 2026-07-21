package com.prohub.assistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single timed exercise entry within a day's session (e.g. "Football" for
 * 42 minutes, "Running" for 20 minutes). Multiple entries share the same
 * sessionDate to form one day's full session — free-text exercise names so
 * this isn't locked to a fixed gym-machine list.
 */
@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionDate: Long,      // midnight timestamp of the day this belongs to
    val name: String,           // free-text exercise name, e.g. "Football", "Running", "Cardio"
    val durationSeconds: Int = 0,
    val completedAt: Long = System.currentTimeMillis()
)