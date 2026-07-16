package com.prohub.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ProHubColors.Indigo,
    secondary = ProHubColors.Purple,
    tertiary = ProHubColors.Green,
    background = ProHubColors.Bg,
    surface = ProHubColors.Surface,
    onPrimary = ProHubColors.Text,
    onSecondary = ProHubColors.Text2,
    onBackground = ProHubColors.Text,
    onSurface = ProHubColors.Text,
    error = ProHubColors.Red,
    outline = ProHubColors.Border
)

@Composable
fun ProHubTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ProHubColors.Bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProHubTypography,
        content = content
    )
}
