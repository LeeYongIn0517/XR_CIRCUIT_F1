package app.yongin.xr_circuit.data.remote.jolpica

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote lookups that can enrich Left Panel identity fields.
 */
@Singleton
class JolpicaRemoteDataSource @Inject constructor(
    private val api: JolpicaApiService,
) {

    suspend fun fetchCircuit(jolpicaCircuitId: String): JolpicaCircuitDto? {
        val response = api.getCircuit(jolpicaCircuitId)
        return response.mrData.circuitTable.circuits.firstOrNull()
    }
}
