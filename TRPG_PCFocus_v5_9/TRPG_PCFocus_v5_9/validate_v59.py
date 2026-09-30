from pathlib import Path
import base64, json
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent


def data_uri(path: Path) -> str:
    return 'data:image/png;base64,' + base64.b64encode(path.read_bytes()).decode()


html = (ROOT / 'index.html').read_text()
css = (ROOT / 'styles.css').read_text()
js = (ROOT / 'app.js').read_text()
html = html.replace('<link rel="stylesheet" href="styles.css" />', f'<style>{css}</style>')
html = html.replace('<script src="app.js"></script>', f'<script>{js}</script>')
for rel in ['assets/v5-main-decoration.png','assets/pcs/pc-1.png','assets/pcs/pc-2.png','assets/pcs/pc-3.png','assets/pcs/pc-4.png']:
    html = html.replace(rel, data_uri(ROOT / rel))

viewports = [(1672,941),(1680,800),(1440,900),(1366,768),(390,844)]

def overlap_1d(a0, a1, b0, b1):
    return max(0, min(a1,b1)-max(a0,b0))

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, executable_path='/usr/bin/chromium', args=['--disable-gpu','--no-sandbox'])
    out = []
    for w,h in viewports:
        page = browser.new_page(viewport={'width':w,'height':h})
        page.set_content(html,wait_until='load')
        page.wait_for_timeout(220)
        for idx in [0,1]:
            if idx == 1:
                page.locator('.selector-card[data-index="1"]').click()
                page.wait_for_timeout(180)
            def box(sel):
                b = page.locator(sel).bounding_box()
                return None if not b else {k:round(v,2) for k,v in b.items()}
            center=box('.center-lane'); pc=box('.pc-info'); nav=box('.view-nav'); ending=box('.ending-value')
            selarea=box('.selector-area'); active=box('.selector-card.is-active'); portrait=box('.portrait-zone'); header=box('.topbar')
            ending_nav=0
            if ending and nav:
                ending_nav=overlap_1d(ending['y'],ending['y']+ending['height'],nav['y'],nav['y']+nav['height'])
            info_active_x_gap=None
            if pc and active:
                info_active_x_gap=active['x']-(pc['x']+pc['width'])
            center_selector_y_overlap=0
            if center and selarea:
                center_selector_y_overlap=overlap_1d(center['y'],center['y']+center['height'],selarea['y'],selarea['y']+selarea['height'])
            out.append({
                'viewport':[w,h], 'pc':idx+1,
                'header':header,'portrait':portrait,'center_lane':center,'pc_info':pc,'ending':ending,'nav':nav,'selector_area':selarea,'active_selector':active,
                'ending_nav_vertical_overlap':round(ending_nav,2),
                'pc_to_active_selector_horizontal_gap':None if info_active_x_gap is None else round(info_active_x_gap,2),
                'center_selector_vertical_overlap':round(center_selector_y_overlap,2),
                'scrollWidth':page.evaluate('document.documentElement.scrollWidth'),
                'clientWidth':page.evaluate('document.documentElement.clientWidth'),
                'scrollHeight':page.evaluate('document.documentElement.scrollHeight'),
                'clientHeight':page.evaluate('document.documentElement.clientHeight')
            })
        # keyboard/listbox smoke check
        first=page.locator('.selector-card[data-index="0"]')
        first.focus()
        first.press('ArrowRight')
        page.wait_for_timeout(160)
        out.append({'viewport':[w,h],'keyboard_after_arrow_right':page.locator('.selector-card.is-active').get_attribute('data-index'),'focused_index':page.evaluate('document.activeElement?.dataset?.index ?? null')})
        page.close()
    browser.close()
print(json.dumps(out,ensure_ascii=False,indent=2))
