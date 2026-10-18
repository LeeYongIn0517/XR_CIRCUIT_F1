"""Run with python3 -m unittest discover -s presentation/src/main/blender/tests -p 'test_*.py'."""
import json
import math
from pathlib import Path
import struct
import unittest

ROOT = Path(__file__).resolve().parents[5]
ASSETS = ROOT / 'presentation/src/main/assets'
DATA = ROOT / 'data/src/main/assets'


def glb(path):
    raw = path.read_bytes()
    magic, version, length = struct.unpack_from('<4sII', raw)
    assert (magic, version, length) == (b'glTF', 2, len(raw))
    size, kind = struct.unpack_from('<II', raw, 12)
    assert kind == 0x4e4f534a
    return json.loads(raw[20:20+size])


class TrackAssetsTest(unittest.TestCase):
    def test_ready_catalog_entries_resolve_to_distinct_combined_models(self):
        catalog = json.loads((DATA/'circuits/index.json').read_text())['circuits']
        ready = [c for c in catalog if (DATA/c['detailAssetPath']).exists()
                 and (ASSETS/c['modelAsset']).exists() and (ASSETS/c['waypointsAsset']).exists()]
        self.assertTrue({'silverstone', 'albert_park', 'shanghai', 'marina_bay', 'suzuka', 'miami', 'villeneuve', 'monaco', 'catalunya'}.issubset({c['id'] for c in ready}))
        self.assertEqual(len(ready), len({c['modelAsset'] for c in ready}))
        for c in ready:
            doc = glb(ASSETS/c['modelAsset'])
            names = {n.get('name') for n in doc['nodes']}
            detail = json.loads((DATA/c['detailAssetPath']).read_text())
            for s in detail['sectors']: self.assertIn('SectorStrip_'+str(s['index']), names)
            for z in detail['drsZones']: self.assertIn('DrsStrip_'+str(z['index']), names)

    def test_melbourne_path_sectors_and_wrapped_drs_share_indices(self):
        detail = json.loads((DATA/'circuits/albert_park.json').read_text())
        path = json.loads((ASSETS/'melbourne_waypoints.json').read_text())
        wp = path['waypoints_gltf_yup']; n = len(wp)
        self.assertTrue(path['closed_loop'])
        self.assertEqual(list(range(n)), [p['index'] for p in wp])
        self.assertEqual(4, len(detail['drsZones']))
        self.assertEqual(14, len(detail['corners']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(n, detail['sectors'][-1]['endWaypoint'])
        for a,b in zip(detail['sectors'],detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'],b['startWaypoint'])
        for z in detail['drsZones']:
            self.assertTrue(0 <= z['startWaypoint'] < n)
            self.assertTrue(0 <= z['endWaypoint'] < n)
        self.assertTrue(any(z['startWaypoint'] > z['endWaypoint'] for z in detail['drsZones']))
        distances=[math.dist(a['position'], b['position']) for a,b in zip(wp,wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances),path['path_length_m'],places=4)

    def test_melbourne_bounds_match_catalog_and_centered_origin(self):
        entry=next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id']=='albert_park')
        doc=glb(ASSETS/entry['modelAsset'])
        bounds=[doc['accessors'][p['attributes']['POSITION']] for mesh in doc['meshes'] for p in mesh['primitives']]
        for axis, key in enumerate(['modelWidthMeters','modelHeightMeters','modelDepthMeters']):
            low=min(a['min'][axis] for a in bounds); high=max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low,entry[key],places=4)
            self.assertAlmostEqual(high+low,0,places=4)
        self.assertFalse(entry['legacyOverlayRotation'])

    def test_silverstone_generated_contract(self):
        detail = json.loads((DATA/'circuits/silverstone.json').read_text())
        path = json.loads((ASSETS/'silverstone_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'silverstone')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_british_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 19)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertEqual(sorted(corners), corners)
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[4] < detail['sectors'][0]['endWaypoint'] < corners[5])
        self.assertTrue(corners[13] < detail['sectors'][1]['endWaypoint'] < corners[14])
        self.assertEqual(2, len(detail['drsZones']))
        for zone, start_turn, end_turn in zip(detail['drsZones'], [5, 14], [6, 15]):
            self.assertTrue(corners[start_turn-1] <= zone['startWaypoint'] < zone['endWaypoint'] < corners[end_turn-1])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)
        self.assertIn('lapRecord', detail)
        self.assertIn('characteristics', detail)
        for key in ['entryWaypoint', 'exitWaypoint']:
            self.assertTrue(0 <= detail['pitLane'][key] < len(wp))

    def test_shanghai_generated_contract(self):
        detail = json.loads((DATA/'circuits/shanghai.json').read_text())
        path = json.loads((ASSETS/'shanghai_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'shanghai')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_chinese_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 17)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[4] < detail['sectors'][0]['endWaypoint'] < corners[5])
        self.assertTrue(corners[9] < detail['sectors'][1]['endWaypoint'] < corners[10])
        self.assertEqual(2, len(detail['drsZones']))
        back, main = detail['drsZones']
        self.assertTrue(corners[12] < back['startWaypoint'] < back['endWaypoint'] < corners[13])
        self.assertTrue(corners[15] < main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        # Sampling is uniform along the curve. The straight chord across the
        # tight T14 hairpin is shorter than its arc; other samples stay uniform.
        hairpin = corners[13]
        regular = [d for i, d in enumerate(distances) if abs(i-hairpin) > 1]
        self.assertLess(max(regular)/min(regular), 1.08)
        self.assertGreater(min(distances), .85 * max(distances))
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

    def test_singapore_generated_contract(self):
        detail = json.loads((DATA/'circuits/marina_bay.json').read_text())
        path = json.loads((ASSETS/'singapore_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'marina_bay')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_singapore_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 20)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[5] < detail['sectors'][0]['endWaypoint'] < corners[6])
        self.assertTrue(corners[12] < detail['sectors'][1]['endWaypoint'] < corners[13])
        self.assertEqual(4, len(detail['drsZones']))
        for zone, start, end in zip(detail['drsZones'][:3], [5, 13, 14], [7, 14, 16]):
            self.assertTrue(corners[start-1] < zone['startWaypoint'] < zone['endWaypoint'] < corners[end-1])
        main = detail['drsZones'][3]
        self.assertTrue(corners[18] < main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

    def test_suzuka_generated_contract(self):
        detail = json.loads((DATA/'circuits/suzuka.json').read_text())
        path = json.loads((ASSETS/'suzuka_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'suzuka')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_japanese_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 19)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[6] < detail['sectors'][0]['endWaypoint'] < corners[7])
        self.assertTrue(corners[13] < detail['sectors'][1]['endWaypoint'] < corners[14])
        self.assertEqual(1, len(detail['drsZones']))
        main = detail['drsZones'][0]
        self.assertTrue(corners[17] < main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        # At the figure-eight crossing the T14-T15 road must clear T9-T10.
        lower = range(corners[8], corners[9]+1)
        upper = range(corners[13], corners[14]+1)
        a, b = min(((i, j) for i in lower for j in upper), key=lambda ij:
            sum((wp[ij[0]]['position'][k]-wp[ij[1]]['position'][k])**2 for k in [0, 2]))
        self.assertLess(math.dist([wp[a]['position'][k] for k in [0, 2]],
                                  [wp[b]['position'][k] for k in [0, 2]]), .15)
        self.assertGreater(wp[b]['position'][1]-wp[a]['position'][1], 1.0)
        for i, point in enumerate(wp):
            delta = [wp[(i+1)%512]['position'][k]-wp[(i-1)%512]['position'][k] for k in range(3)]
            length = math.sqrt(sum(v*v for v in delta))
            for actual, expected in zip(point['forward'], delta):
                self.assertAlmostEqual(actual, expected/length, places=4)
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        # Uniform plan-view arc sampling: ramps increase 3D lengths, while
        # the tight T11 hairpin shortens the chord between adjacent samples.
        self.assertLess(max(distances)/min(distances), 1.4)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

    def test_miami_generated_contract(self):
        detail = json.loads((DATA/'circuits/miami.json').read_text())
        path = json.loads((ASSETS/'miami_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'miami')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_miami_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 20)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[7] < detail['sectors'][0]['endWaypoint'] < corners[8])
        self.assertTrue(corners[15] < detail['sectors'][1]['endWaypoint'] < corners[16])
        self.assertEqual(3, len(detail['drsZones']))
        for zone, start, end in zip(detail['drsZones'][:2], [9, 16], [11, 17]):
            self.assertTrue(corners[start-1] < zone['startWaypoint'] < zone['endWaypoint'] < corners[end-1])
        main = detail['drsZones'][2]
        self.assertTrue(corners[18] <= main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

    def test_montreal_generated_contract(self):
        detail = json.loads((DATA/'circuits/villeneuve.json').read_text())
        path = json.loads((ASSETS/'montreal_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'villeneuve')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_canadian_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 15)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[4] < detail['sectors'][0]['endWaypoint'] < corners[5])
        self.assertTrue(corners[8] < detail['sectors'][1]['endWaypoint'] < corners[9])
        self.assertEqual(3, len(detail['drsZones']))
        for zone, start, end in zip(detail['drsZones'][:2], [7, 11], [8, 13]):
            self.assertTrue(corners[start-1] < zone['startWaypoint'] < zone['endWaypoint'] < corners[end-1])
        main = detail['drsZones'][2]
        self.assertTrue(corners[13] < main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

    def test_monaco_generated_contract(self):
        detail = json.loads((DATA/'circuits/monaco.json').read_text())
        path = json.loads((ASSETS/'monaco_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'monaco')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_monaco_event', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 20)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[3] < detail['sectors'][0]['endWaypoint'] < corners[4])
        self.assertTrue(corners[11] < detail['sectors'][1]['endWaypoint'] < corners[12])
        self.assertEqual(1, len(detail['drsZones']))
        main = detail['drsZones'][0]
        self.assertTrue(corners[18] < main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

    def test_catalunya_generated_contract(self):
        detail = json.loads((DATA/'circuits/catalunya.json').read_text())
        path = json.loads((ASSETS/'barcelona_waypoints.json').read_text())
        entry = next(c for c in json.loads((DATA/'circuits/index.json').read_text())['circuits'] if c['id'] == 'catalunya')
        wp = path['waypoints_gltf_yup']
        self.assertEqual(2025, path['layout_year'])
        self.assertIn('2025_spanish_grand_prix', path['source'])
        self.assertTrue(path['closed_loop'])
        self.assertEqual(512, len(wp))
        self.assertEqual(list(range(512)), [p['index'] for p in wp])
        self.assertEqual(list(range(1, 15)), [c['number'] for c in detail['corners']])
        corners = [c['waypointIndex'] for c in detail['corners']]
        self.assertTrue(all(a < b for a, b in zip(corners, corners[1:])))
        self.assertEqual(3, len(detail['sectors']))
        self.assertEqual(0, detail['sectors'][0]['startWaypoint'])
        self.assertEqual(512, detail['sectors'][-1]['endWaypoint'])
        for a, b in zip(detail['sectors'], detail['sectors'][1:]):
            self.assertEqual(a['endWaypoint'], b['startWaypoint'])
        self.assertTrue(corners[2] < detail['sectors'][0]['endWaypoint'] < corners[3])
        self.assertTrue(corners[8] < detail['sectors'][1]['endWaypoint'] < corners[9])
        self.assertEqual(2, len(detail['drsZones']))
        back = detail['drsZones'][0]
        self.assertTrue(corners[8] < back['startWaypoint'] < back['endWaypoint'] < corners[9])
        main = detail['drsZones'][1]
        self.assertTrue(corners[13] < main['startWaypoint'] < 512)
        self.assertTrue(0 < main['endWaypoint'] < corners[0])
        distances = [math.dist(a['position'], b['position']) for a, b in zip(wp, wp[1:]+wp[:1])]
        self.assertLess(max(distances)/min(distances), 1.08)
        self.assertAlmostEqual(sum(distances), path['path_length_m'], places=4)
        self.assertFalse(entry['legacyOverlayRotation'])
        doc = glb(ASSETS/entry['modelAsset'])
        bounds = [doc['accessors'][p['attributes']['POSITION']] for m in doc['meshes'] for p in m['primitives']]
        for axis, key in enumerate(['modelWidthMeters', 'modelHeightMeters', 'modelDepthMeters']):
            low = min(a['min'][axis] for a in bounds)
            high = max(a['max'][axis] for a in bounds)
            self.assertAlmostEqual(high-low, entry[key], places=4)
            self.assertAlmostEqual(high+low, 0, places=4)

if __name__ == '__main__': unittest.main()
