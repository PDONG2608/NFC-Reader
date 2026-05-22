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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary, // #D0BCFF
    onPrimary = ElectricBlue, // #381E72
    secondary = CyanAccent, // #CAC4D0
    onSecondary = Color.Black,
    tertiary = CyanPrimary,
    background = DarkBackground, // #1C1B1F
    surface = DarkSurface, // #211F26
    onBackground = TextPrimary, // #E6E1E5
    onSurface = TextPrimary, // #E6E1E5
    surfaceVariant = DarkSurfaceVariant, // #2B2930
    onSurfaceVariant = TextSecondary, // #CAC4D0
    outline = ElegantBorder, // #49454F
    error = ErrorRed,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7), // Sky 600
    onPrimary = Color.White,
    secondary = Color(0xFF2563EB), // Blue 600
    onSecondary = Color.White,
    tertiary = Color(0xFF0F766E), // Teal 700
    background = Color(0xFFF8FAFC), // Slate 50
    surface = Color.White,
    onBackground = Color(0xFF0F172A), // Slate 900
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0), // Slate 200
    onSurfaceVariant = Color(0xFF475569), // Slate 600
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to lock in the terminal styling which is much more atmospheric and premium!
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> DarkColorScheme // Force dark theme by default or lock it in because Terminal looks incredible! Wait, to give a great experience we can respect darkTheme or use DarkColorScheme as primary focus. Let's support darkTheme but default to DarkColorScheme if requested.
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
