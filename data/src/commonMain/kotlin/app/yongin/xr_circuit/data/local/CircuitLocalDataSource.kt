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
    private val target: CircuitAssetTarget = CircuitAssetTarget.XR,
) {

    fun loadCatalog(): CircuitCatalogDto {
        return decodeAsset(CATALOG_PATH)
    }

    /** Availability is a packaging decision, independent of API identifiers. */
    fun loadAvailableCatalogEntries(): List<CircuitCatalogEntryDto> {
        val mobile = if (target == CircuitAssetTarget.MOBILE) MobileTrackResolver(assets, json).available() else emptyMap()
        return loadCatalog().circuits.filter { entry ->
            assets.exists(entry.detailAssetPath) && when (target) {
                CircuitAssetTarget.XR -> assets.exists(entry.modelAsset) && assets.exists(entry.waypointsAsset)
                CircuitAssetTarget.MOBILE -> entry.id in mobile
            }
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
