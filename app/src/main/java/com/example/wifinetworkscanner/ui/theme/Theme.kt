package com.example.wifinetworkscanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = wifiLightPrimary,
    onPrimary = wifiLightOnPrimary,
    primaryContainer = wifiLightPrimaryContainer,
    onPrimaryContainer = wifiLightOnPrimaryContainer,
    secondary = wifiLightSecondary,
    onSecondary = wifiLightOnSecondary,
    secondaryContainer = wifiLightSecondaryContainer,
    onSecondaryContainer = wifiLightOnSecondaryContainer,
    tertiary = wifiLightTertiary,
    onTertiary = wifiLightOnTertiary,
    tertiaryContainer = wifiLightTertiaryContainer,
    onTertiaryContainer = wifiLightOnTertiaryContainer,
    error = wifiLightError,
    onError = wifiLightOnError,
    background = wifiLightBackground,
    onBackground = wifiLightOnBackground,
    surface = wifiLightSurface,
    onSurface = wifiLightOnSurface,
    surfaceVariant = wifiLightSurfaceVariant,
    onSurfaceVariant = wifiLightOnSurfaceVariant,
    outline = wifiLightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = wifiDarkPrimary,
    onPrimary = wifiDarkOnPrimary,
    primaryContainer = wifiDarkPrimaryContainer,
    onPrimaryContainer = wifiDarkOnPrimaryContainer,
    secondary = wifiDarkSecondary,
    onSecondary = wifiDarkOnSecondary,
    secondaryContainer = wifiDarkSecondaryContainer,
    onSecondaryContainer = wifiDarkOnSecondaryContainer,
    tertiary = wifiDarkTertiary,
    onTertiary = wifiDarkOnTertiary,
    tertiaryContainer = wifiDarkTertiaryContainer,
    onTertiaryContainer = wifiDarkOnTertiaryContainer,
    error = wifiDarkError,
    onError = wifiDarkOnError,
    background = wifiDarkBackground,
    onBackground = wifiDarkOnBackground,
    surface = wifiDarkSurface,
    onSurface = wifiDarkOnSurface,
    surfaceVariant = wifiDarkSurfaceVariant,
    onSurfaceVariant = wifiDarkOnSurfaceVariant,
    outline = wifiDarkOutline
)

@Composable
fun WifiNetworkScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = resolveColorScheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        context = context
    )

    ConfigureSystemBars(
        colorScheme = colorScheme,
        darkTheme = darkTheme
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun ConfigureSystemBars(
    colorScheme: ColorScheme,
    darkTheme: Boolean
) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect

            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
}

private fun resolveColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    context: android.content.Context
): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }
}