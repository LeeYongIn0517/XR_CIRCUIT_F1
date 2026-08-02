package app.yongin.xr_circuit.data.repository

import app.yongin.xr_circuit.domain.model.GridEntry
import app.yongin.xr_circuit.domain.repository.StartingGridRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder until OpenF1 `starting_grid` / Jolpica qualifying fallback is wired
 * (Expansion Plan Phase 4).
 *
 * Bound now so domain UseCases can depend on the interface without presentation.
 */
@Singleton
class StartingGridRepositoryImpl @Inject constructor() : StartingGridRepository {

    override suspend fun getStartingGrid(circuitId: String): List<GridEntry> {
        return emptyList()
    }
}
