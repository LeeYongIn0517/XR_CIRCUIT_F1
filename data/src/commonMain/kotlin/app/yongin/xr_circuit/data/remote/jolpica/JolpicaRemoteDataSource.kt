package app.yongin.xr_circuit.data.remote.jolpica


/**
 * Remote lookups that can enrich Left Panel identity fields.
 */
class JolpicaRemoteDataSource constructor(
    private val api: JolpicaApiService,
) {

    suspend fun fetchCircuit(jolpicaCircuitId: String): JolpicaCircuitDto? {
        val response = api.getCircuit(jolpicaCircuitId)
        return response.mrData.circuitTable.circuits.firstOrNull()
    }
}
