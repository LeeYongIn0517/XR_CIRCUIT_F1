package app.yongin.xr_circuit.presentation.mapper

import androidx.compose.ui.graphics.Color
import app.yongin.xr_circuit.domain.model.CircuitCorner
import app.yongin.xr_circuit.domain.model.CircuitDetail
import app.yongin.xr_circuit.domain.model.GridEntry
import app.yongin.xr_circuit.domain.model.LapRecord
import app.yongin.xr_circuit.domain.model.TrackWeather
import app.yongin.xr_circuit.presentation.component.CircuitStatUi
import app.yongin.xr_circuit.presentation.component.GridDriverUi
import app.yongin.xr_circuit.presentation.component.KeyCornerUi
import app.yongin.xr_circuit.presentation.component.LapRecordUi
import app.yongin.xr_circuit.presentation.component.TrackWeatherUi
import java.util.Locale
import kotlin.math.roundToInt

internal fun CircuitDetail.toStatsUi(): List<CircuitStatUi> = buildList {
    add(
        CircuitStatUi(
            label = "Length",
            value = String.format(Locale.US, "%.3f", lengthKm),
            unit = "km",
        ),
    )
    add(CircuitStatUi(label = "Turns", value = turns.toString()))
    add(CircuitStatUi(label = "DRS Zones", value = drsZoneCount.toString()))
    elevationDeltaM?.let { delta ->
        add(
            CircuitStatUi(
                label = "Elevation",
                value = String.format(Locale.US, "%.0f", delta),
                unit = "m",
            ),
        )
    }
}

internal fun LapRecord.toUi(): LapRecordUi = LapRecordUi(
    time = time,
    driver = driver,
    team = team,
    year = year,
)

internal fun CircuitCorner.toUi(): KeyCornerUi = KeyCornerUi(
    id = id,
    turnCode = turnCode,
    name = name,
)

internal fun TrackWeather.toUi(): TrackWeatherUi {
    val temp = String.format(Locale.US, "%.0f°C", temperatureC)
    val rainPercent = precipitationProbabilityPercent ?: 0
    val windSpeed = windSpeedKmh.roundToInt()
    val compass = degreesToCompass(windDirectionDeg)
    return TrackWeatherUi(
        airTemperature = temp,
        trackTemperatureLabel = String.format(Locale.US, "Precip: %.1f mm", precipitationMm),
        rainChanceLabel = "$rainPercent% Rain",
        windLabel = "Wind: ${windSpeed}km/h $compass",
        backgroundRes = null,
    )
}

internal fun GridEntry.toUi(): GridDriverUi {
    val displayName = driver.fullName
        .substringAfterLast(' ')
        .ifBlank { driver.acronym }
    return GridDriverUi(
        id = "${position}-${driver.number}",
        position = position,
        driverName = displayName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        },
        teamName = driver.teamName,
        teamColor = parseTeamColor(driver.teamColorHex),
        progress = null,
        emphasized = position <= 3,
    )
}

private fun parseTeamColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFF9CA3AF)
    val normalized = hex.removePrefix("#")
    return runCatching {
        Color(("FF$normalized").toLong(16))
    }.getOrDefault(Color(0xFF9CA3AF))
}

private fun degreesToCompass(degrees: Double): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((degrees % 360.0) / 45.0).roundToInt() % directions.size
    return directions[index]
}
