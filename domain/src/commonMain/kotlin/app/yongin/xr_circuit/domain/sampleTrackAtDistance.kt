package app.yongin.xr_circuit.domain

import kotlin.math.sqrt
import app.yongin.xr_circuit.domain.WayPoint

fun sampleTrackAtDistance(
    wps: List<WayPoint>,
    pathLength: Float,
    distance: Float
): Pair<TrackVector, TrackVector> {
    require(wps.isNotEmpty() && pathLength.isFinite() && pathLength > 0 && distance.isFinite())
    require(wps.all { it.position.size == 3 && it.forward.size == 3 })
    val d = ((distance % pathLength) + pathLength) % pathLength //경로 안으로 접은 거리, 출발점에서 트랙을 따라 실제로 몇 미터 지점인가
    val spacing = pathLength / wps.size // waypoint 하나당 담당 거리
    val i = (d / spacing).toInt() % wps.size //몇 번째 세그먼트까지 왔는지의 인덱스
    val j = (i + 1) % wps.size //다음 waypoint 인덱스
    val t = (d / spacing) - i //i와j 사이 구간 안에서의 진행 비율(0~1)
    //선형 보간
    fun lerp(a: List<Float>, b: List<Float>, t: Float): TrackVector = TrackVector(
        a[0] + (b[0] - a[0]) * t,
        a[1] + (b[1] - a[1]) * t,
        a[2] + (b[2] - a[2]) * t,
    )
    //보간된 위치와 방향
    val pos = lerp(wps[i].position, wps[j].position, t) //i와 j의 위치를 t로 섞은 3D 좌표
    val fwd = lerp(wps[i].forward, wps[j].forward, t).toNormalized() //i와 j의 진행 방향 벡터를 섞은 뒤 길이 1로 정규화
    return pos to fwd
}

data class TrackVector(val x: Float, val y: Float, val z: Float) {
    fun toNormalized(): TrackVector {
        val length = sqrt(x*x + y*y + z*z)
        return if (length > 0) TrackVector(x/length, y/length, z/length) else TrackVector(0f, 0f, 1f)
    }
}
