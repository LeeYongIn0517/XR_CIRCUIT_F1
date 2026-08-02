package app.yongin.xr_circuit.data.remote.openf1

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenF1MeetingDtoParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun parsesMeetingPayload() {
        val meetings = json.decodeFromString<List<OpenF1MeetingDto>>(SAMPLE)
        val meeting = meetings.single()

        assertEquals(1240, meeting.meetingKey)
        assertEquals("Silverstone", meeting.circuitShortName)
        assertEquals(2, meeting.circuitKey)
        assertEquals("United Kingdom", meeting.countryName)
    }

    companion object {
        private val SAMPLE = """
            [
              {
                "meeting_key": 1240,
                "meeting_name": "British Grand Prix",
                "meeting_official_name": "FORMULA 1 QATAR AIRWAYS BRITISH GRAND PRIX 2024",
                "location": "Silverstone",
                "country_key": 2,
                "country_code": "GBR",
                "country_name": "United Kingdom",
                "circuit_key": 2,
                "circuit_short_name": "Silverstone",
                "circuit_type": "Permanent",
                "circuit_info_url": "https://api.multiviewer.app/api/v1/circuits/2/2024",
                "date_start": "2024-07-05T11:30:00+00:00",
                "date_end": "2024-07-07T16:00:00+00:00",
                "year": 2024
              }
            ]
        """.trimIndent()
    }
}
