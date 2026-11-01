package app.yongin.xr_circuit.domain

/** Shared aspect-fit transform for bitmap, overlays, markers and inverse hit testing. */
data class TrackPoint2D(val x: Float, val y: Float)
class TrackViewport(imageWidth: Float, imageHeight: Float, width: Float, height: Float) {
    init { require(listOf(imageWidth, imageHeight, width, height).all { it.isFinite() && it > 0 }) }
    private val scale = minOf(width / imageWidth, height / imageHeight)
    val contentWidth = imageWidth * scale
    val contentHeight = imageHeight * scale
    val left = (width - contentWidth) / 2
    val top = (height - contentHeight) / 2
    fun project(point: TrackPoint2D) = TrackPoint2D(left + point.x * contentWidth, top + point.y * contentHeight)
    /** Reject letterboxing and transparent source padding instead of clamping touches. */
    fun unproject(x: Float, y: Float, padding: Float = 0.08f): TrackPoint2D? {
        require(padding >= 0 && padding < 0.5f)
        val p = TrackPoint2D((x - left) / contentWidth, (y - top) / contentHeight)
        return p.takeIf { it.x in padding..(1-padding) && it.y in padding..(1-padding) }
    }
}
