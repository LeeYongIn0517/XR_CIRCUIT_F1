package app.yongin.xr_circuit.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.SkyBlue400
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Track-conditions weather tile (Figma Weather Card, node `1:84`).
 *
 * Material 3 [Card] with optional photo backdrop + cool tonal wash.
 * Content is presentation-ready strings so API mapping can land later.
 */
@Composable
fun TrackWeatherCard(
    airTemperature: String,
    trackTemperatureLabel: String,
    rainChanceLabel: String,
    windLabel: String,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundRes != null) {
                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                ),
                            ),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SkyBlue400.copy(alpha = 0.2f)),
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text(
                        text = airTemperature,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Normal,
                            lineHeight = 48.sp,
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = trackTemperatureLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_weather_rain),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White,
                        )
                        Text(
                            text = rainChanceLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = windLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Track weather — with photo")
@PreviewLightDark
@Composable
private fun TrackWeatherCardPreview() {
    XR_CIRCUITTheme {
        TrackWeatherCard(
            airTemperature = "24°C",
            trackTemperatureLabel = "Track Temp: 31°C",
            rainChanceLabel = "10% Rain",
            windLabel = "Wind: 15km/h NW",
            backgroundRes = R.drawable.spa_rainy_weather,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Track weather — placeholder")
@Composable
private fun TrackWeatherCardPlaceholderPreview() {
    XR_CIRCUITTheme {
        TrackWeatherCard(
            airTemperature = "--°C",
            trackTemperatureLabel = "Track Temp: —",
            rainChanceLabel = "--% Rain",
            windLabel = "Wind: —",
            backgroundRes = null,
            modifier = Modifier.padding(8.dp),
        )
    }
}
