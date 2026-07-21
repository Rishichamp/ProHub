package com.prohub.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.prohub.assistant.MainActivity
import com.prohub.assistant.R
import com.prohub.assistant.data.prefs.SecurePrefs
import com.prohub.assistant.data.repository.TodoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class FloatingBubbleService : Service() {

    @Inject
    lateinit var securePrefs: SecurePrefs

    @Inject
    lateinit var todoRepository: TodoRepository

    private lateinit var speechRecognizer: SpeechRecognizer
    private var textToSpeech: TextToSpeech? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private var isListening = false

    companion object {
        const val CHANNEL_ID = "prohub_floating"
        const val NOTIFICATION_ID = 1001
        const val TAG = "FloatingBubble"
        const val WAKE_WORD = "hey sage"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initSpeechRecognizer()
        initTextToSpeech()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        startWakeWordListening()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ProHub Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Always-on listening for voice commands"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sage is listening")
            .setContentText("Say \"Hey Sage\" to activate")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {
                Log.d(TAG, "Ready for speech")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Log.e(TAG, "Speech error: $error")
                if (isListening) restartWakeWordListening()
            }
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull()?.lowercase() ?: ""
                Log.d(TAG, "Heard: $transcript")

                if (transcript.contains(WAKE_WORD)) {
                    speak("Yes? I'm listening.")
                    startCommandListening()
                } else {
                    restartWakeWordListening()
                }
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(0.95f)
            }
        }
    }

    private fun startWakeWordListening() {
        isListening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening: ${e.message}")
        }
    }

    private fun restartWakeWordListening() {
        serviceScope.launch {
            delay(500)
            startWakeWordListening()
        }
    }

    private fun startCommandListening() {
        speechRecognizer.stopListening()

        val commandRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        commandRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                speak("I didn't catch that. Please try again.")
                restartWakeWordListening()
            }
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = matches?.firstOrNull() ?: ""
                Log.d(TAG, "Command: $command")
                processCommand(command)
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        commandRecognizer.startListening(intent)
    }

    private fun processCommand(command: String) {
        // "open tasks" / "go to settings" etc — bring ProHub forward to that screen
        AppAutomation.tryGetNavigationRoute(command)?.let { route ->
            PendingNavigationBridge.requestNavigation(route)
            val launchIntent = Intent(this, com.prohub.assistant.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(launchIntent)
            speak("Opening ${route.replaceFirstChar { it.uppercase() }}.")
            return
        }

        // Check for real device automation first (alarms, opening apps, weather)
        AppAutomation.tryHandleCommand(applicationContext, command)?.let { response ->
            speak(response)
            return
        }

        when {
            command.contains("what's", ignoreCase = true) &&
                    command.contains("mean", ignoreCase = true) -> {
                val word = extractWord(command)
                serviceScope.launch { searchDefinition(word) }
            }
            command.contains("what's trending", ignoreCase = true) ||
                    command.contains("trending", ignoreCase = true) -> {
                val topic = extractTopic(command)
                serviceScope.launch { searchTrending(topic) }
            }
            command.contains("times", ignoreCase = true) ||
                    command.contains("calculate", ignoreCase = true) ||
                    command.contains("what is", ignoreCase = true) && containsNumbers(command) -> {
                serviceScope.launch { calculate(command) }
            }
            command.contains("add task", ignoreCase = true) ||
                    command.contains("remind me", ignoreCase = true) -> {
                val task = extractTask(command)
                serviceScope.launch { addTask(task) }
            }
            command.contains("show my tasks", ignoreCase = true) ||
                    command.contains("what are my tasks", ignoreCase = true) -> {
                serviceScope.launch { showTasks() }
            }
            command.contains("mark task complete", ignoreCase = true) ||
                    command.contains("complete task", ignoreCase = true) -> {
                val taskTitle = extractTask(command)
                serviceScope.launch { markTaskComplete(taskTitle) }
            }
            command.contains("snooze reminder", ignoreCase = true) ||
                    command.contains("remind me later", ignoreCase = true) -> {
                val taskTitle = extractTask(command)
                serviceScope.launch { snoozeTask(taskTitle) }
            }
            command.contains("show pending tasks", ignoreCase = true) ||
                    command.contains("what are my pending tasks", ignoreCase = true) -> {
                serviceScope.launch { showPendingTasks() }
            }
            command.contains("read my tasks", ignoreCase = true) ||
                    command.contains("what's my next task", ignoreCase = true) -> {
                serviceScope.launch { readNextTask() }
            }
            command.contains("remind me again", ignoreCase = true) -> {
                val minutes = extractMinutes(command)
                serviceScope.launch { snoozeAll(minutes) }
            }
            else -> {
                serviceScope.launch { generalQuery(command) }
            }
        }
    }

    private fun extractWord(command: String): String {
        val regex = Regex("""what does (\w+) mean|what's the meaning of (\w+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(command)
        return match?.groupValues?.get(1) ?: match?.groupValues?.get(2) ?: command
    }

    private fun extractTopic(command: String): String {
        val regex = Regex("""trending on (\w+)|about (\w+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(command)
        return match?.groupValues?.get(1) ?: match?.groupValues?.get(2) ?: "general"
    }

    private fun containsNumbers(command: String): Boolean {
        return Regex("""\d+""").containsMatchIn(command)
    }

    private fun extractTask(command: String): String {
        return command.replace(Regex("add task|remind me to", RegexOption.IGNORE_CASE), "").trim()
    }

    // ─── AI API CALLS ───

    private suspend fun searchDefinition(word: String) {
        val apiKey = securePrefs.getGeminiKey()
        if (apiKey.isBlank()) {
            speak("Please set your Gemini API key in settings.")
            return
        }

        speak("Looking up the definition of $word")

        val prompt = "Define the word '$word' clearly with pronunciation, part of speech, meaning, and example usage."
        val response = callGemini(prompt, apiKey)
        speak(response ?: "I couldn't find a definition for $word.")
    }

    private suspend fun searchTrending(topic: String) {
        val apiKey = securePrefs.getGeminiKey()
        if (apiKey.isBlank()) {
            speak("Please set your Gemini API key in settings.")
            return
        }

        speak("Searching for trending topics on $topic")

        val prompt = "What are the top 5 trending topics about $topic right now? Provide brief summaries."
        val response = callGemini(prompt, apiKey)
        speak(response ?: "I couldn't find trending topics on $topic.")
    }

    private suspend fun calculate(command: String) {
        // Try local calculation first
        val result = tryLocalCalculate(command)
        if (result != null) {
            speak("The answer is $result.")
            return
        }

        val apiKey = securePrefs.getGeminiKey()
        if (apiKey.isBlank()) {
            speak("Please set your Gemini API key in settings.")
            return
        }

        val prompt = "Calculate: $command. Provide just the numerical answer."
        val response = callGemini(prompt, apiKey)
        speak(response ?: "I couldn't calculate that.")
    }

    private fun tryLocalCalculate(command: String): Double? {
        val clean = command.lowercase()
            .replace("what's", "")
            .replace("what is", "")
            .replace("calculate", "")
            .trim()

        val multiplyRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:times|x|\*)\s*(\d+(?:\.\d+)?)""")
        multiplyRegex.find(clean)?.let {
            return it.groupValues[1].toDouble() * it.groupValues[2].toDouble()
        }

        val divideRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:divided by|/)\s*(\d+(?:\.\d+)?)""")
        divideRegex.find(clean)?.let {
            return it.groupValues[1].toDouble() / it.groupValues[2].toDouble()
        }

        val addRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:plus|\+)\s*(\d+(?:\.\d+)?)""")
        addRegex.find(clean)?.let {
            return it.groupValues[1].toDouble() + it.groupValues[2].toDouble()
        }

        val subtractRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:minus|-)\s*(\d+(?:\.\d+)?)""")
        subtractRegex.find(clean)?.let {
            return it.groupValues[1].toDouble() - it.groupValues[2].toDouble()
        }

        return null
    }

    private suspend fun addTask(task: String) {
        val todo = com.prohub.assistant.data.db.TodoEntity(
            id = UUID.randomUUID().toString(),
            title = task,
            dueDate = null,
            dueTime = null
        )
        todoRepository.insert(todo)
        speak("I've added the task: $task")
    }

    private suspend fun showTasks() {
        val pending = todoRepository.getAllPending()
        val count = pending.size
        if (count == 0) {
            speak("You have no pending tasks. Great job!")
        } else {
            val taskList = pending.take(3).joinToString(", ") { it.title }
            speak("You have $count pending tasks: $taskList")
        }
    }

    private suspend fun generalQuery(command: String) {
        val apiKey = securePrefs.getGeminiKey()
        if (apiKey.isBlank()) {
            speak("Please set your Gemini API key in settings.")
            return
        }

        val prompt = "Answer this question concisely: $command"
        val response = callGemini(prompt, apiKey)
        speak(response ?: "I'm not sure about that. Please try rephrasing.")
    }

    private suspend fun callGemini(prompt: String, apiKey: String): String? = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val text = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            text
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error: ${e.message}")
            null
        }
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "prohub_utterance")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        textToSpeech?.shutdown()
        serviceScope.cancel()
    }

    private suspend fun markTaskComplete(taskTitle: String) {
        val pending = todoRepository.getAllPending()
        val match = pending.find { it.title.contains(taskTitle, ignoreCase = true) }
        if (match != null) {
            todoRepository.update(match.copy(
                completed = true,
                status = "completed",
                completionTime = System.currentTimeMillis()
            ))
            speak("Task '${match.title}' marked as complete. Great job!")
        } else {
            speak("I couldn't find a task matching '$taskTitle'.")
        }
    }

    private suspend fun snoozeTask(taskTitle: String) {
        val pending = todoRepository.getAllPending()
        val match = pending.find { it.title.contains(taskTitle, ignoreCase = true) }
        if (match != null) {
            todoRepository.update(match.copy(
                snoozedUntil = System.currentTimeMillis() + 15 * 60 * 1000,
                status = "snoozed"
            ))
            speak("Snoozed '${match.title}' for 15 minutes.")
        } else {
            speak("I couldn't find that task.")
        }
    }

    private suspend fun showPendingTasks() {
        val pending = todoRepository.getAllPending()
        val count = pending.size
        if (count == 0) {
            speak("You have no pending tasks. All caught up!")
        } else {
            val taskList = pending.take(3).joinToString(", ") { it.title }
            speak("You have $count pending tasks: $taskList")
        }
    }

    private suspend fun readNextTask() {
        val pending = todoRepository.getAllPending()
            .filter { it.dueDate != null || it.dueTime != null }
            .sortedBy { it.dueDate }
        if (pending.isEmpty()) {
            speak("You have no upcoming tasks with due dates.")
        } else {
            val next = pending.first()
            speak("Your next task is '${next.title}', due on ${next.dueDate} at ${next.dueTime ?: "any time"}.")
        }
    }

    private suspend fun snoozeAll(minutes: Int) {
        val pending = todoRepository.getAllPending()
        val snoozeTime = System.currentTimeMillis() + minutes * 60 * 1000
        pending.forEach {
            todoRepository.update(it.copy(snoozedUntil = snoozeTime))
        }
        speak("Snoozed all reminders for $minutes minutes.")
    }

    private fun extractMinutes(command: String): Int {
        val regex = Regex("""(\d+)\s*(minute|min|hour|hr)""", RegexOption.IGNORE_CASE)
        val match = regex.find(command)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 15
    }
}