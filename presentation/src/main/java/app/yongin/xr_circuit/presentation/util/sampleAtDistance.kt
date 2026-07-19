package app.yongin.xr_circuit.presentation.util

import androidx.xr.runtime.math.Vector3
import app.yongin.xr_circuit.domain.WayPoint

fun sampleAtDistance(
    wps: List<WayPoint>,
    pathLength: Float,
    distance: Float
): Pair<Vector3, Vector3> {
    val d = ((distance % pathLength) + pathLength) % pathLength
    val spacing = pathLength / wps.size
    val i = (d / spacing).toInt() % wps.size
    val j = (i + 1) % wps.size
    val t = (d / spacing) - i
    fun lerp(a: List<Float>, b: List<Float>, t: Float): Vector3 = Vector3(
        a[0] + (b[0] - a[0]) * t,
        a[1] + (b[1] - a[1]) * t,
        a[2] + (b[2] - a[2]) * t,
    )
    val pos = lerp(wps[i].position, wps[j].position, t)
    val fwd = lerp(wps[i].forward, wps[j].forward, t).toNormalized()
    return pos to fwd
}