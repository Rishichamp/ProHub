package com.prohub.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling reminders")

            // FloatingBubbleService is intentionally NOT started here.
            // Starting a foreground microphone service from a boot receiver
            // is blocked by Android (API 29+) with:
            //   "Foreground service started from background cannot have
            //    location/camera/microphone access"
            // Sage only starts when the user explicitly taps Start in
            // Settings → Sage Voice Assistant.

            ReminderScheduler.scheduleDailyReminder(context)
            Log.d("BootReceiver", "Daily reminder rescheduled")
        }
    }
}