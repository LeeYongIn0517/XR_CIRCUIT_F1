package app.yongin.xr_circuit.data.remote.openf1

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenF1 API.
 *
 * Base: `https://api.openf1.org/`
 * Primary use later: starting grid / drivers / session weather (Right Panel).
 * For Left Panel, meetings only help resolve circuit identity keys.
 */
interface OpenF1ApiService {

    @GET("v1/meetings")
    suspend fun getMeetings(
        @Query("year") year: Int? = null,
        @Query("circuit_key") circuitKey: Int? = null,
        @Query("circuit_short_name") circuitShortName: String? = null,
        @Query("meeting_key") meetingKey: Int? = null,
    ): List<OpenF1MeetingDto>
}
