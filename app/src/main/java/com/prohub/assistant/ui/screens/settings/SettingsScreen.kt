@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.prohub.assistant.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.prohub.assistant.AuthViewModel
import com.prohub.assistant.ui.animation.*
import com.prohub.assistant.ui.theme.ProHubColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authManager.authState.collectAsState()
    val geminiKey by viewModel.geminiKey.collectAsState()
    var keyInput by remember { mutableStateOf(geminiKey) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var reminderHour by remember { mutableStateOf(com.prohub.assistant.service.ReminderScheduler.getReminderHour(context)) }
    var reminderMinute by remember { mutableStateOf(com.prohub.assistant.service.ReminderScheduler.getReminderMinute(context)) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    var expandedSections by remember { mutableStateOf(setOf<String>()) }

    fun toggleSection(id: String) {
        expandedSections = if (expandedSections.contains(id)) {
            expandedSections - id
        } else {
            expandedSections + id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = ProHubColors.Text,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProHubColors.Bg
                )
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Auth Status Card (animated)
            if (authState.hasPin) {
                FadeInUpAnimation(visible = true, delayMillis = 0) {
                    AuthStatusCard(
                        isAuthenticated = authState.isAuthenticated,
                        onLogout = {
                            authViewModel.authManager.logout()
                            scope.launch {
                                snackbarHostState.showSnackbar("Logged out. Settings locked.")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // AI Configuration Section
            FadeInUpAnimation(visible = true, delayMillis = 100) {
                ExpandableSection(
                    id = "ai",
                    title = "🤖 AI Configuration",
                    isExpanded = expandedSections.contains("ai"),
                    onToggle = { toggleSection("ai") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("Gemini API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ProHubColors.Text,
                                unfocusedTextColor = ProHubColors.Text,
                                focusedBorderColor = ProHubColors.Indigo,
                                unfocusedBorderColor = ProHubColors.Border
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.saveGeminiKey(keyInput)
                                scope.launch {
                                    snackbarHostState.showSnackbar("API Key saved!")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProHubColors.Indigo
                            )
                        ) {
                            Text("Save API Key")
                        }
                        if (geminiKey.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "✓ Key configured",
                                color = ProHubColors.Green,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PIN Protection Section
            FadeInUpAnimation(visible = true, delayMillis = 200) {
                ExpandableSection(
                    id = "pin",
                    title = "🔐 PIN Protection",
                    isExpanded = expandedSections.contains("pin"),
                    onToggle = { toggleSection("pin") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!authState.hasPin) {
                            Text(
                                "Set a PIN to protect your settings from unauthorized changes.",
                                color = ProHubColors.Text2,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    authViewModel.authManager.requestAuthForSettings()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ProHubColors.Indigo
                                )
                            ) {
                                Text("Set PIN")
                            }
                        } else {
                            Text(
                                "Settings are protected by PIN.",
                                color = ProHubColors.Green,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    authViewModel.authManager.clearPin()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("PIN removed")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ProHubColors.Red
                                )
                            ) {
                                Text("Remove PIN Protection")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminder Settings Section
            FadeInUpAnimation(visible = true, delayMillis = 300) {
                ExpandableSection(
                    id = "reminder",
                    title = "⏰ Daily Reminders",
                    isExpanded = expandedSections.contains("reminder"),
                    onToggle = { toggleSection("reminder") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Choose when you'd like your daily reminder to review your tasks and schedule.",
                            color = ProHubColors.Text2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = String.format("%02d:%02d", reminderHour, reminderMinute),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reminder Time") },
                            trailingIcon = {
                                IconButton(onClick = { showReminderTimePicker = true }) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Pick reminder time", tint = ProHubColors.Indigo)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ProHubColors.Text,
                                unfocusedTextColor = ProHubColors.Text,
                                focusedBorderColor = ProHubColors.Indigo,
                                unfocusedBorderColor = ProHubColors.Border
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                com.prohub.assistant.service.ReminderScheduler.setReminderTime(
                                    context, reminderHour, reminderMinute
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Daily reminder set for ${String.format("%02d:%02d", reminderHour, reminderMinute)}"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProHubColors.Indigo
                            )
                        ) {
                            Text("Save Reminder Time")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voice Assistant Section
            FadeInUpAnimation(visible = true, delayMillis = 350) {
                ExpandableSection(
                    id = "voice",
                    title = "🎙️ Sage Voice Assistant",
                    isExpanded = expandedSections.contains("voice"),
                    onToggle = { toggleSection("voice") }
                ) {
                    var overlayGranted by remember {
                        mutableStateOf(android.provider.Settings.canDrawOverlays(context))
                    }
                    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                overlayGranted = android.provider.Settings.canDrawOverlays(context)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Say \"Hey Sage\" to activate hands-free voice commands. This needs the floating overlay permission below.",
                            color = ProHubColors.Text2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (overlayGranted) "Overlay permission: Granted" else "Overlay permission: Not granted",
                                color = if (overlayGranted) ProHubColors.Green else ProHubColors.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    context.startActivity(
                                        android.content.Intent(
                                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            android.net.Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (overlayGranted) ProHubColors.Green else ProHubColors.Indigo
                                )
                            ) {
                                Text(if (overlayGranted) "Granted" else "Allow")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val serviceIntent = android.content.Intent(
                                    context, com.prohub.assistant.service.FloatingBubbleService::class.java
                                )
                                androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Voice assistant (re)started")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = overlayGranted,
                            colors = ButtonDefaults.buttonColors(containerColor = ProHubColors.Purple)
                        ) {
                            Text("Start / Restart Sage")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Data Management Section
            FadeInUpAnimation(visible = true, delayMillis = 400) {
                ExpandableSection(
                    id = "data",
                    title = "💾 Data Management",
                    isExpanded = expandedSections.contains("data"),
                    onToggle = { toggleSection("data") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { /* TODO: Export */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProHubColors.Card2
                            )
                        ) {
                            Text("📤 Export Data", color = ProHubColors.Text)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { /* TODO: Clear */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ProHubColors.Red
                            )
                        ) {
                            Text("🗑 Clear All Data")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Info Section
            FadeInUpAnimation(visible = true, delayMillis = 500) {
                ExpandableSection(
                    id = "info",
                    title = "ℹ️ App Info",
                    isExpanded = expandedSections.contains("info"),
                    onToggle = { toggleSection("info") }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        com.prohub.assistant.ui.theme.ProHubLogo(
                            modifier = Modifier.size(56.dp),
                            color = ProHubColors.Indigo
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "ProHub v1.0.0\nBuilt with Jetpack Compose, Room, Hilt, and Gemini AI.",
                            color = ProHubColors.Text2,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                authViewModel.authManager.logout()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProHubColors.Red.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showReminderTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute
        )
        DatePickerDialog(
            onDismissRequest = { showReminderTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderHour = timePickerState.hour
                        reminderMinute = timePickerState.minute
                        showReminderTimePicker = false
                    }
                ) {
                    Text("OK", color = ProHubColors.Indigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) {
                    Text("Cancel", color = ProHubColors.Text2)
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun AuthStatusCard(
    isAuthenticated: Boolean,
    onLogout: () -> Unit
) {
    val cardColor = if (isAuthenticated) {
        ProHubColors.Green.copy(alpha = 0.1f)
    } else {
        ProHubColors.Gold.copy(alpha = 0.1f)
    }

    val borderColor = if (isAuthenticated) ProHubColors.Green else ProHubColors.Gold

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAuthenticated) "Settings Unlocked" else "Settings Locked",
                    color = if (isAuthenticated) ProHubColors.Green else ProHubColors.Gold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (isAuthenticated) {
                        "You can change settings. Tap logout to lock."
                    } else {
                        "Authentication required to change settings."
                    },
                    color = ProHubColors.Text2,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (isAuthenticated) {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Lock settings",
                        tint = ProHubColors.Text2
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    id: String,
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = ProHubColors.Text,
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = ProHubColors.Text2,
                    modifier = Modifier.rotate(rotation)
                )
            }

            ExpandAnimation(visible = isExpanded) {
                content()
            }
        }
    }
}