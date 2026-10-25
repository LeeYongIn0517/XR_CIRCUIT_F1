# Circuit models

## Folder layout

- `models/`: editable Blender models (`.blend`).
- `backups/`: preserved Blender backup files (`.blend1`).
- `scripts/`: shared model generator and Silverstone compatibility entry points.
- `tracks/`: authored track definitions.
- `previews/`: rendered model previews.
- `tests/`: asset validation tests.

Runtime GLB and waypoint assets remain in `../assets/`.

## Albert Park / Melbourne

`models/MelbourneTrack.blend` is the editable model. `previews/MelbournePreview.png` shows the
track with all overlays enabled. It follows Silverstone's raised, light-gray
track style. Assets in `../assets` include the combined `MelbourneCircuit.glb`,
separate Track/Sectors/Drs exports, and `melbourne_waypoints.json`.

The authored definition is `tracks/albert_park.json`. The centreline is a
stylized trace of page 2 of the [FIA 2025 Australian GP circuit map](https://www.fia.com/system/files/decision-document/2025_australian_grand_prix_-_event_notes_-_circuit_map_pit_lane_and_quarantine_zone.pdf).
It uses the 14-turn, 5.278 km layout, three sectors and four DRS zones. Positions
are approximate map-derived locations, not survey-grade coordinates. This is
explicitly the **2025 DRS layout**, not the 2026 active-aero system.

Rebuild from the repository root (Blender 5.1):

```sh
/Applications/Blender.app/Contents/MacOS/Blender --background \
  --python presentation/src/main/blender/scripts/build_track.py -- tracks/albert_park.json
```

## Silverstone

`tracks/silverstone.json` traces page 2 of the [FIA 2025 British GP Circuit Map V2](https://www.fia.com/system/files/decision-document/2025_british_grand_prix_-_event_notes_-_circuit_map_v2.pdf).
It uses the same generator, materials, cross section and 512 equally spaced
waypoints as Albert Park: 18 turns, three sectors and two DRS zones.
Coordinates refer to a 893 x 1263 rendering of the map, starting at the control
line in race direction. Locations are visual approximations, not survey data.
Sector boundaries are 110 m before T6 and 50 m after T14; DRS activation is
30 m after T5 and at T14. This is the 2025 DRS layout.

```sh
/Applications/Blender.app/Contents/MacOS/Blender --background \
  --python presentation/src/main/blender/scripts/build_track.py -- tracks/silverstone.json
```

The generated editable model is `models/SilverstoneTrack.blend`, with preview
`previews/SilverstonePreview.png`. The old `export_silverstone_*.py` entry points
now rebuild all assets from this definition so stale overlay indices cannot return.

Run in a separate background process: the script replaces that process's scene.
The script writes the blend, preview, GLBs, equally spaced closed-loop waypoints,
profile JSON and matching bounds in `data/src/main/assets/circuits/index.json`.
Sector ranges are start-inclusive/end-exclusive; the final sector ends at the
waypoint count (the seam). DRS ranges with end < start cross the start/finish line.
The model, overlays and waypoints share a centred glTF Y-up coordinate system.

## Shanghai

`tracks/shanghai.json` traces page 2 of the [FIA 2025 Chinese GP Circuit Map V1](https://www.fia.com/system/files/decision-document/2025_chinese_grand_prix_-_event_notes_-_circuit_map_pit_lane_and_quarantine_zone.pdf),
issued 20 March 2025, in a 893 x 1263 pixel rendering. It starts at the control
line in race direction: 16 turns, 5.451 km, three sectors and two DRS zones.
Sector boundaries are 150 m before T6 and 143 m before T11. DRS activates
375 m after T13 and 98 m after T16; the latter crosses the start/finish seam.
Endpoints approximate the T14/T1 braking approaches. The trace is a visual
approximation, not survey data, and represents the 2025 DRS layout.

```sh
/Applications/Blender.app/Contents/MacOS/Blender --background \
  --python presentation/src/main/blender/scripts/build_track.py -- tracks/shanghai.json
```

The shared generator produces `models/ShanghaiTrack.blend`,
`previews/ShanghaiPreview.png`, `ShanghaiCircuit.glb`, separate track/sector/DRS
GLBs and `shanghai_waypoints.json` with 512 samples. Materials, cross section,
coordinate system and SceneCore overlay corrections match Albert Park.
`previewOrthoScale` widens only the preview framing to fit Shanghai's proportions.

## Add another circuit

1. Add a catalog entry in `data/src/main/assets/circuits/index.json` with a unique
   ID, display name, combined `modelAsset`, `waypointsAsset`, detail path and API IDs.
2. Add an authored definition like `tracks/albert_park.json`, with traced points
   in race order, two sector boundaries, any number of DRS ranges, corners and
   circuit profile data. Run `scripts/build_track.py -- tracks/<id>.json` as above. Or
   supply an independently authored model/path/profile using the same contract.
3. Combined GLBs must contain `SectorStrip_<index>` and `DrsStrip_<index>` nodes.
   The renderer discovers these prefixes; zone counts are not hardcoded.
4. Set `modelWidthMeters`, `modelHeightMeters`, `modelDepthMeters` to the full
   glTF bounds including the outline. Centre the combined model and waypoints
   around that bounding box. The generator writes these values automatically.
   `legacyOverlayRotation` is false for all generated circuits. They use the same
   SceneCore overlay correction in `TrackOverlayRotation.kt`.
5. Bundle all three assets (detail, combined GLB, waypoints). Only complete
   entries appear in the selector; planned entries stay in the catalog.

`CircuitIntent.SelectCircuit` changes the profile, model and car path together.
The old composition is disposed using the circuit ID as a key, restarting the
lap animation. `CircuitIntent.ToggleOverlay` controls all circuits and preserves
user choices when switching. Previous weather/grid jobs are cancelled and their
late results are ignored.

Validation:

```sh
python3 -m unittest discover -s presentation/src/main/blender/tests -p 'test_*.py'
./gradlew :app:assembleDebug :data:testDebugUnitTest :presentation:testDebugUnitTest :domain:test
```

The Python tests check bundled asset resolution, glTF overlay nodes, model bounds,
closed-loop sampling and sector/DRS index ranges. ViewModel tests cover switching,
unsupported IDs, overlay choices and late weather responses. Verify rendering and
repeated toggles on an XR device/emulator as well; a desktop render cannot check
SceneCore's material override behavior.

## Singapore / Marina Bay

`tracks/marina_bay.json` traces page 4 of the [FIA 2025 Singapore GP map V2](https://www.fia.com/system/files/decision-document/2025_singapore_grand_prix_-_event_notes_-_circuit_map_pit_lane_emergency_exits_map_and_quarantine_zone.pdf),
issued 1 October 2025, rendered at 893 x 1263 pixels. The 4.927 km layout has
19 corners, three sectors, four DRS zones and 512 generated waypoints.
The trace begins at the control line in race direction. Sector boundaries are
150 m before T7 and 140 m before T14. DRS starts 48 m after T5, 78 m after T13,
100 m after T14 and at the exit of T19; the last zone crosses the lap seam.
DRS endpoints are approximate braking locations; all traced coordinates are
visual approximations, not survey data.

Rebuild with `scripts/build_track.py -- tracks/marina_bay.json` in background
Blender as above. Outputs include `models/SingaporeTrack.blend`,
`previews/SingaporePreview.png`, the combined `SingaporeCircuit.glb`, separate
track/sector/DRS GLBs, and `singapore_waypoints.json`. The same materials, heights
and overlay rotations apply. `trackWidthScale` uniformly scales the track,
outline and overlay widths/offsets to avoid overlap between the nearby T8/T14
roads; the centreline proportions remain unchanged.

## Suzuka

`tracks/suzuka.json` traces page 2 of the [FIA 2025 Japanese GP map V2](https://www.fia.com/system/files/decision-document/2025_japanese_grand_prix_-_event_notes_-_circuit_map_pit_lane_quarantine_and_red_zone.pdf)
in a 893 x 1263 rendering: 5.807 km, 18 corners, three sectors and one DRS zone.
S1 is 105 m before T8; S2 is 270 m before T15. DRS starts 100 m before the
control line and wraps the lap seam, ending approximately before T1 braking.
Rebuild with `scripts/build_track.py -- tracks/suzuka.json` in background Blender.
Outputs use the `Suzuka` prefix, plus `suzuka_waypoints.json` (512 samples).

The T14-T15 overpass is raised above T9-T10. `elevationProfile` contains
race-ordered map anchors and illustrative model-space heights with smooth
ramps, not surveyed elevation data. Track, overlays, editable path, waypoint
height and forward vectors use the same elevation; path length includes it.
Planar sampling remains uniform by 2D arc length, so ramp sample distances
can be slightly longer in 3D. Existing definitions default to zero elevation.
All map-derived positions are visual approximations of the 2025 layout.

## Miami

`tracks/miami.json` traces page 2 of the [FIA 2025 Miami GP map V1](https://www.fia.com/system/files/decision-document/2025_miami_grand_prix_-_event_notes_-_circuit_map_pit_lane_quarantine_zone_emergency_exits_and_red_zones_.pdf),
issued 1 May 2025, rendered at 893 x 1263 pixels. The 5.412 km layout has
19 turns, three sectors and three DRS zones. S1 is 110 m after T8; S2 is
70 m after T16. DRS begins 30 m after T9, 525 m after T16, and at T19 apex.
Zone 3 wraps the control-line seam. Endpoints approximate braking approaches
to T11/T17/T1. Coordinates are visually traced approximations; the model is
planar and does not reproduce surveyed terrain elevation.

Rebuild with `scripts/build_track.py -- tracks/miami.json` in background Blender.
Outputs: `models/MiamiTrack.blend`, `previews/MiamiPreview.png`,
`MiamiCircuit.glb`, separate track/sector/DRS GLBs, and `miami_waypoints.json`
with 512 samples. The shared materials, heights and overlay correction apply;
track widths and offsets are uniformly reduced for the tight T14-T16 complex.

## Montreal / Circuit Gilles-Villeneuve

`tracks/villeneuve.json` traces page 3 of the [FIA 2025 Canadian GP map V1](https://www.fia.com/system/files/decision-document/2025_canadian_grand_prix_-_event_notes_-_circuit_map_pit_lane_emergency_exits_map_ers_battery_containment_area_red_zones.pdf),
issued 21 May 2025, rendered at 893 x 1263 pixels: 4.361 km, 14 turns,
three sectors and three DRS zones. S1 is 145 m before T6; S2 is 190 m before
T10. DRS starts 95 m after T7, 155 m before T12, and 70 m after T14.
Zone 3 wraps the control-line seam. Ends approximate T8/T13/T1 braking.
All coordinates are visual approximations; the model is planar.

Rebuild with `scripts/build_track.py -- tracks/villeneuve.json` in background
Blender. Outputs include `models/MontrealTrack.blend`,
`previews/MontrealPreview.png`, combined `MontrealCircuit.glb`, separate
track/sector/DRS GLBs and `montreal_waypoints.json` with 512 samples.
Shared materials, height and overlay correction apply. Widths are uniformly
reduced to preserve clearance in the hairpin and tight chicanes.

## Monaco

`tracks/monaco.json` traces page 2 of the [FIA 2025 Monaco revised map V3](https://www.fia.com/system/files/decision-document/2025_monaco_event_-_circuit_map_-_monaco_2025_revised_0.pdf),
issued 22 May 2025, at 893 x 1263 pixels. The 3.337 km route has 19 turns,
three sectors and one DRS zone. S1 is 60 m before T5; S2 is 67 m before T13.
DRS starts 18 m after T19 and wraps the control-line seam, ending approximately
before T1 braking. The model is a planar visual trace, not surveyed terrain;
the tunnel follows the racing route but is not enclosed by a roof.

Rebuild with `scripts/build_track.py -- tracks/monaco.json` in background Blender.
Outputs use the `Monaco` prefix (combined and separate GLBs, editable blend,
preview), plus `monaco_waypoints.json` with 512 samples. Shared materials,
height and overlay rotations apply; widths are reduced for the tight hairpin
and swimming-pool chicanes.

## Barcelona / Catalunya

`tracks/catalunya.json` traces page 3 of the [FIA 2025 Spanish GP map V1](https://www.fia.com/system/files/decision-document/2025_spanish_grand_prix_-_race_director_event_notes_-_circuit_map_pit_lane_quarantine_zone_and_red_zones.pdf),
issued 24 April 2025, at 893 x 1263 pixels. The 4.657 km layout has 14 turns
without the final chicane, three sectors and two DRS zones. S1 is 50 m before
T4; S2 is 89 m before T10. DRS starts 40 m after T9 and 162 m after T14.
The main straight zone wraps the control-line seam; endpoints approximate
T10/T1 braking. Coordinates are visual approximations and terrain is planar.

Rebuild with `scripts/build_track.py -- tracks/catalunya.json` in background
Blender. Outputs use the `Barcelona` prefix (combined and separate GLBs,
editable blend, preview), and `barcelona_waypoints.json` with 512 samples.
Materials, track cross section and overlay rotations match Albert Park.

## Red Bull Ring

`tracks/red_bull_ring.json` traces page 2 of the FIA 2025 Austrian GP event
notes, Circuit Map V2 (26 June 2025), at 990 x 1400 pixels. The source URL
is stored in the definition and exported waypoint metadata. Ten turns,
three sectors and three DRS zones use one shared 512-sample centreline.
S1 is 170 m before T3; S2 is 60 m before T7. DRS activation is 102 m after
T1, 100 m after T3, and 106 m after T10. DRS ends approximate braking
approaches; the last zone wraps the control line. The source map specifies
4.326 km centreline length. Geometry is a planar visual approximation.

Rebuild with `scripts/build_track.py -- tracks/red_bull_ring.json` in
background Blender. RedBullRing assets use Albert Park materials, cross
section, overlay heights and rotation handling.

## Spa-Francorchamps

`tracks/spa.json` traces the FIA 2025 Belgian GP circuit map (page 2,
V1 issued 11 June 2025) at 1132 x 1600 pixels; the full source URL is
stored in the definition and waypoint metadata. The 19 corners, three
sectors and two DRS overlays share a 512-point centreline, Albert Park
materials, cross section and rotation handling. S1 is 145m before T5;
S2 is 48m before T15. DRS starts 305m after T4 and 30m after T19, with
approximate braking ends. This is a planar visual trace without elevation.
Rebuild with `scripts/build_track.py -- tracks/spa.json` in background Blender.

## Hungaroring

`tracks/hungaroring.json` traces the map in the FIA 2025 Hungarian GP
Preview (page 2 image, 950 x 1348). Source URLs are in the definition.
Sector and DRS anchors match Circuit Map V1 issued 08.07.25: S1 85m before
T4, S2 100m after T11, DRS1 40m after T14, DRS2 6m after T1.
DRS braking endpoints are approximate. The planar visual model uses the
Albert Park generator, materials, cross section, 512-point path and overlay
rotation. It does not model actual elevation or surveyed coordinates.
Rebuild: `scripts/build_track.py -- tracks/hungaroring.json` in background Blender.

## Zandvoort

`tracks/zandvoort.json` visually traces the FIA 2025 Dutch GP Circuit Map V1
issued 15.08.25 (PDF page 2 at 1132x1600 pixels). Source URL is in the definition.
S1 is 110m before T7; S2 is 150m before T11. DRS1 begins 50m after T10;
DRS2 begins 40m after T13 and wraps across the control line. DRS braking
endpoints are approximate. Uses the Albert Park generator, materials, cross
section, 512-point path and overlay rotation. This is a planar visual model,
without actual elevation, banking or surveyed coordinates.
Rebuild: `scripts/build_track.py -- tracks/zandvoort.json` in background Blender.

## Monza

`tracks/monza.json` traces FIA 2025 Italian GP Circuit Map V2 issued 04.09.25,
PDF page 2 rendered at 1273x1800 pixels. Source URL is in the definition.
S1 is 233m before T4; S2 is 215m before T8. DRS1 starts 170m after T7;
DRS2 starts 12m after the finish line (does not wrap). Braking endpoints are
approximate. Uses the Albert Park generator, materials, 512-point path and
overlay rotation. Width scale 0.55 preserves the tight chicanes. Planar visual
trace without surveyed coordinates or elevation.
Rebuild with background Blender: `scripts/build_track.py -- tracks/monza.json`.

## Madring — 2025 published design

`tracks/madring.json` traces the official 25 April 2025 MADRING design map,
not a 2025 FIA race map. Source page and image URLs are stored in the definition.
Image coordinates (1920 x 1254) are transposed to (y, x) for landscape rendering.
Uses the Albert Park materials and 512-point closed path, with width scale 0.5.
The 22 numbered corners follow race order. This is a planar visual trace:
elevation, tunnel geometry and La Monumental banking are not modeled.
The three colored sectors are PROVISIONAL equal-distance presentation thirds,
not official timing boundaries. DRS is unspecified in the source and omitted;
zero exported zones means unavailable data, not a confirmed race regulation.
The generator skips the separate DRS GLB when there are no zones.
Rebuild: `scripts/build_track.py -- tracks/madring.json` with background Blender.

## Baku

`tracks/baku.json` visually traces FIA 2025 Azerbaijan GP Circuit Map V1,
issued 13.08.25, PDF page 3 at 1273x1800 pixels. Source URL is in the definition.
S1: 46m before T5. S2: 56m before T16. DRS1: 54m after T2.
DRS2: 347m after T20, wrapping across the control line. Braking endpoints
are approximate. Albert Park's generator supplies materials, 512 path points,
sector/DRS meshes and overlay rotation. Width scale 0.22 preserves separation
of the adjacent straights and castle complex. Planar visual trace, no surveyed
elevation. Rebuild: `scripts/build_track.py -- tracks/baku.json`.
