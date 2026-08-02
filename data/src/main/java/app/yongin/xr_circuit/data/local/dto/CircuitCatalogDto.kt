package app.yongin.xr_circuit.data.local.dto

import kotlinx.serialization.Serializable

/**
 * Catalog of circuits bundled with the app.
 *
 * File: `assets/circuits/index.json`
 */
@Serializable
data class CircuitCatalogDto(
    val circuits: List<CircuitCatalogEntryDto>,
)

@Serializable
data class CircuitCatalogEntryDto(
    val id: String,
    val displayName: String,
    val detailAssetPath: String,
    val modelAsset: String,
    val waypointsAsset: String,
    /** Jolpica / Ergast circuitId, e.g. `"silverstone"`. */
    val jolpicaCircuitId: String? = null,
    /** OpenF1 `circuit_key` used by meetings / sessions. */
    val openF1CircuitKey: Int? = null,
    /** OpenF1 `circuit_short_name` filter value. */
    val openF1CircuitShortName: String? = null,
)
