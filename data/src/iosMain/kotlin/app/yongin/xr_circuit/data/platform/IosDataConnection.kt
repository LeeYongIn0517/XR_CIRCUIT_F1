package app.yongin.xr_circuit.data.platform
import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.network.DataJson
import app.yongin.xr_circuit.data.repository.*
import app.yongin.xr_circuit.data.remote.openmeteo.*
import app.yongin.xr_circuit.domain.model.*
/** Stage 3 probe only; app state and observation belong to stage 4. */
class IosDataConnection {
    private val client = iosHttpClient()
    private val local = CircuitLocalDataSource(IosAssetReader(), DataJson)
    private val circuits = CircuitRepositoryImpl(local)
    private val weather = WeatherRepositoryImpl(local, OpenMeteoRemoteDataSource(OpenMeteoApiService(client)))
    @Throws(Exception::class)
    fun detail(circuitId: String): CircuitDetail = circuits.getCircuitDetail(circuitId)
    @Throws(Exception::class)
    suspend fun weather(circuitId: String): TrackWeather = weather.getTrackWeather(circuitId)
    fun close() = client.close()
}
