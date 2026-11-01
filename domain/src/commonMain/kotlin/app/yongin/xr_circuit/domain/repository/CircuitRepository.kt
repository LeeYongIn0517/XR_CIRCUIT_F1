package app.yongin.xr_circuit.domain.repository

import app.yongin.xr_circuit.domain.model.CircuitDetail
import app.yongin.xr_circuit.domain.model.CircuitSummary

interface CircuitRepository {

    fun getCatalog(): List<CircuitSummary>

    /**
     * Loads static circuit profile (Left Panel). Always available offline when the
     * circuit is present in the local catalog.
     */
    fun getCircuitDetail(circuitId: String): CircuitDetail
}
