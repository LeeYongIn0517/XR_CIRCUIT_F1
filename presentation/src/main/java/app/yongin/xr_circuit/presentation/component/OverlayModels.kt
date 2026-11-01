package app.yongin.xr_circuit.presentation.component
import androidx.annotation.DrawableRes
/**
 * Identifiers for 3D overlay layers controlled by the dock.
 * Domain / ViewModel mapping can be added later.
 */
enum class OverlayLayerId {
    Sectors,
    Drs,
    Pins,
}

/**
 * Presentation model for a single overlay toggle in [CircuitControlDock].
 */
data class OverlayToggleUi(
    val id: OverlayLayerId,
    val label: String,
    val selected: Boolean,
    @DrawableRes val iconRes: Int,
)

