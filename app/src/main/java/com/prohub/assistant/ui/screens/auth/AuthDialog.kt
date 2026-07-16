package com.prohub.assistant.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.prohub.assistant.ui.theme.ProHubColors
import kotlinx.coroutines.delay

@Composable
fun AuthDialog(
    isVisible: Boolean,
    hasPin: Boolean,
    onDismiss: () -> Unit,
    onPinSet: (String) -> Boolean,
    onPinVerify: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var isSettingPin by remember { mutableStateOf(!hasPin) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shakeKey by remember { mutableStateOf(0) }
    var successKey by remember { mutableStateOf(0) }

    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeKey > 0) 10f else 0f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        finishedListener = { shakeKey = 0 }
    )

    val successScale by animateFloatAsState(
        targetValue = if (successKey > 0) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { successKey = 0 }
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = EaseOutCubic)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200, easing = EaseInCubic)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .scale(successScale)
                    .offset(x = shakeOffset.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ProHubColors.Card),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProHubColors.Border)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated lock icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                ProHubColors.Indigo.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = ProHubColors.Indigo,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSettingPin) "Create PIN" else "Enter PIN",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ProHubColors.Text
                    )

                    Text(
                        text = if (isSettingPin) {
                            "Set a 4-digit PIN to protect your settings"
                        } else {
                            "Authentication required to change settings"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProHubColors.Text2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN dots
                    PinDots(
                        pinLength = pin.length,
                        isError = errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN input (hidden)
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { 
                            if (it.length <= 6) {
                                pin = it
                                errorMessage = null
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ProHubColors.Text,
                            unfocusedTextColor = ProHubColors.Text,
                            focusedBorderColor = if (errorMessage != null) ProHubColors.Red else ProHubColors.Indigo,
                            unfocusedBorderColor = if (errorMessage != null) ProHubColors.Red else ProHubColors.Border
                        )
                    )

                    // Error message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ProHubColors.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ProHubColors.Text2
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (pin.length < 4) {
                                    errorMessage = "PIN must be at least 4 digits"
                                    shakeKey++
                                    return@Button
                                }

                                if (isSettingPin) {
                                    if (onPinSet(pin)) {
                                        successKey++
                                        // Auto-dismiss after success animation
                                    } else {
                                        errorMessage = "Failed to set PIN"
                                        shakeKey++
                                    }
                                } else {
                                    if (onPinVerify(pin)) {
                                        successKey++
                                    } else {
                                        errorMessage = "Incorrect PIN"
                                        shakeKey++
                                        pin = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProHubColors.Indigo
                            )
                        ) {
                            Text(if (isSettingPin) "Set PIN" else "Unlock")
                        }
                    }
                }
            }
        }
    }

    // Auto-dismiss on success
    LaunchedEffect(successKey) {
        if (successKey > 0) {
            delay(600)
            onDismiss()
            pin = ""
            errorMessage = null
        }
    }
}

@Composable
private fun PinDots(
    pinLength: Int,
    isError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val isFilled = index < pinLength
            val dotColor = when {
                isError -> ProHubColors.Red
                isFilled -> ProHubColors.Indigo
                else -> ProHubColors.Border
            }

            val dotScale by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0.6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(dotScale)
                    .background(
                        color = if (isFilled) dotColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .then(
                        if (!isFilled) {
                            Modifier.border(2.dp, dotColor, CircleShape)
                        } else Modifier
                    )
            )
        }
    }
}
