package app.yongin.xr_circuit.data.remote.openmeteo

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenMeteoRemoteDataSource @Inject constructor(
    private val api: OpenMeteoApiService,
) {

    suspend fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double,
    ): OpenMeteoForecastDto {
        return api.getForecast(latitude = latitude, longitude = longitude)
    }
}
