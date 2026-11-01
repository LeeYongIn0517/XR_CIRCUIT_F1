package app.yongin.xr_circuit.domain.model

/**
 * Catalog entry for circuit switching (bottom dock / multi-circuit expansion).
 */
data class CircuitSummary(
    val id: String,
    val displayName: String,
    val modelAsset: String,
    val waypointsAsset: String,
    /** Authored glTF bounds, including the outline, in metres. */
    val modelWidthMeters: Float = 16.44f,
    val modelHeightMeters: Float = 0.44f,
    val modelDepthMeters: Float = 10f,
    /** Compatibility for the original Silverstone overlay mesh orientation. */
    val legacyOverlayRotation: Boolean = false,
    val jolpicaCircuitId: String? = null,
    val openF1CircuitKey: Int? = null,
    val openF1CircuitShortName: String? = null,
)
