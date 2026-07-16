package com.prohub.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.prohub.assistant.ui.theme.ProHubColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * A branded loading indicator: a small satellite node orbits a pulsing central
 * hub, echoing ProHub's own neural-hub logomark instead of a generic spinner.
 * Inspired by the "playful loader" motion category, built as an original,
 * on-brand design rather than reusing any specific reference animation.
 */
@Composable
fun OrbitLoader(
    modifier: Modifier = Modifier,
    color: Color = ProHubColors.Indigo,
    satelliteColor: Color = ProHubColors.Purple
) {
    val transition = rememberInfiniteTransition(label = "orbit_loader")

    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "orbit_angle"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbit_pulse"
    )

    Canvas(modifier = modifier.size(40.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val orbitRadius = size.minDimension * 0.38f
        val hubRadius = size.minDimension * 0.16f * pulse
        val satelliteRadius = size.minDimension * 0.09f

        val rad = Math.toRadians(angle.toDouble())
        val satelliteOffset = Offset(
            x = center.x + orbitRadius * cos(rad).toFloat(),
            y = center.y + orbitRadius * sin(rad).toFloat()
        )

        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = orbitRadius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
        drawCircle(color = color, radius = hubRadius, center = center)
        drawCircle(color = satelliteColor, radius = satelliteRadius, center = satelliteOffset)
    }
}