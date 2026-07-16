package com.prohub.assistant.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.prohub.assistant.ui.theme.ProHubColors

@Composable
fun PermissionsOnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    var micGranted by remember { mutableStateOf(false) }
    var notifGranted by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { micGranted = it }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifGranted = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to ProHub",
            style = MaterialTheme.typography.displayLarge,
            color = ProHubColors.Text
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your Professional Command Center",
            style = MaterialTheme.typography.bodyLarge,
            color = ProHubColors.Text2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        // Mic Permission
        PermissionCard(
            title = "Microphone Access",
            desc = "For voice commands and AI assistant",
            granted = micGranted,
            onRequest = {
                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = "Notifications",
                desc = "For reminders and updates",
                granted = notifGranted,
                onRequest = {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notification Listener
        PermissionCard(
            title = "Notification Access",
            desc = "To summarize your notifications",
            granted = false,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("todos") { popUpTo("onboarding") { inclusive = true } } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ProHubColors.Indigo)
        ) {
            Text("Get Started", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    desc: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = ProHubColors.Text, style = MaterialTheme.typography.bodyLarge)
                Text(desc, color = ProHubColors.Text2, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (granted) ProHubColors.Green else ProHubColors.Indigo
                )
            ) {
                Text(if (granted) "Granted" else "Allow")
            }
        }
    }
}
