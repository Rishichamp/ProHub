package com.prohub.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.prohub.assistant.service.ReminderScheduler
import com.prohub.assistant.worker.ReminderEscalationWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ProHubApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        const val REMINDER_CHANNEL_ID = "prohub_reminders"
        const val GENERAL_CHANNEL_ID = "prohub_general"
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()

        if (!ReminderScheduler.isReminderScheduled(this)) {
            ReminderScheduler.scheduleDailyReminder(this)
        }

        ReminderEscalationWorker.schedule(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to review your tasks and schedule"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications and updates"
            }

            val escalationChannel = NotificationChannel(
                "reminder_escalation_1",
                "Intelligent Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Progressive reminder escalation for overdue tasks"
            }

            manager.createNotificationChannels(listOf(reminderChannel, generalChannel, escalationChannel))
        }
    }
}