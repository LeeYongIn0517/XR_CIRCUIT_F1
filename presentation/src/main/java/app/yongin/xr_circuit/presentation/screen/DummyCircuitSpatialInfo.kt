package app.yongin.xr_circuit.presentation.screen

import app.yongin.xr_circuit.presentation.component.GridDriverUi
import app.yongin.xr_circuit.presentation.component.KeyCornerUi
import app.yongin.xr_circuit.presentation.component.LapRecordUi
import app.yongin.xr_circuit.presentation.component.OverlayToggleUi
import app.yongin.xr_circuit.presentation.component.SampleCircuitStats
import app.yongin.xr_circuit.presentation.component.SampleGridDrivers
import app.yongin.xr_circuit.presentation.component.SampleKeyCorners
import app.yongin.xr_circuit.presentation.component.SampleLapRecord
import app.yongin.xr_circuit.presentation.component.SampleOverlayToggles
import app.yongin.xr_circuit.presentation.component.SampleStrategyInsight
import app.yongin.xr_circuit.presentation.component.SampleTrackWeather
import app.yongin.xr_circuit.presentation.component.CircuitStatUi
import app.yongin.xr_circuit.presentation.component.StrategyInsightUi
import app.yongin.xr_circuit.presentation.component.TrackWeatherUi

/**
 * Placeholder spatial-layout payload for UI inspection.
 *
 * Replace with ViewModel / server-backed state once data layers are wired.
 */
data class DummyCircuitSpatialInfo(
    val circuitName: String,
    val stats: List<CircuitStatUi>,
    val lapRecord: LapRecordUi?,
    val corners: List<KeyCornerUi>,
    val initiallySelectedCornerId: String?,
    val weather: TrackWeatherUi?,
    val drivers: List<GridDriverUi>,
    val strategy: StrategyInsightUi?,
    val overlays: List<OverlayToggleUi>,
)

val DummyCircuitSpatialInfoDefault = DummyCircuitSpatialInfo(
    circuitName = "Silverstone",
    stats = SampleCircuitStats,
    lapRecord = SampleLapRecord,
    corners = SampleKeyCorners,
    initiallySelectedCornerId = SampleKeyCorners.getOrNull(1)?.id,
    weather = SampleTrackWeather,
    drivers = SampleGridDrivers,
    strategy = SampleStrategyInsight,
    overlays = SampleOverlayToggles,
)
