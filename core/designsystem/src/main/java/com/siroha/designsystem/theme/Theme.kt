package com.siroha.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color(0xFFFFFFFF),
    background = MicaLightBase,
    surface = MicaLightElevated,
    onBackground = OnMicaLight,
    onSurface = OnMicaLight,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = Color(0xFF00304D),
    background = MicaDarkBase,
    surface = MicaDarkElevated,
    onBackground = OnMicaDark,
    onSurface = OnMicaDark,
    error = ErrorRed
)

/**
 * Fluent-specific tokens Material3's ColorScheme has no slot for: acrylic
 * overlay tint, blur radius, and the taskbar's dedicated translucent chrome
 * color (which intentionally differs from `surface`).
 */
data class FluentTokens(
    val acrylicOverlay: Color,
    val taskbarChrome: Color,
    val divider: Color,
    val blurRadius: Dp,
    val cornerRadius: Win11CornerRadius
)

val LocalFluentTokens = compositionLocalOf<FluentTokens> {
    error("FluentTokens not provided — wrap content in Win11LauncherTheme")
}

object Win11LauncherThemeDefaults {
    @Composable
    @ReadOnlyComposable
    fun tokens(): FluentTokens = LocalFluentTokens.current
}

@Composable
fun Win11LauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val tokens = if (darkTheme) {
        FluentTokens(
            acrylicOverlay = AcrylicDarkOverlay,
            taskbarChrome = TaskbarDark,
            divider = DividerDark,
            blurRadius = 40.dp,
            cornerRadius = DefaultWin11CornerRadius
        )
    } else {
        FluentTokens(
            acrylicOverlay = AcrylicLightOverlay,
            taskbarChrome = TaskbarLight,
            divider = DividerLight,
            blurRadius = 40.dp,
            cornerRadius = DefaultWin11CornerRadius
        )
    }

    CompositionLocalProvider(LocalFluentTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Win11Typography,
            shapes = Win11Shapes,
            content = content
        )
    }
}
