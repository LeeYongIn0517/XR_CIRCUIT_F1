package app.yongin.xr_circuit.data.local

import app.yongin.xr_circuit.data.platform.AssetReader
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogDto
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogEntryDto
import app.yongin.xr_circuit.data.local.dto.CircuitDetailDto
import kotlinx.serialization.json.Json

/**
 * Loads bundled circuit JSON from the `:data` module assets.
 *
 * Primary source for Left Spatial Panel fields (specs, lap record, key corners).
 */
class CircuitLocalDataSource constructor(
    private val assets: AssetReader,
    private val json: Json,
) {

    fun loadCatalog(): CircuitCatalogDto {
        return decodeAsset(CATALOG_PATH)
    }

    /** Planned catalog entries become selectable once all three assets are bundled. */
    fun loadAvailableCatalogEntries(): List<CircuitCatalogEntryDto> =
        loadCatalog().circuits.filter { entry ->
            listOf(entry.detailAssetPath, entry.modelAsset, entry.waypointsAsset).all { path ->
                assets.exists(path)
            }
        }

    fun loadCatalogEntry(circuitId: String): CircuitCatalogEntryDto {
        return loadCatalog().circuits.firstOrNull { it.id == circuitId }
            ?: error("Unknown circuit id: $circuitId")
    }

    fun loadCircuitDetail(circuitId: String): CircuitDetailDto {
        val entry = loadCatalogEntry(circuitId)
        return decodeAsset(entry.detailAssetPath)
    }

    fun loadCircuitDetailFromPath(assetPath: String): CircuitDetailDto {
        return decodeAsset(assetPath)
    }

    private inline fun <reified T> decodeAsset(path: String): T {
        val text = assets.readText(path)
        return json.decodeFromString(text)
    }

    companion object {
        const val CATALOG_PATH = "circuits/index.json"
        const val DEFAULT_CIRCUIT_ID = "silverstone"
    }
}
