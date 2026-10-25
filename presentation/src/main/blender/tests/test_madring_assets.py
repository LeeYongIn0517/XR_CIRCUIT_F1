import json,math,struct,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[5]
ASSETS=ROOT/'presentation/src/main/assets'
DATA=ROOT/'data/src/main/assets/circuits'
class MadringAssetsTest(unittest.TestCase):
 def test_design_path_and_model_contract(self):
  definition=json.loads((ROOT/'presentation/src/main/blender/tracks/madring.json').read_text())
  detail=json.loads((DATA/'madring.json').read_text())
  path=json.loads((ASSETS/'madring_waypoints.json').read_text())
  entry=next(c for c in json.loads((DATA/'index.json').read_text())['circuits'] if c['id']=='madring')
  self.assertEqual(definition['sectorStatus'],'provisional_equal_distance')
  self.assertEqual(definition['drsStatus'],'unspecified_in_2025_source')
  self.assertEqual(path['layout_year'],2025)
  self.assertIn('madring.com/notas-prensa/diseno-circuito',path['source'])
  self.assertTrue(path['closed_loop'])
  wp=path['waypoints_gltf_yup']
  self.assertEqual([p['index'] for p in wp],list(range(512)))
  corners=detail['corners'];self.assertEqual([c['number'] for c in corners],list(range(1,23)))
  indices=[c['waypointIndex'] for c in corners]
  self.assertTrue(all(a<b for a,b in zip(indices,indices[1:])))
  self.assertEqual(detail['drsZones'],[]);self.assertEqual(detail['drsZoneCount'],0)
  self.assertEqual(detail['sectors'][0]['startWaypoint'],0)
  self.assertEqual(detail['sectors'][-1]['endWaypoint'],512)
  for a,b in zip(detail['sectors'],detail['sectors'][1:]):self.assertEqual(a['endWaypoint'],b['startWaypoint'])
  for s in detail['sectors']:self.assertLess(abs(s['endWaypoint']-s['startWaypoint']-512/3),3)
  distances=[math.dist(a['position'],b['position']) for a,b in zip(wp,wp[1:]+wp[:1])]
  self.assertLess(max(distances)/min(distances),1.3)
  self.assertAlmostEqual(sum(distances),path['path_length_m'],places=4)
  raw=(ASSETS/entry['modelAsset']).read_bytes()
  self.assertEqual(struct.unpack_from('<4sII',raw),(b'glTF',2,len(raw)))
  size=struct.unpack_from('<I',raw,12)[0];doc=json.loads(raw[20:20+size])
  names=[n.get('name','') for n in doc['nodes']]
  self.assertFalse(any(n.startswith('DrsStrip_') for n in names))
  for i in range(1,4):self.assertIn('SectorStrip_'+str(i),names)
  bounds=[doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
  for axis,key in enumerate(['modelWidthMeters','modelHeightMeters','modelDepthMeters']):
   low=min(a['min'][axis] for a in bounds);high=max(a['max'][axis] for a in bounds)
   self.assertAlmostEqual(high-low,entry[key],places=4)
   self.assertAlmostEqual(high+low,0,places=4)
  self.assertFalse(entry['legacyOverlayRotation'])
