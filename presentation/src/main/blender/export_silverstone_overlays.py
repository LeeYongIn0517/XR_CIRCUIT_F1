"""
Regenerate Silverstone sector / DRS overlay GLBs from the open Blender scene.

Prerequisites:
  - RacePath curve (600 cyclic poly points, length ~53.65 m)
  - SilverstoneTrack mesh (0.44 m square bevel of RacePath)

Sector / DRS strips are extracted from the track mesh top faces so overlays
share the exact same geometry as the asphalt surface (no separate bevel path).

Exports (Y-up glTF):
  presentation/src/main/assets/SilverstoneSectors.glb
  presentation/src/main/assets/SilverstoneDrs.glb

Run inside Blender:
  blender --background SilverstoneTrack.blend --python export_silverstone_overlays.py
  or Scripting workspace → Run Script
"""

from __future__ import annotations

import os

import bmesh
import bpy
from mathutils import Vector

WP_COUNT = 107
ASSETS_DIR = os.path.normpath(
    os.path.join(os.path.dirname(__file__), "..", "assets"),
)

# F1 official Silverstone sectors (clockwise from finish line):
#   S1 red   — Abbey (T1) through Aintree (T5), ends before Brooklands (T6)
#   S2 cyan  — Brooklands (T6) through Chapel (T14)
#   S3 yellow— Stowe (T15) through Club (T18) back to finish
# Waypoint indices derived from known corner anchors (T1=5, T9=30, T10=42, T15=78, T18=95).
SECTORS = [
    (1, 0, 20, "#FF0000"),
    (2, 20, 71, "#00C8FF"),
    (3, 71, 106, "#FFFF00"),
]
DRS_ZONES = [
    (1, 0, 8),
    (2, 88, 102),
]
DRS_COLOR = "#9333EA"

# Extend each sector/DRS slice by this many RacePath points (~0.5 m) so strips
# overlap at boundaries and the start/finish loop closes without visible gaps.
RACE_POINT_OVERLAP = 8

# Square TrackProfile top in Blender Z-up (glTF Y-up after export).
TRACK_TOP_Z = 0.22
# Lift overlays above asphalt. 0.05 m authored ≈ 2.5 mm after TrackDisplayScale 0.05.
SURFACE_LIFT_Z = 0.05
OVERLAY_THICKNESS = 0.003

REMOVE_PREFIXES = (
    "OverlayProfile",
    "SectorPath",
    "DrsPath",
    "SectorStrip",
    "DrsStrip",
    "OverlayBound",
)


def _cleanup() -> None:
    for obj in list(bpy.data.objects):
        if obj.name.startswith(REMOVE_PREFIXES):
            bpy.data.objects.remove(obj, do_unlink=True)
    for mat in list(bpy.data.materials):
        if mat.name.startswith(("SectorMat", "DrsMat", "OverlayBoundMat")):
            bpy.data.materials.remove(mat)
    for curve in list(bpy.data.curves):
        if curve.name.startswith(("OverlayProfile", "SectorPath", "DrsPath")):
            bpy.data.curves.remove(curve)
    for mesh in list(bpy.data.meshes):
        if mesh.name.startswith(("SectorStrip", "DrsStrip", "OverlayBound")):
            bpy.data.meshes.remove(mesh)


def _hex_to_rgba(hex_color: str) -> tuple[float, float, float, float]:
    h = hex_color.lstrip("#")
    return (
        int(h[0:2], 16) / 255.0,
        int(h[2:4], 16) / 255.0,
        int(h[4:6], 16) / 255.0,
        1.0,
    )


def _make_emissive_mat(name: str, hex_color: str, strength: float) -> bpy.types.Material:
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    # Render both sides so the overlay stays visible from the aerial XR view.
    mat.use_backface_culling = False
    nodes = mat.node_tree.nodes
    links = mat.node_tree.links
    nodes.clear()
    out = nodes.new("ShaderNodeOutputMaterial")
    emis = nodes.new("ShaderNodeEmission")
    rgba = _hex_to_rgba(hex_color)
    emis.inputs["Color"].default_value = rgba
    emis.inputs["Strength"].default_value = strength
    links.new(emis.outputs["Emission"], out.inputs["Surface"])
    mat.diffuse_color = rgba
    return mat


def _nearest_race_index(point: Vector, race_pts: list[Vector]) -> int:
    px, py = point.x, point.y
    best_i = 0
    best_d = float("inf")
    for i, rp in enumerate(race_pts):
        d = (px - rp.x) ** 2 + (py - rp.y) ** 2
        if d < best_d:
            best_d = d
            best_i = i
    return best_i


def _build_top_face_race_map(
    track: bpy.types.Object,
    race_pts: list[Vector],
) -> dict[int, int]:
    """Map each upward-facing track face index to its nearest RacePath point."""
    mesh = track.data
    face_race: dict[int, int] = {}
    for poly in mesh.polygons:
        if poly.normal.z < 0.9:
            continue
        center = Vector((0.0, 0.0, 0.0))
        for vert_idx in poly.vertices:
            center += mesh.vertices[vert_idx].co
        center /= len(poly.vertices)
        face_race[poly.index] = _nearest_race_index(center, race_pts)
    return face_race


