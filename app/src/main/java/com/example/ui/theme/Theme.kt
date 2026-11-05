package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AcademyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = AcademyBlueSecondary,
    onPrimaryContainer = Color.White,
    secondary = AmberGold,
    onSecondary = Color.White,
    secondaryContainer = AmberGoldLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = AcademyBlueTertiary,
    onTertiary = Color.White,
    background = SlateBackground,
    onBackground = SlateTextPrimary,
    surface = SlateSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = AcademyBlueTertiary,
    onPrimary = Color.White,
    primaryContainer = AcademyBluePrimary,
    onPrimaryContainer = Color.White,
    secondary = AmberGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = AmberGoldLight,
    tertiary = AcademyBlueSecondary,
    onTertiary = Color.White,
    background = DarkSlateBackground,
    onBackground = DarkSlateTextPrimary,
    surface = DarkSlateSurface,
    onSurface = DarkSlateTextPrimary,
    surfaceVariant = DarkSlateSurfaceVariant,
    onSurfaceVariant = DarkSlateTextSecondary,
    outline = Color(0xFF475569)
)

@Composable
fun SchoolManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
