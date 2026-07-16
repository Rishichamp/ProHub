package com.prohub.assistant.data.repository

import com.prohub.assistant.data.db.NotificationDao
import com.prohub.assistant.data.db.NotificationEntity
import com.prohub.assistant.data.db.SummaryDao
import com.prohub.assistant.data.db.SummaryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val summaryDao: SummaryDao
) {
    fun getUnprocessed(): Flow<List<NotificationEntity>> = notificationDao.getUnprocessed()
    suspend fun insert(notification: NotificationEntity) = notificationDao.insert(notification)
    suspend fun markProcessed(id: String) = notificationDao.markProcessed(id)
    suspend fun getRecent(cutoff: Long): List<NotificationEntity> = notificationDao.getRecent(cutoff)
    suspend fun deleteOldNotifications(cutoff: Long) = notificationDao.deleteOlderThan(cutoff)

    fun getActiveSummaries(): Flow<List<SummaryEntity>> = summaryDao.getActiveSummaries()
    fun getArchivedSummaries(): Flow<List<SummaryEntity>> = summaryDao.getArchivedSummaries()
    suspend fun insertSummary(summary: SummaryEntity) = summaryDao.insert(summary)
    suspend fun markSummaryRead(id: String) = summaryDao.markRead(id)
    suspend fun archiveSummary(id: String) = summaryDao.archive(id)
    suspend fun deleteOldSummaries(cutoff: Long) = summaryDao.deleteOlderThan(cutoff)
}
