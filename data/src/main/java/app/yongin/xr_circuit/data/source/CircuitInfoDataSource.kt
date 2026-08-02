package app.yongin.xr_circuit.data.source

import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogDto
import app.yongin.xr_circuit.data.local.dto.CircuitDetailDto
import app.yongin.xr_circuit.data.remote.jolpica.JolpicaCircuitDto
import app.yongin.xr_circuit.data.remote.jolpica.JolpicaRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregates Left Spatial Panel data sources.
 *
 * Prefer [app.yongin.xr_circuit.domain.repository.CircuitRepository] from domain /
 * presentation. This helper remains for direct DTO access and Jolpica enrichment.
 */
@Singleton
class CircuitInfoDataSource @Inject constructor(
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

        return runCatching { jolpica.fetchCircuit(jolpicaId) }.getOrNull()
    }
}
