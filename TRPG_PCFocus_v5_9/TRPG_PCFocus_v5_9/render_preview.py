from pathlib import Path
import base64
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent


def data_uri(path: Path) -> str:
    mime = 'image/png' if path.suffix.lower() == '.png' else 'application/octet-stream'
    return f"data:{mime};base64," + base64.b64encode(path.read_bytes()).decode('ascii')


def build_html() -> str:
    html = (ROOT / 'index.html').read_text()
    css = (ROOT / 'styles.css').read_text()
    js = (ROOT / 'app.js').read_text()
    html = html.replace('<link rel="stylesheet" href="styles.css" />', f'<style>{css}</style>')
    html = html.replace('<script src="app.js"></script>', f'<script>{js}</script>')
    for rel in [
        'assets/v5-main-decoration.png',
        'assets/pcs/pc-1.png',
        'assets/pcs/pc-2.png',
        'assets/pcs/pc-3.png',
        'assets/pcs/pc-4.png',
    ]:
        html = html.replace(rel, data_uri(ROOT / rel))
    return html


HTML = build_html()
VIEWPORTS = [
    (1672, 941, '1672x941'),
    (1680, 800, '1680x800'),
    (1440, 900, '1440x900'),
    (1366, 768, '1366x768'),
    (390, 844, '390x844'),
]

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, executable_path='/usr/bin/chromium', args=['--disable-gpu', '--no-sandbox'])
    for width, height, label in VIEWPORTS:
        page = browser.new_page(viewport={'width': width, 'height': height}, device_scale_factor=1)
        page.set_content(HTML, wait_until='load')
        page.wait_for_timeout(300)
        page.screenshot(path=str(ROOT / f'v5_9_{label}_pc1.png'), full_page=False)
        page.locator('.selector-card[data-index="1"]').click()
        page.wait_for_timeout(320)
        page.screenshot(path=str(ROOT / f'v5_9_{label}_pc2.png'), full_page=False)
        page.close()
    browser.close()
