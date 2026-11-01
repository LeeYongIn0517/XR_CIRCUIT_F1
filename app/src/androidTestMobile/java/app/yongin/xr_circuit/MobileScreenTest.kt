package app.yongin.xr_circuit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MobileScreenTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun selectionOverlayAndRecreation() {
        rule.waitUntil(15000) { rule.onAllNodesWithText("Silverstone").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("DRS").performClick().assertIsSelected()
        rule.onNodeWithText("Silverstone").performClick()
        rule.onNodeWithText("Suzuka").performClick()
        rule.onNodeWithText("DRS").assertIsSelected()
        rule.onNodeWithText("Suzuka").assertExists()
        rule.onNodeWithText("Marker").performClick().assertIsNotSelected()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Marker").assertIsNotSelected()
        rule.onNodeWithText("Sectors").assertIsSelected()
    }
    @Test fun allCircuitsLoadAndKeepOverlaySelection() {
        rule.waitUntil(15000) { rule.onAllNodesWithText("Silverstone").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("DRS").performClick()
        var current = "Silverstone"
        for (name in listOf("Melbourne", "Shanghai", "Suzuka", "Miami", "Montreal", "Monaco", "Barcelona", "Red Bull Ring", "Silverstone", "Spa-Francorchamps", "Hungaroring", "Zandvoort", "Monza", "Madring", "Baku", "Sepang", "Singapore", "Austin", "Mexico City", "Interlagos", "Las Vegas", "Lusail", "Yas Marina")) {
            rule.onNodeWithText(current).performClick()
            rule.onAllNodesWithText(name).onLast().performScrollTo().performClick()
            rule.waitUntil(15000) {
                rule.onAllNodes(hasContentDescription("$name 2D track.", substring = true)).fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("DRS").assertIsSelected()
            rule.onNodeWithText("Sectors").assertIsSelected()
            current = name
        }
    }
    @Test fun decodeEveryBundledTrackAndRecordCost() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val manifest = org.json.JSONObject(context.assets.open("tracks/index.json").bufferedReader().use { it.readText() }).getJSONArray("circuits")
        org.junit.Assert.assertEquals(23, manifest.length())
        for (i in 0 until manifest.length()) {
            val entry = manifest.getJSONObject(i)
            val start = android.os.SystemClock.elapsedRealtimeNanos()
            var bytes = 0
            for (key in listOf("track", "sectors", "drs")) {
                val bitmap = context.assets.open(entry.getString(key)).use { android.graphics.BitmapFactory.decodeStream(it) }
                org.junit.Assert.assertNotNull(bitmap)
                org.junit.Assert.assertEquals(1024, bitmap.width)
                org.junit.Assert.assertEquals(1024, bitmap.height)
                bytes += bitmap.allocationByteCount
                bitmap.recycle()
            }
            android.util.Log.i("Stage8Metric", "${entry.getString("circuitId")} decode3_ms=${(android.os.SystemClock.elapsedRealtimeNanos()-start)/1e6} bitmap_bytes=$bytes")
        }
    }
}
