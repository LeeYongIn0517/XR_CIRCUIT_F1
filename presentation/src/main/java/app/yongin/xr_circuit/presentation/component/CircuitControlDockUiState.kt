package app.yongin.xr_circuit.presentation.component

import app.yongin.xr_circuit.presentation.R

/**
 * User-driven selection state for [CircuitControlDock] overlay toggles.
 *
 * Included in [app.yongin.xr_circuit.presentation.viewmodel.CircuitUiState] and
 * managed by [app.yongin.xr_circuit.presentation.viewmodel.CircuitViewModel].
 */
data class CircuitControlDockUiState(
    val overlays: List<OverlayToggleUi> = DefaultOverlayToggles,
) {
    fun toggleOverlay(id: OverlayLayerId): CircuitControlDockUiState = copy(
        overlays = overlays.map { overlay ->
            if (overlay.id == id) {
                overlay.copy(selected = !overlay.selected)
            } else {
                overlay
            }
        },
    )
}

val DefaultOverlayToggles = listOf(
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
