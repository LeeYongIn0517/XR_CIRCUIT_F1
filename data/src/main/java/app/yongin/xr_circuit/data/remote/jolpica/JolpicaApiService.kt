package app.yongin.xr_circuit.data.remote.jolpica

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Jolpica F1 API — Ergast successor.
 *
 * Base: `https://api.jolpi.ca/ergast/f1/`
 * Rate limit applies; prefer caching for non-critical reads.
 */
interface JolpicaApiService {

    @GET("circuits/{circuitId}.json")
    suspend fun getCircuit(
        @Path("circuitId") circuitId: String,
    ): JolpicaCircuitResponseDto
}
