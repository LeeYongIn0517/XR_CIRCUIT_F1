package app.yongin.xr_circuit.domain.usecase

import app.yongin.xr_circuit.domain.model.CircuitDetail
import app.yongin.xr_circuit.domain.repository.CircuitRepository
import javax.inject.Inject

class GetCircuitDetailUseCase @Inject constructor(
    private val circuitRepository: CircuitRepository,
) {
    operator fun invoke(circuitId: String): CircuitDetail {
        return circuitRepository.getCircuitDetail(circuitId)
    }
}
