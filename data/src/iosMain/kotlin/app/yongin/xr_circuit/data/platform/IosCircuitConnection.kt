package app.yongin.xr_circuit.data.platform

import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.local.CircuitAssetTarget
import app.yongin.xr_circuit.data.network.DataJson
import app.yongin.xr_circuit.data.repository.*
import app.yongin.xr_circuit.data.remote.openmeteo.*
import app.yongin.xr_circuit.data.mapper.toDomain
import app.yongin.xr_circuit.domain.model.*
import app.yongin.xr_circuit.domain.repository.*
import app.yongin.xr_circuit.domain.usecase.*
import app.yongin.xr_circuit.domain.state.*
import kotlinx.coroutines.*

/** Main-thread-only adapter. The Swift owner closes this on screen exit. */
class IosCircuitConnection {
    private val client = iosHttpClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val local = CircuitLocalDataSource(IosAssetReader(), DataJson, CircuitAssetTarget.MOBILE)
    // Product availability uses the mobile manifest and never requires GLB.
    private val circuits = object : CircuitRepository {
        override fun getCatalog() = local.loadAvailableCatalogEntries().map { it.toDomain() }
        override fun getCircuitDetail(circuitId: String) = local.loadCircuitDetail(circuitId).toDomain()
    }
    private val weather = WeatherRepositoryImpl(local, OpenMeteoRemoteDataSource(OpenMeteoApiService(client)))
    private val store = CircuitStore(GetCircuitCatalogUseCase(circuits), GetCircuitDetailUseCase(circuits),
        GetTrackWeatherUseCase(weather), GetStartingGridUseCase(object : StartingGridRepository {
            override suspend fun getStartingGrid(circuitId: String): List<GridEntry> = emptyList()
        }), scope)
    fun observe(onState: (CircuitState) -> Unit): CircuitObservation = store.observe(scope, onState)
    fun selectCircuit(id: String) = store.selectCircuit(id)
    fun selectCorner(id: String) = store.selectCorner(id)
    fun toggleOverlay(overlay: Overlay) = store.toggleOverlay(overlay)
    fun retry() = store.retry()
    fun close() { store.close(); scope.cancel(); client.close() }
}
