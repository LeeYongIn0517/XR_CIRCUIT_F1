package app.yongin.xr_circuit.data.remote.openmeteo
import app.yongin.xr_circuit.data.network.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
class OpenMeteoApiService(private val client: HttpClient) {
    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        current: String = "temperature_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
        hourly: String = "precipitation_probability",
        forecastDays: Int = 1,
        timezone: String = "auto"
    ): OpenMeteoForecastDto = DataJson.decodeFromString(client.get(ApiBases.OPEN_METEO + "v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", current)
            parameter("hourly", hourly)
            parameter("forecast_days", forecastDays)
            parameter("timezone", timezone)
    }.bodyAsText())
}
