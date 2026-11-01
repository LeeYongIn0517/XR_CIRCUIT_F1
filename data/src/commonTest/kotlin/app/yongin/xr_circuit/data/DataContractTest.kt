package app.yongin.xr_circuit.data
import app.yongin.xr_circuit.data.platform.*
import app.yongin.xr_circuit.data.local.*
import app.yongin.xr_circuit.data.network.*
import app.yongin.xr_circuit.data.repository.*
import app.yongin.xr_circuit.data.remote.openmeteo.*
import app.yongin.xr_circuit.data.mapper.toDomain
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DataContractTest {
    private class Assets : AssetReader {
        val files = mutableMapOf(
            "circuits/index.json" to """{"circuits":[{"id":"silverstone","displayName":"Silverstone","detailAssetPath":"circuits/silverstone.json","modelAsset":"track.glb","waypointsAsset":"path.json"},{"id":"suzuka","displayName":"Suzuka","detailAssetPath":"circuits/suzuka.json","modelAsset":"track.glb","waypointsAsset":"path.json"}]}""",
            "circuits/silverstone.json" to """{"id":"silverstone","name":"Silverstone","country":"UK","lengthKm":5.891,"turns":18,"drsZoneCount":2,"location":{"latitude":52.0786,"longitude":-1.0169},"characteristics":{"elevationMinM":148,"elevationMaxM":172}}""",
            "circuits/suzuka.json" to """{"id":"suzuka","name":"Suzuka","country":"Japan","lengthKm":5.807,"turns":18,"drsZoneCount":1,"location":{"latitude":34.8431,"longitude":136.541}}""",
            "track.glb" to "", "path.json" to ""
        )
        override fun readText(path: String) = files[path] ?: error("Missing asset: $path")
        override fun exists(path: String) = path in files
    }
    private class Clock(var value: Long = 0) : TimeProvider { override fun nowMillis() = value }
    private val response = """{"unknown":true,"current":{"time":"2026-11-01T12:00","temperature_2m":19.5,"precipitation":0.2,"weather_code":3,"wind_speed_10m":12.4,"wind_direction_10m":270},"hourly":{"precipitation_probability":[null,30,40]}}"""
    private fun repository(client: HttpClient, clock: TimeProvider = Clock(), assets: Assets = Assets()) =
        WeatherRepositoryImpl(CircuitLocalDataSource(assets, DataJson), OpenMeteoRemoteDataSource(OpenMeteoApiService(client)), clock)
    @Test fun localMappingAndXrAvailability() {
        val assets = Assets()
        val repo = CircuitRepositoryImpl(CircuitLocalDataSource(assets, DataJson))
        assertEquals(2, repo.getCatalog().size)
        assertEquals(24.0, repo.getCircuitDetail("silverstone").elevationDeltaM)
        assets.files.remove("track.glb")
        assertTrue(repo.getCatalog().isEmpty())
        assertEquals(18, repo.getCircuitDetail("silverstone").turns)
        assertFails { repo.getCircuitDetail("missing") }
    }
    @Test fun missingAndMalformedAssetsFail() {
        val assets = Assets()
        val repo = CircuitRepositoryImpl(CircuitLocalDataSource(assets, DataJson))
        assets.files.remove("circuits/silverstone.json")
        assertFails { repo.getCircuitDetail("silverstone") }
        assets.files["circuits/silverstone.json"] = "{invalid"
        assertFails { repo.getCircuitDetail("silverstone") }
    }
    @Test fun requestAndWeatherMapping() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals("/v1/forecast", request.url.encodedPath)
            assertEquals("52.0786", request.url.parameters["latitude"])
            assertEquals("-1.0169", request.url.parameters["longitude"])
            assertEquals("auto", request.url.parameters["timezone"])
            assertEquals("1", request.url.parameters["forecast_days"])
            assertEquals("precipitation_probability", request.url.parameters["hourly"])
            assertEquals("temperature_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m", request.url.parameters["current"])
            respond(response)
        }) { expectSuccess = true }
        try {
            val weather = repository(client).getTrackWeather("silverstone")
            assertEquals(19.5, weather.temperatureC)
            assertEquals(30, weather.precipitationProbabilityPercent)
            assertEquals(270.0, weather.windDirectionDeg)
            assertEquals(12.4, weather.windSpeedKmh)
        } finally { client.close() }
    }
    @Test fun cacheExpiresExactlyAtTenMinutesAndRetainsOneCircuit() = runTest {
        var requests = 0
        val clock = Clock()
        val client = HttpClient(MockEngine { requests++; respond(response) })
        try {
            val repo = repository(client, clock)
            repo.getTrackWeather("silverstone")
            clock.value = 599999
            repo.getTrackWeather("silverstone")
            assertEquals(1, requests)
            clock.value = 600000
            repo.getTrackWeather("silverstone")
            assertEquals(2, requests)
            repo.getTrackWeather("suzuka")
            repo.getTrackWeather("silverstone")
            assertEquals(4, requests)
        } finally { client.close() }
    }
    @Test fun httpFailureDoesNotPoisonCache() = runTest {
        var requests = 0
        val client = HttpClient(MockEngine {
            requests++
            if (requests == 1) respond("unavailable", HttpStatusCode.ServiceUnavailable) else respond(response)
        }) { expectSuccess = true }
        try {
            val repo = repository(client)
            assertFailsWith<ServerResponseException> { repo.getTrackWeather("silverstone") }
            assertEquals(19.5, repo.getTrackWeather("silverstone").temperatureC)
            assertEquals(2, requests)
        } finally { client.close() }
    }
    @Test fun networkFailurePropagates() = runTest {
        val client = HttpClient(MockEngine { throw IllegalStateException("offline") })
        try { assertFailsWith<IllegalStateException> { repository(client).getTrackWeather("silverstone") } }
        finally { client.close() }
    }
    @Test fun malformedAndIncompleteResponsesFail() = runTest {
        for (payload in listOf("{invalid", "{}", "{\"current\":{}}")) {
            val client = HttpClient(MockEngine { respond(payload) })
            try { assertFails { repository(client).getTrackWeather("silverstone") } }
            finally { client.close() }
        }
    }
    @Test fun cancellationReachesEngineAndDoesNotCache() = runTest {
        val entered = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        var requests = 0
        val client = HttpClient(MockEngine {
            requests++
            if (requests == 1) {
                entered.complete(Unit)
                try { awaitCancellation() } finally { cancelled.complete(Unit) }
            }
            respond(response)
        })
        try {
            val repo = repository(client)
            val job = launch { repo.getTrackWeather("silverstone") }
            entered.await()
            job.cancelAndJoin()
            cancelled.await()
            assertTrue(cancelled.isCompleted)
            assertEquals(19.5, repo.getTrackWeather("silverstone").temperatureC)
            assertEquals(2, requests)
        } finally { client.close() }
    }
}
