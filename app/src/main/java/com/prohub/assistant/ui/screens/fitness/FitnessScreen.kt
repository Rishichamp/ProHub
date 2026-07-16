package com.prohub.assistant.ui.screens.fitness

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.prohub.assistant.data.db.FitnessEntry
import com.prohub.assistant.data.db.FitnessGoal
import com.prohub.assistant.data.repository.FitnessStats
import com.prohub.assistant.ui.theme.ProHubColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(
    navController: NavController,
    viewModel: FitnessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState(initial = emptyList())
    val activeGoals by viewModel.activeGoals.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today", "Week", "Goals", "History")

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        viewModel.clearMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fitness Dashboard", color = ProHubColors.Text) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProHubColors.Bg),
                actions = {
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(Icons.Default.Flag, contentDescription = "Goals", tint = ProHubColors.Indigo)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Workout", tint = ProHubColors.Indigo)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ProHubColors.Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ProHubColors.Surface,
                contentColor = ProHubColors.Indigo
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) ProHubColors.Indigo else ProHubColors.Text2) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TodayTab(uiState.todayStats, todayEntries, activeGoals, viewModel)
                1 -> WeekTab(uiState.weekStats, viewModel)
                2 -> GoalsTab(activeGoals, uiState.todayStats, viewModel)
                3 -> HistoryTab(todayEntries, viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                viewModel.addEntry(entry)
                showAddDialog = false
            }
        )
    }

    if (showGoalDialog) {
        AddGoalDialog(
            onDismiss = { showGoalDialog = false },
            onAdd = { goal ->
                viewModel.addGoal(goal)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun TodayTab(
    stats: FitnessStats,
    entries: List<FitnessEntry>,
    goals: List<FitnessGoal>,
    viewModel: FitnessViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(icon = "Steps", label = "Steps", value = "${stats.steps}", modifier = Modifier.weight(1f), color = ProHubColors.Green)
            StatCard(icon = "Cal", label = "Calories", value = "${stats.calories}", modifier = Modifier.weight(1f), color = ProHubColors.Gold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(icon = "Dist", label = "Distance", value = "${String.format("%.1f", stats.distanceKm)} km", modifier = Modifier.weight(1f), color = ProHubColors.Blue)
            StatCard(icon = "Min", label = "Active Min", value = "${stats.activeMinutes}", modifier = Modifier.weight(1f), color = ProHubColors.Purple)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (goals.isNotEmpty()) {
            Text("Goal Progress", style = MaterialTheme.typography.titleLarge, color = ProHubColors.Text, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            goals.forEach { goal ->
                val progress = viewModel.getGoalProgress(goal, stats)
                GoalProgressCard(goal, progress, stats)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Today's Workouts", style = MaterialTheme.typography.titleLarge, color = ProHubColors.Text, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        if (entries.isEmpty()) {
            EmptyFitnessState()
        } else {
            entries.forEach { entry ->
                WorkoutCard(entry, onDelete = { viewModel.deleteEntry(entry) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatCard(icon: String, label: String, value: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = ProHubColors.Text2)
        }
    }
}

@Composable
private fun GoalProgressCard(goal: FitnessGoal, progress: Float, stats: FitnessStats) {
    val currentValue = when (goal.goalType) {
        "steps" -> stats.steps
        "calories" -> stats.calories
        "distance" -> stats.distanceKm.toInt()
        "activeMinutes" -> stats.activeMinutes
        "workouts" -> stats.workoutCount
        else -> 0
    }
    val color = when (goal.goalType) {
        "steps" -> ProHubColors.Green
        "calories" -> ProHubColors.Gold
        "distance" -> ProHubColors.Blue
        "activeMinutes" -> ProHubColors.Purple
        else -> ProHubColors.Indigo
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.goalType.replaceFirstChar { it.uppercase() }, color = ProHubColors.Text, fontWeight = FontWeight.Medium)
                Text("$currentValue / ${goal.targetValue}", color = ProHubColors.Text2, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = ProHubColors.Surface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${(progress * 100).toInt()}% complete", color = ProHubColors.Text2, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WorkoutCard(entry: FitnessEntry, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.workoutType.replaceFirstChar { it.uppercase() }, color = ProHubColors.Text, fontWeight = FontWeight.Medium)
                Text("${entry.durationMinutes} min - ${entry.calories} cal - ${entry.steps} steps", color = ProHubColors.Text2, style = MaterialTheme.typography.bodySmall)
                if (entry.notes.isNotBlank()) {
                    Text(entry.notes, color = ProHubColors.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(dateFormat.format(Date(entry.date)), color = ProHubColors.Text2, style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ProHubColors.Red)
            }
        }
    }
}

@Composable
private fun EmptyFitnessState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gym", fontSize = 48.sp, color = ProHubColors.Indigo)
        Spacer(modifier = Modifier.height(12.dp))
        Text("No workouts today", color = ProHubColors.Text2, style = MaterialTheme.typography.bodyLarge)
        Text("Tap + to log your first workout!", color = ProHubColors.Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onAdd: (FitnessEntry) -> Unit
) {
    var workoutType by remember { mutableStateOf("walking") }
    var duration by remember { mutableStateOf("30") }
    var steps by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("walking", "running", "cycling", "gym", "yoga", "swimming", "other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Workout", color = ProHubColors.Text) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = workoutType.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workout Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                onClick = { workoutType = type; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Steps (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Distance km (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = heartRate,
                    onValueChange = { heartRate = it },
                    label = { Text("Avg Heart Rate (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entry = FitnessEntry(
                        workoutType = workoutType,
                        durationMinutes = duration.toIntOrNull() ?: 0,
                        steps = steps.toIntOrNull() ?: 0,
                        calories = calories.toIntOrNull() ?: 0,
                        distanceKm = distance.toFloatOrNull() ?: 0f,
                        heartRateAvg = heartRate.toIntOrNull() ?: 0,
                        notes = notes
                    )
                    onAdd(entry)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProHubColors.Indigo)
            ) { Text("Log Workout") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ProHubColors.Text2) }
        },
        containerColor = ProHubColors.Card
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (FitnessGoal) -> Unit
) {
    var goalType by remember { mutableStateOf("steps") }
    var targetValue by remember { mutableStateOf("10000") }
    var period by remember { mutableStateOf("daily") }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("steps", "calories", "distance", "activeMinutes", "workouts")
    val periods = listOf("daily", "weekly", "monthly")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Goal", color = ProHubColors.Text) },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = goalType.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Goal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                onClick = { goalType = type; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target Value") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProHubColors.Text,
                        unfocusedTextColor = ProHubColors.Text
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    periods.forEach { p ->
                        FilterChip(
                            selected = period == p,
                            onClick = { period = p },
                            label = { Text(p.replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(FitnessGoal(
                        goalType = goalType,
                        targetValue = targetValue.toIntOrNull() ?: 10000,
                        period = period
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProHubColors.Indigo)
            ) { Text("Set Goal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ProHubColors.Text2) }
        },
        containerColor = ProHubColors.Card
    )
}

@Composable
private fun WeekTab(stats: FitnessStats, viewModel: FitnessViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Weekly Summary",
            style = MaterialTheme.typography.headlineSmall,
            color = ProHubColors.Text,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        WeeklyStatRow("Total Steps", "${stats.steps}", ProHubColors.Green)
        WeeklyStatRow("Total Calories", "${stats.calories}", ProHubColors.Gold)
        WeeklyStatRow("Total Distance", "${String.format("%.1f", stats.distanceKm)} km", ProHubColors.Blue)
        WeeklyStatRow("Active Minutes", "${stats.activeMinutes}", ProHubColors.Purple)
        WeeklyStatRow("Workouts", "${stats.workoutCount}", ProHubColors.Indigo)
    }
}

@Composable
private fun WeeklyStatRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = ProHubColors.Text, modifier = Modifier.weight(1f))
            Text(value, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GoalsTab(
    goals: List<FitnessGoal>,
    stats: FitnessStats,
    viewModel: FitnessViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (goals.isEmpty()) {
            EmptyFitnessState()
        } else {
            goals.forEach { goal ->
                val progress = viewModel.getGoalProgress(goal, stats)
                GoalProgressCard(goal, progress, stats)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HistoryTab(entries: List<FitnessEntry>, viewModel: FitnessViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (entries.isEmpty()) {
            item { EmptyFitnessState() }
        } else {
            items(entries, key = { it.id }) { entry ->
                WorkoutCard(entry, onDelete = { viewModel.deleteEntry(entry) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
