package app.yongin.xr_circuit.domain.usecase

import app.yongin.xr_circuit.domain.model.CircuitSummary
import app.yongin.xr_circuit.domain.repository.CircuitRepository
import javax.inject.Inject

class GetCircuitCatalogUseCase @Inject constructor(
    private val circuitRepository: CircuitRepository,
) {
    operator fun invoke(): List<CircuitSummary> {
        return circuitRepository.getCatalog()
    }
}
