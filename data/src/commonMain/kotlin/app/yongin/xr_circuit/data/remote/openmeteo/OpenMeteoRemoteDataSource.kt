package app.yongin.xr_circuit.data.remote.openmeteo


class OpenMeteoRemoteDataSource constructor(
    private val api: OpenMeteoApiService,
) {

    suspend fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double,
    ): OpenMeteoForecastDto {
        return api.getForecast(latitude = latitude, longitude = longitude)
    }
}
