package app.yongin.xr_circuit.domain

import app.yongin.xr_circuit.domain.model.*
import app.yongin.xr_circuit.domain.repository.*
import app.yongin.xr_circuit.domain.usecase.*
import app.yongin.xr_circuit.domain.state.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CircuitStoreTest {
    private class Fixture(scope: CoroutineScope) {
        val calls = mutableListOf<Pair<String, CompletableDeferred<TrackWeather>>>()
        val grids = mutableListOf<CompletableDeferred<List<GridEntry>>>()
        var failProfile = false
        var failCatalog = false
        val store = CircuitStore(
            GetCircuitCatalogUseCase(object : CircuitRepository {
                override fun getCatalog(): List<CircuitSummary> {
                    if (failCatalog) error("catalog")
                    return listOf("silverstone", "suzuka").map { CircuitSummary(it, it, "", "") }
                }
                override fun getCircuitDetail(circuitId: String): CircuitDetail = error("unused")
            }),
            GetCircuitDetailUseCase(object : CircuitRepository {
                override fun getCatalog(): List<CircuitSummary> = emptyList()
                override fun getCircuitDetail(circuitId: String): CircuitDetail {
                    if (failProfile) error("asset missing")
                    return CircuitDetail(circuitId, circuitId, "", 5.0, 2, 1, GeoLocation(0.0, 0.0),
                        corners = listOf(CircuitCorner(1, "First", "T1"), CircuitCorner(2, "Second", "T2")))
                }
            }),
            GetTrackWeatherUseCase(object : WeatherRepository {
                override suspend fun getTrackWeather(circuitId: String): TrackWeather {
                    val result = CompletableDeferred<TrackWeather>()
                    calls += circuitId to result
                    // Deliberately ignore request cancellation to exercise generation checks.
                    return withContext(NonCancellable) { result.await() }
                }
            }),
            GetStartingGridUseCase(object : StartingGridRepository {
                override suspend fun getStartingGrid(circuitId: String): List<GridEntry> {
                    val result = CompletableDeferred<List<GridEntry>>()
                    grids += result
                    return withContext(NonCancellable) { result.await() }
                }
            }), scope)
        fun finish() {
            calls.forEach { it.second.complete(TrackWeather(0.0, 0.0, 0, 0.0, 0.0)) }
            grids.forEach { it.complete(emptyList()) }
            store.close()
        }
    }
    private fun weather(n: Double) = TrackWeather(n, 0.0, 0, 0.0, 0.0)
    @Test fun rapidABARejectsOldResponsesAndKeepsOverlays() = runTest {
        val f = Fixture(this); runCurrent()
        f.store.toggleOverlay(Overlay.Drs)
        f.store.selectCircuit("suzuka"); runCurrent()
        f.store.selectCircuit("silverstone"); runCurrent()
        f.calls[2].second.complete(weather(30.0)); runCurrent()
        f.calls[0].second.complete(weather(10.0)); f.calls[1].second.complete(weather(20.0)); runCurrent()
        f.grids[0].completeExceptionally(IllegalStateException("old grid error")); runCurrent()
        assertEquals(30.0, f.store.state.value.weather?.temperatureC)
        assertNull(f.store.state.value.gridError)
        assertEquals(setOf(Overlay.Sectors, Overlay.Drs), f.store.state.value.overlays)
        assertEquals("silverstone", f.store.state.value.detail?.id)
        f.finish()
    }
    @Test fun unknownSelectionDoesNotReplaceCurrentCircuit() = runTest {
        val f = Fixture(this); runCurrent()
        f.store.selectCircuit("missing")
        assertEquals(CircuitError.UnknownCircuit, f.store.state.value.selectionError)
        assertEquals("silverstone", f.store.state.value.selectedCircuit?.id)
        f.store.selectCircuit("silverstone")
        assertNull(f.store.state.value.selectionError)
        assertEquals(1, f.calls.size)
        f.finish()
    }
    @Test fun failureThenRetryClearsErrors() = runTest {
        val f = Fixture(this); runCurrent()
        f.calls[0].second.completeExceptionally(IllegalStateException("network")); runCurrent()
        assertEquals(CircuitError.WeatherUnavailable, f.store.state.value.weatherError)
        assertFalse(f.store.state.value.weatherLoading)
        f.failProfile = true; f.store.retry(); runCurrent()
        assertNull(f.store.state.value.detail)
        assertEquals(CircuitError.ProfileUnavailable, f.store.state.value.profileError)
        f.failProfile = false; f.store.retry(); runCurrent()
        f.calls.last().second.complete(weather(7.0)); runCurrent()
        assertNull(f.store.state.value.profileError); assertNull(f.store.state.value.weatherError)
        assertEquals(7.0, f.store.state.value.weather?.temperatureC)
        f.finish()
    }
    @Test fun observationCancellationAndResubscriptionDoNotDuplicateUpdates() = runTest {
        val f = Fixture(this); var first = 0; var second = 0
        val one = f.store.observe(this) { first++ }; runCurrent()
        one.cancel(); val before = first
        val two = f.store.observe(this) { second++ }; runCurrent()
        f.store.toggleOverlay(Overlay.Drs); runCurrent()
        assertEquals(before, first); assertEquals(2, second)
        two.cancel(); f.finish()
    }
    @Test fun closeIgnoresLateResultsAndActionsWithoutCancellingParent() = runTest {
        val f = Fixture(this); runCurrent(); f.store.close()
        val before = f.store.state.value
        f.calls[0].second.complete(weather(99.0)); runCurrent()
        f.store.selectCircuit("suzuka"); f.store.toggleOverlay(Overlay.Pins); f.store.retry()
        assertEquals(before, f.store.state.value); assertTrue(coroutineContext[Job]!!.isActive)
        f.finish()
    }
    @Test fun cornerSelectionAndCircuitChangeUseDomainIdentifiers() = runTest {
        val f = Fixture(this); runCurrent()
        assertEquals("t2", f.store.state.value.selectedCornerId)
        f.store.selectCorner("t1"); assertEquals("t1", f.store.state.value.selectedCornerId)
        f.store.selectCorner("missing"); assertEquals("t1", f.store.state.value.selectedCornerId)
        f.store.selectCircuit("suzuka"); runCurrent()
        assertEquals("t2", f.store.state.value.selectedCornerId)
        f.finish()
    }
    @Test fun interpolationWrapsNegativeAndFullLapDistances() {
        val points = listOf(WayPoint(0, listOf(0f,0f,0f), listOf(1f,0f,0f)), WayPoint(1, listOf(10f,0f,0f), listOf(1f,0f,0f)))
        assertEquals(5f, sampleTrackAtDistance(points,20f,5f).first.x)
        assertEquals(5f, sampleTrackAtDistance(points,20f,-5f).first.x)
        assertEquals(0f, sampleTrackAtDistance(points,20f,20f).first.x)
        assertEquals(1f, sampleTrackAtDistance(points,20f,5f).second.x)
        assertFailsWith<IllegalArgumentException> { sampleTrackAtDistance(emptyList(),20f,0f) }
        assertFailsWith<IllegalArgumentException> { sampleTrackAtDistance(points,0f,0f) }
    }
}
