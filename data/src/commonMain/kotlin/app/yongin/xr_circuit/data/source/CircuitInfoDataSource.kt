package app.yongin.xr_circuit.data.source

import kotlinx.coroutines.CancellationException
import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogDto
import app.yongin.xr_circuit.data.local.dto.CircuitDetailDto
import app.yongin.xr_circuit.data.remote.jolpica.JolpicaCircuitDto
import app.yongin.xr_circuit.data.remote.jolpica.JolpicaRemoteDataSource

/**
 * Aggregates Left Spatial Panel data sources.
 *
 * Prefer [app.yongin.xr_circuit.domain.repository.CircuitRepository] from domain /
 * presentation. This helper remains for direct DTO access and Jolpica enrichment.
 */
class CircuitInfoDataSource constructor(
    private val local: CircuitLocalDataSource,
    private val jolpica: JolpicaRemoteDataSource,
) {

    fun loadCatalog(): CircuitCatalogDto = local.loadCatalog()

    fun loadLocalDetail(
        circuitId: String = CircuitLocalDataSource.DEFAULT_CIRCUIT_ID,
    ): CircuitDetailDto = local.loadCircuitDetail(circuitId)

    /**
     * Fetches Jolpica metadata for the circuit when a mapping exists in the catalog.
     * Returns null when offline, unmapped, or the remote call fails.
     */
    suspend fun fetchRemoteCircuitIdentity(circuitId: String): JolpicaCircuitDto? {
        val jolpicaId = runCatching {
            local.loadCatalogEntry(circuitId).jolpicaCircuitId
        }.getOrNull() ?: return null

        return try { jolpica.fetchCircuit(jolpicaId) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { null }
    }
}
