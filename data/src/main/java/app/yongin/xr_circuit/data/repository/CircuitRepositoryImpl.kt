package app.yongin.xr_circuit.data.repository

import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.mapper.toDomain
import app.yongin.xr_circuit.domain.model.CircuitDetail
import app.yongin.xr_circuit.domain.model.CircuitSummary
import app.yongin.xr_circuit.domain.repository.CircuitRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CircuitRepositoryImpl @Inject constructor(
    private val local: CircuitLocalDataSource,
) : CircuitRepository {

    override fun getCatalog(): List<CircuitSummary> {
        return local.loadCatalog().circuits.map { it.toDomain() }
    }

    override fun getCircuitDetail(circuitId: String): CircuitDetail {
        return local.loadCircuitDetail(circuitId).toDomain()
    }
}
