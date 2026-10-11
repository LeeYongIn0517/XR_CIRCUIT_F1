# Circuit models

## Folder layout

- `models/`: editable Blender models (`.blend`).
- `backups/`: preserved Blender backup files (`.blend1`).
- `scripts/`: model generation and Silverstone export scripts.
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

To export the existing Silverstone model from the repository root:

```sh
/Applications/Blender.app/Contents/MacOS/Blender --background \
  presentation/src/main/blender/models/SilverstoneTrack.blend \
  --python presentation/src/main/blender/scripts/export_silverstone_circuit.py
```

Run in a separate background process: the script replaces that process's scene.
The script writes the blend, preview, GLBs, equally spaced closed-loop waypoints,
profile JSON and matching bounds in `data/src/main/assets/circuits/index.json`.
Sector ranges are start-inclusive/end-exclusive; the final sector ends at the
waypoint count (the seam). DRS ranges with end < start cross the start/finish line.
The model, overlays and waypoints share a centred glTF Y-up coordinate system.

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
   `legacyOverlayRotation` is only true for the existing Silverstone asset.
5. Bundle all three assets (detail, combined GLB, waypoints). Only complete
   entries appear in the selector; planned entries stay in the catalog.

`CircuitIntent.SelectCircuit` changes the profile, model and car path together.
The old composition is disposed using the circuit ID as a key, restarting the
lap animation. `CircuitIntent.ToggleOverlay` controls both circuits and preserves
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
