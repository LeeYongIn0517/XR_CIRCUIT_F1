"""Opt-in native HTTP integration. Uses only a loopback fixture, never a live API."""
from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from pathlib import Path
from threading import Thread, Lock
import subprocess, time
counts = {}
lock = Lock()
class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        with lock:
            counts[self.path] = counts.get(self.path, 0) + 1
            body = str(counts[self.path]).encode()
        if self.path.startswith('/timeout/'):
            time.sleep(35)
        if self.path.startswith('/slow/'):
            time.sleep(5)
        self.send_response(200)
        self.send_header('Content-Type', 'text/plain')
        self.send_header('Content-Length', str(len(body)))
        self.send_header('Cache-Control', 'max-age=600' if self.path.startswith('/cache/') else 'no-store')
        self.end_headers()
        try:
            self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            pass
server = ThreadingHTTPServer(('127.0.0.1', 18765), Handler)
Thread(target=server.serve_forever, daemon=True).start()
try:
    result = subprocess.run(['./gradlew', ':data:iosSimulatorArm64Test', '-Pstage3HttpIntegration', '--console=plain'], cwd=Path(__file__).resolve().parents[2])
finally:
    server.shutdown()
    server.server_close()
raise SystemExit(result.returncode)
