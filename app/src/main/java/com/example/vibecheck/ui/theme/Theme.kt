package com.example.vibecheck.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkCharcoal,
    surface = DarkCharcoal,
    onBackground = Color.White,
    onSurface = Color.White
)

private val CrimsonColorScheme = darkColorScheme(
    primary = CrimsonRed,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkCharcoal,
    surface = DarkCharcoal,
    onBackground = Color.White,
    onSurface = Color.White
)

private val GoldenColorScheme = darkColorScheme(
    primary = GoldenYellow,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkCharcoal,
    surface = DarkCharcoal,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun VibeCheckTheme(
    themeMode: String = "Cyan",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "Crimson" -> CrimsonColorScheme
        "Golden" -> GoldenColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
