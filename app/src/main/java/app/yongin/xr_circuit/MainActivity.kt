package app.yongin.xr_circuit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.yongin.xr_circuit.presentation.CircuitMainScreen
import app.yongin.xr_circuit.ui.theme.XR_CIRCUITTheme

/**
 * On XR devices the launcher activity starts in managed full space (see manifest
 * `android.window.PROPERTY_XR_ACTIVITY_START_MODE`). On non‑XR devices the spatial branch is
 * skipped and the flat UI is shown instead.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XR_CIRCUITTheme {
                CircuitMainScreen(
                    panelContent = { MainContent() },
                    fullSpaceButton = { onClick, modifier ->
                        FullSpaceModeIconButton(onClick = onClick, modifier = modifier)
                    },
                )
            }
        }
    }
}

@Composable
private fun MainContent(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.hello_android_xr), modifier = modifier)
}

@Composable
private fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Composable
private fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@PreviewLightDark
@Composable
private fun My2dContentPreview() {
    XR_CIRCUITTheme {
        CircuitMainScreen(
            panelContent = { MainContent() },
            fullSpaceButton = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FullSpaceModeButtonPreview() {
    XR_CIRCUITTheme {
        FullSpaceModeIconButton(onClick = {})
    }
}

@PreviewLightDark
@Composable
private fun HomeSpaceModeButtonPreview() {
    XR_CIRCUITTheme {
        HomeSpaceModeIconButton(onClick = {})
    }
}
