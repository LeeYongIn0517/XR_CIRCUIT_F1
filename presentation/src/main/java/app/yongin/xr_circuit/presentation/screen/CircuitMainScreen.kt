package app.yongin.xr_circuit.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialGltfModel
import androidx.xr.compose.subspace.SpatialGltfModelSource
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.rememberSpatialGltfModelState
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.absoluteOffset
import androidx.xr.compose.subspace.layout.requiredDepth
import androidx.xr.compose.subspace.layout.requiredHeight
import androidx.xr.compose.subspace.layout.requiredWidth
import androidx.xr.compose.unit.Meter
import app.yongin.xr_circuit.presentation.util.loadTrackPath
import kotlin.io.path.Path

/** Merged into the app APK from `presentation/src/main/assets/`. */
private const val TRACK_GLTF_ASSET_FILE_NAME = "SilverstoneTrack.glb"
private const val MARKER_GLTF_ASSET_FILE_NAME = "CarDotMat.glb"

/** glTF Y-up track bbox in meters (1:1 with Blender asset). */
private val TrackWidth = Meter(16.44f)
private val TrackHeight = Meter(0.44f)
private val TrackDepth = Meter(10.0f)

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

    SpatialRow {
        // Force meter-sized layout so SpatialRow cannot shrink the track away from 1m=1m.
        SpatialGltfModel(
            state = trackState,
            modifier = SubspaceModifier
                .requiredWidth(TrackWidth.toDp())
                .requiredHeight(TrackHeight.toDp())
                .requiredDepth(TrackDepth.toDp())
        ) {
            // Pin to waypoint[0] in the same meter space as the track.
            val wp = trackPath.waypoints_gltf_yup.first()
            val x = Meter(wp.position[0]).toDp()
            val y = Meter(wp.position[1]).toDp()
            val z = Meter(wp.position[2]).toDp()
            SpatialGltfModel(
                state = markerState,
                // No width/height — marker keeps asset scale inside the track's meter space.
                modifier = SubspaceModifier.absoluteOffset(x = x, y = y, z = z)
            )
        }
    }
}
