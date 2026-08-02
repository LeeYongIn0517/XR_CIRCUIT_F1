package app.yongin.xr_circuit.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Identifiers for 3D overlay layers controlled by the dock.
 * Domain / ViewModel mapping can be added later.
 */
enum class OverlayLayerId {
    Sectors,
    Drs,
    Pins,
}

/**
 * Presentation model for a single overlay toggle in [CircuitControlDock].
 */
data class OverlayToggleUi(
    val id: OverlayLayerId,
    val label: String,
    val selected: Boolean,
    @DrawableRes val iconRes: Int,
)

/**
 * Bottom Orbiter / Control Dock (Figma Nav, node `1:164`).
 *
 * Glass Material 3 surface composing:
 * - [CircuitSelectorButton] — circuit picker trigger
 * - [OverlayToggleButton] — independent layer toggles
 * - [ViewResetButton] — camera / view reset
 *
 * Data binding is deferred: pass sample / empty values until the ViewModel is wired.
 */
@Composable
fun CircuitControlDock(
    circuitName: String,
    overlays: List<OverlayToggleUi>,
    onCircuitClick: () -> Unit,
    onOverlayToggle: (OverlayToggleUi) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
    circuitLabel: String = "CIRCUIT",
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.4f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 25.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircuitSelectorButton(
                circuitName = circuitName,
                onClick = onCircuitClick,
                label = circuitLabel,
                showTrailingDivider = true,
            )

            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                overlays.forEach { toggle ->
                    OverlayToggleButton(
                        label = toggle.label,
                        selected = toggle.selected,
                        onClick = { onOverlayToggle(toggle) },
                        iconRes = toggle.iconRes,
                    )
                }
            }

            ViewResetButton(
                onClick = onResetClick,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

// region Sample / Preview data (replace when ViewModel is wired)

internal val SampleOverlayToggles = listOf(
    OverlayToggleUi(
        id = OverlayLayerId.Sectors,
        label = "Sectors",
        selected = true,
        iconRes = R.drawable.ic_overlay_sectors,
    ),
    OverlayToggleUi(
        id = OverlayLayerId.Drs,
        label = "DRS",
        selected = false,
        iconRes = R.drawable.ic_overlay_drs,
    ),
    OverlayToggleUi(
        id = OverlayLayerId.Pins,
        label = "Pins",
        selected = false,
        iconRes = R.drawable.ic_overlay_pins,
    ),
)

internal val SampleOverlayTogglesAllOff = SampleOverlayToggles.map {
    it.copy(selected = false)
}

// endregion

@Preview(showBackground = true, widthDp = 560, heightDp = 100, name = "Control dock — default")
@PreviewLightDark
@Composable
private fun CircuitControlDockPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CircuitControlDock(
                circuitName = "Spa-Francorchamps",
                overlays = SampleOverlayToggles,
                onCircuitClick = {},
                onOverlayToggle = {},
                onResetClick = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 560, heightDp = 100, name = "Control dock — all overlays off")
@Composable
private fun CircuitControlDockAllOffPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CircuitControlDock(
                circuitName = "Silverstone",
                overlays = SampleOverlayTogglesAllOff,
                onCircuitClick = {},
                onOverlayToggle = {},
                onResetClick = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 560, heightDp = 100, name = "Control dock — placeholder circuit")
@Composable
private fun CircuitControlDockPlaceholderPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CircuitControlDock(
                circuitName = "—",
                overlays = SampleOverlayTogglesAllOff,
                onCircuitClick = {},
                onOverlayToggle = {},
                onResetClick = {},
            )
        }
    }
}
