package app.yongin.xr_circuit.data.remote.openmeteo

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OpenMeteoForecastDtoParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun parsesForecastPayload() {
        val forecast = json.decodeFromString<OpenMeteoForecastDto>(SAMPLE)

        assertNotNull(forecast.current)
        assertEquals(11.4, forecast.current!!.temperature2m!!, 0.01)
        assertEquals(1, forecast.current!!.weatherCode)
        assertEquals(6.8, forecast.current!!.windSpeed10m!!, 0.01)
        assertEquals(listOf(10, 20), forecast.hourly?.precipitationProbability)
    }

    companion object {
        private val SAMPLE = """
            {
              "latitude": 52.08424,
              "longitude": -1.0076141,
              "elevation": 152.0,
              "timezone": "GMT",
              "current": {
                "time": "2026-08-02T05:00",
                "interval": 900,
                "temperature_2m": 11.4,
                "precipitation": 0.0,
                "weather_code": 1,
                "wind_speed_10m": 6.8,
                "wind_direction_10m": 48
              },
              "hourly": {
                "time": ["2026-08-02T00:00", "2026-08-02T01:00"],
                "precipitation_probability": [10, 20]
              }
            }
        """.trimIndent()
    }
}
