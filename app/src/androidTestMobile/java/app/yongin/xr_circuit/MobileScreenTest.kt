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
}
