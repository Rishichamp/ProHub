package com.prohub.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, starting ProHub services")

            // Start floating bubble service
            val serviceIntent = Intent(context, FloatingBubbleService::class.java)
            context.startForegroundService(serviceIntent)

            // Reschedule daily reminder
            ReminderScheduler.scheduleDailyReminder(context)
            Log.d("BootReceiver", "Daily reminder rescheduled for 8:00 PM")
        }
    }
}
