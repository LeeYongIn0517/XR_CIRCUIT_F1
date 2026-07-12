package app.yongin.xr_circuit.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialGltfModel
import androidx.xr.compose.subspace.SpatialGltfModelSource
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.rememberSpatialGltfModelState
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.width
import kotlin.io.path.Path

/** Merged into the app APK from `presentation/src/main/assets/`. */
private const val TRACK_GLTF_ASSET_FILE_NAME = "Circuit Sliverstone.glb"

/**
 * XR vs 2D 분기, 공간 모드에서 트랙 GLB 표시, 2D에서는 패널·풀스페이스 버튼을 presentation에서 담당한다.
 *
 * @param panelContent 메인 패널 안에 그릴 2D Compose UI (보통 앱 모듈의 문자열·리소스 사용).
 * @param fullSpaceButton 풀 스페이스 전환 버튼.
 */
@SuppressLint("RestrictedApi")
@Composable
fun CircuitMainScreen(
    panelContent: @Composable () -> Unit,
    fullSpaceButton: @Composable (onClick: () -> Unit, modifier: Modifier) -> Unit,
) {
    val spatialConfiguration = LocalSpatialConfiguration.current
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        Subspace {
            CircuitSpatialContent()
        }
    } else {
        CircuitTwoDContent(
            onRequestFullSpaceMode = spatialConfiguration::requestFullSpaceMode,
            panelContent = panelContent,
            fullSpaceButton = fullSpaceButton,
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun CircuitSpatialContent() {
    val trackSource = remember {
        SpatialGltfModelSource.fromPath(Path(TRACK_GLTF_ASSET_FILE_NAME))
    }
    val trackState = rememberSpatialGltfModelState(trackSource)
    DisposableEffect(trackState) {
        onDispose { trackState.close() }
    }

    SpatialRow(
        modifier = SubspaceModifier.height(900.dp).fillMaxWidth()
    ) {
        SpatialGltfModel(
            state = trackState,
            modifier = SubspaceModifier
                .padding(start = 24.dp)
                .width(960.dp)
                .height(800.dp)
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun CircuitTwoDContent(
    onRequestFullSpaceMode: () -> Unit,
    panelContent: @Composable () -> Unit,
    fullSpaceButton: @Composable (onClick: () -> Unit, modifier: Modifier) -> Unit,
) {
    Surface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.padding(48.dp)) {
                panelContent()
            }
            if (LocalSpatialConfiguration.current.hasXrSpatialFeature) {
                Box(Modifier.padding(32.dp)) {
                    fullSpaceButton(onRequestFullSpaceMode, Modifier)
                }
            }
        }
    }
}
