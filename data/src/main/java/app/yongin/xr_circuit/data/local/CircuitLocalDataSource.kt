package app.yongin.xr_circuit.data.local

import android.content.Context
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogDto
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogEntryDto
import app.yongin.xr_circuit.data.local.dto.CircuitDetailDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * Loads bundled circuit JSON from the `:data` module assets.
 *
 * Primary source for Left Spatial Panel fields (specs, lap record, key corners).
 */
@Singleton
class CircuitLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {

    fun loadCatalog(): CircuitCatalogDto {
        return decodeAsset(CATALOG_PATH)
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
        val text = context.assets.open(path).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    companion object {
        const val CATALOG_PATH = "circuits/index.json"
        const val DEFAULT_CIRCUIT_ID = "silverstone"
    }
}
