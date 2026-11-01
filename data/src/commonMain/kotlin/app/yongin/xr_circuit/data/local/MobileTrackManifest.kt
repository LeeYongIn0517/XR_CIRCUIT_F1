package app.yongin.xr_circuit.data.local

import app.yongin.xr_circuit.data.platform.AssetReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class CircuitAssetTarget { XR, MOBILE }

@Serializable
data class MobileTrackManifest(val schemaVersion: Int, val circuits: List<MobileTrackAssets>)

@Serializable
data class MobileTrackAssets(
    val circuitId: String, val track: String, val sectors: String,
    val drs: String, val path: String, val width: Int, val height: Int,
) {
    fun requiredPaths() = listOf(track, sectors, drs, path)
}

/** Missing manifest means no mobile circuits. Invalid manifests surface as load errors. */
class MobileTrackResolver(private val assets: AssetReader, private val json: Json) {
    fun available(): Map<String, MobileTrackAssets> {
        if (!assets.exists("tracks/index.json")) return emptyMap()
        val manifest = json.decodeFromString<MobileTrackManifest>(assets.readText("tracks/index.json"))
        require(manifest.schemaVersion == 1) { "Unsupported mobile asset schema" }
        require(manifest.circuits.map { it.circuitId }.distinct().size == manifest.circuits.size)
        return manifest.circuits.filter { entry ->
            require(entry.width > 0 && entry.height > 0)
            require(entry.requiredPaths().all { it.startsWith("tracks/${entry.circuitId}/") && ".." !in it })
            entry.requiredPaths().all(assets::exists)
        }.associateBy { it.circuitId }
    }
}
