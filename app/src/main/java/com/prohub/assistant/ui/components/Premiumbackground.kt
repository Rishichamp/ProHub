package com.prohub.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * A slowly "breathing" gradient background — the colors don't just sit static,
 * they gently drift between two positions on an infinite loop. Inspired by the
 * "animated background" motion category: subtle, ambient, never distracting.
 *
 * Use anywhere a hero surface (header, splash, empty state) wants some life
 * without pulling focus from content on top of it.
 */
@Composable
fun Modifier.premiumGradientBackground(
    colorA: Color,
    colorB: Color,
    durationMillis: Int = 6000
): Modifier {
    val transition = rememberInfiniteTransition(label = "gradient_breathe")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_t"
    )

    val start = lerp(colorA, colorB, t * 0.3f)
    val end = lerp(colorB, colorA, t * 0.3f)

    return this.background(
        Brush.linearGradient(
            colors = listOf(start, end),
            start = Offset(0f, 0f),
            end = Offset(1000f * (0.6f + t * 0.4f), 1000f)
        )
    )
}