package app.yongin.xr_circuit.domain.state

import app.yongin.xr_circuit.domain.model.*
import app.yongin.xr_circuit.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class Overlay { Sectors, Drs, Pins }
enum class CircuitError { CatalogUnavailable, ProfileUnavailable, WeatherUnavailable, GridUnavailable, UnknownCircuit }
data class CircuitState(
    val catalog: List<CircuitSummary> = emptyList(),
    val selectedCircuit: CircuitSummary? = null,
    val detail: CircuitDetail? = null,
    val selectedCornerId: String? = null,
    val overlays: Set<Overlay> = setOf(Overlay.Sectors),
    val weather: TrackWeather? = null,
    val drivers: List<GridEntry> = emptyList(),
    val weatherLoading: Boolean = false,
    val catalogError: CircuitError? = null,
    val selectionError: CircuitError? = null,
    val profileError: CircuitError? = null,
    val weatherError: CircuitError? = null,
    val gridError: CircuitError? = null,
)

/** Confined to the owner's UI dispatcher. Owns a child job, never the parent scope.
 * close invalidates even non-cooperative responses; every reload has a unique generation.
 */
class CircuitStore(
    private val catalog: GetCircuitCatalogUseCase,
    private val detail: GetCircuitDetailUseCase,
    private val weather: GetTrackWeatherUseCase,
    private val grid: GetStartingGridUseCase,
    parentScope: CoroutineScope,
) {
    private val job = SupervisorJob(parentScope.coroutineContext[Job])
    private val scope = CoroutineScope(parentScope.coroutineContext + job)
    private val mutable = MutableStateFlow(CircuitState())
    val state: StateFlow<CircuitState> = mutable.asStateFlow()
    private var request: Job? = null
    private var generation = 0L
    init { reloadCatalog() }
    private fun reloadCatalog() {
        try { mutable.update { it.copy(catalog = catalog(), catalogError = null) } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutable.update { it.copy(catalogError = CircuitError.CatalogUnavailable) }; return }
        val initial = state.value.catalog.firstOrNull { it.id == "silverstone" } ?: state.value.catalog.firstOrNull()
        initial?.let { load(it) }
    }
    fun selectCircuit(id: String) {
        if (!job.isActive) return
        val entry = state.value.catalog.firstOrNull { it.id == id }
        if (entry == null) { mutable.update { it.copy(selectionError = CircuitError.UnknownCircuit) }; return }
        mutable.update { it.copy(selectionError = null) }
        if (state.value.selectedCircuit?.id != id) load(entry)
    }
    fun selectCorner(id: String) {
        if (job.isActive && state.value.detail?.corners?.any { it.id == id } == true)
            mutable.update { it.copy(selectedCornerId = id) }
    }
    fun toggleOverlay(overlay: Overlay) {
        if (job.isActive) mutable.update { it.copy(overlays = if (overlay in it.overlays) it.overlays - overlay else it.overlays + overlay) }
    }
    fun retry() {
        if (!job.isActive) return
        state.value.selectedCircuit?.let { load(it) } ?: reloadCatalog()
    }
    private fun load(entry: CircuitSummary) {
        val token = ++generation
        request?.cancel()
        mutable.update { CircuitState(catalog = it.catalog, selectedCircuit = entry, overlays = it.overlays, weatherLoading = true) }
        try {
            val value = detail(entry.id)
            val corner = value.corners.firstOrNull { it.name.contains("Maggotts", true) }
                ?: value.corners.getOrNull(1) ?: value.corners.firstOrNull()
            mutable.update { it.copy(detail = value, selectedCornerId = corner?.id) }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutable.update { it.copy(profileError = CircuitError.ProfileUnavailable) } }
        request = scope.launch {
            launch {
                try {
                    val value = weather(entry.id)
                    if (current(token)) mutable.update { it.copy(weather = value, weatherLoading = false) }
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { if (current(token)) mutable.update { it.copy(weatherError = CircuitError.WeatherUnavailable, weatherLoading = false) } }
            }
            launch {
                try {
                    val value = grid(entry.id)
                    if (current(token)) mutable.update { it.copy(drivers = value) }
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { if (current(token)) mutable.update { it.copy(gridError = CircuitError.GridUnavailable) } }
            }
        }
    }
    private fun current(token: Long) = job.isActive && token == generation
    fun close() { ++generation; job.cancel() }
}

/** Callback is synchronous on the owner's dispatcher. Cancellation releases the callback. */
class CircuitObservation(private val job: Job) { fun cancel() = job.cancel() }
fun CircuitStore.observe(scope: CoroutineScope, onState: (CircuitState) -> Unit): CircuitObservation =
    CircuitObservation(scope.launch { state.collect { onState(it) } })
