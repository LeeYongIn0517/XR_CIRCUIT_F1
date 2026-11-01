package app.yongin.xr_circuit.domain.model

/**
 * Full circuit profile for the Left Spatial Panel and 3D overlays.
 */
data class CircuitDetail(
    val id: String,
    val name: String,
    val country: String,
    val lengthKm: Double,
    val turns: Int,
    val drsZoneCount: Int,
    val location: GeoLocation,
    val characteristics: CircuitCharacteristics? = null,
    val lapRecord: LapRecord? = null,
    val sectors: List<Sector> = emptyList(),
    val drsZones: List<DrsZone> = emptyList(),
    val corners: List<CircuitCorner> = emptyList(),
    val pitLane: PitLane? = null,
) {
    val elevationDeltaM: Double?
        get() {
            val min = characteristics?.elevationMinM ?: return null
            val max = characteristics?.elevationMaxM ?: return null
            return max - min
        }
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val locality: String? = null,
)

data class CircuitCharacteristics(
    val elevationMinM: Double? = null,
    val elevationMaxM: Double? = null,
    val overtakingDifficulty: Int? = null,
    val tyreWearIndex: Int? = null,
    val fullThrottlePercent: Int? = null,
)

data class LapRecord(
    val time: String,
    val driver: String,
    val team: String,
    val year: Int,
)

data class Sector(
    val index: Int,
    val startWaypoint: Int,
    val endWaypoint: Int,
    val colorHex: String? = null,
)

data class DrsZone(
    val index: Int,
    val startWaypoint: Int,
    val endWaypoint: Int,
)

data class CircuitCorner(
    val number: Int,
    val name: String,
    val turnCode: String,
    val waypointIndex: Int? = null,
    val anchor: Anchor3d? = null,
    val description: String? = null,
) {
    val id: String get() = "t$number"
}

data class Anchor3d(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class PitLane(
    val speedLimitKmh: Int,
    val entryWaypoint: Int? = null,
    val exitWaypoint: Int? = null,
)
