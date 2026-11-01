package app.yongin.xr_circuit.data.platform
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSUUID
import kotlin.test.*
/** Run with plan/kmp-migration/test-ios-http.py; excluded from normal offline tests. */
class DarwinHttpTest {
    @Test fun nativeHttpCacheAndNoStore() = runTest {
        val key = NSUUID().UUIDString
        val url = "http://127.0.0.1:18765/cache/$key"
        val first = iosHttpClient()
        try {
            assertEquals("1", first.get(url).bodyAsText())
            withContext(Dispatchers.Default) { delay(1500) }
            assertEquals("1", first.get(url).bodyAsText())
        } finally { first.close() }
        val second = iosHttpClient()
        try {
            assertEquals("1", second.get(url).bodyAsText())
            val fresh = "http://127.0.0.1:18765/no-store/$key"
            assertEquals("1", second.get(fresh).bodyAsText())
            assertEquals("2", second.get(fresh).bodyAsText())
        } finally { second.close() }
    }
    @Test fun nativeRequestTimeoutIsThirtySeconds() = runTest {
        val client = iosHttpClient()
        val started = PlatformTimeProvider().nowMillis()
        try {
            assertFails { client.get("http://127.0.0.1:18765/timeout/${NSUUID().UUIDString}") }
            val elapsed = PlatformTimeProvider().nowMillis() - started
            assertTrue(elapsed in 28000L..40000L, "timeout elapsed: $elapsed ms")
        } finally { client.close() }
    }
    @Test fun nativeCancellation() = runTest {
        val client = iosHttpClient()
        try {
            val request = async { client.get("http://127.0.0.1:18765/slow/${NSUUID().UUIDString}") }
            withContext(Dispatchers.Default) { delay(500) }
            request.cancelAndJoin()
            assertTrue(request.isCancelled)
            assertEquals("1", client.get("http://127.0.0.1:18765/no-store/${NSUUID().UUIDString}").bodyAsText())
        } finally { client.close() }
    }
}
