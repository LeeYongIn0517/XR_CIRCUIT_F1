package app.yongin.xr_circuit.data.repository

import app.yongin.xr_circuit.domain.model.GridEntry
import app.yongin.xr_circuit.domain.repository.StartingGridRepository

/**
 * Placeholder until OpenF1 `starting_grid` / Jolpica qualifying fallback is wired
 * (Expansion Plan Phase 4).
 *
 * Bound now so domain UseCases can depend on the interface without presentation.
 */
class StartingGridRepositoryImpl constructor() : StartingGridRepository {

    override suspend fun getStartingGrid(circuitId: String): List<GridEntry> {
        return emptyList()
    }
}
