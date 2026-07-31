package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensitySurfaceVariant,
    onPrimaryContainer = HighDensityPrimary,
    secondary = HighDensitySecondary,
    onSecondary = HighDensityBackground,
    tertiary = HighDensityTertiary,
    background = HighDensityBackground,
    surface = HighDensitySurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onBackground = HighDensityTextPrimary,
    onSurface = HighDensityTextPrimary,
    onSurfaceVariant = HighDensityTextMuted,
    error = RoseError
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityLightSurfaceVariant,
    onPrimaryContainer = HighDensityPrimary,
    secondary = HighDensitySecondary,
    onSecondary = HighDensityLightBackground,
    tertiary = HighDensityTertiary,
    background = HighDensityLightBackground,
    surface = HighDensityLightSurface,
    surfaceVariant = HighDensityLightSurfaceVariant,
    onBackground = HighDensityLightTextPrimary,
    onSurface = HighDensityLightTextPrimary,
    onSurfaceVariant = HighDensityLightTextSecondary,
    error = RoseError
)

@Composable
fun SettingsManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve app's visual identity across devices
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
