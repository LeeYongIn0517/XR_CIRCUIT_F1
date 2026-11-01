package app.yongin.xr_circuit.domain
import kotlin.test.*
class TrackViewportTest {
    @Test fun aspectFitAndTouchRoundTrip() {
        for ((w,h) in listOf(320f to 640f, 900f to 300f, 512f to 512f)) {
            val view = TrackViewport(1024f,1024f,w,h)
            val p = TrackPoint2D(.3f,.7f)
            val pixel = view.project(p)
            val back = assertNotNull(view.unproject(pixel.x,pixel.y))
            assertEquals(p.x,back.x,.00001f); assertEquals(p.y,back.y,.00001f)
            assertNull(view.unproject(0f,0f))
            assertNull(view.unproject(Float.NaN,20f))
        }
    }
    @Test fun invalidDimensionsFail() { assertFailsWith<IllegalArgumentException> { TrackViewport(0f,1f,1f,1f) } }
}
