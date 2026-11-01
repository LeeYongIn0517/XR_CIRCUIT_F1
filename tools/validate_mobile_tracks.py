"""Validate actual pixels, coordinates, seams, provenance and packaging boundaries."""
from pathlib import Path
import hashlib,json,math
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
assets=ROOT/'mobile-assets'
manifest=json.loads((assets/'tracks/index.json').read_text())
assert manifest['schemaVersion']==1
assert not list(assets.rglob('*.glb'))
catalog=json.loads((ROOT/'data/assets/circuits/index.json').read_text())['circuits']
by_id={c['id']:c for c in catalog}
assert len(manifest['circuits'])==len(catalog)==len({c['circuitId'] for c in manifest['circuits']})
assert {c['circuitId'] for c in manifest['circuits']}==set(by_id)
checks=0
for entry in manifest['circuits']:
 cid=entry['circuitId']; folder=assets/'tracks'/cid
 p=json.loads((folder/'path.json').read_text()); pts=p['points']; n=len(pts)
 entry_catalog=by_id[cid]
 src=ROOT/'presentation/src/main/assets'/entry_catalog['waypointsAsset']
 detail=json.loads((ROOT/'data/assets'/entry_catalog['detailAssetPath']).read_text())
 assert (ROOT/'presentation/src/main/assets'/entry_catalog['modelAsset']).is_file()
 assert p['circuitId']==cid
 assert entry['path']==f'tracks/{cid}/path.json'
 assert p['width']==entry['width']==1024 and p['height']==entry['height']==1024
 assert [(c['id'],c['waypointIndex']) for c in p['corners']]==[(c['turnCode'],c['waypointIndex']) for c in detail['corners']]
 assert len(p['corners'])==len(detail['corners'])
 assert p['sectors']==detail['sectors'] and p['drsZones']==detail['drsZones']
 assert len(p['sectors'])==3
 assert p['sectors'][0]['startWaypoint']==0 and p['sectors'][-1]['endWaypoint']==len(pts)
 assert all(a['endWaypoint']==b['startWaypoint'] for a,b in zip(p['sectors'],p['sectors'][1:]))
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
 assert len(set(c['id'] for c in p['corners']))==len(detail['corners'])
 for name in ('track','sectors','drs'):
  assert entry[name]==f'tracks/{cid}/{name}.png'
  im=Image.open(assets/entry[name]); assert im.size==(1024,1024) and im.mode=='RGBA'
  assert im.getpixel((0,0))[3]==0
  if name=='drs' and not p['drsZones']: assert im.getchannel('A').getbbox() is None
  ranges=range(n) if name!='drs' else [i%n for z in p['drsZones'] for i in range(z['startWaypoint'],z['endWaypoint']+(n if z['endWaypoint']<=z['startWaypoint'] else 0)+1)]
  for i in ranges:
   q=pts[i]; assert im.getpixel((round(q['x']*1024),round(q['y']*1024)))[3]>200,(cid,name,i)
  checks+=1
 # Suzuka's DRS crosses the seam: pixel checks above include 511->0.
 print(cid,f': {n} projection/alpha checks, {len(p["corners"])} corners, sector/DRS coverage, hashes PASS')
print('PASS:',checks,'PNG layers; catalog/detail/2D/3D parity; mobile root contains zero GLB')
