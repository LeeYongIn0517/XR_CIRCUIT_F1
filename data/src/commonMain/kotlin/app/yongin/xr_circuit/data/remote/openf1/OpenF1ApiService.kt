package app.yongin.xr_circuit.data.remote.openf1
import app.yongin.xr_circuit.data.network.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
class OpenF1ApiService(private val client: HttpClient) {
    suspend fun getMeetings(
        year: Int? = null,
        circuitKey: Int? = null,
        circuitShortName: String? = null,
        meetingKey: Int? = null
    ): List<OpenF1MeetingDto> = DataJson.decodeFromString(client.get(ApiBases.OPEN_F1 + "v1/meetings") {
            if (year != null) parameter("year", year)
            if (circuitKey != null) parameter("circuit_key", circuitKey)
            if (circuitShortName != null) parameter("circuit_short_name", circuitShortName)
            if (meetingKey != null) parameter("meeting_key", meetingKey)
    }.bodyAsText())
}
