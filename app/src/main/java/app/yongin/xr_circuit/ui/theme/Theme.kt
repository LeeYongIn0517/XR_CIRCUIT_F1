package app.yongin.xr_circuit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme as PresentationTheme

/**
 * App entry theme. Delegates to the presentation-module Figma-aligned scheme
 * so screens and reusable components share one Material 3 color source.
 */
@Composable
fun XR_CIRCUITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    PresentationTheme(
        darkTheme = darkTheme,
        content = content,
    )
}
