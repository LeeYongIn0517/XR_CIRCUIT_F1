package app.yongin.xr_circuit.domain

import kotlinx.serialization.Serializable

@Serializable
data class TrackPath(
    val path_length_m: Float,
    val closed_loop: Boolean,
    val waypoints_gltf_yup: List<WayPoint>
)