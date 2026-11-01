"""Regenerate the fixed 23-profile cross-target test snapshot after intentional catalog changes.
Run with --check to detect fixture drift without writing.
"""
import sys
from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]
catalog=json.loads((r/'data/assets/circuits/index.json').read_text())['circuits']
files={'circuits/index.json':(r/'data/assets/circuits/index.json').read_text(),'tracks/index.json':(r/'mobile-assets/tracks/index.json').read_text()}
for c in catalog:
 files[c['detailAssetPath']]=(r/'data/assets'/c['detailAssetPath']).read_text()
 files[c['modelAsset']]='';files[c['waypointsAsset']]=''
 for n in ('track.png','sectors.png','drs.png','path.json'):files[f'tracks/{c["id"]}/{n}']=''
# Compact snapshots deliberately fixed for deterministic Android/Native contract comparison.
pairs=',\n'.join(json.dumps(k)+' to """'+json.dumps(json.loads(v),ensure_ascii=False,separators=(',',':'))+'"""' if v else json.dumps(k)+' to ""' for k,v in files.items())
source='''package app.yongin.xr_circuit.data
import app.yongin.xr_circuit.data.local.*
import app.yongin.xr_circuit.data.platform.AssetReader
import app.yongin.xr_circuit.data.network.DataJson
import app.yongin.xr_circuit.data.repository.CircuitRepositoryImpl
import app.yongin.xr_circuit.domain.model.*
import app.yongin.xr_circuit.domain.repository.*
import app.yongin.xr_circuit.domain.usecase.*
import app.yongin.xr_circuit.domain.state.*
import kotlinx.coroutines.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.*
/** Stage 8 snapshot: all 23 bundled profiles, same fixture on Android host and iOS Native. */
@OptIn(ExperimentalCoroutinesApi::class)
class AllCircuitRegressionTest {
 private val files = mapOf(
PAIRS
 )
 private val reader = object : AssetReader {
  override fun readText(path: String) = files.getValue(path)
  override fun exists(path: String) = path in files
 }
 @Test fun allProfilesAndCatalogOrderMatchAcrossTargets() {
  val mobile=CircuitRepositoryImpl(CircuitLocalDataSource(reader,DataJson,CircuitAssetTarget.MOBILE))
  val xr=CircuitRepositoryImpl(CircuitLocalDataSource(reader,DataJson,CircuitAssetTarget.XR))
  assertEquals(23,mobile.getCatalog().size)
  assertEquals(xr.getCatalog(),mobile.getCatalog())
  for (c in mobile.getCatalog()) {
   val d=mobile.getCircuitDetail(c.id)
   assertEquals(xr.getCircuitDetail(c.id),d)
   assertEquals(c.id,d.id)
   assertEquals(d.turns,d.corners.size)
   assertEquals(d.drsZoneCount,d.drsZones.size)
   assertTrue(d.lengthKm>0)
   assertEquals(3,d.sectors.size)
  }
 }
 @Test fun allSelectionsPreserveOverlaysAndFixtureWeather() = runTest {
  val repo=CircuitRepositoryImpl(CircuitLocalDataSource(reader,DataJson,CircuitAssetTarget.MOBILE))
  val fixed=TrackWeather(19.5,0.2,3,12.4,270.0,30)
  val store=CircuitStore(GetCircuitCatalogUseCase(repo),GetCircuitDetailUseCase(repo),
   GetTrackWeatherUseCase(object:WeatherRepository { override suspend fun getTrackWeather(circuitId:String)=fixed }),
   GetStartingGridUseCase(object:StartingGridRepository { override suspend fun getStartingGrid(circuitId:String)=emptyList<GridEntry>() }),this)
  try {
   store.toggleOverlay(Overlay.Drs)
   for(c in repo.getCatalog()) {
    store.selectCircuit(c.id); runCurrent()
    val corner=repo.getCircuitDetail(c.id).corners.first().id
    store.selectCorner(corner)
    assertEquals(c.id,store.state.value.selectedCircuit?.id)
    assertEquals(c.id,store.state.value.detail?.id)
    assertEquals(corner,store.state.value.selectedCornerId)
    assertEquals(fixed,store.state.value.weather)
    assertEquals(setOf(Overlay.Sectors,Overlay.Drs),store.state.value.overlays)
    assertNull(store.state.value.profileError)
   }
  } finally {store.close()}
 }
}
'''.replace('PAIRS',pairs)
target=r/'data/src/commonTest/kotlin/app/yongin/xr_circuit/data/AllCircuitRegressionTest.kt'
if '--check' in sys.argv:
    assert target.read_text()==source, 'Catalog fixture changed; review and regenerate explicitly'
    print('All-circuit regression snapshot matches bundled catalog/profiles')
else:
    target.write_text(source)
