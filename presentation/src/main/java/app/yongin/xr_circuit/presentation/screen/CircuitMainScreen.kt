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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import app.yongin.xr_circuit.domain.model.CircuitSummary
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterAnchorPoint
import androidx.xr.compose.spatial.OrbiterDefaults
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialGltfModel
import androidx.xr.compose.subspace.SpatialGltfModelSource
import androidx.xr.compose.subspace.SpatialGltfModelStatus
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
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.math.Vector4
import androidx.xr.scenecore.AlphaMode
import androidx.xr.scenecore.KhronosUnlitMaterial
import app.yongin.xr_circuit.presentation.component.CircuitControlDock
import app.yongin.xr_circuit.presentation.component.CircuitControlDockUiState
import app.yongin.xr_circuit.presentation.component.CircuitProfilePanel
import app.yongin.xr_circuit.presentation.component.GridWeatherPanel
import app.yongin.xr_circuit.presentation.component.OverlayLayerId
import app.yongin.xr_circuit.presentation.util.loadTrackPath
import app.yongin.xr_circuit.presentation.util.sampleAtDistance
import app.yongin.xr_circuit.presentation.viewmodel.CircuitIntent
import app.yongin.xr_circuit.presentation.viewmodel.CircuitUiState
import app.yongin.xr_circuit.presentation.viewmodel.CircuitViewModel
import kotlin.io.path.Path

private const val MARKER_GLTF_ASSET_FILE_NAME = "CarDotMat.glb"

/**
 * Impress only draws overlay meshes after a node transform write (see A/B 4).
 * Wake with a localPose write; visibility uses material override (never scale-to-zero).
 *
 * Transform A/B: Roll 180° about Z, then Yaw 180° about Y (CCW from above; 180° = same CW).
 * Composition matches SceneCore Euler YXZ: yaw * roll.
 */
