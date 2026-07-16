package com.prohub.assistant.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        NotificationEntity::class,
        SummaryEntity::class,
        TodoEntity::class,
        FitnessEntry::class,
        FitnessGoal::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun summaryDao(): SummaryDao
    abstract fun todoDao(): TodoDao
    abstract fun fitnessDao(): FitnessDao
    abstract fun fitnessGoalDao(): FitnessGoalDao
}
