package app.yongin.xr_circuit.domain.model

/**
 * Driver identity used by starting-grid rows and related UI.
 */
data class Driver(
    val number: Int,
    val fullName: String,
    val acronym: String,
    val teamName: String,
    val teamColorHex: String? = null,
)

/**
 * One starting-grid slot for the Right Spatial Panel.
 */
data class GridEntry(
    val position: Int,
    val driver: Driver,
    val tyreCompound: String? = null,
)
