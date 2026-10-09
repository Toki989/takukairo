import {chromium,expect} from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath,pathToFileURL} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const output=path.join(root,'.runtime/qa');
fs.mkdirSync(output,{recursive:true});
const browser=await chromium.launch();
try {
  const context=await browser.newContext();
  const errors=[];
  context.on('page',page=>page.on('pageerror',error=>errors.push(error.message)));
  const page=await context.newPage();
  await page.goto(pathToFileURL(path.join(root,'index.html')).href);
  await expect(page.getByRole('heading',{level:1})).toHaveText('あなたの回廊を、ローカルで確認する。');
  for(const [width,height] of [[1440,900],[390,844]]){
    await page.setViewportSize({width,height});
    expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
    await page.screenshot({path:path.join(output,`local-entry-${width}x${height}.png`),fullPage:true});
  }
  const open=page.getByRole('link',{name:'卓回廊を開く',exact:false});
  await open.focus();
  await expect(open).toBeFocused();
  const opening=page.waitForEvent('popup');
  await page.keyboard.press('Enter');
  const app=await opening;
  await expect(app.getByRole('button',{name:'固定テストユーザーでログイン'})).toBeVisible();
  await app.getByRole('button',{name:'固定テストユーザーでログイン'}).click();
  await expect(app.getByRole('heading',{name:'あなたの回廊'})).toBeVisible();
  const importing=page.waitForEvent('popup');
  await page.getByRole('link',{name:'Import',exact:true}).click();
  const importer=await importing;
  await expect(importer).toHaveURL('http://localhost:5173/import');
  await expect(importer.getByRole('button',{name:/^(続きから再開|インポートを始める)$/})).toBeVisible();
  expect(errors).toEqual([]);
  console.log('PASS: file:// index.html, Desktop/Mobile, Keyboard, real Dev Login, Import entry. Browser errors: 0.');
}finally {
  await browser.close();
}
