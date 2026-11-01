package app.yongin.xr_circuit.domain

import kotlinx.serialization.Serializable

@Serializable
data class WayPoint(
    val index: Int,
    val position: List<Float>, // [x, y, z]
    val forward: List<Float>
)
