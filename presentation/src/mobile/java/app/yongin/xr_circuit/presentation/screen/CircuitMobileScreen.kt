package app.yongin.xr_circuit.presentation.screen

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.yongin.xr_circuit.domain.TrackPoint2D
import app.yongin.xr_circuit.domain.TrackViewport
import app.yongin.xr_circuit.presentation.component.*
import app.yongin.xr_circuit.presentation.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.hypot
import kotlin.math.roundToInt

internal data class Point(val position: TrackPoint2D, val progress: Float)
internal data class Corner(val id: String, val position: TrackPoint2D)
internal data class Track(val base: ImageBitmap, val sectors: ImageBitmap, val drs: ImageBitmap,
                         val points: List<Point>, val corners: List<Corner>) {
    fun sample(progress: Float): TrackPoint2D {
        val i = points.indexOfLast { it.progress <= progress }.coerceAtLeast(0)
        val a = points[i]; val b = points[(i + 1) % points.size]
        val end = if (i == points.lastIndex) 1f else b.progress
        val t = ((progress - a.progress) / (end - a.progress)).coerceIn(0f, 1f)
        return TrackPoint2D(a.position.x + (b.position.x-a.position.x)*t,
                            a.position.y + (b.position.y-a.position.y)*t)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CircuitMobileScreen(viewModel: CircuitViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    var marker by rememberSaveable { mutableStateOf(true) }
    var assetRetry by remember { mutableIntStateOf(0) }
    val retry = { assetRetry++; viewModel.retry() }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("CIRCUIT / 2D", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(state.circuitName.ifEmpty { "Select circuit" })
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.availableCircuits.forEach { circuit ->
                        DropdownMenuItem(text = { Text(circuit.displayName) }, onClick = {
                            viewModel.onIntent(CircuitIntent.SelectCircuit(circuit.id)); expanded = false
                        })
                    }
                }
            }
            if (state.contentError || state.availableCircuits.isEmpty()) {
                Text("Circuit information unavailable")
                Button(onClick = retry) { Text("Retry circuit") }
            }
            state.selectedCircuit?.let {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(OverlayLayerId.Sectors to "Sectors", OverlayLayerId.Drs to "DRS").forEach { (id, label) ->
                        FilterChip(selected = state.dockUiState.overlays.any { it.id == id && it.selected },
                            onClick = { viewModel.onIntent(CircuitIntent.ToggleOverlay(id)) },
                            label = { Text(label) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                    FilterChip(selected = marker, onClick = { marker = !marker }, label = { Text("Marker") },
                        modifier = Modifier.heightIn(min = 48.dp))
                }
                key(state.circuitId) { MobileTrack(state, marker, assetRetry, retry) { id ->
                    // Asset ids are turnCode; domain corner ids may use a different key.
                    state.corners.firstOrNull { it.turnCode == id }?.let { corner ->
                        viewModel.onIntent(CircuitIntent.SelectCorner(corner.id))
                    }
                } }
                Text("45-second visual lap · 2025 layout", style = MaterialTheme.typography.bodySmall)
            }
            Text("Circuit Profile", style = MaterialTheme.typography.titleLarge)
            if (!state.profileReady && !state.contentError) CircularProgressIndicator(Modifier.semantics { contentDescription = "Loading circuit profile" })
            // Shared cards also used by the XR panels; one column avoids clipped large text.
            state.stats.forEach { CircuitStatCard(it.label, it.value, unit = it.unit) }
            state.lapRecord?.let { LapRecordCard(it.time, it.driver, it.team, it.year) }
            Text("Track Conditions", style = MaterialTheme.typography.titleLarge)
            if (state.weatherLoading) {
                Text("Loading weather…")
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else if (state.weatherError != null) {
                Text("Weather unavailable")
                Button(onClick = retry) { Text("Retry weather") }
            } else state.weather?.let {
                // Flexible height for large fonts; same mapped values as XR.
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(it.airTemperature, style = MaterialTheme.typography.headlineMedium)
                        Text(it.trackTemperatureLabel); Text(it.rainChanceLabel); Text(it.windLabel)
                    }
                }
            }
            Text("Key Corners", style = MaterialTheme.typography.titleLarge)
            state.corners.forEach { corner ->
                OutlinedButton(onClick = { viewModel.onIntent(CircuitIntent.SelectCorner(corner.id)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("${if (corner.id == state.selectedCornerId) "● " else ""}${corner.turnCode} · ${corner.name}")
                }
            }
        }
    }
}

@Composable
private fun MobileTrack(state: CircuitUiState, marker: Boolean, retryKey: Int, retry: () -> Unit, select: (String) -> Unit) {
    val context = LocalContext.current
    val track by produceState<Result<Track>?>(null, state.circuitId, retryKey) {
        value = null
        value = withContext(Dispatchers.IO) { runCatching {
            val prefix = "tracks/${state.circuitId}"
            fun bitmap(name: String) = context.assets.open("$prefix/$name.png").use {
                requireNotNull(BitmapFactory.decodeStream(it)).asImageBitmap()
            }
            val json = context.assets.open("$prefix/path.json").bufferedReader().use { JSONObject(it.readText()) }
            fun position(o: JSONObject) = TrackPoint2D(o.getDouble("x").toFloat(), o.getDouble("y").toFloat())
            val p = json.getJSONArray("points"); val c = json.getJSONArray("corners")
            Track(bitmap("track"), bitmap("sectors"), bitmap("drs"),
                List(p.length()) { p.getJSONObject(it).let { Point(position(it), it.getDouble("progress").toFloat()) } },
                List(c.length()) { c.getJSONObject(it).let { Corner(it.getString("id"), position(it)) } })
        } }
    }
    if (track == null) { Text("Loading track…"); return }
    val data = track?.getOrNull()
    if (data == null) { Text("Track image unavailable"); Button(onClick = retry) { Text("Retry track") }; return }
    val owner = LocalLifecycleOwner.current
    val lifecycleState by owner.lifecycle.currentStateFlow.collectAsState()
    val progress = if (marker && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
        val transition = rememberInfiniteTransition(label = "visualLap")
        val value by transition.animateFloat(0f, 1f,
            infiniteRepeatable(tween(45_000, easing = LinearEasing), RepeatMode.Restart), label = "progress")
        value
    } else 0f
    val radius = with(LocalDensity.current) { 24.dp.toPx() }
    val selectedCode = state.corners.firstOrNull { it.id == state.selectedCornerId }?.turnCode
    val configuration = LocalConfiguration.current
    val trackHeight = if (configuration.screenWidthDp > configuration.screenHeightDp) 220.dp
        else minOf(configuration.screenWidthDp - 32, 380).dp
    Canvas(Modifier.fillMaxWidth().height(trackHeight).background(Color(0xFF101B27))
        .semantics { contentDescription = "${state.circuitName} 2D track. Selected corner ${selectedCode.orEmpty()}. Corner controls follow below." }
        .pointerInput(data, radius) { detectTapGestures { tap ->
            val viewport = TrackViewport(1024f,1024f,size.width.toFloat(),size.height.toFloat())
            if (viewport.unproject(tap.x,tap.y) != null) data.corners.minByOrNull {
                val p=viewport.project(it.position); hypot(p.x-tap.x,p.y-tap.y)
            }?.let { corner ->
                val p=viewport.project(corner.position)
                if (hypot(p.x-tap.x,p.y-tap.y) <= radius) select(corner.id)
            }
        } }) {
        val v=TrackViewport(1024f,1024f,size.width,size.height)
        fun layer(bitmap: ImageBitmap) = drawImage(bitmap, dstOffset=IntOffset(v.left.roundToInt(),v.top.roundToInt()),
            dstSize=IntSize(v.contentWidth.roundToInt(),v.contentHeight.roundToInt()))
        layer(data.base)
        if (state.dockUiState.overlays.any { it.id == OverlayLayerId.Sectors && it.selected }) layer(data.sectors)
        if (state.dockUiState.overlays.any { it.id == OverlayLayerId.Drs && it.selected }) layer(data.drs)
        data.corners.firstOrNull { it.id == selectedCode }?.let {
            val p=v.project(it.position); drawCircle(Color.Cyan, 8.dp.toPx(), Offset(p.x,p.y))
        }
        if (marker) { val p=v.project(data.sample(progress)); drawCircle(Color.White,5.dp.toPx(),Offset(p.x,p.y)) }
    }
}
