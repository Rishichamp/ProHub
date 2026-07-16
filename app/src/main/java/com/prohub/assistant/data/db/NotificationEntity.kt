package com.prohub.assistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val appName: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val processed: Boolean = false
)
