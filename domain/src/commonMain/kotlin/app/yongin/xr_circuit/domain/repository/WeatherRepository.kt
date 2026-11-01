package app.yongin.xr_circuit.domain.repository

import app.yongin.xr_circuit.domain.model.TrackWeather

interface WeatherRepository {

    /**
     * Fetches current weather for the given circuit's coordinates (Open-Meteo).
     */
    suspend fun getTrackWeather(circuitId: String): TrackWeather
}
