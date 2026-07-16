package com.prohub.assistant.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.prohub.assistant.data.db.NotificationEntity
import com.prohub.assistant.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ProHubNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: NotificationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val TAG = "ProHubNotif"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val extras = it.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val packageName = it.packageName

            val entity = NotificationEntity(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = packageName.split(".").last().replaceFirstChar { c -> c.uppercase() },
                sender = title,
                content = text,
                timestamp = System.currentTimeMillis(),
                processed = false
            )

            serviceScope.launch {
                try {
                    repository.insert(entity)
                    Log.d(TAG, "Notification saved: ${entity.appName} from ${entity.sender}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to insert notification: ${e.message}")
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Optional: Handle notification removal if needed
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        serviceScope.cancel()
    }
}
