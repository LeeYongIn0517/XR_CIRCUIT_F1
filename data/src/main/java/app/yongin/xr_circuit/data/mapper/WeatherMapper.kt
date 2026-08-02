package app.yongin.xr_circuit.data.mapper

import app.yongin.xr_circuit.data.remote.openmeteo.OpenMeteoForecastDto
import app.yongin.xr_circuit.domain.model.TrackWeather

internal fun OpenMeteoForecastDto.toDomain(): TrackWeather {
    val current = requireNotNull(current) { "Open-Meteo response missing current weather" }
    val temperature = requireNotNull(current.temperature2m) { "Missing temperature_2m" }
    val precipitation = current.precipitation ?: 0.0
    val weatherCode = requireNotNull(current.weatherCode) { "Missing weather_code" }
    val windSpeed = requireNotNull(current.windSpeed10m) { "Missing wind_speed_10m" }
    val windDirection = requireNotNull(current.windDirection10m) { "Missing wind_direction_10m" }

    val probability = hourly
        ?.precipitationProbability
        ?.firstOrNull { it != null }

    return TrackWeather(
        temperatureC = temperature,
        precipitationMm = precipitation,
        weatherCode = weatherCode,
        windSpeedKmh = windSpeed,
        windDirectionDeg = windDirection,
        precipitationProbabilityPercent = probability,
        observedAtIso = current.time,
    )
}