def _extract_track_overlay(
    track: bpy.types.Object,
    face_race: dict[int, int],
    allowed_race_indices: set[int],
    mesh_name: str,
    material: bpy.types.Material,
) -> bpy.types.Object:
    """Copy selected asphalt top faces into a single-sided emissive sheet on the track."""
    bm_src = bmesh.new()
    bm_src.from_mesh(track.data)
    bm_src.faces.ensure_lookup_table()
    bm_src.verts.ensure_lookup_table()
    bm_src.normal_update()

    bm_out = bmesh.new()
    vert_map: dict[int, bmesh.types.BMVert] = {}
    overlay_z = TRACK_TOP_Z + SURFACE_LIFT_Z

    for face_idx, race_idx in face_race.items():
        if race_idx not in allowed_race_indices:
            continue
        face = bm_src.faces[face_idx]
        out_verts = []
        for vert in face.verts:
            if vert.index not in vert_map:
                lifted = vert.co.copy()
                lifted.z = overlay_z
                vert_map[vert.index] = bm_out.verts.new(lifted)
            out_verts.append(vert_map[vert.index])
        bm_out.faces.new(out_verts)

    if not bm_out.faces:
        bm_src.free()
        bm_out.free()
        raise RuntimeError(f"No faces selected for {mesh_name}")

    bm_out.normal_update()
    downward = [face for face in bm_out.faces if face.normal.z < 0.0]
    if downward:
        bmesh.ops.reverse_faces(bm_out, faces=downward)
        bm_out.normal_update()

    extruded = bmesh.ops.extrude_face_region(bm_out, geom=bm_out.faces[:])
    extruded_verts = [elem for elem in extruded["geom"] if isinstance(elem, bmesh.types.BMVert)]
    bmesh.ops.translate(bm_out, vec=(0.0, 0.0, OVERLAY_THICKNESS), verts=extruded_verts)
    bm_out.normal_update()
    for face in bm_out.faces:
        if face.normal.z < 0.0:
            bmesh.ops.reverse_faces(bm_out, faces=[face])
    bm_out.normal_update()

    mesh_data = bpy.data.meshes.new(mesh_name)
    bm_out.to_mesh(mesh_data)
    bm_out.free()
    bm_src.free()
    mesh_data.update()

    obj = bpy.data.objects.new(mesh_name, mesh_data)
    obj.matrix_world = track.matrix_world.copy()
    bpy.context.scene.collection.objects.link(obj)
    obj.data.materials.clear()
    obj.data.materials.append(material)
    return obj


def _export_glb(filepath: str, object_names: list[str]) -> None:
    bpy.ops.object.select_all(action="DESELECT")
    for name in object_names:
        obj = bpy.data.objects[name]
        obj.hide_set(False)
        obj.select_set(True)
    bpy.context.view_layer.objects.active = bpy.data.objects[object_names[0]]
    bpy.ops.export_scene.gltf(
        filepath=filepath,
        use_selection=True,
        export_format="GLB",
        export_apply=True,
        export_yup=True,
        export_materials="EXPORT",
        export_extras=False,
        export_animations=False,
    )


def main() -> None:
    race = bpy.data.objects["RacePath"]
    track = bpy.data.objects["SilverstoneTrack"]
    race_pts = [Vector(p.co[:3]) for p in race.data.splines[0].points]
    n = len(race_pts)

    def wp_to_idx(wp: int) -> int:
        return int(round(wp * n / WP_COUNT)) % n

    def slice_indices(start_wp: int, end_wp: int) -> set[int]:
        a = wp_to_idx(start_wp)
        b = wp_to_idx(end_wp)
        a = (a - RACE_POINT_OVERLAP) % n
        b = (b + RACE_POINT_OVERLAP) % n
        if a <= b:
            return set(range(a, b + 1))
        return set(range(a, n)) | set(range(0, b + 1))

    _cleanup()
    face_race = _build_top_face_race_map(track, race_pts)

    sector_names: list[str] = []
    for idx, start, end, color in SECTORS:
        mat = _make_emissive_mat(f"SectorMat_{idx}", color, 2.5)
        mesh = _extract_track_overlay(
            track,
            face_race,
            slice_indices(start, end),
            f"SectorStrip_{idx}",
            mat,
        )
        sector_names.append(mesh.name)

    drs_names: list[str] = []
    drs_mat = _make_emissive_mat("DrsMat", DRS_COLOR, 3.0)
    for idx, start, end in DRS_ZONES:
        mesh = _extract_track_overlay(
            track,
            face_race,
            slice_indices(start, end),
            f"DrsStrip_{idx}",
            drs_mat,
        )
        drs_names.append(mesh.name)

    os.makedirs(ASSETS_DIR, exist_ok=True)
    _export_glb(
        os.path.join(ASSETS_DIR, "SilverstoneSectors.glb"),
        sector_names,
    )
    _export_glb(
        os.path.join(ASSETS_DIR, "SilverstoneDrs.glb"),
        drs_names,
    )
    print("Exported overlays to", ASSETS_DIR)


if __name__ == "__main__":
    main()
