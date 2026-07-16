package com.prohub.assistant.ui.screens.todos

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.prohub.assistant.data.db.TodoEntity
import com.prohub.assistant.ui.animation.FadeInUpAnimation
import com.prohub.assistant.ui.components.EmptyState
import com.prohub.assistant.ui.components.IconBadge
import com.prohub.assistant.ui.theme.ProHubColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    navController: NavController,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingTodos by viewModel.pendingTodos.collectAsState(initial = emptyList())
    val completedTodos by viewModel.completedTodos.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        viewModel.clearMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks", color = ProHubColors.Text) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ProHubColors.Text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProHubColors.Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ProHubColors.Indigo
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = ProHubColors.Text)
            }
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
                FadeInUpAnimation(visible = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        IconBadge(icon = Icons.Default.Assignment, color = ProHubColors.Indigo, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Pending (${pendingTodos.size})",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ProHubColors.Text,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (pendingTodos.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Assignment,
                        title = "No pending tasks",
                        description = "You're all caught up! Add your first task to get started.",
                        ctaText = "Add Task",
                        onCta = { showAddDialog = true }
                    )
                }
            } else {
                items(
                    items = pendingTodos,
                    key = { it.id }
                ) { todo ->
                    TodoItem(
                        todo = todo,
                        onToggle = { viewModel.toggleComplete(todo) },
                        onDelete = { viewModel.delete(todo) }
                    )
                }
            }

            item {
                FadeInUpAnimation(visible = true, delayMillis = 100) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(icon = Icons.Default.CheckCircle, color = ProHubColors.Green, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Completed (${completedTodos.size})",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ProHubColors.Text,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(onClick = { showCompleted = !showCompleted }) {
                            Text(
                                if (showCompleted) "Hide" else "Show",
                                color = ProHubColors.Indigo
                            )
                        }
                    }
                }
            }

            if (showCompleted && completedTodos.isNotEmpty()) {
                items(
                    items = completedTodos,
                    key = { it.id }
                ) { todo ->
                    TodoItem(
                        todo = todo,
                        onToggle = { viewModel.toggleComplete(todo) },
                        onDelete = { viewModel.delete(todo) }
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Task", color = ProHubColors.Text) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedBorderColor = ProHubColors.Indigo,
                            unfocusedBorderColor = ProHubColors.Border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Date picker button
                    OutlinedTextField(
                        value = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date", tint = ProHubColors.Indigo)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedBorderColor = ProHubColors.Indigo,
                            unfocusedBorderColor = ProHubColors.Border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Time picker button
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Time") },
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick time", tint = ProHubColors.Indigo)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedBorderColor = ProHubColors.Indigo,
                            unfocusedBorderColor = ProHubColors.Border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            val dateStr = selectedDateMillis?.let { dateFormatter.format(Date(it)) }
                            viewModel.addTodo(
                                TodoEntity(
                                    id = UUID.randomUUID().toString(),
                                    title = newTitle.trim(),
                                    dueDate = dateStr,
                                    dueTime = selectedTime.ifBlank { null }
                                )
                            )
                            newTitle = ""
                            selectedDateMillis = null
                            selectedTime = ""
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

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = ProHubColors.Indigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = ProHubColors.Text2)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK", color = ProHubColors.Indigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = ProHubColors.Text2)
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun TodoItem(
    todo: TodoEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var isChecked by remember { mutableStateOf(todo.completed) }
    var isAnimating by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { isAnimating = false }
    )

    FadeInUpAnimation(visible = visible, durationMillis = 350) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        isAnimating = true
                        isChecked = !isChecked
                        onToggle()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ProHubColors.Green,
                        uncheckedColor = ProHubColors.Border
                    ),
                    modifier = Modifier.scale(scale)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = todo.title,
                        color = if (todo.completed) ProHubColors.Muted else ProHubColors.Text,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (todo.dueDate != null) {
                        Text(
                            text = "Due: ${todo.dueDate} ${todo.dueTime ?: ""}",
                            color = ProHubColors.Text2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ProHubColors.Red)
                }
            }
        }
    }
}