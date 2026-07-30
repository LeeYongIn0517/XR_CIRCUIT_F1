package app.yongin.xr_circuit.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light scheme mapped from Figma Circuit Profile (node 1:12).
 *
 * | Role                 | Hex       | Figma use                          |
 * |----------------------|-----------|------------------------------------|
 * | primary              | `#2563EB` | Stat values, Lap Record title      |
 * | secondary            | `#3B82F6` | Selected corner, idle turn codes   |
 * | primaryContainer     | `#EFF6FF` | Lap Record card fill               |
 * | onPrimaryContainer   | `#1D4ED8` | Lap Record time                    |
 * | secondaryContainer   | `#BFDBFE` | Lap Record card border             |
 * | onSurface            | `#0F172A` | "Circuit Profile" title            |
 * | onSurfaceVariant     | `#64748B` | Stat labels                        |
 * | outline              | `#94A3B8` | Year, KEY CORNERS                  |
 * | outlineVariant       | `#E2E8F0` | Stat card borders                  |
 */
private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,
    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Blue200,
    onSecondaryContainer = Blue700,
    tertiary = Blue500,
    onTertiary = Color.White,
    background = Slate100,
    onBackground = Slate900,
    surface = SurfaceWhite,
    onSurface = Slate900,
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate500,
    surfaceContainerHighest = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainer = SurfaceWhite,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainerLowest = SurfaceWhite,
    outline = Slate400,
    outlineVariant = Slate200,
    inverseSurface = Slate900,
    inverseOnSurface = Slate100,
    inversePrimary = Blue80,
    scrim = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Blue80,
    onSecondary = Blue20,
    secondaryContainer = Blue30,
    onSecondaryContainer = Blue90,
    tertiary = Blue80,
    onTertiary = Blue20,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkOutlineVariant,
    onSurfaceVariant = DarkOutline,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkSurface,
    inversePrimary = Blue600,
    scrim = Color.Black,
)

/**
 * App theme using the Figma-aligned Material 3 color scheme.
 *
 * [dynamicColor] defaults to false so wallpaper-based dynamic color does not
 * override the Circuit Profile brand blues.
 */
@Composable
fun XR_CIRCUITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
