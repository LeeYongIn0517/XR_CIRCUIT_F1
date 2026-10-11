package app.yongin.xr_circuit.presentation.viewmodel

import app.yongin.xr_circuit.domain.model.CircuitDetail
import app.yongin.xr_circuit.domain.model.CircuitSummary
import app.yongin.xr_circuit.domain.model.GeoLocation
import app.yongin.xr_circuit.domain.model.GridEntry
import app.yongin.xr_circuit.domain.model.TrackWeather
import app.yongin.xr_circuit.domain.repository.CircuitRepository
import app.yongin.xr_circuit.domain.repository.StartingGridRepository
import app.yongin.xr_circuit.domain.repository.WeatherRepository
import app.yongin.xr_circuit.domain.usecase.GetCircuitCatalogUseCase
import app.yongin.xr_circuit.domain.usecase.GetCircuitDetailUseCase
import app.yongin.xr_circuit.domain.usecase.GetStartingGridUseCase
import app.yongin.xr_circuit.domain.usecase.GetTrackWeatherUseCase
import app.yongin.xr_circuit.presentation.component.OverlayLayerId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CircuitViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val circuits = listOf(
        CircuitSummary("silverstone", "Silverstone", "SilverstoneCircuit.glb", "silverstone_waypoints.json"),
        CircuitSummary("albert_park", "Melbourne", "MelbourneCircuit.glb", "melbourne_waypoints.json"),
    )

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel(): CircuitViewModel {
        val repository = object : CircuitRepository {
            override fun getCatalog() = circuits
            override fun getCircuitDetail(circuitId: String) = CircuitDetail(
                circuitId, circuitId, "", 5.278, 14, 4, GeoLocation(0.0, 0.0),
            )
        }
        return CircuitViewModel(
            GetCircuitCatalogUseCase(repository),
            GetCircuitDetailUseCase(repository),
            GetTrackWeatherUseCase(object : WeatherRepository {
                override suspend fun getTrackWeather(circuitId: String): TrackWeather {
                    // Simulate an old network request that finishes despite cancellation.
                    withContext(NonCancellable) { delay(if (circuitId == "silverstone") 1000 else 10) }
                    return TrackWeather(if (circuitId == "silverstone") 11.0 else 28.0, 0.0, 0, 0.0, 0.0)
                }
            }),
            GetStartingGridUseCase(object : StartingGridRepository {
                override suspend fun getStartingGrid(circuitId: String): List<GridEntry> = emptyList()
            }),
        )
    }

    @Test fun selectionReplacesAssetsAndKeepsOverlayChoices() = runTest(dispatcher) {
        val vm = viewModel()
        val previous = vm.uiState.value.dockUiState
        vm.onIntent(CircuitIntent.ToggleOverlay(OverlayLayerId.Drs))
        vm.onIntent(CircuitIntent.ToggleOverlay(OverlayLayerId.Sectors))
        val toggled = vm.uiState.value.dockUiState
        assertFalse(previous == toggled)
        vm.onIntent(CircuitIntent.SelectCircuit("albert_park"))
        assertEquals(circuits[1], vm.uiState.value.selectedCircuit)
        assertEquals("Melbourne", vm.uiState.value.circuitName)
        assertEquals(toggled, vm.uiState.value.dockUiState)
        vm.onIntent(CircuitIntent.SelectCircuit("silverstone"))
        assertEquals(circuits[0], vm.uiState.value.selectedCircuit)
        assertEquals(toggled, vm.uiState.value.dockUiState)
        advanceUntilIdle()
    }

    @Test fun missingTrackIsIgnored() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onIntent(CircuitIntent.SelectCircuit("shanghai"))
        assertEquals("silverstone", vm.uiState.value.circuitId)
        assertEquals(circuits[0], vm.uiState.value.selectedCircuit)
        advanceUntilIdle()
    }

    @Test fun previousWeatherCannotOverwriteSelectedTrack() = runTest(dispatcher) {
        val vm = viewModel()
        runCurrent()
        vm.onIntent(CircuitIntent.SelectCircuit("albert_park"))
        assertEquals(null, vm.uiState.value.weather)
        advanceUntilIdle()
        assertEquals("albert_park", vm.uiState.value.circuitId)
        assertEquals("28°C", vm.uiState.value.weather?.airTemperature)
        assertFalse(vm.uiState.value.weatherLoading)
        assertTrue(vm.uiState.value.profileReady)
    }
}
