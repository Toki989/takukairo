// Static-only QA host: a repository subpath, with no API or SPA fallback.
import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const root = fileURLToPath(new URL('../dist-demo/', import.meta.url));
const prefix = '/takukairo/';
const mime = {'.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8', '.json': 'application/json', '.webmanifest': 'application/manifest+json', '.png': 'image/png', '.jpg': 'image/jpeg', '.svg': 'image/svg+xml'};
export function startDemoServer() {
const server = http.createServer(async (req, res) => {
  try {
    const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
    if (!pathname.startsWith(prefix) || !['GET', 'HEAD'].includes(req.method)) throw new Error('Not found');
    const relative = pathname.slice(prefix.length) || 'index.html';
    const filename = path.resolve(root, relative);
    if (!filename.startsWith(path.resolve(root) + path.sep)) throw new Error('Outside root');
    const body = await fs.readFile(filename);
    res.writeHead(200, {'Content-Type': mime[path.extname(filename)] ?? 'application/octet-stream', 'Cache-Control': 'no-store'});
    res.end(req.method === 'HEAD' ? undefined : body);
  } catch { res.writeHead(404); res.end('Not found'); }
});
return new Promise((resolve, reject) => {
  server.once('error', reject);
  server.listen(4175, '127.0.0.1', () => { console.log('Demo QA: http://127.0.0.1:4175/takukairo/'); resolve(server); });
});
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) await startDemoServer();
