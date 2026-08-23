"""Export SilverstoneTrack.glb with a neutral light-gray palette."""

from __future__ import annotations

import os

import bpy

ASSETS_DIR = os.path.normpath(
    os.path.join(os.path.dirname(__file__), "..", "assets"),
)

ASPHALT = (0.898, 0.906, 0.922, 1.0)  # ~ #E5E7EB
KERB = (0.820, 0.835, 0.859, 1.0)  # ~ #D1D5DB


def _set_principled(mat: bpy.types.Material, rgba: tuple[float, float, float, float]) -> None:
    mat.diffuse_color = rgba
    if not mat.use_nodes:
        mat.use_nodes = True
    for node in mat.node_tree.nodes:
        if node.type == "BSDF_PRINCIPLED":
            node.inputs["Base Color"].default_value = rgba
            node.inputs["Roughness"].default_value = 0.55
            node.inputs["Metallic"].default_value = 0.0
            node.inputs["Emission Color"].default_value = (0.0, 0.0, 0.0, 1.0)
            node.inputs["Emission Strength"].default_value = 0.0


def main() -> None:
    _set_principled(bpy.data.materials["Asphalt"], ASPHALT)
    _set_principled(bpy.data.materials["OrangeKerb"], KERB)

    out = os.path.join(ASSETS_DIR, "SilverstoneTrack.glb")
    os.makedirs(ASSETS_DIR, exist_ok=True)

    bpy.ops.object.select_all(action="DESELECT")
    for name in ("SilverstoneTrack", "SilverstoneOutline"):
        obj = bpy.data.objects[name]
        obj.hide_set(False)
        obj.select_set(True)
    bpy.context.view_layer.objects.active = bpy.data.objects["SilverstoneTrack"]

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
    print("Exported", out)


if __name__ == "__main__":
    main()
