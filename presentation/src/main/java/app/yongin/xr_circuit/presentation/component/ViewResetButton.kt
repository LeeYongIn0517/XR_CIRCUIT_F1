package app.yongin.xr_circuit.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.yongin.xr_circuit.presentation.R
import app.yongin.xr_circuit.presentation.theme.XR_CIRCUITTheme

/**
 * Icon-only control that resets the 3D viewpoint / orbit camera.
 *
 * Uses a Material 3 tonal surface + outline border so it reads as a secondary
 * action next to the overlay toggles. Bind [onClick] when the camera controller
 * is ready.
 */
@Composable
fun ViewResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Reset view",
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_view_reset),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "View reset")
@PreviewLightDark
@Composable
private fun ViewResetButtonPreview() {
    XR_CIRCUITTheme {
        ViewResetButton(
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}
