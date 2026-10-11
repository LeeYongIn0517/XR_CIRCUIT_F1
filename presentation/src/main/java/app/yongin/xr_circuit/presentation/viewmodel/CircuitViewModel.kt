package app.yongin.xr_circuit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yongin.xr_circuit.domain.usecase.GetCircuitCatalogUseCase
import app.yongin.xr_circuit.domain.usecase.GetCircuitDetailUseCase
import app.yongin.xr_circuit.domain.usecase.GetStartingGridUseCase
import app.yongin.xr_circuit.domain.usecase.GetTrackWeatherUseCase
import app.yongin.xr_circuit.presentation.mapper.toStatsUi
import app.yongin.xr_circuit.presentation.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import app.yongin.xr_circuit.domain.model.CircuitSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CircuitViewModel @Inject constructor(
    private val getCircuitCatalog: GetCircuitCatalogUseCase,
    private val getCircuitDetail: GetCircuitDetailUseCase,
    private val getTrackWeather: GetTrackWeatherUseCase,
    private val getStartingGrid: GetStartingGridUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircuitUiState())
    val uiState: StateFlow<CircuitUiState> = _uiState.asStateFlow()

    private var catalog: List<CircuitSummary> = emptyList()
    private var weatherJob: Job? = null
    private var gridJob: Job? = null

    init {
        loadCatalog()
        loadCircuit(DEFAULT_CIRCUIT_ID)
    }

    fun onIntent(intent: CircuitIntent) {
        when (intent) {
            is CircuitIntent.SelectCorner -> {
                _uiState.update { it.copy(selectedCornerId = intent.cornerId) }
            }
            is CircuitIntent.SelectCircuit -> selectCircuit(intent.circuitId)
            is CircuitIntent.ToggleOverlay -> {
                _uiState.update {
                    it.copy(dockUiState = it.dockUiState.toggleOverlay(intent.overlayId))
                }
            }
        }
    }

    private fun selectCircuit(circuitId: String) {
        if (circuitId == _uiState.value.circuitId || catalog.none { it.id == circuitId }) return
        loadCircuit(circuitId)
    }

    private fun loadCatalog() {
        runCatching { getCircuitCatalog() }
            .onSuccess { catalog ->
                this.catalog = catalog
                _uiState.update {
                    it.copy(availableCircuits = catalog.map { entry -> entry.toUi() })
                }
            }
    }

    private fun loadCircuit(circuitId: String) {
        val entry = catalog.firstOrNull { it.id == circuitId } ?: return
        weatherJob?.cancel()
        gridJob?.cancel()
        _uiState.update {
            it.copy(
                circuitId = circuitId,
                circuitName = entry.displayName,
                selectedCircuit = entry,
                weather = null,
                weatherError = null,
                drivers = emptyList(),
                strategy = null,
            )
        }
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
        weatherJob = viewModelScope.launch {
            _uiState.update {
                it.copy(weatherLoading = true, weatherError = null)
            }
            runCatching { getTrackWeather(circuitId) }
                .also { result ->
                    (result.exceptionOrNull() as? CancellationException)?.let { throw it }
                    if (_uiState.value.circuitId != circuitId) return@launch
                }
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
        gridJob = viewModelScope.launch {
            runCatching { getStartingGrid(circuitId) }
                .also { result ->
                    (result.exceptionOrNull() as? CancellationException)?.let { throw it }
                    if (_uiState.value.circuitId != circuitId) return@launch
                }
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
        const val DEFAULT_CIRCUIT_ID = "silverstone"
    }
}
