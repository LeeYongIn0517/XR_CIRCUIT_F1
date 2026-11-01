package app.yongin.xr_circuit.data.platform
import android.content.Context
import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Cache
import java.io.File
import java.util.concurrent.TimeUnit
class AndroidAssetReader(private val context: Context) : AssetReader {
    override fun readText(path: String) = context.assets.open(path).bufferedReader().use { it.readText() }
    override fun exists(path: String) = runCatching { context.assets.open(path).use {} }.isSuccess
}
actual class PlatformTimeProvider actual constructor() : TimeProvider {
    actual override fun nowMillis() = System.currentTimeMillis()
}
fun androidHttpClient(context: Context, debug: Boolean = false) = androidHttpClient(context.cacheDir, debug)
internal fun androidHttpClient(cacheDir: File, debug: Boolean = false) = HttpClient(OkHttp) {
    expectSuccess = true
    engine { config {
        cache(Cache(File(cacheDir, "http_cache"), 10L * 1024 * 1024))
        if (debug) addInterceptor(HttpLoggingInterceptor { Log.d("XrCircuitHttp", it) }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    } }
}
