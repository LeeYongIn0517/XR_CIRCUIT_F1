package app.yongin.xr_circuit.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.TeamFerrari
import app.yongin.xr_circuit.presentation.theme.TeamMcLaren
import app.yongin.xr_circuit.presentation.theme.TeamRedBull
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * UI model for the Track Conditions weather card.
 * Keep presentation-only; Open-Meteo mapping can be added later.
 */
data class TrackWeatherUi(
    val airTemperature: String,
    val trackTemperatureLabel: String,
    val rainChanceLabel: String,
    val windLabel: String,
    @DrawableRes val backgroundRes: Int? = null,
)

/**
 * UI model for a starting-grid driver row.
 */
data class GridDriverUi(
    val id: String,
    val position: Int,
    val driverName: String,
    val teamName: String,
    val teamColor: Color,
    val progress: Float? = null,
    val emphasized: Boolean = true,
)

/**
 * UI model for the strategy / insight callout under the grid.
 */
data class StrategyInsightUi(
    val label: String,
    val body: String,
)

/**
 * Right Spatial Panel — Live Environment & Grid (Figma node `1:78`).
 *
 * Material 3 spatial-overlay surface composing:
 * [TrackWeatherCard], [StartingGridItem], and [StrategyInsightCard].
 *
 * Data binding is intentionally deferred: pass empty / sample values until
 * the ViewModel layer is wired.
 */
@Composable
fun GridWeatherPanel(
    weather: TrackWeatherUi?,
    drivers: List<GridDriverUi>,
    strategy: StrategyInsightUi?,
    modifier: Modifier = Modifier,
    weatherSectionTitle: String = "Track Conditions",
    gridSectionTitle: String = "Starting Grid",
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
                    text = weatherSectionTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (weather != null) {
                    TrackWeatherCard(
                        airTemperature = weather.airTemperature,
                        trackTemperatureLabel = weather.trackTemperatureLabel,
                        rainChanceLabel = weather.rainChanceLabel,
                        windLabel = weather.windLabel,
                        backgroundRes = weather.backgroundRes,
                    )
                }

                Text(
                    text = gridSectionTitle,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 4.dp),
                ) {
                    items(
                        items = drivers,
                        key = { it.id },
                    ) { driver ->
                        StartingGridItem(
                            position = driver.position,
                            driverName = driver.driverName,
                            teamName = driver.teamName,
                            teamColor = driver.teamColor,
                            progress = driver.progress,
                            emphasized = driver.emphasized,
                        )
                    }
                }

                if (strategy != null) {
                    StrategyInsightCard(
                        label = strategy.label,
                        body = strategy.body,
                    )
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

// region Sample / Preview data (replace when ViewModel is wired)

internal val SampleTrackWeather = TrackWeatherUi(
    airTemperature = "24°C",
    trackTemperatureLabel = "Track Temp: 31°C",
    rainChanceLabel = "10% Rain",
    windLabel = "Wind: 15km/h NW",
    backgroundRes = R.drawable.spa_rainy_weather,
)

internal val SampleGridDrivers = listOf(
    GridDriverUi(
        id = "ver",
        position = 1,
        driverName = "Verstappen",
        teamName = "Red Bull",
        teamColor = TeamRedBull,
        progress = 1f,
        emphasized = true,
    ),
    GridDriverUi(
        id = "nor",
        position = 2,
        driverName = "Norris",
        teamName = "McLaren",
        teamColor = TeamMcLaren,
        progress = 0.75f,
        emphasized = true,
    ),
    GridDriverUi(
        id = "lec",
        position = 3,
        driverName = "Leclerc",
        teamName = "Ferrari",
        teamColor = TeamFerrari,
        progress = 0.67f,
        emphasized = true,
    ),
    GridDriverUi(
        id = "sai",
        position = 4,
        driverName = "Sainz",
        teamName = "Ferrari",
        teamColor = TeamFerrari,
        progress = null,
        emphasized = false,
    ),
)

internal val SampleStrategyInsight = StrategyInsightUi(
    label = "Tire Strategy",
    body = "Expected 2-stop race. Soft (12-15 laps) followed by Medium-Medium.",
)

// endregion

@Preview(showBackground = true, widthDp = 360, heightDp = 820, name = "Grid Weather Panel")
@PreviewLightDark
@Composable
private fun GridWeatherPanelPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GridWeatherPanel(
                weather = SampleTrackWeather,
                drivers = SampleGridDrivers,
                strategy = SampleStrategyInsight,
                modifier = Modifier.width(320.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 480, name = "Empty data skeleton")
@Composable
private fun GridWeatherPanelEmptyPreview() {
    XR_CIRCUITTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GridWeatherPanel(
                weather = null,
                drivers = emptyList(),
                strategy = null,
                modifier = Modifier.width(320.dp),
            )
        }
    }
}
