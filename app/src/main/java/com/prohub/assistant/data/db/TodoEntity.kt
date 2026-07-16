package com.prohub.assistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val completed: Boolean = false,
    val recurring: Boolean = false,
    val recurrenceRule: String = "none",
    val snoozedUntil: Long = 0,
    val priority: String = "medium",
    val status: String = "pending",
    val completionTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
