package app.yongin.xr_circuit.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * UI model for a single quick-stat tile in [CircuitProfilePanel].
 * Keep presentation-only; domain mapping can be added later.
 */
data class CircuitStatUi(
    val label: String,
    val value: String,
    val unit: String? = null,
)

/**
 * UI model for the featured lap-record section.
 */
data class LapRecordUi(
    val time: String,
    val driver: String,
    val team: String,
    val year: Int,
)

/**
 * UI model for a key-corner list row.
 */
data class KeyCornerUi(
    val id: String,
    val turnCode: String,
    val name: String,
)

/**
 * Left Spatial Panel — Circuit Profile.
 *
 * Material 3 spatial-overlay surface that composes reusable tiles:
 * [CircuitStatCard], [LapRecordCard], and [KeyCornerListItem].
 *
 * Data binding is intentionally deferred: pass empty / sample values until
 * the ViewModel layer is wired.
 */
@Composable
fun CircuitProfilePanel(
    stats: List<CircuitStatUi>,
    lapRecord: LapRecordUi?,
    corners: List<KeyCornerUi>,
    selectedCornerId: String?,
    onCornerClick: (KeyCornerUi) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Circuit Profile",
    cornersSectionTitle: String = "Key Corners",
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PanelDragHandle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                QuickStatsGrid(stats = stats)

                if (lapRecord != null) {
                    LapRecordCard(
                        time = lapRecord.time,
                        driver = lapRecord.driver,
                        team = lapRecord.team,
                        year = lapRecord.year,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = cornersSectionTitle.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 265.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 4.dp),
                    ) {
                        items(
                            items = corners,
                            key = { it.id },
                        ) { corner ->
                            KeyCornerListItem(
                                turnCode = corner.turnCode,
                                name = corner.name,
                                selected = corner.id == selectedCornerId,
                                onClick = { onCornerClick(corner) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(width = 40.dp, height = 4.dp),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        ) {}
    }
}

@Composable
private fun QuickStatsGrid(
    stats: List<CircuitStatUi>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        stats.chunked(2).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowStats.forEach { stat ->
                    CircuitStatCard(
                        label = stat.label,
                        value = stat.value,
                        unit = stat.unit,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keep grid alignment when the last row has a single item.
                if (rowStats.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// region Sample / Preview data (replace when ViewModel is wired)

internal val SampleCircuitStats = listOf(
    CircuitStatUi(label = "Length", value = "7.004", unit = "km"),
    CircuitStatUi(label = "Turns", value = "20"),
    CircuitStatUi(label = "DRS Zones", value = "2"),
    CircuitStatUi(label = "G-Force", value = "5.2"),
)

internal val SampleLapRecord = LapRecordUi(
    time = "1:41.252",
    driver = "Charles Leclerc",
    team = "Ferrari",
    year = 2023,
)

internal val SampleKeyCorners = listOf(
    KeyCornerUi(id = "t1", turnCode = "T1", name = "La Source"),
    KeyCornerUi(id = "t2-4", turnCode = "T2-4", name = "Eau Rouge"),
    KeyCornerUi(id = "t18-19", turnCode = "T18-19", name = "Blanchimont"),
)

// endregion

@Preview(showBackground = true, widthDp = 320, heightDp = 720, name = "Circuit Profile Panel")
@PreviewLightDark
@Composable
private fun CircuitProfilePanelPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CircuitProfilePanel(
                stats = SampleCircuitStats,
                lapRecord = SampleLapRecord,
                corners = SampleKeyCorners,
                selectedCornerId = "t2-4",
                onCornerClick = {},
                modifier = Modifier.width(288.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480, name = "Empty data skeleton")
@Composable
private fun CircuitProfilePanelEmptyPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CircuitProfilePanel(
                stats = emptyList(),
                lapRecord = null,
                corners = emptyList(),
                selectedCornerId = null,
                onCornerClick = {},
                modifier = Modifier.width(288.dp),
            )
        }
    }
}
