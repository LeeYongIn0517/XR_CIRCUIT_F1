"""Compatibility entry point: rebuild all Silverstone assets from the FIA trace.

Run in a separate background Blender process, as documented in ../README.md.
"""
from pathlib import Path
import runpy
import sys

if __name__ == '__main__':
    builder = Path(__file__).with_name('build_track.py')
    sys.argv = [str(builder), '--', 'tracks/silverstone.json']
    runpy.run_path(str(builder), run_name='__main__')
