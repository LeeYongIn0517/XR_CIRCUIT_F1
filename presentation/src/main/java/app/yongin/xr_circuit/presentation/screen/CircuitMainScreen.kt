package app.yongin.xr_circuit.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterAnchorPoint
import androidx.xr.compose.spatial.OrbiterDefaults
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialGltfModel
import androidx.xr.compose.subspace.SpatialGltfModelSource
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SpatialAlignment
import androidx.xr.compose.subspace.layout.SpatialArrangement
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.absoluteOffset
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.requiredDepth
import androidx.xr.compose.subspace.layout.requiredHeight
import androidx.xr.compose.subspace.layout.requiredWidth
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.rememberSpatialGltfModelState
import androidx.xr.compose.unit.DpVolumeOffset
import androidx.xr.compose.unit.Meter
import app.yongin.xr_circuit.presentation.component.CircuitControlDock
import app.yongin.xr_circuit.presentation.component.CircuitProfilePanel
import app.yongin.xr_circuit.presentation.component.GridWeatherPanel
import app.yongin.xr_circuit.presentation.component.OverlayToggleUi
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

/**
 * Keeps the authored track inside ~40% of the recommended content box so
 * left/right panels are not starved by SpatialRow's sequential measure pass.
 * (~0.82m wide × ~0.50m deep)
 */
private const val TrackDisplayScale = 0.05f

private val DisplayTrackWidth = TrackWidth * TrackDisplayScale
private val DisplayTrackHeight = TrackHeight * TrackDisplayScale
private val DisplayTrackDepth = TrackDepth * TrackDisplayScale

/** One full lap around the sampled path. */
private const val LapDurationMs = 45_000

private val SidePanelHeight = 720.dp
private val PanelTrackGap = Meter(0.04f).toDp()

private const val SidePanelWeight = 0.28f
private const val CenterTrackWeight = 0.44f

/**
 * Aerial-view staging: track sits below eye level so the user looks down onto it,
 * while side panels sit further back and yaw inward like a folding screen.
 *
 * Positive Z faces the user (Orbiter elevation convention); negative Z pushes back.
 */
private val TrackDownOffset = Meter(0.28f).toDp()
private val TrackForwardOffset = Meter(0.06f).toDp()
private val PanelBackOffset = Meter(0.18f).toDp()
private const val PanelInwardYawDegrees = 18f

/** Gap between the track volume top and the control dock Orbiter. */
private val DockAboveTrackGap = 12.dp

@SuppressLint("RestrictedApi")
@Composable
fun CircuitMainScreen(
    dummyInfo: DummyCircuitSpatialInfo = DummyCircuitSpatialInfoDefault,
) {
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        Subspace {
            CircuitSpatialLayout(dummyInfo = dummyInfo)
        }
    }
}

/**
 * Wireframe layout from Expansion Plan §4.5:
 * Left Spatial Panel | Center Volume | Right Panel, with Bottom Dock Orbiter.
 *
 * Uses weight-based SpatialRow sizing inside the recommended content box so
 * absolute track `required*` sizes cannot push the right panel to zero width.
 * Side panels are offset back and yawed inward; the track drops below gaze
 * for an airplane-style top-down view. The control dock is a Top Orbiter on
 * the center slot so it keeps its natural size above the circuit.
 */
@SuppressLint("RestrictedApi")
@Composable
private fun CircuitSpatialLayout(dummyInfo: DummyCircuitSpatialInfo) {
    var selectedCornerId by remember(dummyInfo.initiallySelectedCornerId) {
        mutableStateOf(dummyInfo.initiallySelectedCornerId)
    }
    var overlays by remember(dummyInfo.overlays) {
        mutableStateOf(dummyInfo.overlays)
    }

    SpatialColumn(
        modifier = SubspaceModifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = SpatialAlignment.CenterHorizontally,
    ) {
        SpatialRow(
            modifier = SubspaceModifier
                .fillMaxWidth()
                .height(SidePanelHeight),
            verticalAlignment = SpatialAlignment.CenterVertically,
            horizontalArrangement = SpatialArrangement.spacedBy(PanelTrackGap),
        ) {
            SpatialPanel(
                modifier = SubspaceModifier
                    .weight(SidePanelWeight)
                    .fillMaxHeight()
                    .offset(z = -PanelBackOffset)
                    .rotate(yaw = PanelInwardYawDegrees),
            ) {
                CircuitProfilePanel(
                    stats = dummyInfo.stats,
                    lapRecord = dummyInfo.lapRecord,
                    corners = dummyInfo.corners,
                    selectedCornerId = selectedCornerId,
                    onCornerClick = { selectedCornerId = it.id },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            SpatialBox(
                modifier = SubspaceModifier
                    .weight(CenterTrackWeight)
                    .fillMaxHeight(),
                alignment = SpatialAlignment.Center,
            ) {
                SpatialColumn(
                    modifier = SubspaceModifier
                        .offset(y = -TrackDownOffset, z = TrackForwardOffset),
                    horizontalAlignment = SpatialAlignment.CenterHorizontally,
                ) {
                    CircuitSpatialContent()
                }

                // Anchor to this tall center slot (not the thin track volume)
                // so the dock keeps its natural size.
                Orbiter(
                    anchorPoint = OrbiterAnchorPoint.Top,
                    offset = DpVolumeOffset(
                        y = DockAboveTrackGap,
                        z = OrbiterDefaults.Elevation,
                    ),
                ) {
                    CircuitControlDock(
                        circuitName = dummyInfo.circuitName,
                        overlays = overlays,
                        onCircuitClick = { /* circuit picker — wired later */ },
                        onOverlayToggle = { toggled ->
                            overlays = overlays.toggle(toggled)
                        },
                        onResetClick = { /* view reset — wired later */ },
                    )
                }
            }

            SpatialPanel(
                modifier = SubspaceModifier
                    .weight(SidePanelWeight)
                    .fillMaxHeight()
                    .offset(z = -PanelBackOffset)
                    .rotate(yaw = -PanelInwardYawDegrees),
            ) {
                GridWeatherPanel(
                    weather = dummyInfo.weather,
                    drivers = dummyInfo.drivers,
                    strategy = dummyInfo.strategy,
                    modifier = Modifier.fillMaxSize(),
                )
            }
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

    SpatialGltfModel(
        state = trackState,
        modifier = SubspaceModifier
            .requiredWidth(DisplayTrackWidth.toDp())
            .requiredHeight(DisplayTrackHeight.toDp())
            .requiredDepth(DisplayTrackDepth.toDp()),
    ) {
        // CarDotMat is authored at local origin; JSON waypoints drive world offset.
        // Offsets follow TrackDisplayScale so the marker stays on the scaled path.
        val x = Meter(markerPos.x * TrackDisplayScale).toDp()
        val y = Meter(markerPos.y * TrackDisplayScale).toDp()
        val z = Meter(markerPos.z * TrackDisplayScale).toDp()
        SpatialGltfModel(
            state = markerState,
            modifier = SubspaceModifier.absoluteOffset(x = x, y = y, z = z),
        )
    }
}

private fun List<OverlayToggleUi>.toggle(target: OverlayToggleUi): List<OverlayToggleUi> =
    map { overlay ->
        if (overlay.id == target.id) overlay.copy(selected = !overlay.selected) else overlay
    }
