package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyberCyan,
    secondary = CyberBlue,
    onSecondary = Color.White,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = CyberPurple,
    onTertiary = Color.White,
    background = CyberBgDark,
    onBackground = TextPrimary,
    surface = CyberSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberCardBorder,
    error = CyberRed,
    onError = Color.White,
  )

@Composable
fun CyberShieldTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CyberDarkColorScheme,
    typography = Typography,
    content = content,
  )
}

