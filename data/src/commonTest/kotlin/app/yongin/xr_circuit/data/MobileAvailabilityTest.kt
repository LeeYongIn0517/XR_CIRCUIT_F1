package app.yongin.xr_circuit.data
import app.yongin.xr_circuit.data.local.*
import app.yongin.xr_circuit.data.platform.AssetReader
import app.yongin.xr_circuit.data.network.DataJson
import kotlin.test.*
class MobileAvailabilityTest {
    @Test fun mobileAndXrHaveIndependentRequirements() {
        val files = mutableMapOf(
            "circuits/index.json" to """{"circuits":[{"id":"silverstone","displayName":"Silverstone","detailAssetPath":"circuits/silverstone.json","modelAsset":"track.glb","waypointsAsset":"xr.json"}]}""",
            "circuits/silverstone.json" to "{}",
            "tracks/index.json" to """{"schemaVersion":1,"circuits":[{"circuitId":"silverstone","track":"tracks/silverstone/track.png","sectors":"tracks/silverstone/sectors.png","drs":"tracks/silverstone/drs.png","path":"tracks/silverstone/path.json","width":1024,"height":1024}]}"""
        )
        val reader = object : AssetReader {
            override fun readText(path: String) = files.getValue(path)
            override fun exists(path: String) = path in files
        }
        val mobile = CircuitLocalDataSource(reader,DataJson,CircuitAssetTarget.MOBILE)
        val xr = CircuitLocalDataSource(reader,DataJson)
        for (name in listOf("track.png","sectors.png","drs.png","path.json")) files["tracks/silverstone/$name"]=""
        assertEquals(1,mobile.loadAvailableCatalogEntries().size)
        assertTrue(xr.loadAvailableCatalogEntries().isEmpty())
        files["track.glb"]=""; files["xr.json"]=""
        assertEquals(1,xr.loadAvailableCatalogEntries().size)
        for (name in listOf("track.png","sectors.png","drs.png","path.json")) {
            files.remove("tracks/silverstone/$name")
            assertTrue(mobile.loadAvailableCatalogEntries().isEmpty())
            assertEquals(1,xr.loadAvailableCatalogEntries().size)
            files["tracks/silverstone/$name"]=""
        }
        files.remove("circuits/silverstone.json")
        assertTrue(mobile.loadAvailableCatalogEntries().isEmpty())
        assertTrue(xr.loadAvailableCatalogEntries().isEmpty())
    }
}
