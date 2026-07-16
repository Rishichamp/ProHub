package com.prohub.assistant.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────
// FADE ANIMATIONS
// ─────────────────────────────────────────────────────────────

@Composable
fun FadeInAnimation(
    visible: Boolean,
    durationMillis: Int = 300,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis)),
        exit = fadeOut(animationSpec = tween(durationMillis / 2)),
        content = content
    )
}

@Composable
fun FadeInUpAnimation(
    visible: Boolean,
    durationMillis: Int = 400,
    delayMillis: Int = 0,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis, delayMillis, easing = EaseOutCubic)
        ) + slideInVertically(
            animationSpec = tween(durationMillis, delayMillis, easing = EaseOutCubic),
            initialOffsetY = { it / 4 }
        ),
        exit = fadeOut(animationSpec = tween(durationMillis / 2)) + slideOutVertically(
            animationSpec = tween(durationMillis / 2),
            targetOffsetY = { it / 4 }
        ),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────
// SCALE ANIMATIONS
// ─────────────────────────────────────────────────────────────

@Composable
fun ScaleInAnimation(
    visible: Boolean,
    durationMillis: Int = 300,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMillis, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(durationMillis)),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMillis / 2, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(durationMillis / 2)),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────
// STAGGERED LIST ANIMATION
// ─────────────────────────────────────────────────────────────

@Composable
fun <T> AnimatedListItem(
    item: T,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val delay = index * 50

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400, delay, easing = EaseOutCubic)
    )

    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(400, delay, easing = EaseOutCubic)
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .graphicsLayer { translationY = offsetY }
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────
// PULSE ANIMATION (for attention)
// ─────────────────────────────────────────────────────────────

@Composable
fun PulseAnimation(
    targetValue: Float = 1.15f,
    content: @Composable (Modifier) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    content(Modifier.scale(scale))
}

// ─────────────────────────────────────────────────────────────
// SHAKE ANIMATION (for errors)
// ─────────────────────────────────────────────────────────────

@Composable
fun ShakeAnimation(
    trigger: Int,
    content: @Composable (Modifier) -> Unit
) {
    val offsetX by animateFloatAsState(
        targetValue = if (trigger > 0) 10f else 0f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        finishedListener = { /* reset handled by trigger */ }
    )

    content(Modifier.offset(x = offsetX.dp))
}

// ─────────────────────────────────────────────────────────────
// COUNT-UP ANIMATION (for numbers)
// ─────────────────────────────────────────────────────────────

@Composable
fun AnimatedCounter(
    targetValue: Int,
    durationMillis: Int = 800,
    content: @Composable (Int) -> Unit
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis, easing = EaseOutCubic),
        label = "counter"
    )

    content(animatedValue)
}

// ─────────────────────────────────────────────────────────────
// SLIDE IN FROM SIDE
// ─────────────────────────────────────────────────────────────

@Composable
fun SlideInFromRight(
    visible: Boolean,
    durationMillis: Int = 300,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(durationMillis, easing = EaseOutCubic),
            initialOffsetX = { it }
        ) + fadeIn(animationSpec = tween(durationMillis)),
        exit = slideOutHorizontally(
            animationSpec = tween(durationMillis / 2, easing = EaseInCubic),
            targetOffsetX = { it }
        ) + fadeOut(animationSpec = tween(durationMillis / 2)),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────
// EXPAND/COLLAPSE
// ─────────────────────────────────────────────────────────────

@Composable
fun ExpandAnimation(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = tween(200, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(200)),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────
// LOADING DOTS (typing indicator)
// ─────────────────────────────────────────────────────────────

@Composable
fun LoadingDots(
    dotColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_y_$index"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { translationY = offsetY }
                    .background(dotColor, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// RIPPLE EFFECT (for button presses)
// ─────────────────────────────────────────────────────────────

@Composable
fun PressableScale(
    onPress: () -> Unit,
    content: @Composable (Modifier, Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when {
                            event.changes.any { it.pressed } -> isPressed = true
                            else -> {
                                if (isPressed) {
                                    isPressed = false
                                    onPress()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        content(Modifier, isPressed)
    }
}

// ─────────────────────────────────────────────────────────────
// PAGE TRANSITION
// ─────────────────────────────────────────────────────────────

@Composable
fun PageTransition(
    targetState: Int,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInHorizontally { width -> width * direction } + fadeIn()) togetherWith
            (slideOutHorizontally { width -> width * -direction } + fadeOut())
        },
        label = "page_transition"
    ) { page ->
        content(page)
    }
}
