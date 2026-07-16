package com.prohub.assistant.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.prohub.assistant.ui.theme.ProHubColors

/**
 * A custom animated toggle switch: the thumb slides with a springy bounce and
 * the track smoothly crossfades color, rather than the abrupt on/off snap of
 * the stock Material Switch. Inspired by the "animated toggle" motion
 * category, built fresh for ProHub's own color language.
 */
@Composable
fun PremiumToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = ProHubColors.Indigo,
    inactiveColor: Color = ProHubColors.Border
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        label = "track_color"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "thumb_offset"
    )

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .semantics { role = Role.Switch }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .padding(start = thumbOffset)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}