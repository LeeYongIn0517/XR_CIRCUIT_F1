package app.yongin.xr_circuit.domain.repository

import app.yongin.xr_circuit.domain.model.GridEntry

interface StartingGridRepository {

    /**
     * Loads the starting grid for the circuit's latest known GP session.
     *
     * Preferred source: OpenF1. Jolpica may be used as fallback by the data layer.
     */
    suspend fun getStartingGrid(circuitId: String): List<GridEntry>
}
