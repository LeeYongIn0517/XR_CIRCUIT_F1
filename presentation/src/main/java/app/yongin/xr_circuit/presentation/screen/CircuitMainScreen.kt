package app.yongin.xr_circuit.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialGltfModel
import androidx.xr.compose.subspace.SpatialGltfModelSource
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.rememberSpatialGltfModelState
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.absoluteOffset
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.unit.Meter
import app.yongin.xr_circuit.presentation.util.loadTrackPath
import kotlin.io.path.Path

/** Merged into the app APK from `presentation/src/main/assets/`. */
private const val TRACK_GLTF_ASSET_FILE_NAME = "SilverstoneTrack.glb"
private const val MARKER_GLTF_ASSET_FILE_NAME = "CarDotMat.glb"

@SuppressLint("RestrictedApi")
@Composable
fun CircuitMainScreen() {
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        Subspace {
            CircuitSpatialContent()
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun CircuitSpatialContent() {
    val context = LocalContext.current
    val trackSource = remember {
        SpatialGltfModelSource.fromPath(Path(TRACK_GLTF_ASSET_FILE_NAME))
    }
    val markerSource = remember {
        SpatialGltfModelSource.fromPath(Path(MARKER_GLTF_ASSET_FILE_NAME))
    }
    val trackState = rememberSpatialGltfModelState(trackSource)
    val markerState = rememberSpatialGltfModelState(markerSource)
    val trackPath = remember { context.loadTrackPath() }

    DisposableEffect(trackState) {
        onDispose {
            trackState.close()
            markerState.close()
        }
    }

    SpatialRow(
//        modifier = SubspaceModifier.height(900.dp).fillMaxWidth()
    ) {
        SpatialGltfModel(
            state = trackState,
            modifier = SubspaceModifier
//                .padding(start = 24.dp)
//                .width(960.dp)
//                .height(800.dp)
        ) {
            // 일단 waypoint[0]에 고정
            val wp = trackPath.waypoints_gltf_yup.first()
            val x = Meter(wp.position[0]).toDp()
            val y = Meter(wp.position[1]).toDp()
            val z = Meter(wp.position[2]).toDp()
            SpatialGltfModel(
                state = markerState,
                modifier = SubspaceModifier
//                    .width(40.dp)   // 작게
//                    .height(40.dp)
                    .absoluteOffset(x = x, y = 0.dp, z = z)
            )
        }
    }
}