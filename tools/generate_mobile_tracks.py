"""Generate representative mobile assets from checked-in XR paths. Requires Pillow.
Run from repo root: python3 tools/generate_mobile_tracks.py
No network, Blender, or generated geographic imagery is used.
"""
from pathlib import Path
import hashlib, json, math
from PIL import Image, ImageDraw
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'mobile-assets/tracks'
SIZE, PAD, AA = 1024, 0.08, 3

def write(path, value):
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n')

def generate(cid):
    source = ROOT / f'presentation/src/main/assets/{cid}_waypoints.json'
    detail_source = ROOT / f'data/assets/circuits/{cid}.json'
    raw = json.loads(source.read_text())
    detail = json.loads(detail_source.read_text())
    # glTF X right, Z down, top view: no per-platform rotation/reflection.
    xyz = [p['position'] for p in raw['waypoints_gltf_yup']]
    minx,maxx = min(p[0] for p in xyz),max(p[0] for p in xyz)
    minz,maxz = min(p[2] for p in xyz),max(p[2] for p in xyz)
    span=max(maxx-minx,maxz-minz)
    scale=(1-2*PAD)/span
    points=[[(p[0]-(minx+maxx)/2)*scale+.5,(p[2]-(minz+maxz)/2)*scale+.5] for p in xyz]
    n=len(points)
    # Preserve XR traversal timing, including Suzuka elevation at the crossover.
    distances=[0.0]
    for a,b in zip(xyz,xyz[1:]+xyz[:1]):
        distances.append(distances[-1]+math.dist(a,b))
    progress=[d/distances[-1] for d in distances[:-1]]
    out=OUT/cid; out.mkdir(parents=True,exist_ok=True)
    def render(name, segments):
        im=Image.new('RGBA',(SIZE*AA,SIZE*AA))
        draw=ImageDraw.Draw(im)
        for indices,color,width in segments:
            xy=[(points[i%n][0]*SIZE*AA,points[i%n][1]*SIZE*AA) for i in indices]
            draw.line(xy,fill=color,width=round(width*AA),joint='curve')
            radius=width*AA/2
            for x,y in (xy[0],xy[-1]):
                draw.ellipse((x-radius,y-radius,x+radius,y+radius),fill=color)
        im.resize((SIZE,SIZE),Image.Resampling.LANCZOS).save(out/name)
    def indices(a,b):
        if b<=a: b+=n
        return range(a,b+1)
    render('track.png',[(range(n+1),'#465363',22),(range(n+1),'#DCE4EE',16)])
    render('sectors.png',[(indices(s['startWaypoint'],s['endWaypoint']),s['colorHex'],10) for s in detail['sectors']])
    render('drs.png',[(indices(s['startWaypoint'],s['endWaypoint']),'#38EB85',5) for s in detail['drsZones']])
    path={'schemaVersion':1,'circuitId':cid,'width':SIZE,'height':SIZE,'closedLoop':True,
          'progressBasis':'normalized 3D polyline arc length; same traversal order as XR',
          'projection':{'xAxis':'gltf +X','yAxis':'gltf +Z','rotationDegrees':0,'padding':PAD,'scale':scale,'centerX':(minx+maxx)/2,'centerZ':(minz+maxz)/2},
          'points':[{'x':p[0],'y':p[1],'progress':progress[i]} for i,p in enumerate(points)],
          'corners':[{'id':c['turnCode'],'waypointIndex':c['waypointIndex'],'x':points[c['waypointIndex']][0],'y':points[c['waypointIndex']][1]} for c in detail['corners']],
          'sectors':detail['sectors'],'drsZones':detail['drsZones']}
    write(out/'path.json',path)
    write(out/'provenance.json',{'source':raw['source'],'layoutYear':raw['layout_year'],
        'derivedFrom':str(source.relative_to(ROOT)), 'sha256':hashlib.sha256(source.read_bytes()).hexdigest(),
        'detailSha256':hashlib.sha256(detail_source.read_bytes()).hexdigest(),
        'rights':'Existing repository trace; no explicit redistribution license verified. Release permission review remains open.',
        'accuracy':'Map-derived stylized centreline, not survey data. 2025 DRS layout. Suzuka elevation flattened in 2D; traversal remains ordered.'})
    return {'circuitId':cid,'track':f'tracks/{cid}/track.png','sectors':f'tracks/{cid}/sectors.png','drs':f'tracks/{cid}/drs.png','path':f'tracks/{cid}/path.json','width':SIZE,'height':SIZE}

if __name__=='__main__':
    OUT.mkdir(parents=True,exist_ok=True)
    write(OUT/'index.json',{'schemaVersion':1,'circuits':[generate(c) for c in ('silverstone','suzuka')]})
