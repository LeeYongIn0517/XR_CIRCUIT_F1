package app.yongin.xr_circuit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.yongin.xr_circuit.presentation.theme.Slate100
import app.yongin.xr_circuit.presentation.theme.TeamFerrari
import app.yongin.xr_circuit.presentation.theme.TeamMcLaren
import app.yongin.xr_circuit.presentation.theme.TeamRedBull
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Single starting-grid driver row (Figma Driver 1–4, nodes `1:105`–`1:132`).
 *
 * Uses Material 3 [Surface] + [LinearProgressIndicator]. Team accent is passed
 * in so constructor/team colors stay presentation-flexible until domain mapping.
 *
 * @param progress Gap / pace indicator in `0f..1f`, or `null` to hide the bar.
 * @param emphasized `false` dims the row (e.g. lower grid slots in the mock).
 */
@Composable
fun StartingGridItem(
    position: Int,
    driverName: String,
    teamName: String,
    teamColor: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    emphasized: Boolean = true,
) {
    val containerAlpha = if (emphasized) 0.6f else 0.3f
    val contentAlpha = if (emphasized) 1f else 0.6f
    val shape = MaterialTheme.shapes.extraSmall

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha),
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = containerAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (emphasized) 1.dp else 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(teamColor),
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = position.toString(),
                    modifier = Modifier.width(16.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = teamColor,
                    maxLines = 1,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = driverName.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = teamName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                lineHeight = 15.sp,
                            ),
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = teamColor,
                            trackColor = Slate100,
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Grid item — P1")
@PreviewLightDark
@Composable
private fun StartingGridItemP1Preview() {
    XR_CIRCUITTheme {
        StartingGridItem(
            position = 1,
            driverName = "Verstappen",
            teamName = "Red Bull",
            teamColor = TeamRedBull,
            progress = 1f,
            emphasized = true,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Grid item — P2")
@Composable
private fun StartingGridItemP2Preview() {
    XR_CIRCUITTheme {
        StartingGridItem(
            position = 2,
            driverName = "Norris",
            teamName = "McLaren",
            teamColor = TeamMcLaren,
            progress = 0.75f,
            emphasized = true,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Grid item — dimmed")
@Composable
private fun StartingGridItemDimmedPreview() {
    XR_CIRCUITTheme {
        StartingGridItem(
            position = 4,
            driverName = "Sainz",
            teamName = "Ferrari",
            teamColor = TeamFerrari,
            progress = null,
            emphasized = false,
            modifier = Modifier.padding(8.dp),
        )
    }
}
