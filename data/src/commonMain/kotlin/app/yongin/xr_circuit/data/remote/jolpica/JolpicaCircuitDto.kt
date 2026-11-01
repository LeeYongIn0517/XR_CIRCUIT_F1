package app.yongin.xr_circuit.data.remote.jolpica

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jolpica F1 (Ergast-compatible) circuit response.
 *
 * GET `https://api.jolpi.ca/ergast/f1/circuits/{circuitId}.json`
 *
 * Useful for Left Panel enrichment: official name, country, lat/long.
 * Does **not** include track length, turn count, DRS zones, or lap records.
 */
@Serializable
data class JolpicaCircuitResponseDto(
    @SerialName("MRData") val mrData: JolpicaMrDataDto,
)

@Serializable
data class JolpicaMrDataDto(
    val xmlns: String? = null,
    val series: String? = null,
    val url: String? = null,
    val limit: String? = null,
    val offset: String? = null,
    val total: String? = null,
    @SerialName("CircuitTable") val circuitTable: JolpicaCircuitTableDto,
)

@Serializable
data class JolpicaCircuitTableDto(
    val circuitId: String? = null,
    @SerialName("Circuits") val circuits: List<JolpicaCircuitDto> = emptyList(),
)

@Serializable
data class JolpicaCircuitDto(
    val circuitId: String,
    val url: String? = null,
    val circuitName: String,
    @SerialName("Location") val location: JolpicaLocationDto,
)

@Serializable
data class JolpicaLocationDto(
    val lat: String,
    @SerialName("long") val longitude: String,
    val locality: String? = null,
    val country: String? = null,
)
