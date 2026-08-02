package app.yongin.xr_circuit.data.local.dto

import kotlinx.serialization.Serializable

/**
 * Static circuit detail used by the Left Spatial Panel (and later 3D overlays).
 *
 * File: `assets/circuits/{id}.json`
 *
 * Open APIs do **not** provide length / turns / DRS / named corners / official lap
 * records, so these fields live in local assets. Jolpica can only enrich
 * name / country / lat-long (see [app.yongin.xr_circuit.data.remote.jolpica]).
 */
@Serializable
data class CircuitDetailDto(
    val id: String,
    val name: String,
    val country: String,
    val lengthKm: Double,
    val turns: Int,
    val drsZoneCount: Int,
    val location: CircuitLocationDto,
    val characteristics: CircuitCharacteristicsDto? = null,
    val lapRecord: LapRecordDto? = null,
    val sectors: List<SectorDto> = emptyList(),
    val drsZones: List<DrsZoneDto> = emptyList(),
    val corners: List<CircuitCornerDto> = emptyList(),
    val pitLane: PitLaneDto? = null,
)

@Serializable
data class CircuitLocationDto(
    val latitude: Double,
    val longitude: Double,
    val locality: String? = null,
)

@Serializable
data class CircuitCharacteristicsDto(
    val elevationMinM: Double? = null,
    val elevationMaxM: Double? = null,
    val overtakingDifficulty: Int? = null,
    val tyreWearIndex: Int? = null,
    val fullThrottlePercent: Int? = null,
)

@Serializable
data class LapRecordDto(
    val time: String,
    val driver: String,
    val team: String,
    val year: Int,
)

@Serializable
data class SectorDto(
    val index: Int,
    val startWaypoint: Int,
    val endWaypoint: Int,
    val colorHex: String? = null,
)

@Serializable
data class DrsZoneDto(
    val index: Int,
    val startWaypoint: Int,
    val endWaypoint: Int,
)

@Serializable
data class CircuitCornerDto(
    val number: Int,
    val name: String,
    val turnCode: String? = null,
    val waypointIndex: Int? = null,
    val anchor: AnchorDto? = null,
    val description: String? = null,
)

@Serializable
data class AnchorDto(
    val x: Float,
    val y: Float,
    val z: Float,
)

@Serializable
data class PitLaneDto(
    val speedLimitKmh: Int,
    val entryWaypoint: Int? = null,
    val exitWaypoint: Int? = null,
)
