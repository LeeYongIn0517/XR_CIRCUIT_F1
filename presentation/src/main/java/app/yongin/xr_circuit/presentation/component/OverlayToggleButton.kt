package app.yongin.xr_circuit.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Independent overlay layer toggle (Sectors / DRS / Pins).
 *
 * Selected state uses [androidx.compose.material3.ColorScheme.secondary] (`#3B82F6`)
 * to match Figma; idle uses transparent fill + [onSurfaceVariant] content —
 * aligned with Material 3 filter-chip / toggle semantics without locking to
 * FilterChip chrome so the dock can stay compact.
 */
@Composable
fun OverlayToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .semantics {
                this.selected = selected
                role = Role.Checkbox
            },
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (selected) 2.dp else 0.dp,
        shadowElevation = if (selected) 6.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = contentColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, name = "Overlay toggle — selected")
@PreviewLightDark
@Composable
private fun OverlayToggleButtonSelectedPreview() {
    XR_CIRCUITTheme {
        OverlayToggleButton(
            label = "Sectors",
            selected = true,
            onClick = {},
            iconRes = R.drawable.ic_overlay_sectors,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, name = "Overlay toggle — idle")
@Composable
private fun OverlayToggleButtonIdlePreview() {
    XR_CIRCUITTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OverlayToggleButton(
                label = "DRS",
                selected = false,
                onClick = {},
                iconRes = R.drawable.ic_overlay_drs,
            )
            OverlayToggleButton(
                label = "Pins",
                selected = false,
                onClick = {},
                iconRes = R.drawable.ic_overlay_pins,
            )
        }
    }
}
