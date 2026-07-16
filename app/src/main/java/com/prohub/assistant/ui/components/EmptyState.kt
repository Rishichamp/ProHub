package com.prohub.assistant.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.prohub.assistant.ui.theme.ProHubColors

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
    accentColor: androidx.compose.ui.graphics.Color = ProHubColors.Indigo
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500, easing = EaseOutCubic)) +
                slideInVertically(animationSpec = tween(500, easing = EaseOutCubic)) { it / 3 } +
                scaleIn(initialScale = 0.9f, animationSpec = tween(500, easing = EaseOutCubic)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBadge(icon = icon, color = accentColor, size = 72.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = ProHubColors.Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = ProHubColors.Text2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (ctaText != null && onCta != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCta,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(ctaText)
                }
            }
        }
    }
}