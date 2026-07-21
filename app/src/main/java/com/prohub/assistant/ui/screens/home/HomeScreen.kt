package com.prohub.assistant.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.prohub.assistant.ui.animation.AnimatedCounter
import com.prohub.assistant.ui.animation.FadeInUpAnimation
import com.prohub.assistant.ui.components.IconBadge
import com.prohub.assistant.ui.components.premiumGradientBackground
import com.prohub.assistant.ui.theme.ProHubColors
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val greeting = getGreeting()
    val currentTime = remember { mutableStateOf(Calendar.getInstance().time) }
    val pendingCount by viewModel.pendingTaskCount.collectAsState()
    val activeMinutes by viewModel.activeMinutesToday.collectAsState()
    val userName by viewModel.userName.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime.value = Calendar.getInstance().time
        }
    }

    val cal = Calendar.getInstance().apply { time = currentTime.value }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProHubColors.Bg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero gradient header
        FadeInUpAnimation(visible = true, delayMillis = 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .premiumGradientBackground(
                        colorA = ProHubColors.Indigo2,
                        colorB = ProHubColors.Purple
                    )
                    .padding(20.dp)
            ) {
                // Gently floating decorative orb, echoes the app's launcher icon motif
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                ) {
                    com.prohub.assistant.ui.components.FloatingOrb(size = 90.dp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userName.ifBlank { "Welcome back" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Metric cards row
        FadeInUpAnimation(visible = true, delayMillis = 100) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.Assignment,
                    label = "Tasks",
                    value = pendingCount,
                    color = ProHubColors.Indigo,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("todos") }
                )
                MetricCard(
                    icon = Icons.Default.FitnessCenter,
                    label = "Exercise",
                    value = activeMinutes,
                    suffix = "m",
                    color = ProHubColors.Green,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("fitness") }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Coming up next section
        FadeInUpAnimation(visible = true, delayMillis = 200) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.CalendarMonth, color = ProHubColors.Blue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Coming Up Next",
                            style = MaterialTheme.typography.titleMedium,
                            color = ProHubColors.Text,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add entries in Schedule to see them here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ProHubColors.Text2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        FadeInUpAnimation(visible = true, delayMillis = 300) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        color = ProHubColors.Text,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Mic,
                            label = "Voice",
                            color = ProHubColors.Purple,
                            onClick = {
                                com.prohub.assistant.service.VoiceEntryBridge.requestAutoStart()
                                navController.navigate("ai")
                            }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Assignment,
                            label = "Task",
                            color = ProHubColors.Indigo,
                            onClick = { navController.navigate("todos") }
                        )
                        QuickActionButton(
                            icon = Icons.Default.SmartToy,
                            label = "AI",
                            color = ProHubColors.Blue,
                            onClick = { navController.navigate("ai") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: Int,
    suffix: String = "",
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            IconBadge(icon = icon, color = color)
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedCounter(targetValue = value) { animatedValue ->
                Text(
                    text = "$animatedValue$suffix",
                    style = MaterialTheme.typography.headlineMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = ProHubColors.Text2
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { isPressed = false }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Surface(
            onClick = {
                isPressed = true
                onClick()
            },
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = color)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = ProHubColors.Text2,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        hour < 21 -> "Good Evening"
        else -> "Good Night"
    }
}