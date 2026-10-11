package app.yongin.xr_circuit.presentation.viewmodel

import app.yongin.xr_circuit.presentation.component.OverlayLayerId

/** User actions that change the circuit screen state. */
sealed interface CircuitIntent {
    data class SelectCorner(val cornerId: String) : CircuitIntent
    data class SelectCircuit(val circuitId: String) : CircuitIntent
    data class ToggleOverlay(val overlayId: OverlayLayerId) : CircuitIntent
}