private val InvisibleBaseColor = Vector4(0f, 0f, 0f, 0f)
private const val OverlayRollAbDegrees = 180f
private const val OverlayYawAbDegrees = 180f
private val OverlayAbRotation =
    Quaternion.fromAxisAngle(Vector3.Up, OverlayYawAbDegrees) *
        Quaternion.fromAxisAngle(Vector3.Backward, OverlayRollAbDegrees)

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
    viewModel: CircuitViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        Subspace {
            CircuitSpatialLayout(
                uiState = uiState,
                onIntent = viewModel::onIntent,
            )
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
private fun CircuitSpatialLayout(
    uiState: CircuitUiState,
    onIntent: (CircuitIntent) -> Unit,
) {
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
                    stats = uiState.stats,
                    lapRecord = uiState.lapRecord,
                    corners = uiState.corners,
                    selectedCornerId = uiState.selectedCornerId,
                    onCornerClick = { onIntent(CircuitIntent.SelectCorner(it.id)) },
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
                    uiState.selectedCircuit?.let { circuit ->
                        // Dispose the previous model, material and lap animation together.
                        key(circuit.id) {
                            CircuitSpatialContent(circuit = circuit, dockUiState = uiState.dockUiState)
                        }
                    }
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
                        circuitName = uiState.circuitName,
                        circuits = uiState.availableCircuits,
                        selectedCircuitId = uiState.circuitId,
                        uiState = uiState.dockUiState,
                        onCircuitSelected = { onIntent(CircuitIntent.SelectCircuit(it)) },
                        onOverlayToggle = { onIntent(CircuitIntent.ToggleOverlay(it)) },
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
                    weather = uiState.weather,
                    drivers = uiState.drivers,
                    strategy = uiState.strategy,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun CircuitSpatialContent(
    circuit: CircuitSummary,
    dockUiState: CircuitControlDockUiState,
) {
    val displayScale = minOf(0.822f / circuit.modelWidthMeters, 0.5f / circuit.modelDepthMeters)
    val context = LocalContext.current
    val session = LocalSession.current
    val showSectors = dockUiState.overlays.any {
        it.id == OverlayLayerId.Sectors && it.selected
    }
    val showDrs = dockUiState.overlays.any {
        it.id == OverlayLayerId.Drs && it.selected
    }

    val circuitSource = remember {
        SpatialGltfModelSource.fromPath(Path(circuit.modelAsset))
    }
    val markerSource = remember {
        SpatialGltfModelSource.fromPath(Path(MARKER_GLTF_ASSET_FILE_NAME))
    }
    val circuitState = rememberSpatialGltfModelState(circuitSource)
    val markerState = rememberSpatialGltfModelState(markerSource)
    val trackPath = remember { context.loadTrackPath(circuit.waypointsAsset) }
    val waypoints = trackPath.waypoints_gltf_yup

    var overlayHiddenMaterial by remember { mutableStateOf<KhronosUnlitMaterial?>(null) }
    var overlaysWoken by remember { mutableStateOf(false) }

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

    LaunchedEffect(session) {
        if (session == null) {
            overlayHiddenMaterial = null
            return@LaunchedEffect
        }
        val material = KhronosUnlitMaterial.create(session, AlphaMode.MASK)
        material.setBaseColorFactor(InvisibleBaseColor)
        material.setAlphaCutoff(0.5f)
        overlayHiddenMaterial = material
    }

    DisposableEffect(circuitState, markerState) {
        onDispose {
            circuitState.close()
            markerState.close()
        }
    }

    DisposableEffect(overlayHiddenMaterial) {
        val material = overlayHiddenMaterial
        onDispose {
            material?.close()
        }
    }

    // Wake every overlay; apply the legacy correction only to assets that request it.
    LaunchedEffect(circuitState.status) {
        if (circuitState.status != SpatialGltfModelStatus.Loaded) {
            overlaysWoken = false
            return@LaunchedEffect
        }
        circuitState.nodes.forEach { node ->
            val name = node.name ?: return@forEach
            if (!name.startsWith("SectorStrip_") && !name.startsWith("DrsStrip_")) return@forEach
            node.localPose = Pose(
                translation = node.localPose.translation,
                rotation = if (circuit.legacyOverlayRotation) OverlayAbRotation else node.localPose.rotation,
            )
        }
        overlaysWoken = true
    }

    // Toggle visibility without scale-to-zero (avoids TRS corruption on hide/show).
    LaunchedEffect(
        circuitState.status,
        showSectors,
        showDrs,
        overlayHiddenMaterial,
        overlaysWoken,
    ) {
        if (circuitState.status != SpatialGltfModelStatus.Loaded) return@LaunchedEffect
        if (!overlaysWoken) return@LaunchedEffect
        val hiddenMaterial = overlayHiddenMaterial ?: return@LaunchedEffect
        circuitState.nodes.forEach { node ->
            when {
                node.name?.startsWith("SectorStrip_") == true -> {
                    if (showSectors) {
                        node.clearMaterialOverride()
                    } else {
                        node.setMaterialOverride(hiddenMaterial)
                    }
                }
                node.name?.startsWith("DrsStrip_") == true -> {
                    if (showDrs) {
                        node.clearMaterialOverride()
                    } else {
                        node.setMaterialOverride(hiddenMaterial)
                    }
                }
                else -> return@forEach
            }
        }
    }

    val trackVolumeModifier = SubspaceModifier
        .requiredWidth(Meter(circuit.modelWidthMeters * displayScale).toDp())
        .requiredHeight(Meter(circuit.modelHeightMeters * displayScale).toDp())
        .requiredDepth(Meter(circuit.modelDepthMeters * displayScale).toDp())

    SpatialGltfModel(
        state = circuitState,
        modifier = trackVolumeModifier,
    ) {
        val x = Meter(markerPos.x * displayScale).toDp()
        val y = Meter(markerPos.y * displayScale).toDp()
        val z = Meter(markerPos.z * displayScale).toDp()
        SpatialGltfModel(
            state = markerState,
            modifier = SubspaceModifier.absoluteOffset(x = x, y = y, z = z),
        )
    }
}
