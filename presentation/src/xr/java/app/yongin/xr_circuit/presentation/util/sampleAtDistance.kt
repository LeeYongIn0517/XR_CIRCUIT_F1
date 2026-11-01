package app.yongin.xr_circuit.presentation.util
import androidx.xr.runtime.math.Vector3
import app.yongin.xr_circuit.domain.WayPoint
import app.yongin.xr_circuit.domain.sampleTrackAtDistance
fun sampleAtDistance(wps: List<WayPoint>, pathLength: Float, distance: Float): Pair<Vector3, Vector3> {
    val (position, forward) = sampleTrackAtDistance(wps, pathLength, distance)
    return Vector3(position.x, position.y, position.z) to Vector3(forward.x, forward.y, forward.z)
}
