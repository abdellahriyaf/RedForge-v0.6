package com.redforge.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ForgeDarkColorScheme = darkColorScheme(
    primary = ForgeRed,
    onPrimary = ForgeWhite,
    primaryContainer = ForgeRedDark,
    onPrimaryContainer = ForgeWhite,
    secondary = ForgeGold,
    onSecondary = ForgeBlack,
    background = ForgeBlack,
    onBackground = ForgeWhite,
    surface = ForgeSurface,
    onSurface = ForgeWhite,
    surfaceVariant = ForgeSurfaceHigh,
    onSurfaceVariant = ForgeAsh,
    error = ForgeRedBright,
    onError = ForgeWhite,
    outline = ForgeSurfaceHigh
)

private val ForgeLightColorScheme = lightColorScheme(
    primary = ForgeRed,
    onPrimary = ForgeWhite,
    primaryContainer = Color(0xFFFFDAD8),
    secondary = ForgeGold,
    background = Color(0xFFFAFAFA),
    onBackground = ForgeBlack,
    surface = Color(0xFFFFFFFF),
    onSurface = ForgeBlack,
    error = ForgeRedDark
)

@Composable
fun RedForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    forceDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val useDark = forceDark || darkTheme
    val colorScheme = if (useDark) ForgeDarkColorScheme else ForgeLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDark
                isAppearanceLightNavigationBars = !useDark
            }
            window.setStatusBarColorCompat(colorScheme.background.toArgb())
            window.setNavigationBarColorCompat(colorScheme.background.toArgb())
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = ForgeTypography, content = content)
}

private fun android.view.Window.setStatusBarColorCompat(color: Int) {
    addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = color
}

private fun android.view.Window.setNavigationBarColorCompat(color: Int) {
    addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    navigationBarColor = color
}
