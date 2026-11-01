package app.yongin.xr_circuit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.yongin.xr_circuit.domain.usecase.*
import app.yongin.xr_circuit.domain.state.*
import app.yongin.xr_circuit.presentation.mapper.toStatsUi
import app.yongin.xr_circuit.presentation.mapper.toUi
import app.yongin.xr_circuit.presentation.component.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class CircuitViewModel @Inject constructor(
    getCircuitCatalog: GetCircuitCatalogUseCase,
    getCircuitDetail: GetCircuitDetailUseCase,
    getTrackWeather: GetTrackWeatherUseCase,
    getStartingGrid: GetStartingGridUseCase,
) : ViewModel() {
    private val store = CircuitStore(getCircuitCatalog, getCircuitDetail, getTrackWeather, getStartingGrid, viewModelScope)
    val uiState: StateFlow<CircuitUiState> = store.state.map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, store.state.value.toUiState())
    fun onIntent(intent: CircuitIntent) {
        when (intent) {
            is CircuitIntent.SelectCircuit -> store.selectCircuit(intent.circuitId)
            is CircuitIntent.SelectCorner -> store.selectCorner(intent.cornerId)
            is CircuitIntent.ToggleOverlay -> store.toggleOverlay(Overlay.valueOf(intent.overlayId.name))
        }
    }
    fun retry() = store.retry()
    override fun onCleared() { store.close(); super.onCleared() }
    companion object { const val DEFAULT_CIRCUIT_ID = "silverstone" }
}

internal fun CircuitState.toUiState() = CircuitUiState(
    circuitId = selectedCircuit?.id ?: CircuitViewModel.DEFAULT_CIRCUIT_ID,
    circuitName = selectedCircuit?.displayName.orEmpty(), selectedCircuit = selectedCircuit,
    availableCircuits = catalog.map { it.toUi() }, stats = detail?.toStatsUi().orEmpty(),
    lapRecord = detail?.lapRecord?.toUi(), corners = detail?.corners?.map { it.toUi() }.orEmpty(),
    selectedCornerId = selectedCornerId,
    dockUiState = CircuitControlDockUiState(DefaultOverlayToggles.map { it.copy(selected = Overlay.valueOf(it.id.name) in overlays) }),
    weather = weather?.toUi(), drivers = drivers.map { it.toUi() },
    profileReady = detail != null, weatherLoading = weatherLoading,
    weatherError = weatherError?.let { "Weather unavailable" },
)
