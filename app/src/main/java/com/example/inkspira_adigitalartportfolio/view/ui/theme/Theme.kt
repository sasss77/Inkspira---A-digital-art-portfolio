package com.example.inkspira_adigitalartportfolio.view.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Inkspira Dark Color Scheme
private val InkspiraDarkColorScheme = darkColorScheme(
    primary = InkspiraPrimary,           // Color(0xFF8B5CF6)
    secondary = InkspiraSecondary,       // Color(0xFFEC4899)
    tertiary = InkspiraTertiary,         // Color(0xFF06B6D4)
    background = DeepDarkBlue,           // Color(0xFF0F0F23)
    surface = DarkNavy,                  // Color(0xFF1A1A2E)
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = DarkerBlue,         // Color(0xFF16213E)
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    outline = InkspiraPrimary.copy(alpha = 0.4f),
    outlineVariant = Color.White.copy(alpha = 0.4f)
)

// Light Color Scheme (fallback)
private val InkspiraLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun InkspiraTheme(
    darkTheme: Boolean = true, // Default to dark theme
    dynamicColor: Boolean = false, // Disable for brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> InkspiraDarkColorScheme
        else -> InkspiraLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InkspiraTypography,
        content = content
    )
}

// Backward compatibility for existing activities
@Composable
fun InkspiraDarkTheme(content: @Composable () -> Unit) {
    InkspiraTheme(
        darkTheme = true,
        dynamicColor = false,
        content = content
    )
}
