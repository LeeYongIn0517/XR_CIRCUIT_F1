package app.yongin.xr_circuit.data.platform
interface AssetReader {
    fun readText(path: String): String
    fun exists(path: String): Boolean
}
fun interface TimeProvider { fun nowMillis(): Long }
expect class PlatformTimeProvider() : TimeProvider { override fun nowMillis(): Long }
