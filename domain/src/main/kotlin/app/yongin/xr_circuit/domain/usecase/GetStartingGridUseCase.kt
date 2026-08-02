package app.yongin.xr_circuit.domain.usecase

import app.yongin.xr_circuit.domain.model.GridEntry
import app.yongin.xr_circuit.domain.repository.StartingGridRepository
import javax.inject.Inject

class GetStartingGridUseCase @Inject constructor(
    private val startingGridRepository: StartingGridRepository,
) {
    suspend operator fun invoke(circuitId: String): List<GridEntry> {
        return startingGridRepository.getStartingGrid(circuitId)
    }
}
