package app.yongin.xr_circuit.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import app.yongin.xr_circuit.presentation.util.sampleAtDistance
import kotlin.io.path.Path

/** Merged into the app APK from `presentation/src/main/assets/`. */
private const val TRACK_GLTF_ASSET_FILE_NAME = "SilverstoneTrack.glb"
private const val MARKER_GLTF_ASSET_FILE_NAME = "CarDotMat.glb"

/** glTF Y-up track bbox in meters (1:1 with Blender asset). */
private val TrackWidth = Meter(16.44f)
private val TrackHeight = Meter(0.44f)
private val TrackDepth = Meter(10.0f)

/** One full lap around the sampled path. */
private const val LapDurationMs = 45_000

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
    val waypoints = trackPath.waypoints_gltf_yup

    val infiniteTransition = rememberInfiniteTransition(label = "carDotLap")
    val distanceAlongPath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = trackPath.path_length_m,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = LapDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "distanceAlongPath",
    )
    val (markerPos, _) = sampleAtDistance(
        waypoints,
        trackPath.path_length_m,
        distanceAlongPath,
    )

    DisposableEffect(trackState) {
        onDispose {
            trackState.close()
            markerState.close()
        }
    }

    SpatialRow {
        SpatialGltfModel(
            state = trackState,
            modifier = SubspaceModifier
                .requiredWidth(TrackWidth.toDp())
                .requiredHeight(TrackHeight.toDp())
                .requiredDepth(TrackDepth.toDp())
        ) {
            // CarDotMat is authored at local origin; JSON waypoints drive world offset.
            val x = Meter(markerPos.x).toDp()
            val y = Meter(markerPos.y).toDp()
            val z = Meter(markerPos.z).toDp()
            SpatialGltfModel(
                state = markerState,
                modifier = SubspaceModifier.absoluteOffset(x = x, y = y, z = z)
            )
        }
    }
}
