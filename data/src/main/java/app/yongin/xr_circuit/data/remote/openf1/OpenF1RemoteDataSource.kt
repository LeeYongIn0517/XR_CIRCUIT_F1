package app.yongin.xr_circuit.data.remote.openf1

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenF1RemoteDataSource @Inject constructor(
    private val api: OpenF1ApiService,
) {

    suspend fun fetchMeetingsByCircuitKey(
        circuitKey: Int,
        year: Int? = null,
    ): List<OpenF1MeetingDto> {
        return api.getMeetings(year = year, circuitKey = circuitKey)
    }

    suspend fun fetchMeetingsByShortName(
        circuitShortName: String,
        year: Int? = null,
    ): List<OpenF1MeetingDto> {
        return api.getMeetings(year = year, circuitShortName = circuitShortName)
    }
}
