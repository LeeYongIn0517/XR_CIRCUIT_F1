package app.yongin.xr_circuit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Circuit picker trigger for the control dock.
 *
 * Material 3 clickable [Surface] with a label + value hierarchy and trailing
 * chevron. Use [CircuitSelectorDropdown] for the anchored menu.
 */
@Composable
fun CircuitSelectorButton(
    circuitName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "CIRCUIT",
    showTrailingDivider: Boolean = true,
    expanded: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.semantics { role = Role.Button },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier.padding(end = if (showTrailingDivider) 25.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_circuit_map),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                    )
                    Text(
                        text = circuitName.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
        if (showTrailingDivider) {
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

/**
 * Circuit picker with a vertical [DropdownMenu] anchored below the trigger.
 *
 * In spatial UI the menu inherits popup elevation from Jetpack Compose for XR;
 * in 2D it falls back to a standard Material menu.
 */
@Composable
fun CircuitSelectorDropdown(
    circuitName: String,
    circuits: List<CircuitSummaryUi>,
    selectedCircuitId: String,
    onCircuitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "CIRCUIT",
    showTrailingDivider: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        CircuitSelectorButton(
            circuitName = circuitName,
            onClick = { expanded = true },
            label = label,
            showTrailingDivider = showTrailingDivider,
            expanded = expanded,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            circuits.forEach { circuit ->
                val selected = circuit.id == selectedCircuitId
                DropdownMenuItem(
                    text = {
                        Text(
                            text = circuit.displayName.uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                    },
                    onClick = {
                        expanded = false
                        onCircuitSelected(circuit.id)
                    },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 280, name = "Circuit selector dropdown")
@PreviewLightDark
@Composable
private fun CircuitSelectorDropdownPreview() {
    XR_CIRCUITTheme {
        CircuitSelectorDropdown(
            circuitName = "Silverstone",
            circuits = listOf(
                CircuitSummaryUi(id = "silverstone", displayName = "Silverstone"),
                CircuitSummaryUi(id = "spa", displayName = "Spa-Francorchamps"),
                CircuitSummaryUi(id = "monza", displayName = "Monza"),
            ),
            selectedCircuitId = "silverstone",
            onCircuitSelected = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 280, name = "Circuit selector")
@PreviewLightDark
@Composable
private fun CircuitSelectorButtonPreview() {
    XR_CIRCUITTheme {
        CircuitSelectorButton(
            circuitName = "Spa-Francorchamps",
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 280, name = "Circuit selector — empty name")
@Composable
private fun CircuitSelectorButtonEmptyPreview() {
    XR_CIRCUITTheme {
        CircuitSelectorButton(
            circuitName = "—",
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}
