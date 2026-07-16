@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.prohub.assistant.ui.screens.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.prohub.assistant.ui.components.EmptyState
import com.prohub.assistant.ui.theme.ProHubColors
import java.util.*

@Composable
fun TimetableScreen(navController: NavController) {
    var entries by remember { mutableStateOf(listOf<TimetableEntry>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTime by remember { mutableStateOf("") }
    var newActivity by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("work") }

    val typeColors = mapOf(
        "work" to ProHubColors.Indigo,
        "study" to ProHubColors.Purple,
        "exercise" to ProHubColors.Green,
        "meal" to ProHubColors.Gold,
        "sleep" to ProHubColors.Blue,
        "social" to androidx.compose.ui.graphics.Color(0xFFEC4899),
        "commute" to androidx.compose.ui.graphics.Color(0xFFF97316),
        "free" to ProHubColors.Muted
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable", color = ProHubColors.Text) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProHubColors.Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ProHubColors.Indigo
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry", tint = ProHubColors.Text)
            }
        },
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
                    "Today's Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ProHubColors.Text2,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (entries.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No schedule yet",
                        description = "Plan your day by adding activities to your timetable.",
                        ctaText = "Add Entry",
                        onCta = { showAddDialog = true }
                    )
                }
            } else {
                items(entries.sortedBy { it.time }, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            typeColors[entry.type] ?: ProHubColors.Border
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.time,
                                    color = ProHubColors.Indigo,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = entry.activity,
                                    color = ProHubColors.Text,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = entry.type.replaceFirstChar { it.uppercase() },
                                    color = typeColors[entry.type] ?: ProHubColors.Text2,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                entries = entries.filter { it.id != entry.id }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ProHubColors.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Entry", color = ProHubColors.Text) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Time (e.g. 09:00)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedBorderColor = ProHubColors.Indigo,
                            unfocusedBorderColor = ProHubColors.Border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newActivity,
                        onValueChange = { newActivity = it },
                        label = { Text("Activity") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedBorderColor = ProHubColors.Indigo,
                            unfocusedBorderColor = ProHubColors.Border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = newType.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ProHubColors.Text,
                                unfocusedTextColor = ProHubColors.Text
                            )
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("work", "study", "exercise", "meal", "sleep", "social", "commute", "free").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                    onClick = { newType = type; expanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTime.isNotBlank() && newActivity.isNotBlank()) {
                            entries = entries + TimetableEntry(
                                id = UUID.randomUUID().toString(),
                                time = newTime,
                                activity = newActivity,
                                type = newType
                            )
                            newTime = ""
                            newActivity = ""
                            newType = "work"
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProHubColors.Indigo)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = ProHubColors.Text2)
                }
            },
            containerColor = ProHubColors.Card
        )
    }
}

data class TimetableEntry(
    val id: String,
    val time: String,
    val activity: String,
    val type: String
)