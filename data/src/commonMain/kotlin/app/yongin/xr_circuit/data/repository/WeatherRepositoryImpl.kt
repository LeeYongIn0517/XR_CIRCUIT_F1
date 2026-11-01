package app.yongin.xr_circuit.data.repository

import app.yongin.xr_circuit.data.platform.TimeProvider
import app.yongin.xr_circuit.data.platform.PlatformTimeProvider
import kotlin.concurrent.Volatile
import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.mapper.toDomain
import app.yongin.xr_circuit.data.remote.openmeteo.OpenMeteoRemoteDataSource
import app.yongin.xr_circuit.domain.model.TrackWeather
import app.yongin.xr_circuit.domain.repository.WeatherRepository

class WeatherRepositoryImpl constructor(
    private val local: CircuitLocalDataSource,
    private val openMeteo: OpenMeteoRemoteDataSource,
    private val time: TimeProvider = PlatformTimeProvider(),
) : WeatherRepository {

    @Volatile
    private var cache: CacheEntry? = null

    override suspend fun getTrackWeather(circuitId: String): TrackWeather {
        val now = time.nowMillis()
        cache
            ?.takeIf { it.circuitId == circuitId && now - it.fetchedAtMs < TTL_MS }
            ?.let { return it.weather }

        val location = local.loadCircuitDetail(circuitId).location
        val weather = openMeteo
            .fetchCurrentWeather(location.latitude, location.longitude)
            .toDomain()

        cache = CacheEntry(circuitId, weather, now)
        return weather
    }

    private data class CacheEntry(
        val circuitId: String,
        val weather: TrackWeather,
        val fetchedAtMs: Long,
    )

    companion object {
        /** Matches Circuit Info XR plan weather TTL. */
        const val TTL_MS = 10L * 60L * 1000L
    }
}
