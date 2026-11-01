package app.yongin.xr_circuit.domain.usecase

import app.yongin.xr_circuit.domain.model.*
import app.yongin.xr_circuit.domain.repository.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class UseCaseTest {
    private val summary = CircuitSummary("silverstone", "Silverstone", "track.glb", "path.json")
    private val detail = CircuitDetail("silverstone", "Silverstone", "UK", 5.891, 18, 2, GeoLocation(52.0786, -1.0169))
    private val repository = object : CircuitRepository {
        override fun getCatalog() = listOf(summary)
        override fun getCircuitDetail(circuitId: String): CircuitDetail {
            require(circuitId == detail.id)
            return detail
        }
    }
    @Test fun catalogAndDetailDelegateToRepository() {
        assertEquals(listOf(summary), GetCircuitCatalogUseCase(repository)())
        assertEquals(detail, GetCircuitDetailUseCase(repository)(summary.id))
        assertFailsWith<IllegalArgumentException> { GetCircuitDetailUseCase(repository)("missing") }
    }
    @Test fun suspendUseCasesForwardCircuitIdAndResults() = runTest {
        val weather = TrackWeather(8.0, 0.0, 1, 10.0, 90.0)
        val weatherRepository = object : WeatherRepository {
            override suspend fun getTrackWeather(circuitId: String): TrackWeather {
                assertEquals("suzuka", circuitId)
                return weather
            }
        }
        val entries = listOf(GridEntry(1, Driver(1, "Fixture Driver", "FIX", "Fixture Team")))
        val gridRepository = object : StartingGridRepository {
            override suspend fun getStartingGrid(circuitId: String): List<GridEntry> {
                assertEquals("suzuka", circuitId)
                return entries
            }
        }
        assertEquals(weather, GetTrackWeatherUseCase(weatherRepository)("suzuka"))
        assertEquals(entries, GetStartingGridUseCase(gridRepository)("suzuka"))
    }
}
