package app.yongin.xr_circuit.data.mapper

import app.yongin.xr_circuit.data.local.dto.CircuitDetailDto
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CircuitMapperTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun mapsDetailDtoToDomain() {
        val dto = json.decodeFromString<CircuitDetailDto>(DETAIL_JSON)
        val domain = dto.toDomain()

        assertEquals("silverstone", domain.id)
        assertEquals(5.891, domain.lengthKm, 0.0001)
        assertEquals(24.0, domain.elevationDeltaM)
        assertEquals("T9", domain.corners.single().turnCode)
        assertEquals("t9", domain.corners.single().id)
        assertEquals("1:27.097", domain.lapRecord?.time)
    }

    @Test
    fun mapsCatalogDtoToDomain() {
        val catalog = json.decodeFromString<CircuitCatalogDto>(CATALOG_JSON)
        val summaries = catalog.circuits.map { it.toDomain() }

        assertEquals(1, summaries.size)
        assertTrue(summaries.first().modelAsset.endsWith(".glb"))
        assertEquals(2, summaries.first().openF1CircuitKey)
    }

    companion object {
        private val CATALOG_JSON = """
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

        private val DETAIL_JSON = """
            {
              "id": "silverstone",
              "name": "Silverstone Circuit",
              "country": "United Kingdom",
              "lengthKm": 5.891,
              "turns": 18,
              "drsZoneCount": 2,
              "location": { "latitude": 52.0786, "longitude": -1.0169 },
              "characteristics": {
                "elevationMinM": 148,
                "elevationMaxM": 172
              },
              "lapRecord": {
                "time": "1:27.097",
                "driver": "Max Verstappen",
                "team": "Red Bull Racing",
                "year": 2020
              },
              "corners": [
                { "number": 9, "name": "Copse", "turnCode": "T9" }
              ]
            }
        """.trimIndent()
    }
}
