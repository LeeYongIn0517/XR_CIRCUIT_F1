package app.yongin.xr_circuit.data.mapper

import app.yongin.xr_circuit.data.local.dto.AnchorDto
import app.yongin.xr_circuit.data.local.dto.CircuitCatalogEntryDto
import app.yongin.xr_circuit.data.local.dto.CircuitCharacteristicsDto
import app.yongin.xr_circuit.data.local.dto.CircuitCornerDto
import app.yongin.xr_circuit.data.local.dto.CircuitDetailDto
import app.yongin.xr_circuit.data.local.dto.CircuitLocationDto
import app.yongin.xr_circuit.data.local.dto.DrsZoneDto
import app.yongin.xr_circuit.data.local.dto.LapRecordDto
import app.yongin.xr_circuit.data.local.dto.PitLaneDto
import app.yongin.xr_circuit.data.local.dto.SectorDto
import app.yongin.xr_circuit.domain.model.Anchor3d
import app.yongin.xr_circuit.domain.model.CircuitCharacteristics
import app.yongin.xr_circuit.domain.model.CircuitCorner
import app.yongin.xr_circuit.domain.model.CircuitDetail
import app.yongin.xr_circuit.domain.model.CircuitSummary
import app.yongin.xr_circuit.domain.model.DrsZone
import app.yongin.xr_circuit.domain.model.GeoLocation
import app.yongin.xr_circuit.domain.model.LapRecord
import app.yongin.xr_circuit.domain.model.PitLane
import app.yongin.xr_circuit.domain.model.Sector

internal fun CircuitCatalogEntryDto.toDomain(): CircuitSummary = CircuitSummary(
    id = id,
    displayName = displayName,
    modelAsset = modelAsset,
    waypointsAsset = waypointsAsset,
    jolpicaCircuitId = jolpicaCircuitId,
    openF1CircuitKey = openF1CircuitKey,
    openF1CircuitShortName = openF1CircuitShortName,
)

internal fun CircuitDetailDto.toDomain(): CircuitDetail = CircuitDetail(
    id = id,
    name = name,
    country = country,
    lengthKm = lengthKm,
    turns = turns,
    drsZoneCount = drsZoneCount,
    location = location.toDomain(),
    characteristics = characteristics?.toDomain(),
    lapRecord = lapRecord?.toDomain(),
    sectors = sectors.map { it.toDomain() },
    drsZones = drsZones.map { it.toDomain() },
    corners = corners.map { it.toDomain() },
    pitLane = pitLane?.toDomain(),
)

private fun CircuitLocationDto.toDomain(): GeoLocation = GeoLocation(
    latitude = latitude,
    longitude = longitude,
    locality = locality,
)

private fun CircuitCharacteristicsDto.toDomain(): CircuitCharacteristics = CircuitCharacteristics(
    elevationMinM = elevationMinM,
    elevationMaxM = elevationMaxM,
    overtakingDifficulty = overtakingDifficulty,
    tyreWearIndex = tyreWearIndex,
    fullThrottlePercent = fullThrottlePercent,
)

private fun LapRecordDto.toDomain(): LapRecord = LapRecord(
    time = time,
    driver = driver,
    team = team,
    year = year,
)

private fun SectorDto.toDomain(): Sector = Sector(
    index = index,
    startWaypoint = startWaypoint,
    endWaypoint = endWaypoint,
    colorHex = colorHex,
)

private fun DrsZoneDto.toDomain(): DrsZone = DrsZone(
    index = index,
    startWaypoint = startWaypoint,
    endWaypoint = endWaypoint,
)

private fun CircuitCornerDto.toDomain(): CircuitCorner = CircuitCorner(
    number = number,
    name = name,
    turnCode = turnCode ?: "T$number",
    waypointIndex = waypointIndex,
    anchor = anchor?.toDomain(),
    description = description,
)

private fun AnchorDto.toDomain(): Anchor3d = Anchor3d(x = x, y = y, z = z)

private fun PitLaneDto.toDomain(): PitLane = PitLane(
    speedLimitKmh = speedLimitKmh,
    entryWaypoint = entryWaypoint,
    exitWaypoint = exitWaypoint,
)
