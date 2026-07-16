@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.prohub.assistant.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.prohub.assistant.data.db.NotificationEntity
import com.prohub.assistant.data.repository.NotificationRepository
import com.prohub.assistant.ui.components.EmptyState
import com.prohub.assistant.ui.theme.ProHubColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {
    val unprocessedNotifications: Flow<List<NotificationEntity>> = repository.getUnprocessed()

    fun markProcessed(id: String) {
        viewModelScope.launch {
            repository.markProcessed(id)
        }
    }
}

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.unprocessedNotifications.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = ProHubColors.Text) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProHubColors.Bg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ProHubColors.Bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Recent Notifications",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ProHubColors.Text2,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (notifications.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Notifications,
                        title = "No notifications",
                        description = "Notifications from your apps will appear here when you enable Notification Listener access in Settings.",
                        ctaText = "Open Settings",
                        onCta = { navController.navigate("settings") }
                    )
                }
            } else {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkRead = { viewModel.markProcessed(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationEntity,
    onMarkRead: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
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
                Text(
                    text = notification.appName,
                    color = ProHubColors.Indigo,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = notification.sender,
                    color = ProHubColors.Text,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = notification.content,
                    color = ProHubColors.Text2,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(Date(notification.timestamp)),
                    color = ProHubColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            TextButton(onClick = onMarkRead) {
                Text("Mark Read", color = ProHubColors.Indigo)
            }
        }
    }
}