package app.yongin.xr_circuit.presentation.util

import android.content.Context
import app.yongin.xr_circuit.domain.TrackPath
import kotlinx.serialization.json.Json

fun Context.loadTrackPath(): TrackPath {
    val text = assets.open("silverstone_waypoints.json").bufferedReader().use { it.readText() }
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(text)
}