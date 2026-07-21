@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.prohub.assistant.ui.screens.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.prohub.assistant.data.prefs.SecurePrefs
import com.prohub.assistant.ui.components.LoadingDots
import com.prohub.assistant.ui.theme.ProHubColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val securePrefs: SecurePrefs,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(message: String): String? {
        // Check for real device automation first (alarms, opening apps, weather)
        com.prohub.assistant.service.AppAutomation.tryHandleCommand(context, message)?.let {
            return it
        }

        val apiKey = securePrefs.getGeminiKey()
        if (apiKey.isBlank()) return "Please set your Gemini API key in Settings first."

        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply { put("text", message) })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext "No response from server."

                if (!response.isSuccessful) {
                    val errorDetail = try {
                        JSONObject(body).optJSONObject("error")?.optString("message")
                    } catch (e: Exception) { null }
                    return@withContext "Error: HTTP ${response.code}" +
                            (errorDetail?.let { " — $it" } ?: " — $body".take(300))
                }

                val json = JSONObject(body)
                val candidates = json.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "No response generated."
                }

                candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } catch (e: Exception) {
                "Error: ${e.message ?: e.javaClass.simpleName}"
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AIChatScreen(
    navController: NavController,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (!granted) {
        scope.launch { snackbarHostState.showSnackbar("Microphone permission is needed for voice input") }
    } }

    fun submitMessage(text: String) {
        if (text.isBlank()) return

        // Voice/typed navigation commands act immediately, e.g. "open tasks",
        // "go to settings" — these move you to the right section instead of
        // just being sent to Sage as a chat message.
        val navRoute = com.prohub.assistant.service.AppAutomation.tryGetNavigationRoute(text)
        if (navRoute != null) {
            inputText = ""
            if (navRoute == "home") {
                navController.navigate("home") { popUpTo("home") { inclusive = true } }
            } else {
                navController.navigate(navRoute)
            }
            return
        }

        val userMsg = text.trim()
        messages = messages + ChatMessage(userMsg, true)
        inputText = ""
        isLoading = true
        scope.launch {
            val response = viewModel.sendMessage(userMsg)
            isLoading = false
            messages = messages + ChatMessage(response ?: "No response", false)
        }
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(Unit) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { isListening = false }
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                // Live preview only, while still speaking — does not act yet
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (text != null) inputText = text
            }

            override fun onResults(results: android.os.Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                isListening = false
                // Voice command finished — act on it immediately, same as
                // Sage's "Hey Sage" pipeline, instead of waiting for a manual tap.
                if (!text.isNullOrBlank()) submitMessage(text)
            }
        })
        onDispose { speechRecognizer?.destroy() }
    }

    fun startListening() {
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasMic) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (speechRecognizer == null) {
            scope.launch { snackbarHostState.showSnackbar("Voice input isn't available on this device") }
            return
        }
        inputText = ""
        isListening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    // Auto-start dictation if we arrived here via Home's "Voice" quick action
    LaunchedEffect(Unit) {
        if (com.prohub.assistant.service.VoiceEntryBridge.shouldAutoStart.value) {
            com.prohub.assistant.service.VoiceEntryBridge.consume()
            startListening()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sage", color = ProHubColors.Text) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProHubColors.Bg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ProHubColors.Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "AI",
                                fontSize = MaterialTheme.typography.displayLarge.fontSize,
                                color = ProHubColors.Indigo.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Ask me anything!",
                                color = ProHubColors.Text2,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Try: 'What's the weather?' or 'Summarize my tasks'",
                                color = ProHubColors.Muted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                items(messages, key = { it.timestamp }) { message ->
                    ChatBubble(message = message)
                }
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.prohub.assistant.ui.components.OrbitLoader()
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Thinking...",
                                color = ProHubColors.Text2,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Sage...", color = ProHubColors.Text2) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = ProHubColors.Indigo
                        ),
                        maxLines = 4
                    )
                    IconButton(onClick = { if (isListening) stopListening() else startListening() }) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop listening" else "Voice input",
                            tint = if (isListening) ProHubColors.Red else ProHubColors.Text2
                        )
                    }
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                submitMessage(inputText)
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading) ProHubColors.Indigo else ProHubColors.Muted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) ProHubColors.Indigo else ProHubColors.Card
            ),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = message.text,
                color = ProHubColors.Text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}