import {test, expect} from '@playwright/test';

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
