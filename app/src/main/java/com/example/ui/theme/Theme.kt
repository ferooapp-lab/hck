package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0E3A4A),
    onPrimaryContainer = CyanGlow,
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF33205E),
    onSecondaryContainer = VioletLight,
    tertiary = NeonCoral,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = DeepSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = StrokeOutline
)

private val LightColorScheme = DarkColorScheme // Modern cyber dark styling is default for optimal live screen & map contrast

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
