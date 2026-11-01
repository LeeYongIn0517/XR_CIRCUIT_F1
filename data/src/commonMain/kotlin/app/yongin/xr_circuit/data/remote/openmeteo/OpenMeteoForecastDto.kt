package app.yongin.xr_circuit.data.remote.openmeteo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo current weather response.
 *
 * GET `https://api.open-meteo.com/v1/forecast?...`
 *
 * Used by the Right Panel weather card; included here so the data layer owns
 * the verified response shape next to other remote sources.
 */
@Serializable
data class OpenMeteoForecastDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val elevation: Double? = null,
    val timezone: String? = null,
    val current: OpenMeteoCurrentDto? = null,
    val hourly: OpenMeteoHourlyDto? = null,
)

@Serializable
data class OpenMeteoCurrentDto(
    val time: String? = null,
    val interval: Int? = null,
    @SerialName("temperature_2m") val temperature2m: Double? = null,
    val precipitation: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("wind_speed_10m") val windSpeed10m: Double? = null,
    @SerialName("wind_direction_10m") val windDirection10m: Double? = null,
)

@Serializable
data class OpenMeteoHourlyDto(
    val time: List<String> = emptyList(),
    @SerialName("precipitation_probability")
    val precipitationProbability: List<Int?> = emptyList(),
)
