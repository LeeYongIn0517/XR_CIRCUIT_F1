package app.yongin.xr_circuit.presentation.component

/**
 * Catalog entry for the circuit picker dropdown.
 *
 * Sourced from bundled [assets/circuits/index.json] via [app.yongin.xr_circuit.domain.usecase.GetCircuitCatalogUseCase].
 */
data class CircuitSummaryUi(
    val id: String,
    val displayName: String,
)
