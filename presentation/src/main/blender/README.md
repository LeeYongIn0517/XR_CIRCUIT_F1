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
