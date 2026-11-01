@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package app.yongin.xr_circuit.data.platform
import platform.Foundation.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
class IosAssetReader(private val bundle: NSBundle = NSBundle.mainBundle) : AssetReader {
    override fun readText(path: String): String {
        val file = bundle.resourcePath + "/" + path
        return NSString.stringWithContentsOfFile(file, NSUTF8StringEncoding.toULong(), null)
            ?: error("Missing or unreadable asset: $path")
    }
    override fun exists(path: String) = NSFileManager.defaultManager.fileExistsAtPath(bundle.resourcePath + "/" + path)
}
actual class PlatformTimeProvider actual constructor() : TimeProvider {
    actual override fun nowMillis() = (NSDate().timeIntervalSince1970 * 1000).toLong()
}
fun iosHttpClient() = HttpClient(Darwin) {
    expectSuccess = true
    engine {
        // Ktor 3.0.3 overrides each request to ignore cache; restore HTTP semantics.
        configureRequest { setCachePolicy(NSURLRequestUseProtocolCachePolicy) }
        configureSession {
            timeoutIntervalForRequest = 30.0
            URLCache = NSURLCache(memoryCapacity = 0uL, diskCapacity = 10485760uL, diskPath = "http_cache")
            requestCachePolicy = NSURLRequestUseProtocolCachePolicy
        }
    }
}
