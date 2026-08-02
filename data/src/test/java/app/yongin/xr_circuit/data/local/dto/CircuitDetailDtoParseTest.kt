package app.yongin.xr_circuit.data.local.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CircuitDetailDtoParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun parsesSilverstoneSchema() {
        val dto = json.decodeFromString<CircuitDetailDto>(SILVERSTONE_SAMPLE)

        assertEquals("silverstone", dto.id)
        assertEquals(5.891, dto.lengthKm, 0.0001)
        assertEquals(18, dto.turns)
        assertEquals(2, dto.drsZoneCount)
        assertEquals("1:27.097", dto.lapRecord?.time)
        assertEquals("Max Verstappen", dto.lapRecord?.driver)
        assertTrue(dto.corners.any { it.name == "Copse" })
        assertEquals(1, dto.sectors.size)
        assertEquals(148.0, dto.characteristics?.elevationMinM)
        assertEquals(172.0, dto.characteristics?.elevationMaxM)
    }

    @Test
    fun parsesCatalogSchema() {
        val catalog = json.decodeFromString<CircuitCatalogDto>(CATALOG_SAMPLE)

        assertEquals(1, catalog.circuits.size)
        assertEquals("silverstone", catalog.circuits.first().id)
        assertEquals(2, catalog.circuits.first().openF1CircuitKey)
    }

    companion object {
        private val CATALOG_SAMPLE = """
            {
              "circuits": [
                {
                  "id": "silverstone",
                  "displayName": "Silverstone",
                  "detailAssetPath": "circuits/silverstone.json",
                  "modelAsset": "SilverstoneTrack.glb",
                  "waypointsAsset": "silverstone_waypoints.json",
                  "jolpicaCircuitId": "silverstone",
                  "openF1CircuitKey": 2,
                  "openF1CircuitShortName": "Silverstone"
                }
              ]
            }
        """.trimIndent()

        private val SILVERSTONE_SAMPLE = """
            {
              "id": "silverstone",
              "name": "Silverstone Circuit",
              "country": "United Kingdom",
              "lengthKm": 5.891,
              "turns": 18,
              "drsZoneCount": 2,
              "location": { "latitude": 52.0786, "longitude": -1.0169, "locality": "Silverstone" },
              "characteristics": {
                "elevationMinM": 148,
                "elevationMaxM": 172,
                "overtakingDifficulty": 2,
                "tyreWearIndex": 5,
                "fullThrottlePercent": 71
              },
              "lapRecord": {
                "time": "1:27.097",
                "driver": "Max Verstappen",
                "team": "Red Bull Racing",
                "year": 2020
              },
              "sectors": [
                { "index": 1, "startWaypoint": 0, "endWaypoint": 35, "colorHex": "#E10600" }
              ],
              "drsZones": [
                { "index": 1, "startWaypoint": 0, "endWaypoint": 8 }
              ],
              "corners": [
                {
                  "number": 9,
                  "name": "Copse",
                  "turnCode": "T9",
                  "waypointIndex": 30,
                  "anchor": { "x": 0.42, "y": 0.02, "z": -0.31 },
                  "description": "고속 우코너."
                }
              ],
              "pitLane": {
                "speedLimitKmh": 80,
                "entryWaypoint": 100,
                "exitWaypoint": 6
              }
            }
        """.trimIndent()
    }
}
