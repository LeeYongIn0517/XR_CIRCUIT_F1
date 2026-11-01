package app.yongin.xr_circuit.data.remote.openf1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenF1 meeting entry.
 *
 * GET `https://api.openf1.org/v1/meetings?year={year}&circuit_key={key}`
 * or `...?circuit_short_name=Silverstone`
 *
 * Provides GP identity + circuit keys. Specs / lap records / named corners
 * are not available here. `circuit_info_url` points at MultiViewer (unofficial)
 * and is intentionally unused for Left Panel static fields.
 */
@Serializable
data class OpenF1MeetingDto(
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("meeting_name") val meetingName: String? = null,
    @SerialName("meeting_official_name") val meetingOfficialName: String? = null,
    val location: String? = null,
    @SerialName("country_key") val countryKey: Int? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("country_name") val countryName: String? = null,
    @SerialName("circuit_key") val circuitKey: Int? = null,
    @SerialName("circuit_short_name") val circuitShortName: String? = null,
    @SerialName("circuit_type") val circuitType: String? = null,
    @SerialName("circuit_info_url") val circuitInfoUrl: String? = null,
    @SerialName("date_start") val dateStart: String? = null,
    @SerialName("date_end") val dateEnd: String? = null,
    val year: Int? = null,
)
