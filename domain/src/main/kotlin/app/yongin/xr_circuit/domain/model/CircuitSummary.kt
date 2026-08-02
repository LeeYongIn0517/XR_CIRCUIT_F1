package app.yongin.xr_circuit.domain.model

/**
 * Catalog entry for circuit switching (bottom dock / multi-circuit expansion).
 */
data class CircuitSummary(
    val id: String,
    val displayName: String,
    val modelAsset: String,
    val waypointsAsset: String,
    val jolpicaCircuitId: String? = null,
    val openF1CircuitKey: Int? = null,
    val openF1CircuitShortName: String? = null,
)
