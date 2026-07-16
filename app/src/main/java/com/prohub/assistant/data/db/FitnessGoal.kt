package com.prohub.assistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_goals")
data class FitnessGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalType: String = "steps",
    val targetValue: Int = 10000,
    val period: String = "daily",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
