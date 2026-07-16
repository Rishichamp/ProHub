package com.prohub.assistant.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.prohub.assistant.MainActivity
import com.prohub.assistant.R
import com.prohub.assistant.data.repository.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderEscalationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val todoRepository: TodoRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_TAG = "reminder_escalation"
        const val CHANNEL_ID = "reminder_escalation"
        const val NOTIFICATION_ID_BASE = 3000

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderEscalationWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
        }
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                checkAndEscalateReminders()
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    private suspend fun checkAndEscalateReminders() {
        val pendingTasks = todoRepository.getAllPending()
        val now = System.currentTimeMillis()

        pendingTasks.forEach { task ->
            val dueTime = task.dueDate?.let { dateStr ->
                task.dueTime?.let { timeStr ->
                    parseDueTime(dateStr, timeStr)
                }
            }

            if (dueTime != null && dueTime < now) {
                val overdueMinutes = (now - dueTime) / (1000 * 60)
                val escalationLevel = when {
                    overdueMinutes < 15 -> 1
                    overdueMinutes < 60 -> 2
                    overdueMinutes < 240 -> 3
                    else -> 4
                }

                if (task.snoozedUntil == 0L || now > task.snoozedUntil) {
                    sendEscalatedNotification(task, escalationLevel, overdueMinutes)
                }
            }
        }
    }

    private fun parseDueTime(dateStr: String, timeStr: String): Long {
        val cal = Calendar.getInstance()
        val dateParts = dateStr.split("-").map { it.toIntOrNull() ?: 0 }
        val timeParts = timeStr.split(":").map { it.toIntOrNull() ?: 0 }
        if (dateParts.size == 3) {
            cal.set(dateParts[0], dateParts[1] - 1, dateParts[2])
        }
        if (timeParts.size >= 2) {
            cal.set(Calendar.HOUR_OF_DAY, timeParts[0])
            cal.set(Calendar.MINUTE, timeParts[1])
        }
        return cal.timeInMillis
    }

    private fun sendEscalatedNotification(task: com.prohub.assistant.data.db.TodoEntity, level: Int, overdueMinutes: Long) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createEscalationChannel(notificationManager, level)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "todos")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "${CHANNEL_ID}_$level")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        when (level) {
            1 -> {
                builder.setContentTitle("Task Due: ${task.title}")
                    .setContentText("Your task is now due. Tap to view.")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("Your task '${task.title}' is now due. Don't forget to complete it!"))
                    .addAction(R.drawable.ic_notification, "Complete", createActionPendingIntent(task.id, "complete"))
                    .addAction(R.drawable.ic_notification, "Snooze 15m", createActionPendingIntent(task.id, "snooze_15"))
            }
            2 -> {
                builder.setContentTitle("Overdue: ${task.title}")
                    .setContentText("Overdue by ${overdueMinutes} minutes")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("'${task.title}' is overdue by ${overdueMinutes} minutes. Please complete it soon."))
                    .setVibrate(longArrayOf(0, 500, 200, 500))
                    .addAction(R.drawable.ic_notification, "Complete", createActionPendingIntent(task.id, "complete"))
                    .addAction(R.drawable.ic_notification, "Snooze 30m", createActionPendingIntent(task.id, "snooze_30"))
            }
            3 -> {
                builder.setContentTitle("Urgent: ${task.title}")
                    .setContentText("Overdue by ${overdueMinutes / 60} hours!")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("URGENT: '${task.title}' is overdue by ${overdueMinutes / 60} hours! This needs immediate attention."))
                    .setVibrate(longArrayOf(0, 1000, 300, 1000, 300, 1000))
                    .addAction(R.drawable.ic_notification, "Complete Now", createActionPendingIntent(task.id, "complete"))
                    .addAction(R.drawable.ic_notification, "Snooze 1h", createActionPendingIntent(task.id, "snooze_60"))
            }
            4 -> {
                builder.setContentTitle("CRITICAL: ${task.title}")
                    .setContentText("Overdue by ${overdueMinutes / 60} hours - CRITICAL!")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("CRITICAL: '${task.title}' is severely overdue (${overdueMinutes / 60} hours). Complete immediately or re-prioritize."))
                    .setVibrate(longArrayOf(0, 1500, 500, 1500, 500, 1500))
                    .addAction(R.drawable.ic_notification, "Complete", createActionPendingIntent(task.id, "complete"))
                    .addAction(R.drawable.ic_notification, "Re-prioritize", createActionPendingIntent(task.id, "reprioritize"))
            }
        }

        notificationManager.notify(NOTIFICATION_ID_BASE + task.id.hashCode(), builder.build())
    }

    private fun createEscalationChannel(manager: NotificationManager, level: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "${CHANNEL_ID}_$level",
                "Intelligent Reminders (Level $level)",
                when (level) {
                    1 -> NotificationManager.IMPORTANCE_DEFAULT
                    else -> NotificationManager.IMPORTANCE_HIGH
                }
            ).apply {
                description = "Escalation level $level reminders"
                if (level >= 2) {
                    enableVibration(true)
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
                }
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun createActionPendingIntent(taskId: String, action: String): PendingIntent {
        val intent = Intent(applicationContext, ReminderActionReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("action", action)
        }
        return PendingIntent.getBroadcast(
            applicationContext,
            (taskId.hashCode() * 10 + action.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
