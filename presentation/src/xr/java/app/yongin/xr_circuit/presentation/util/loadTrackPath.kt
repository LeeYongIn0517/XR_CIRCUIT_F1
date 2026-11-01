package app.yongin.xr_circuit.presentation.util

import android.content.Context
import app.yongin.xr_circuit.domain.TrackPath
import kotlinx.serialization.json.Json

fun Context.loadTrackPath(assetPath: String): TrackPath {
    val text = assets.open(assetPath).bufferedReader().use { it.readText() }
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(text)
}