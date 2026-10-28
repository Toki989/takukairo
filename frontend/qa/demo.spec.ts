import {test, expect, type Page} from '@playwright/test';
import {createSeed, DEMO_KEY} from '../src/demo/store';

async function expectCenteredGalleryRows(page: Page) {
  await expect(page.locator('.scenario-gallery')).toHaveCSS('display', 'flex');
  await expect.poll(() => page.locator('.scenario-gallery').evaluate(gallery => {
    const bounds = gallery.getBoundingClientRect();
    const rows: {center: number; left: number; right: number}[] = [];
    for (const item of Array.from(gallery.children)) {
      const rect = item.getBoundingClientRect();
      const row = rows.at(-1);
      const center = rect.top + rect.height / 2;
      if (row && Math.abs(center - row.center) < 1) row.right = rect.right;
      else rows.push({center, left: rect.left, right: rect.right});
    }
    return rows.length > 0 && rows.every(row => Math.abs((row.left - bounds.left) - (bounds.right - row.right)) < 1);
  })).toBe(true);
}

test('BOOTH candidates require explicit adoption and persist only an external image URL',async({page})=>{
 const candidate={name:'BOOTH取得確認用シナリオ',authorName:'作者候補',gameSystem:'クトゥルフ神話TRPG 6版',sourceUrl:'https://booth.pm/ja/items/3403696',externalImageUrl:'https://booth.pximg.net/test/i/3403696/cover.png'};
 await page.route(/\/(?:__)?booth\/preview\?/,route=>route.fulfill({status:200,contentType:'application/json',headers:{'Access-Control-Allow-Origin':'*'},body:JSON.stringify(candidate)}));
 await page.route(candidate.externalImageUrl,route=>route.fulfill({status:200,contentType:'image/svg+xml',body:'<svg xmlns="http://www.w3.org/2000/svg" width="400" height="600"><rect width="400" height="600" fill="#3f7d79"/></svg>'}));
 await page.goto('./#/scenarios/new');
 await page.getByText('BOOTHから入力を補助',{exact:true}).click();
 await page.getByLabel('BOOTHの商品URL',{exact:true}).fill(candidate.sourceUrl);
 await page.getByRole('button',{name:'情報を取得',exact:true}).click();
 await expect(page.getByRole('region',{name:'BOOTHの商品情報候補'})).toBeVisible();
 await expect(page.getByLabel('シナリオ名',{exact:true})).toHaveValue('');await expect(page.getByLabel('外部画像URL',{exact:true})).toHaveValue('');
 expect(await page.evaluate(()=>localStorage.getItem('takukairo-demo-v1'))).toBeNull();
 await page.getByRole('button',{name:'選択した情報を入力欄へ反映',exact:true}).click();
 await page.getByText('商品画像の候補を確認',{exact:true}).click();await page.getByRole('button',{name:'この画像を使用',exact:true}).click();
 await expect(page.getByLabel('外部画像URL',{exact:true})).toHaveValue(candidate.externalImageUrl);
 await page.getByRole('button',{name:'保存',exact:true}).click();await expect(page.getByRole('dialog')).toContainText('外部画像');await page.getByRole('dialog').getByRole('button',{name:'続ける',exact:true}).click();
 await expect(page.getByRole('heading',{name:candidate.name,exact:true})).toBeVisible();
 let stored=await page.evaluate(()=>JSON.parse(localStorage.getItem('takukairo-demo-v1')!));let saved=stored.scenarios.find((item:{name:string})=>item.name===candidate.name);
 expect(saved.externalImageUrl).toBe(candidate.externalImageUrl);expect(saved.image).toBeNull();expect(stored.images['scenarios/'+saved.id]).toBeUndefined();expect(stored.externalImageConsentGiven).toBe(true);
 await page.reload();await expect(page.getByRole('img',{name:candidate.name,exact:true})).toHaveAttribute('src',candidate.externalImageUrl);
 stored=await page.evaluate(()=>JSON.parse(localStorage.getItem('takukairo-demo-v1')!));saved=stored.scenarios.find((item:{name:string})=>item.name===candidate.name);expect(saved.externalImageUrl).toBe(candidate.externalImageUrl);
});

