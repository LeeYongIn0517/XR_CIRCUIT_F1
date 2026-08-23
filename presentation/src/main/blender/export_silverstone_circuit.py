"""
Export a single combined Silverstone circuit GLB (track + sector + DRS overlays).

The app loads this one file and toggles overlay nodes at runtime, avoiding extra
SpatialGltfModel siblings that break Subspace layout orientation.

Run:
  blender --background SilverstoneTrack.blend --python export_silverstone_track.py
  blender --background SilverstoneTrack.blend --python export_silverstone_overlays.py
  blender --background SilverstoneTrack.blend --python export_silverstone_circuit.py

Or only the last script after the blend already contains generated strip meshes.
"""

from __future__ import annotations

import os
import runpy

import bpy

ASSETS_DIR = os.path.normpath(
    os.path.join(os.path.dirname(__file__), "..", "assets"),
)

TRACK_OBJECT = "SilverstoneTrack"
OUTLINE_OBJECT = "SilverstoneOutline"
OVERLAY_OBJECTS = (
    "SectorStrip_1",
    "SectorStrip_2",
    "SectorStrip_3",
    "DrsStrip_1",
    "DrsStrip_2",
)


def _parent_overlays_to_track() -> None:
    """Attach overlay meshes to the track node so they inherit the same transform."""
    track = bpy.data.objects[TRACK_OBJECT]
    overlays = [bpy.data.objects[name] for name in OVERLAY_OBJECTS]
    bpy.ops.object.select_all(action="DESELECT")
    for obj in overlays:
        obj.select_set(True)
    track.select_set(True)
    bpy.context.view_layer.objects.active = track
    bpy.ops.object.parent_set(type="OBJECT", keep_transform=True)


def _run_script(filename: str) -> None:
    path = os.path.join(os.path.dirname(__file__), filename)
    runpy.run_path(path, run_name="__main__")


def main() -> None:
    _run_script("export_silverstone_track.py")
    _run_script("export_silverstone_overlays.py")
    _parent_overlays_to_track()

    os.makedirs(ASSETS_DIR, exist_ok=True)
    out = os.path.join(ASSETS_DIR, "SilverstoneCircuit.glb")

    bpy.ops.object.select_all(action="DESELECT")
    for name in (TRACK_OBJECT, OUTLINE_OBJECT, *OVERLAY_OBJECTS):
        obj = bpy.data.objects.get(name)
        if obj is None:
            raise RuntimeError(f"Missing object {name!r} — run overlay export first")
        obj.hide_set(False)
        obj.select_set(True)

    bpy.context.view_layer.objects.active = bpy.data.objects[TRACK_OBJECT]
    bpy.ops.export_scene.gltf(
        filepath=out,
        use_selection=True,
        export_format="GLB",
        export_apply=True,
        export_yup=True,
        export_materials="EXPORT",
        export_extras=False,
        export_animations=False,
    )
    print("Exported combined circuit to", out)


if __name__ == "__main__":
    main()
