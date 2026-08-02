package app.yongin.xr_circuit.presentation.viewmodel

import app.yongin.xr_circuit.presentation.component.CircuitStatUi
import app.yongin.xr_circuit.presentation.component.GridDriverUi
import app.yongin.xr_circuit.presentation.component.KeyCornerUi
import app.yongin.xr_circuit.presentation.component.LapRecordUi
import app.yongin.xr_circuit.presentation.component.OverlayToggleUi
import app.yongin.xr_circuit.presentation.component.SampleOverlayToggles
import app.yongin.xr_circuit.presentation.component.StrategyInsightUi
import app.yongin.xr_circuit.presentation.component.TrackWeatherUi

/**
 * Screen-level UI state for Left / Center dock / Right spatial panels.
 *
 * Profile (Left) comes from local circuit assets; weather / grid are remote and
 * may be null while loading or after failure.
 */
data class CircuitUiState(
    val circuitId: String = CircuitViewModel.DEFAULT_CIRCUIT_ID,
    val circuitName: String = "",
    val stats: List<CircuitStatUi> = emptyList(),
    val lapRecord: LapRecordUi? = null,
    val corners: List<KeyCornerUi> = emptyList(),
    val selectedCornerId: String? = null,
    val weather: TrackWeatherUi? = null,
    val drivers: List<GridDriverUi> = emptyList(),
    val strategy: StrategyInsightUi? = null,
    val overlays: List<OverlayToggleUi> = SampleOverlayToggles,
    val profileReady: Boolean = false,
    val weatherLoading: Boolean = false,
    val weatherError: String? = null,
)