test('BOOTH retrieval failure preserves manual input in the demo',async({page})=>{
 await page.route(/\/(?:__)?booth\/preview\?/,route=>route.fulfill({status:502,contentType:'application/json',headers:{'Access-Control-Allow-Origin':'*'},body:JSON.stringify({code:'BOOTH_FETCH_FAILED',message:'商品情報を取得できませんでした。',fieldErrors:[]})}));
 await page.goto('./#/scenarios/new');await page.getByLabel('シナリオ名',{exact:true}).fill('手入力の作品');await page.getByText('BOOTHから入力を補助',{exact:true}).click();await page.getByLabel('BOOTHの商品URL',{exact:true}).fill('https://booth.pm/ja/items/3403696');await page.getByRole('button',{name:'情報を取得',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('商品情報を取得できませんでした。');await expect(page.getByLabel('シナリオ名',{exact:true})).toHaveValue('手入力の作品');expect(await page.evaluate(()=>localStorage.getItem('takukairo-demo-v1'))).toBeNull();
});

test('a static subpath opens without login, with all trailers and no API calls', async ({page, context}) => {
  const errors: string[] = [], requests: string[] = [];
  page.on('pageerror', e => errors.push(e.message));
  page.on('request', request => requests.push(request.url()));
  await page.goto('./');
  await expect(page.getByRole('heading', {name: 'あなたの回廊', exact: true})).toBeVisible();
  await expect(page.locator('.gallery-item')).toHaveCount(19);
  await expect.poll(() => page.locator('.gallery-item img').evaluateAll(images => images.every(image => (image as HTMLImageElement).complete && (image as HTMLImageElement).naturalWidth > 0))).toBe(true);
  await expect(page).toHaveTitle('卓回廊 — ブラウザ体験版');
  expect(requests.some(url => new URL(url).pathname.startsWith('/api'))).toBe(false);
  expect(requests.every(url => new URL(url).origin === 'http://127.0.0.1:4175')).toBe(true);
  expect(await context.cookies()).toEqual([]);
  expect(errors).toEqual([]);
  await page.screenshot({path: '../.runtime/demo-home-desktop.png', fullPage: true});
});

test('gallery honors artwork proportions, gives wide art more space and preserves sort order', async ({page}) => {
  for (const width of [1440, 1200, 1199, 768, 767, 375]) {
    await page.setViewportSize({width, height: 1000});
    await page.goto('./');
    await expect(page.locator('.scenario-gallery .gallery-item')).toHaveCount(19);
    await expect.poll(() => page.locator('.scenario-gallery img').evaluateAll(images => images.length === 19 && images.every(image => (image as HTMLImageElement).complete && (image as HTMLImageElement).naturalWidth > 0))).toBe(true);
    const geometry = await page.locator('.scenario-gallery .gallery-item').evaluateAll(items => items.map(item => {
      const image = item.querySelector('img')!;
      const rect = image.getBoundingClientRect();
      const art = image.parentElement!.getBoundingClientRect();
      const work = item.getBoundingClientRect();
      const title = item.querySelector('h2')!.getBoundingClientRect();
      const favorite = item.querySelector('button')!.getBoundingClientRect();
      return {shape: item.getAttribute('data-shape'), width: rect.width, height: rect.height, naturalRatio: image.naturalWidth / image.naturalHeight, artHeight: art.height, x: rect.x, rowCenter: work.top + work.height / 2, titleBottom: title.bottom, favoriteTop: favorite.top};
    }));
    for (const image of geometry) {
      expect(Math.abs(image.width / image.height - image.naturalRatio)).toBeLessThan(0.01);
      expect(Math.abs(image.artHeight - image.height)).toBeLessThan(1);
      expect(image.titleBottom).toBeLessThanOrEqual(image.favoriteTop + 1);
    }
    expect(geometry[0].shape).toBe('portrait');
    expect(geometry[0].height).toBeGreaterThan(geometry[0].width * 1.5);
    expect(geometry[1].shape).toBe('landscape');
    expect(geometry[1].width).toBeGreaterThan(geometry[0].width * 1.9);
    expect(geometry[2].shape).toBe('square');
    // Read each centered exhibition row from left to right, then move to the next row.
    for (let i = 1; i < geometry.length; i++) expect(geometry[i].rowCenter >= geometry[i - 1].rowCenter - 1 && (geometry[i].rowCenter > geometry[i - 1].rowCenter + 1 || geometry[i].x > geometry[i - 1].x)).toBe(true);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  }
  await page.getByLabel('並び順', {exact: true}).selectOption('NAME');
  await expect.poll(async () => {
    const names = await page.locator('.scenario-gallery h2').allTextContents();
    return names.join('|') === [...names].sort((a, b) => a.localeCompare(b, 'ja')).join('|');
  }).toBe(true);
});

test('secondary gallery information is quiet until pointer or keyboard focus, with touch access', async ({page, browser}) => {
  await page.goto('./');
  const work = page.locator('.scenario-gallery .gallery-item').filter({has: page.getByRole('heading', {name: '灯台は夢を見るか', exact: true})});
  const count = work.getByText('1 卓', {exact: true});
  const favorite = work.getByRole('button', {name: '灯台は夢を見るかのお気に入り', exact: true});
  await expect(count).toHaveCSS('opacity', '0');
  await expect(favorite).toHaveCSS('opacity', '0');
  await work.getByRole('link').focus();
  await expect(count).toHaveCSS('opacity', '1');
  await page.keyboard.press('Tab');
  await expect(favorite).toBeFocused();
  await page.keyboard.press('Space');
  await expect(favorite).toHaveAttribute('aria-pressed', 'true');
  const touch = await browser.newContext({viewport: {width: 375, height: 812}, hasTouch: true, isMobile: true});
  try {
    const mobile = await touch.newPage();
    await mobile.goto(page.url());
    const button = mobile.getByRole('button', {name: '灯台は夢を見るかのお気に入り', exact: true});
    await expect(button).toHaveCSS('opacity', '1');
    await button.tap();
    await expect(button).toHaveAttribute('aria-pressed', 'true');
  } finally { await touch.close(); }
});

test('No29 centers every row after sorting, filtering and viewport changes', async ({page, context, browserName}) => {
  // Reproduce CI's delayed controlled-input updates instead of relying on a fast renderer.
  test.setTimeout(60000);
  if (browserName === 'chromium') {
    const renderer = await context.newCDPSession(page);
    await renderer.send('Emulation.setCPUThrottlingRate', {rate: 6});
  }
  const names = createSeed().scenarios.map(scenario => scenario.name);
  const expectedNames = {
    NAME: [...names].sort((a, b) => a.localeCompare(b, 'ja')),
    CREATED_ASC: names,
    CREATED_DESC: [...names].reverse(),
    RECENT_PLAYED: [...names.slice(0, 12), ...names.slice(12).reverse()],
  };
  await page.goto('./');
  await expect.poll(() => page.locator('.scenario-gallery img').evaluateAll(images => images.length === 19 && images.every(image => (image as HTMLImageElement).complete && (image as HTMLImageElement).naturalWidth > 0))).toBe(true);
  for (const width of [1440, 1200, 1199, 768, 767, 375]) {
    await page.setViewportSize({width, height: 1000});
    for (const sort of ['NAME', 'CREATED_ASC', 'CREATED_DESC', 'RECENT_PLAYED'] as const) {
      await page.getByLabel('並び順', {exact: true}).selectOption(sort);
      await expect(page).toHaveURL(url => new URLSearchParams(url.hash.split('?')[1]).get('sort') === sort);
      await expect(page.getByLabel('並び順', {exact: true})).toHaveValue(sort);
      await expect(page.locator('.scenario-gallery h2')).toHaveText(expectedNames[sort]);
      await expect(page.locator('.scenario-gallery')).toHaveAttribute('aria-busy', 'false');
      await expectCenteredGalleryRows(page);
    }
  }
  const filter = page.getByLabel('お気に入り', {exact: true});
  await expect(filter).not.toBeChecked();
  // check()/uncheck() assert immediately after the click; query-backed state commits later.
  await filter.click();
  await expect(filter).toBeChecked();
  await expect(page).toHaveURL(url => new URLSearchParams(url.hash.split('?')[1]).get('favorite') === 'true');
  await expect(page.locator('.scenario-gallery .gallery-item')).toHaveCount(3);
  await expectCenteredGalleryRows(page);
  await filter.click();
  await expect(filter).not.toBeChecked();
  await expect(page).toHaveURL(url => !new URLSearchParams(url.hash.split('?')[1]).has('favorite'));
  await expect(page.locator('.scenario-gallery .gallery-item')).toHaveCount(19);
  await page.getByLabel('シナリオ・作者・PL・PCを検索', {exact: true}).fill('灯台は夢を見るか');
  await expect(page.locator('.scenario-gallery .gallery-item')).toHaveCount(1);
  await expectCenteredGalleryRows(page);
});

test('No29 splits unused space equally for the reported landscape and portrait pair', async ({page}) => {
  const data = createSeed();
  data.scenarios = data.scenarios.filter(scenario => [1, 15].includes(scenario.id));
  data.tables = data.tables.filter(table => [1, 15].includes(table.scenarioId));
  await page.addInitScript(({key, data}) => localStorage.setItem(key, JSON.stringify(data)), {key: DEMO_KEY, data});
  await page.goto('./#/?sort=NAME');
  await expect(page.locator('.scenario-gallery .gallery-item')).toHaveCount(2);
  await expect.poll(() => page.locator('.scenario-gallery img').evaluateAll(images => images.length === 2 && images.every(image => (image as HTMLImageElement).complete && (image as HTMLImageElement).naturalWidth > 0))).toBe(true);
  for (const width of [1440, 1199, 768, 767, 375]) {
    await page.setViewportSize({width, height: 1000});
    await expectCenteredGalleryRows(page);
    await expect(page.locator('.scenario-gallery h2')).toHaveText(['失われた遊園地', '硝子温室の残響']);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
    if (width === 1440) {
      const margins = await page.locator('.scenario-gallery').evaluate(gallery => {
        const bounds = gallery.getBoundingClientRect();
        const items = Array.from(gallery.children).map(item => item.getBoundingClientRect());
        return {left: items[0].left - bounds.left, right: bounds.right - items[1].right};
      });
      expect(margins.left).toBeGreaterThan(100);
      expect(Math.abs(margins.left - margins.right)).toBeLessThan(1);
      await page.screenshot({path: '../.runtime/no29-two-works-desktop.png', fullPage: true});
    }
  }
});

test('hover fades supplemental controls without layout shifts and supports reduced motion', async ({page}) => {
  await page.goto('./');
  const work = page.locator('.scenario-gallery .gallery-item').filter({has: page.getByRole('heading', {name: '灯台は夢を見るか', exact: true})});
  const count = work.getByText('1 卓', {exact: true});
  const favorite = work.getByRole('button', {name: '灯台は夢を見るかのお気に入り', exact: true});
  await work.locator('img').waitFor();
  await expect(count).toHaveCSS('opacity', '0');
  await expect(count).toHaveCSS('transition-duration', '0.26s');
  await work.scrollIntoViewIfNeeded();
  const originalHeight = await work.evaluate(element => element.getBoundingClientRect().height);
  await work.getByRole('link').hover();
  await expect(count).toHaveCSS('opacity', '1');
  await expect(favorite).toHaveCSS('opacity', '1');
  await expect(work.getByRole('heading')).toHaveCSS('color', 'rgb(63, 125, 121)');
  expect(await work.evaluate(element => element.getBoundingClientRect().height)).toBe(originalHeight);
  const href = page.url();
  await count.click();
  expect(page.url()).toBe(href);
  await page.mouse.move(0, 0);
  await work.getByRole('link').focus();
  await expect(work.getByRole('heading')).toHaveCSS('color', 'rgb(63, 125, 121)');
  await expect(count).toHaveCSS('opacity', '1');
  await page.emulateMedia({reducedMotion: 'reduce'});
  await expect(count).toHaveCSS('transition-duration', '0s');
  await page.keyboard.press('Tab');
  await expect(favorite).toBeFocused();
  await page.keyboard.press('Space');
  await expect(favorite).toHaveAttribute('aria-pressed', 'true');
});

test('favorite, search, persistence and reset preserve unrelated browser data', async ({page}) => {
  await page.goto('./');
  const favorite = page.getByRole('button', {name: '硝子温室の残響のお気に入り', exact: true});
  await expect(favorite).toHaveAttribute('aria-pressed', 'true');
  await favorite.click();
  await expect(favorite).toHaveAttribute('aria-pressed', 'false');
  await page.reload();
  await expect(favorite).toHaveAttribute('aria-pressed', 'false');
  await page.getByLabel('シナリオ・作者・PL・PCを検索', {exact: true}).fill('逆さ水族館');
  await expect(page.locator('.gallery-item')).toHaveCount(1);
  await page.getByLabel('シナリオ・作者・PL・PCを検索', {exact: true}).fill('見つからない作品');
  await expect(page.getByText('条件に一致する作品がありません。')).toBeVisible();
  await page.evaluate(() => localStorage.setItem('unrelated-project', 'keep'));
  await page.getByRole('button', {name: 'サンプルに戻す', exact: true}).click();
  await page.getByRole('dialog', {name: '確認', exact: true}).getByRole('button', {name: '続ける'}).click();
  await expect(page.locator('.gallery-item')).toHaveCount(19);
  await expect(favorite).toHaveAttribute('aria-pressed', 'true');
  expect(await page.evaluate(() => localStorage.getItem('unrelated-project'))).toBe('keep');
});

test('scenario creation, edit, uploaded image and deletion stay local', async ({page}) => {
  await page.goto('./#/scenarios/new');
  await page.getByLabel('シナリオ名', {exact: true}).fill('体験版で作った物語');
  await page.getByLabel('作者名', {exact: true}).fill('見学者');
  await page.getByRole('button', {name: '保存', exact: true}).click();
  await expect(page.getByRole('heading', {name: '体験版で作った物語', exact: true})).toBeVisible();
  const url = page.url();
  await page.getByRole('link', {name: 'シナリオを編集'}).click();
  await page.getByLabel('シナリオ名', {exact: true}).fill('名前を変更した物語');
  await page.locator('input[type=file]').setInputFiles('public/demo-assets/pc-01.png');
  await page.getByRole('button', {name: '保存', exact: true}).click();
  await expect(page.getByRole('heading', {name: '名前を変更した物語', exact: true})).toBeVisible();
  await page.reload();
  await expect(page.locator('.hero img')).toHaveAttribute('src', /^data:image\/png/);
  await page.getByRole('link', {name: 'シナリオを編集'}).click();
  await page.getByRole('button', {name: 'シナリオを削除', exact: true}).click();
  await page.getByRole('dialog', {name: '確認', exact: true}).getByRole('button', {name: '続ける'}).click();
  await expect(page.getByRole('heading', {name: 'あなたの回廊', exact: true})).toBeVisible();
  await page.goto(url);
  await expect(page.getByRole('alert')).toContainText('対象が見つかりません');
});

test('table detail, keyboard selection, edit and deep-link reload work', async ({page}) => {
  await page.goto('./#/tables/1');
  await expect(page.getByRole('heading', {name: '鴉羽 律', exact: true})).toBeVisible();
  await page.getByRole('option').first().focus();
  await page.keyboard.press('ArrowRight');
  await expect(page.getByRole('heading', {name: '水瀬 澪', exact: true})).toBeVisible();
  await expect.poll(() => page.locator('img[alt=""]').evaluate(image => (image as HTMLImageElement).naturalWidth)).toBeGreaterThan(0);
  await page.screenshot({path: '../.runtime/demo-table-desktop.png', fullPage: true});
  await page.getByRole('link', {name: '卓の記録を編集', exact: true}).click();
  await page.getByLabel('卓名（任意）', {exact: true}).fill('体験版で編集した卓');
  await page.getByRole('button', {name: '卓を保存', exact: true}).click();
  await expect(page.getByRole('heading', {name: '体験版で編集した卓'})).toBeVisible();
  await page.goto('./#/tables/1');
  await page.reload();
  await expect(page.getByRole('heading', {name: '鴉羽 律', exact: true})).toBeVisible();
});

test('PC creation and editing reflect in the collection after reload', async ({page}) => {
  await page.goto('./#/pcs/new');
  await page.getByLabel('PC名', {exact: true}).fill('体験の探索者');
  await page.getByRole('combobox', {name: '現在PL', exact: true}).click();
  await page.getByRole('option', {name: 'あなた（体験用）', exact: true}).click();
  await page.getByRole('button', {name: '保存', exact: true}).click();
  await expect(page.getByRole('heading', {name: '体験の探索者', exact: true})).toBeVisible();
  await page.getByRole('link', {name: 'PCを編集', exact: true}).click();
  await page.getByLabel('PC名', {exact: true}).fill('旅を続ける探索者');
  await page.getByRole('button', {name: '保存', exact: true}).click();
  await expect(page.getByRole('heading', {name: '旅を続ける探索者', exact: true})).toBeVisible();
  await page.reload();
  await expect(page.getByRole('heading', {name: '旅を続ける探索者', exact: true})).toBeVisible();
});

test('CSV sample can be reviewed, edited and registered with draft restoration', async ({page}) => {
  await page.goto('./#/import');
  await page.getByRole('button', {name: 'サンプルCSVを使う', exact: true}).click();
  await page.getByRole('button', {name: '内容を確認する →', exact: true}).click();
  await expect(page.getByRole('heading', {name: '2件の記録を確認'})).toBeVisible();
  await page.locator('#row-1-tableName').fill('ブラウザから届いた手紙');
  await page.reload();
  await expect(page.locator('#row-1-tableName')).toHaveValue('ブラウザから届いた手紙');
  await page.getByRole('button', {name: '2件をブラウザ内に登録', exact: true}).click();
  await expect(page.getByRole('heading', {name: '2件の記録が加わりました'})).toBeVisible();
  await page.getByRole('link', {name: '回廊を見る →', exact: true}).click();
  await expect(page.locator('.gallery-item')).toHaveCount(20);
  await page.getByRole('link', {name: /夜明けの郵便局/}).click();
  await expect(page.getByRole('heading', {name: 'ブラウザから届いた手紙'})).toBeVisible();
});

test('CSV invalid values cannot register, while quoted commas and newlines survive', async ({page}) => {
  await page.goto('./#/import');
  await page.getByLabel('CSVを貼り付け', {exact: true}).fill('シナリオ名,ゲームシステム,卓名,開催日,KP,PL,PC\n"夜明け,の物語",,"二夜に\nわたる卓",2026-02-30,白凪,,探索者');
  await page.getByRole('button', {name: '内容を確認する →', exact: true}).click();
  await expect(page.locator('#row-0-scenarioName')).toHaveValue('夜明け,の物語');
  await expect(page.getByRole('button', {name: '1件をブラウザ内に登録', exact: true})).toBeDisabled();
  await page.locator('#row-0-playedOn').fill('2026-02-28');
  await expect(page.getByText('PCを登録する場合はPL名も入力してください。', {exact: true})).toBeVisible();
  await page.locator('#row-0-pl').fill('体験PL');
  await page.getByRole('button', {name: '1件をブラウザ内に登録', exact: true}).click();
  await expect(page.getByRole('heading', {name: '1件の記録が加わりました'})).toBeVisible();
});

test('storage denial still permits a memory-only trial', async ({page}) => {
  await page.addInitScript(() => {
    Storage.prototype.setItem = () => { throw new DOMException('Blocked', 'SecurityError'); };
    Storage.prototype.getItem = () => { throw new DOMException('Blocked', 'SecurityError'); };
    Storage.prototype.removeItem = () => { throw new DOMException('Blocked', 'SecurityError'); };
  });
  await page.goto('./');
  await expect(page.getByText('このブラウザでは保存領域を利用できないため、変更はページを閉じるまで保持します。', {exact: true})).toBeVisible();
  const favorite = page.getByRole('button', {name: '硝子温室の残響のお気に入り', exact: true});
  await favorite.click();
  await expect(favorite).toHaveAttribute('aria-pressed', 'false');
  await page.getByRole('link', {name: 'PL / PC', exact: true}).click();
  await expect(page.getByRole('heading', {name: 'PL / PC', exact: true})).toBeVisible();
  await page.getByLabel('Mobileの列数', {exact: true}).selectOption('3');
  await page.getByRole('link', {name: 'HOME', exact: true}).click();
  await expect(favorite).toHaveAttribute('aria-pressed', 'false');
});

test('reset during editing discards the draft without another navigation prompt', async ({page}) => {
  await page.goto('./#/scenarios/new');
  await page.getByLabel('シナリオ名', {exact: true}).fill('破棄する入力');
  await page.getByRole('button', {name: 'サンプルに戻す', exact: true}).click();
  await page.getByRole('dialog', {name: '確認', exact: true}).getByRole('button', {name: '続ける'}).click();
  await expect(page.getByRole('heading', {name: 'あなたの回廊', exact: true})).toBeVisible();
  await expect(page.locator('.gallery-item')).toHaveCount(19);
  await expect(page.getByRole('dialog')).toHaveCount(0);
});

test('storage quota failure keeps the input and never reports a successful save', async ({page}) => {
  await page.goto('./#/scenarios/new');
  await expect(page.getByRole('heading', {name: 'シナリオを登録', exact: true})).toBeVisible();
  await page.evaluate(() => {
    const original = Storage.prototype.setItem;
    Storage.prototype.setItem = function(key, value) {
      if (key === 'takukairo-demo-v1') throw new DOMException('Full', 'QuotaExceededError');
      original.call(this, key, value);
    };
  });
  await page.getByLabel('シナリオ名', {exact: true}).fill('保存容量を超えた記録');
  await page.getByRole('button', {name: '保存', exact: true}).click();
  await expect(page.getByRole('alert')).toContainText('ブラウザの保存容量を超えたか');
  await expect(page.getByLabel('シナリオ名', {exact: true})).toHaveValue('保存容量を超えた記録');
  expect(await page.evaluate(() => localStorage.getItem('takukairo-demo-v1'))).toBeNull();
});

test('each visitor gets an independent gallery', async ({page, browser}) => {
  await page.goto('./');
  const favorite = page.getByRole('button', {name: '硝子温室の残響のお気に入り', exact: true});
  await favorite.click();
  await expect(favorite).toHaveAttribute('aria-pressed', 'false');
  const visitor = await browser.newContext();
  try {
    const second = await visitor.newPage();
    await second.goto(page.url());
    await expect(second.getByRole('button', {name: '硝子温室の残響のお気に入り', exact: true})).toHaveAttribute('aria-pressed', 'true');
  } finally { await visitor.close(); }
});

for (const width of [375, 768]) {
  test(`gallery, import and table remain usable at ${width}px`, async ({page}) => {
    await page.setViewportSize({width, height: 812});
    const errors: string[] = [];
    page.on('pageerror', e => errors.push(e.message));
    for (const hash of ['/', '/pcs', '/import', '/tables/1']) {
      await page.goto('./#' + hash);
      await expect(page.getByRole('button', {name: 'サンプルに戻す', exact: true})).toBeVisible();
      await expect.poll(() => page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
      if (hash === '/tables/1') await expect(page.getByRole('heading', {name: '鴉羽 律', exact: true})).toBeVisible();
      if (width === 375 && (hash === '/' || hash === '/tables/1')) await page.screenshot({path: `../.runtime/demo-${hash === '/' ? 'home' : 'table'}-mobile.png`, fullPage: true});
    }
    expect(errors).toEqual([]);
  });
}
