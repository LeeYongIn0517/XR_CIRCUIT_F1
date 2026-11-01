package app.yongin.xr_circuit.data.platform
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.nio.file.Files
import kotlin.test.*
class HttpDiskCacheTest {
    @Test fun readTimeoutRemainsThirtySeconds() = runTest {
        val directory = Files.createTempDirectory("circuit-timeout-test").toFile()
        val server = MockWebServer()
        server.start()
        val client = androidHttpClient(directory)
        try {
            server.enqueue(MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.NO_RESPONSE))
            val started = System.nanoTime()
            assertFails { client.get(server.url("/timeout").toString()) }
            val elapsed = (System.nanoTime() - started) / 1_000_000
            assertTrue(elapsed in 28000L..40000L, "timeout elapsed: $elapsed ms")
        } finally { client.close(); server.shutdown(); directory.deleteRecursively() }
    }
    @Test fun httpCacheSurvivesClientRecreationAndHonorsNoStore() = runTest {
        val directory = Files.createTempDirectory("circuit-http-test").toFile()
        val server = MockWebServer()
        server.start()
        val first = androidHttpClient(directory)
        try {
            server.enqueue(MockResponse().setHeader("Cache-Control", "max-age=600").setBody("cached"))
            val url = server.url("/cache").toString()
            assertEquals("cached", first.get(url).bodyAsText())
            // Closing the engine persists and releases its OkHttp disk cache.
            first.close()
            val second = androidHttpClient(directory)
            try {
                assertEquals("cached", second.get(url).bodyAsText())
                assertEquals(1, server.requestCount)
                repeat(2) { index ->
                    server.enqueue(MockResponse().setHeader("Cache-Control", "no-store").setBody("fresh-$index"))
                    assertEquals("fresh-$index", second.get(server.url("/no-store").toString()).bodyAsText())
                }
                assertEquals(3, server.requestCount)
            } finally { second.close() }
        } finally { first.close(); server.shutdown(); directory.deleteRecursively() }
    }
}
