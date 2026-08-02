package app.yongin.xr_circuit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yongin.xr_circuit.domain.usecase.GetCircuitDetailUseCase
import app.yongin.xr_circuit.domain.usecase.GetStartingGridUseCase
import app.yongin.xr_circuit.domain.usecase.GetTrackWeatherUseCase
import app.yongin.xr_circuit.presentation.component.OverlayToggleUi
import app.yongin.xr_circuit.presentation.mapper.toStatsUi
import app.yongin.xr_circuit.presentation.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CircuitViewModel @Inject constructor(
    private val getCircuitDetail: GetCircuitDetailUseCase,
    private val getTrackWeather: GetTrackWeatherUseCase,
    private val getStartingGrid: GetStartingGridUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircuitUiState())
    val uiState: StateFlow<CircuitUiState> = _uiState.asStateFlow()

    init {
        loadCircuit(DEFAULT_CIRCUIT_ID)
    }

    fun selectCorner(cornerId: String) {
        _uiState.update { it.copy(selectedCornerId = cornerId) }
    }

    fun toggleOverlay(target: OverlayToggleUi) {
        _uiState.update { state ->
            state.copy(
                overlays = state.overlays.map { overlay ->
                    if (overlay.id == target.id) {
                        overlay.copy(selected = !overlay.selected)
                    } else {
                        overlay
                    }
                },
            )
        }
    }

    fun loadCircuit(circuitId: String) {
        loadProfile(circuitId)
        loadWeather(circuitId)
        loadGrid(circuitId)
    }

    private fun loadProfile(circuitId: String) {
        runCatching { getCircuitDetail(circuitId) }
            .onSuccess { detail ->
                val corners = detail.corners.map { it.toUi() }
                val preferredSelection = corners
                    .firstOrNull { it.name.contains("Maggotts", ignoreCase = true) }
                    ?.id
                    ?: corners.getOrNull(1)?.id
                    ?: corners.firstOrNull()?.id

                _uiState.update {
                    it.copy(
                        circuitId = detail.id,
                        circuitName = detail.name
                            .removeSuffix(" Circuit")
                            .ifBlank { detail.name },
                        stats = detail.toStatsUi(),
                        lapRecord = detail.lapRecord?.toUi(),
                        corners = corners,
                        selectedCornerId = preferredSelection,
                        profileReady = true,
                    )
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        circuitId = circuitId,
                        profileReady = false,
                        stats = emptyList(),
                        lapRecord = null,
                        corners = emptyList(),
                        selectedCornerId = null,
                    )
                }
            }
    }

    private fun loadWeather(circuitId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(weatherLoading = true, weatherError = null)
            }
            runCatching { getTrackWeather(circuitId) }
                .onSuccess { weather ->
                    _uiState.update {
                        it.copy(
                            weather = weather.toUi(),
                            weatherLoading = false,
                            weatherError = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            weather = null,
                            weatherLoading = false,
                            weatherError = error.message ?: "Weather unavailable",
                        )
                    }
                }
        }
    }

    private fun loadGrid(circuitId: String) {
        viewModelScope.launch {
            runCatching { getStartingGrid(circuitId) }
                .onSuccess { entries ->
                    _uiState.update {
                        it.copy(drivers = entries.map { entry -> entry.toUi() })
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(drivers = emptyList()) }
                }
        }
    }

    companion object {
        /** Fixed until the top Orbiter circuit picker is wired. */
        const val DEFAULT_CIRCUIT_ID = "silverstone"
    }
}
