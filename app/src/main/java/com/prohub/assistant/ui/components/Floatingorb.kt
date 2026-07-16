package com.prohub.assistant.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A soft, gently bobbing decorative orb — inspired by the "floating character"
 * motion category, distilled to pure abstract shape so it fits ProHub's
 * geometric brand language instead of a literal illustrated character.
 * Good for decorating hero headers, empty states, or splash screens.
 */
@Composable
fun FloatingOrb(
    size: Dp = 90.dp,
    color: Color = Color.White.copy(alpha = 0.08f),
    floatRangeDp: Dp = 10.dp,
    durationMillis: Int = 3200
) {
    val transition = rememberInfiniteTransition(label = "orb_float")
    val offsetY by transition.animateFloat(
        initialValue = -floatRangeDp.value,
        targetValue = floatRangeDp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_offset"
    )

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}