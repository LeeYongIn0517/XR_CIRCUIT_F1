package app.yongin.xr_circuit.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.Slate600
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Compact insight callout (Figma Strategy Insight, node `1:139`).
 *
 * Material 3 [primaryContainer] / [secondaryContainer] tonal pair — same roles
 * as [LapRecordCard] so the Right Spatial Panel stays on-brand.
 */
@Composable
fun StrategyInsightCard(
    label: String,
    body: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int = R.drawable.ic_strategy_info,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 19.5.sp,
                ),
                color = Slate600,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Strategy insight")
@PreviewLightDark
@Composable
private fun StrategyInsightCardPreview() {
    XR_CIRCUITTheme {
        StrategyInsightCard(
            label = "Tire Strategy",
            body = "Expected 2-stop race. Soft (12-15 laps) followed by Medium-Medium.",
            modifier = Modifier.padding(8.dp),
        )
    }
}
