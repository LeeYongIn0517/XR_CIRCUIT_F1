"""Inspect local release packages. Never signs, exports or uploads anything."""
import argparse, hashlib, json, os, plistlib, subprocess, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--archive',type=Path);p.add_argument('--android',action='store_true');p.add_argument('--output',type=Path,required=True);a=p.parse_args()
r=Path(__file__).resolve().parents[1]
# Also permit running the review copy from a workspace with an explicit source root.
r=Path(os.environ.get('CIRCUIT_PROJECT_ROOT',r))
result={}
def run(args):
 x=subprocess.run(list(map(str,args)),capture_output=True,text=True)
 return {'exit_code':x.returncode,'text':(x.stdout+x.stderr).strip()}
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()
tracks={str(f.relative_to(r/'mobile-assets')):f for f in (r/'mobile-assets/tracks').rglob('*') if f.is_file()}
if a.android:
 sdk=Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk')))
 bt=sdk/'build-tools/35.0.0'
 for variant in ['mobile','xr']:
  for kind in ['apk','aab']:
   path=(r/f'app/build/outputs/apk/{variant}/release/app-{variant}-release-unsigned.apk' if kind=='apk' else r/f'app/build/outputs/bundle/{variant}Release/app-{variant}-release.aab')
   with zipfile.ZipFile(path) as z:
    names=z.namelist(); prefix='assets/' if kind=='apk' else 'base/assets/'
    glb=sum(n.endswith('.glb') for n in names)
    track_files=[n for n in names if n.startswith(prefix+'tracks/') and not n.endswith('/')]
    dex=b''.join(z.read(n) for n in names if n.endswith('.dex'))
    xr_refs=b'Landroidx/xr/' in dex
    if variant=='mobile':
     assert glb==0 and not xr_refs
     assert len(track_files)==len(tracks)==116
     for name,source in tracks.items(): assert z.read(prefix+name)==source.read_bytes(),name
    else: assert glb==92 and len(track_files)==0
    item={'bytes':path.stat().st_size,'sha256':digest(path),'glb_count':glb,'track_files':len(track_files),'xr_dex_references':xr_refs}
    if kind=='apk':
     item['manifest']=run([bt/'aapt2','dump','badging',path]);assert item['manifest']['exit_code']==0
     assert "minSdkVersion:'34'" in item['manifest']['text']
     assert "targetSdkVersion:'36'" in item['manifest']['text']
     assert "application-debuggable" not in item['manifest']['text']
     assert ("android.software.xr.api.spatial" in item['manifest']['text'])==(variant=='xr')
     item['signature']=run([bt/'apksigner','verify','--verbose','--print-certs',path])
    else:
     item['signature_entries']=[n for n in names if n.startswith('META-INF/') and n.upper().endswith(('.RSA','.DSA','.EC','.SF'))]
     item['signature']=run([Path(os.environ['JAVA_HOME'])/'bin/jarsigner','-verify',path])
    result[variant+'_'+kind]=item
if a.archive:
 app=a.archive/'Products/Applications/CircuitConnection.app';assert app.is_dir()
 info=plistlib.loads((app/'Info.plist').read_bytes());archive=plistlib.loads((a.archive/'Info.plist').read_bytes())
 assert info['CFBundleIdentifier']=='app.yongin.xr-circuit.connection'
 assert info['CFBundleShortVersionString']=='1.0' and info['CFBundleVersion']=='1'
 assert info['MinimumOSVersion']=='17.0'
 for name,source in tracks.items(): assert (app/name).read_bytes()==source.read_bytes(),name
 assert not list(app.rglob('*.glb'))
 result['ios']={'info':{k:info.get(k) for k in ['CFBundleIdentifier','CFBundleShortVersionString','CFBundleVersion','MinimumOSVersion','UIDeviceFamily','CFBundleIcons']},'archive_application_properties':archive.get('ApplicationProperties'),'track_files':len(tracks),'glb_count':0,'binary_sha256':digest(app/'CircuitConnection'),'signature':run(['codesign','--verify','--deep','--strict',app]),'embedded_profile':(app/'embedded.mobileprovision').exists()}
a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n');print(a.output)
