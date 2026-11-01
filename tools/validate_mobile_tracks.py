"""Validate actual pixels, coordinates, seams, provenance and packaging boundaries."""
from pathlib import Path
import hashlib,json,math
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
assets=ROOT/'mobile-assets'
manifest=json.loads((assets/'tracks/index.json').read_text())
assert manifest['schemaVersion']==1
assert not list(assets.rglob('*.glb'))
checks=0
for entry in manifest['circuits']:
 cid=entry['circuitId']; folder=assets/'tracks'/cid
 p=json.loads((folder/'path.json').read_text()); pts=p['points']; n=len(pts)
 src=ROOT/f'presentation/src/main/assets/{cid}_waypoints.json'
 original=json.loads(src.read_text())['waypoints_gltf_yup']
 provenance=json.loads((folder/'provenance.json').read_text())
 assert provenance['sha256']==hashlib.sha256(src.read_bytes()).hexdigest()
 assert provenance['detailSha256']==hashlib.sha256((ROOT/f'data/assets/circuits/{cid}.json').read_bytes()).hexdigest()
 assert n==512 and pts[0]['progress']==0 and pts[-1]['progress']<1
 assert all(a['progress']<b['progress'] for a,b in zip(pts,pts[1:]))
 for pt,srcpt in zip(pts,original):
  t=p['projection']; x,y,z=srcpt['position']
  assert abs(pt['x']-((x-t['centerX'])*t['scale']+.5))<1e-10
  assert abs(pt['y']-((z-t['centerZ'])*t['scale']+.5))<1e-10
  assert .079999<=pt['x']<=.920001 and .079999<=pt['y']<=.920001
 for c in p['corners']:
  assert (c['x'],c['y'])==(pts[c['waypointIndex']]['x'],pts[c['waypointIndex']]['y'])
 assert len(set(c['id'] for c in p['corners']))==18
 for name in ('track','sectors','drs'):
  im=Image.open(assets/entry[name]); assert im.size==(1024,1024) and im.mode=='RGBA'
  assert im.getpixel((0,0))[3]==0
  ranges=range(n) if name!='drs' else [i%n for z in p['drsZones'] for i in range(z['startWaypoint'],z['endWaypoint']+(n if z['endWaypoint']<=z['startWaypoint'] else 0)+1)]
  for i in ranges:
   q=pts[i]; assert im.getpixel((round(q['x']*1024),round(q['y']*1024)))[3]>200,(cid,name,i)
  checks+=1
 # Suzuka's DRS crosses the seam: pixel checks above include 511->0.
 print(cid,': 512 projection/alpha checks, 18 corners, sector/DRS coverage, hashes PASS')
print('PASS:',checks,'PNG layers; mobile root contains zero GLB')
