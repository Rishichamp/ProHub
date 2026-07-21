package com.prohub.assistant.service

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock

/**
 * Routes voice/text commands to real system actions via Android Intents.
 *
 * IMPORTANT SCOPE NOTE: Android's security model does not allow one app to
 * silently read another app's private data (like your Gmail inbox) without
 * that app's own explicit OAuth-based API. "Open Gmail and read/summarize my
 * latest email" genuinely cannot be done with a simple Intent — it requires
 * integrating the real Gmail API with a Google sign-in flow and a read-only
 * mail scope, which is a substantial separate feature (Google Cloud project,
 * OAuth consent screen, token storage, etc.), not a quick addition.
 *
 * What Intents CAN do reliably, no extra permissions needed:
 *  - Launch another app to its home screen (e.g. "open Gmail")
 *  - Pre-fill a specific action in another app (e.g. set an alarm, open a
 *    web search) using that app's documented Intent actions
 *
 * This router handles the second category. Returns a spoken/displayed
 * confirmation string if it handled the command, or null if the command
 * wasn't an automation request (so the caller can fall through to the
 * normal AI/local-parsing pipeline).
 */
object AppAutomation {

    private val KNOWN_APPS = mapOf(
        "gmail" to "com.google.android.gm",
        "mail" to "com.google.android.gm",
        "whatsapp" to "com.whatsapp",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "camera" to "com.android.camera",
        "calendar" to "com.google.android.calendar",
        "chrome" to "com.android.chrome",
        "youtube" to "com.google.android.youtube",
        "spotify" to "com.spotify.music",
        "phone" to "com.android.dialer",
        "messages" to "com.google.android.apps.messaging",
        "clock" to "com.google.android.deskclock",
        "alarm" to "com.google.android.deskclock"
    )

    // Phrases that mean "navigate to this screen inside ProHub" — checked
    // separately from KNOWN_APPS since these are in-app Compose destinations,
    // not other Android apps to launch.
    private val NAV_TARGETS = linkedMapOf(
        "tasks" to "todos", "todo" to "todos", "todos" to "todos", "to-do" to "todos",
        "schedule" to "timetable", "timetable" to "timetable",
        "fitness" to "fitness", "workout" to "fitness", "exercise" to "fitness",
        "settings" to "settings",
        "notifications" to "notifications", "alerts" to "notifications",
        "chat" to "ai", "ai chat" to "ai", "assistant" to "ai",
        "home" to "home", "dashboard" to "home"
    )

    /**
     * Checks if a command means "go to this screen" (e.g. "open tasks",
     * "go to settings", "show my fitness"). Returns the nav-graph route to
     * navigate to, or null if this isn't a navigation command.
     */
    fun tryGetNavigationRoute(command: String): String? {
        val cmd = command.lowercase().trim()
        val isNavPhrase = cmd.startsWith("open ") || cmd.startsWith("go to ") ||
                cmd.startsWith("show ") || cmd.startsWith("take me to ") ||
                cmd.startsWith("navigate to ")
        if (!isNavPhrase) return null

        // Longer phrases first so "ai chat" matches before "chat" alone, etc.
        return NAV_TARGETS.entries
            .sortedByDescending { it.key.length }
            .firstOrNull { cmd.contains(it.key) }
            ?.value
    }

    fun tryHandleCommand(context: Context, command: String): String? {
        val cmd = command.lowercase().trim()

        // "start timing for running" / "start timer for football" / "start a timer for cardio"
        startExerciseTimer(cmd)?.let { return it }

        // "set an alarm for/at 5:30 am"
        setAlarmIntent(context, cmd)?.let { return it }

        // "open <app>"
        openAppIntent(context, cmd)?.let { return it }

        // "check the weather" / "what's the weather"
        if (cmd.contains("weather")) {
            return openWeatherSearch(context)
        }

        return null
    }

    private fun startExerciseTimer(cmd: String): String? {
        val regex = Regex("""start (?:a )?tim(?:er|ing) for (?:my )?([a-z ]+)""")
        val match = regex.find(cmd) ?: return null
        val exerciseName = match.groupValues[1].trim().replaceFirstChar { it.uppercase() }
        if (exerciseName.isBlank()) return null

        ActiveTimerBridge.requestStart(exerciseName)
        return "Starting timer for $exerciseName."
    }

    private fun setAlarmIntent(context: Context, cmd: String): String? {
        if (!cmd.contains("alarm")) return null

        val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""")
        val match = timeRegex.find(cmd) ?: return "I heard you want an alarm, but couldn't catch the time — try 'set an alarm for 5:30 am'."

        var hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: 0
        val meridiem = match.groupValues[3]

        if (meridiem.equals("pm", ignoreCase = true) && hour < 12) hour += 12
        if (meridiem.equals("am", ignoreCase = true) && hour == 12) hour = 0

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "ProHub Alarm")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            val displayHour = if (hour % 12 == 0) 12 else hour % 12
            val displayMeridiem = if (hour < 12) "AM" else "PM"
            "Alarm set for %d:%02d %s.".format(displayHour, minute, displayMeridiem)
        } catch (e: Exception) {
            "I couldn't open the alarm app on this device."
        }
    }

    private fun openAppIntent(context: Context, cmd: String): String? {
        if (!cmd.startsWith("open ") && !cmd.contains("go to my ")) return null

        val appName = KNOWN_APPS.keys.firstOrNull { cmd.contains(it) } ?: return null
        val packageName = KNOWN_APPS[appName] ?: return null

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            "Opening ${appName.replaceFirstChar { it.uppercase() }}."
        } else {
            "${appName.replaceFirstChar { it.uppercase() }} doesn't seem to be installed on this device."
        }
    }

    private fun openWeatherSearch(context: Context): String {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            "Here's the weather."
        } catch (e: Exception) {
            "I couldn't open a weather source on this device."
        }
    }
}