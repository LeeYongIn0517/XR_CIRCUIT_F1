"""Build a track, overlays and equal-distance car path from one authored definition.

From the repository root:
blender --background --python presentation/src/main/blender/scripts/build_track.py -- tracks/albert_park.json
Uses no external Python packages. Run in a separate background Blender process.
"""
import bisect
import json
import math
from pathlib import Path
import sys

import bpy
import bmesh
from mathutils import Vector

HERE = Path(__file__).resolve().parent.parent
ROOT = HERE.parents[3]
ASSETS = HERE.parent / 'assets'
MODELS = HERE / 'models'
PREVIEWS = HERE / 'previews'
COUNT = 512


def main():
    config_path = Path(sys.argv[sys.argv.index('--') + 1])
    if not config_path.is_absolute():
        config_path = HERE / config_path
    config = json.loads(config_path.read_text())
    prefix = config['assetPrefix']
    for directory in (ASSETS, MODELS, PREVIEWS):
        directory.mkdir(parents=True, exist_ok=True)
    # Round traced corners conservatively, then resample by arc length.
    points = [Vector(p) for p in config['centerline']]
    for _ in range(2):
        points = [v for a, b in zip(points, points[1:] + points[:1])
                  for v in (a * .75 + b * .25, a * .25 + b * .75)]
    first = min(range(len(points)), key=lambda i: (points[i] - Vector(config['centerline'][0])).length)
    points = points[first:] + points[:first]
    points.append(points[0])
    cumulative = [0.0]
    for a, b in zip(points, points[1:]):
        cumulative.append(cumulative[-1] + (b - a).length)
    samples = []
    for i in range(COUNT):
        distance = i * cumulative[-1] / COUNT
        j = min(bisect.bisect_right(cumulative, distance) - 1, len(points) - 2)
        samples.append(points[j].lerp(points[j+1], (distance-cumulative[j])/(cumulative[j+1]-cumulative[j])))
    # Landscape presentation, uniform scale; preserve the actual layout proportions.
    span = max(p.y for p in samples) - min(p.y for p in samples)
    scale = 16.0 / span
    mid_x = (max(p.x for p in samples) + min(p.x for p in samples)) / 2
    mid_y = (max(p.y for p in samples) + min(p.y for p in samples)) / 2
    path = [Vector(((p.y-mid_y)*scale, (p.x-mid_x)*scale, 0)) for p in samples]
    def nearest(point):
        return min(range(COUNT), key=lambda i:(samples[i]-Vector(point)).length)
    # Optional model-space heights; raised crossings share geometry with car paths.
    elevation = [(nearest(k['point']), k['height']) for k in config.get('elevationProfile', [])]
    if elevation:
        if any(a[0] >= b[0] for a, b in zip(elevation, elevation[1:])):
            raise ValueError('Elevation anchors must follow race order with distinct indices')
        if elevation[0][1] != 0 or elevation[-1][1] != 0:
            raise ValueError('Elevation profile must start and end at ground level')
        for (start, low), (end, high) in zip(elevation, elevation[1:]):
            for i in range(start, end+1):
                t = (i-start)/(end-start)
                path[i].z = low + (high-low)*(t*t*(3-2*t))
    length = sum((b-a).length for a,b in zip(path,path[1:]+path[:1]))
    boundaries = [0] + [nearest(p) for p in config['sectorBoundaries']] + [COUNT]
    drs = [dict(index=z['index'], startWaypoint=nearest(z['start']), endWaypoint=nearest(z['end'])) for z in config['drsZones']]
    colors = [(1,.015,.035,1),(.0,.58,1,1),(1,.77,.015,1)]
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.delete(use_global=False)
    def material(name, color):
        mat = bpy.data.materials.new(name)
        mat.diffuse_color = color
        mat.use_nodes = True
        node = next(n for n in mat.node_tree.nodes if n.type == 'BSDF_PRINCIPLED')
        node.inputs['Base Color'].default_value = color
        node.inputs['Roughness'].default_value = .55
        return mat
    asphalt = material('Asphalt', (.898,.906,.922,1))
    kerb = material('Outline', (.820,.835,.859,1))
    sector_mats = [material('Sector'+str(i+1), c) for i,c in enumerate(colors)]
    drs_mat = material('DRS', (.01,1,.11,1))
    def strip(name, start, end, width, offset, bottom, top, mat):
        width *= config.get('trackWidthScale', 1.0)
        offset *= config.get('trackWidthScale', 1.0)
        indices = list(range(start, end+1)) if end > start else list(range(start, end+COUNT+1))
        verts, faces = [], []
        for i in indices:
            tangent = (path[(i+1)%COUNT] - path[(i-1)%COUNT]).normalized()
            normal = Vector((-tangent.y,tangent.x,0)).normalized()
            center = path[i%COUNT] + normal*offset
            for side,z in [(-1,bottom),(1,bottom),(-1,top),(1,top)]:
                p = center+normal*(side*width/2)
                verts.append((p.x,p.y,p.z+z))
        for j in range(len(indices)-1):
            a,b = j*4,(j+1)*4
            faces += [(a+2,a+3,b+3,b+2),(a,b,b+1,a+1),(a,a+2,b+2,b),(a+1,b+1,b+3,a+3)]
        if end - start != COUNT:
            faces += [(0,1,3,2),(len(verts)-4,len(verts)-2,len(verts)-1,len(verts)-3)]
        mesh=bpy.data.meshes.new(name)
        mesh.from_pydata(verts,[],faces)
        mesh.update()
        bm = bmesh.new()
        bm.from_mesh(mesh)
        bmesh.ops.remove_doubles(bm, verts=list(bm.verts), dist=0.00001)
        bmesh.ops.recalc_face_normals(bm, faces=list(bm.faces))
        bm.to_mesh(mesh)
        bm.free()
        obj=bpy.data.objects.new(name,mesh)
        bpy.context.collection.objects.link(obj)
        obj.data.materials.append(mat)
        return obj
    outline=strip(prefix+'Outline',0,COUNT,.52,0,-.26,.18,kerb)
    track=strip(prefix+'Track',0,COUNT,.44,0,-.22,.22,asphalt)
    sectors=[strip('SectorStrip_'+str(i+1),boundaries[i],boundaries[i+1],.12,-.14,.223,.229,sector_mats[i]) for i in range(3)]
    zones=[strip('DrsStrip_'+str(z['index']),z['startWaypoint'],z['endWaypoint'],.14,.10,.231,.237,drs_mat) for z in drs]
    # Recenter every exported mesh and the car path to the same bounding-box origin.
    all_meshes=[track,outline]+sectors+zones
    coords=[v.co.copy() for obj in all_meshes for v in obj.data.vertices]
    mins=Vector(tuple(min(p[i] for p in coords) for i in range(3)))
    maxs=Vector(tuple(max(p[i] for p in coords) for i in range(3)))
    center=(mins+maxs)/2
    for obj in all_meshes:
        for v in obj.data.vertices:v.co-=center
    def waypoint(i):
        p=path[i]-center
        forward=(path[(i+1)%COUNT]-path[(i-1)%COUNT]).normalized()
        return dict(index=i,position=[round(p.x,6),round(p.z+.32,6),round(-p.y,6)],forward=[forward.x,forward.z,-forward.y])
    path_data=dict(track=config['name'],source=config['source'],layout_year=config['layoutYear'],closed_loop=True,units='meters',path_length_m=length,waypoint_count=COUNT,waypoints_gltf_yup=[waypoint(i) for i in range(COUNT)])
    (ASSETS/config['waypointsAsset']).write_text(json.dumps(path_data,indent=2)+'\n')
    def export(name, objects):
        bpy.ops.object.select_all(action='DESELECT')
        for obj in objects:obj.select_set(True)
        bpy.context.view_layer.objects.active=objects[0]
        bpy.ops.export_scene.gltf(filepath=str(ASSETS/name),use_selection=True,export_format='GLB',export_apply=True,export_yup=True,export_materials='EXPORT',export_animations=False)
    export(prefix+'Circuit.glb',all_meshes)
    export(prefix+'Track.glb',[track,outline])
    export(prefix+'Sectors.glb',sectors)
    if zones:
        export(prefix+'Drs.glb',zones)
    # Keep an editable centreline in the .blend, excluded from exported meshes.
    curve=bpy.data.curves.new('RacePath','CURVE');curve.dimensions='3D'
    spline=curve.splines.new('POLY');spline.points.add(COUNT-1)
    for i,p in enumerate(path):spline.points[i].co=(*(p-center),1)
    spline.use_cyclic_u=True
    guide=bpy.data.objects.new('RacePath',curve);bpy.context.collection.objects.link(guide);guide.hide_render=True
    scene=bpy.context.scene
    scene['source']=config['source'];scene['layout_year']=config['layoutYear'];scene['notes']=config['notes']
    bpy.ops.object.camera_add(location=(0,-10,23))
    camera=bpy.context.object;camera.name='PreviewCamera'
    camera.rotation_euler=(Vector((0,0,0))-camera.location).to_track_quat('-Z','Y').to_euler()
    camera.data.type='ORTHO';camera.data.ortho_scale=config.get('previewOrthoScale', 19)
    scene.camera=camera
    scene.render.engine='BLENDER_WORKBENCH'
    scene.display.shading.light='STUDIO';scene.display.shading.color_type='MATERIAL'
    scene.display.shading.show_shadows=True;scene.display.shading.show_cavity=True
    scene.display.shading.background_type='WORLD';scene.world.color=(.055,.065,.085)
    scene.render.resolution_x=1400;scene.render.resolution_y=1000;scene.render.resolution_percentage=100
    scene.render.image_settings.file_format='PNG'
    scene.render.filepath=str(PREVIEWS/(prefix+'Preview.png'))
    bpy.ops.object.select_all(action='DESELECT');track.select_set(True);bpy.context.view_layer.objects.active=track
    bpy.context.preferences.filepaths.save_version = 0
    bpy.ops.wm.save_as_mainfile(filepath=str(MODELS/(prefix+'Track.blend')))
    bpy.ops.render.render(write_still=True)
    detail=dict(config['profile'],id=config['id'],name=config['name'],drsZoneCount=len(zones),sectors=[dict(index=i+1,startWaypoint=boundaries[i],endWaypoint=boundaries[i+1],colorHex=['#FF0000','#00C8FF','#FFFF00'][i]) for i in range(3)],drsZones=drs,corners=[dict(number=c['number'],name=c['name'],turnCode='T'+str(c['number']),waypointIndex=nearest(c['point']), **({'description': c['description']} if 'description' in c else {})) for c in config['corners']])
    if 'pitLane' in config:
        pit = config['pitLane']
        detail['pitLane'] = dict(speedLimitKmh=pit['speedLimitKmh'], entryWaypoint=nearest(pit['entry']), exitWaypoint=nearest(pit['exit']))
    (ROOT/'data/src/main/assets/circuits'/ (config['id']+'.json')).write_text(json.dumps(detail,indent=2)+'\n')
    catalog_path=ROOT/'data/src/main/assets/circuits/index.json'
    catalog=json.loads(catalog_path.read_text())
    entry=next(e for e in catalog['circuits'] if e['id']==config['id'])
    entry.update(modelAsset=prefix+'Circuit.glb',waypointsAsset=config['waypointsAsset'],modelWidthMeters=round(maxs.x-mins.x,6),modelHeightMeters=round(maxs.z-mins.z,6),modelDepthMeters=round(maxs.y-mins.y,6),legacyOverlayRotation=False)
    catalog_path.write_text(json.dumps(catalog,indent=2)+'\n')
    print('Generated',config['id'],'bounds',list(maxs-mins),'sectors',boundaries,'DRS',drs)

if __name__=='__main__':main()
