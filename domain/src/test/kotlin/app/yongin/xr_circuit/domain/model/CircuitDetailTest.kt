package app.yongin.xr_circuit.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CircuitDetailTest {

    @Test
    fun elevationDeltaUsesCharacteristics() {
        val detail = CircuitDetail(
            id = "silverstone",
            name = "Silverstone Circuit",
            country = "United Kingdom",
            lengthKm = 5.891,
            turns = 18,
            drsZoneCount = 2,
            location = GeoLocation(52.0786, -1.0169),
            characteristics = CircuitCharacteristics(
                elevationMinM = 148.0,
                elevationMaxM = 172.0,
            ),
        )

        assertEquals(24.0, detail.elevationDeltaM)
    }

    @Test
    fun elevationDeltaNullWhenIncomplete() {
        val detail = CircuitDetail(
            id = "x",
            name = "X",
            country = "Y",
            lengthKm = 1.0,
            turns = 1,
            drsZoneCount = 0,
            location = GeoLocation(0.0, 0.0),
            characteristics = CircuitCharacteristics(elevationMinM = 10.0),
        )

        assertNull(detail.elevationDeltaM)
    }

    @Test
    fun cornerIdDerivedFromNumber() {
        val corner = CircuitCorner(number = 10, name = "Maggotts", turnCode = "T10")
        assertEquals("t10", corner.id)
    }
}
