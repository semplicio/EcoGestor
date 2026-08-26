package br.com.autombot.ecogestor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EcoGreen = Color(0xFF43A047)
val EcoGreenLight = Color(0xFF79C843)
val EcoTeal = Color(0xFF0B6B68)
val EcoDark = Color(0xFF073B4C)
val EcoSurface = Color(0xFFF6FAF7)
val EcoSurfaceAlt = Color(0xFFEAF4ED)
val EcoWarning = Color(0xFFF4A261)

private val LightColors = lightColorScheme(
    primary = EcoGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3D5),
    onPrimaryContainer = EcoDark,
    secondary = EcoTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4EFEC),
    onSecondaryContainer = EcoDark,
    background = EcoSurface,
    onBackground = Color(0xFF14231D),
    surface = Color.White,
    onSurface = Color(0xFF14231D),
    surfaceVariant = EcoSurfaceAlt,
    onSurfaceVariant = Color(0xFF4A5A52),
    outline = Color(0xFFB8C8BF)
)

private val DarkColors = darkColorScheme(
    primary = EcoGreenLight,
    onPrimary = Color(0xFF0E3117),
    secondary = Color(0xFF67D5CF),
    onSecondary = Color(0xFF003735),
    background = Color(0xFF0E1713),
    onBackground = Color(0xFFE1ECE5),
    surface = Color(0xFF16211C),
    onSurface = Color(0xFFE1ECE5),
    surfaceVariant = Color(0xFF203129),
    onSurfaceVariant = Color(0xFFC2D2C8)
)

@Composable
fun EcoGestorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
