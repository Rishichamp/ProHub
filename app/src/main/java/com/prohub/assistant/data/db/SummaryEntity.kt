package com.prohub.assistant.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey val id: String,
    val appName: String,
    val summary: String,
    val senderList: String,
    val suggestedAction: String,
    val urgencyLevel: String,
    val timestamp: Long,
    val read: Boolean = false,
    val archived: Boolean = false
)
