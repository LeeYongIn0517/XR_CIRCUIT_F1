package app.yongin.xr_circuit.domain.model

/**
 * Live (or last-known) track weather for the Right Spatial Panel.
 */
data class TrackWeather(
    val temperatureC: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Double,
    val precipitationProbabilityPercent: Int? = null,
    val observedAtIso: String? = null,
)
