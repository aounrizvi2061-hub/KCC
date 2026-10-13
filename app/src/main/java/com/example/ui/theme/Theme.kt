package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KccColorScheme = darkColorScheme(
    primary = KccGoldPrimary,
    onPrimary = Color(0xFF1E1602),
    primaryContainer = KccGoldContainer,
    onPrimaryContainer = KccOnGoldContainer,
    secondary = KccGoldLight,
    onSecondary = Color(0xFF1E1602),
    tertiary = KccGoldDark,
    background = KccBlackBackground,
    onBackground = KccTextWhite,
    surface = KccSurfaceDark,
    onSurface = KccTextWhite,
    surfaceVariant = KccSurfaceVariantDark,
    onSurfaceVariant = KccTextSecondary,
    outline = KccBorderDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KccColorScheme,
        typography = Typography,
        content = content
    )
}
