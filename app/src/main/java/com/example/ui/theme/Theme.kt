package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaDarkColorScheme = darkColorScheme(
    primary = ZoyaCyan,
    onPrimary = NovaObsidian,
    primaryContainer = NovaDarkElevated,
    onPrimaryContainer = ZoyaCyanBright,
    secondary = ZoyaVioletBright,
    onSecondary = NovaObsidian,
    secondaryContainer = NovaDarkElevated,
    onSecondaryContainer = ZoyaVioletBright,
    tertiary = ZoyaElectricBlue,
    onTertiary = NovaObsidian,
    background = NovaObsidian,
    onBackground = TextPrimary,
    surface = NovaDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = NovaCardGlass,
    onSurfaceVariant = TextSecondary,
    error = ZoyaCoral,
    onError = TextPrimary,
    outline = NovaBorderGlow
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NovaObsidian.toArgb()
                window.navigationBarColor = NovaObsidian.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
