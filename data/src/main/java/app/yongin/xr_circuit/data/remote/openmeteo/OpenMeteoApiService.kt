package app.yongin.xr_circuit.data.remote.openmeteo

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo Weather API (no API key).
 *
 * Base: `https://api.open-meteo.com/`
 */
interface OpenMeteoApiService {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("hourly") hourly: String = "precipitation_probability",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto",
    ): OpenMeteoForecastDto
}
