package app.yongin.xr_circuit.data.remote.jolpica

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class JolpicaCircuitDtoParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun parsesCircuitEndpointPayload() {
        val response = json.decodeFromString<JolpicaCircuitResponseDto>(SAMPLE)
        val circuit = response.mrData.circuitTable.circuits.single()

        assertEquals("silverstone", circuit.circuitId)
        assertEquals("Silverstone Circuit", circuit.circuitName)
        assertEquals("52.0786", circuit.location.lat)
        assertEquals("-1.01694", circuit.location.longitude)
        assertEquals("UK", circuit.location.country)
    }

    companion object {
        private val SAMPLE = """
            {
              "MRData": {
                "xmlns": "",
                "series": "f1",
                "url": "https://api.jolpi.ca/ergast/f1/circuits/silverstone.json",
                "limit": "30",
                "offset": "0",
                "total": "1",
                "CircuitTable": {
                  "circuitId": "silverstone",
                  "Circuits": [
                    {
                      "circuitId": "silverstone",
                      "url": "https://en.wikipedia.org/wiki/Silverstone_Circuit",
                      "circuitName": "Silverstone Circuit",
                      "Location": {
                        "lat": "52.0786",
                        "long": "-1.01694",
                        "locality": "Silverstone",
                        "country": "UK"
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
