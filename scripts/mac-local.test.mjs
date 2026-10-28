import {test} from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import net from 'node:net';
import {readEnvironment, dependencyHash, ownsProcess, acquireLock, assertPortFree, createLauncher} from './mac-local.mjs';

function fixture(t) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), '卓回廊 Mac test '));
  t.after(() => fs.rmSync(root, {recursive: true, force: true}));
  fs.mkdirSync(path.join(root, '.runtime'));
  return root;
}

test('.env is parsed as data, preserving quoted paths and never executing shell text', t => {
  const root = fixture(t);
  fs.writeFileSync(path.join(root, '.env'), '# comment\r\nSTORAGE_PATH="../画像 storage"\r\nDB_PASSWORD=from-file\r\nPAYLOAD=$(touch never-created)\r\n');
  const env = readEnvironment(root, {DB_PASSWORD: 'inherited'});
  assert.equal(env.STORAGE_PATH, '../画像 storage');
  assert.equal(env.DB_PASSWORD, 'inherited');
  assert.equal(env.PAYLOAD, '$(touch never-created)');
  assert.equal(fs.existsSync(path.join(root, 'never-created')), false);
});

test('dependency cache is invalidated by manifest and lockfile changes', t => {
  const root = fixture(t);
  fs.mkdirSync(path.join(root, 'frontend'));
  for (const name of ['package.json', 'package-lock.json']) fs.writeFileSync(path.join(root, 'frontend', name), '{}');
  const first = dependencyHash(root);
  fs.writeFileSync(path.join(root, 'frontend/package-lock.json'), '{"changed":true}');
  const second = dependencyHash(root);
  assert.notEqual(first, second);
  fs.writeFileSync(path.join(root, 'frontend/package.json'), '{"name":"changed"}');
  assert.notEqual(second, dependencyHash(root));
});

test('process ownership requires matching birth time and absolute workspace argument', () => {
  const expected = '/Users/test/卓回廊 space/backend/target/takukairo-0.1.0.jar';
  const record = {pid: 123, start: 'Mon Oct 26 12:00:00 2026', argument: expected};
  assert.equal(ownsProcess(record, expected, () => ({start: record.start, command: `java -jar ${expected}`})), true);
  assert.equal(ownsProcess(record, expected, () => ({start: 'different', command: `java -jar ${expected}`})), false);
  assert.equal(ownsProcess(record, expected, () => ({start: record.start, command: 'java -jar /another/app.jar'})), false);
  assert.equal(ownsProcess(record, '/another/folder/app.jar', () => ({start: record.start, command: expected})), false);
  assert.equal(ownsProcess(record, expected, () => null), false);
});

test('simultaneous launch is refused and releasing a lock permits the next launch', t => {
  const root = fixture(t);
  const lock = path.join(root, '.runtime/launcher.lock');
  const release = acquireLock(lock);
  try { assert.throws(() => acquireLock(lock), /進行中/); } finally { release(); }
  acquireLock(lock)();
  assert.equal(fs.existsSync(lock), false);
});

test('an incomplete lock is preserved while its owner may be writing it', t => {
  const root = fixture(t);
  const lock = path.join(root, '.runtime/launcher.lock');
  fs.writeFileSync(lock, '');
  assert.throws(() => acquireLock(lock), /進行中/);
  assert.equal(fs.readFileSync(lock, 'utf8'), '');
});

test('stale locks are not automatically removed by competing launchers', t => {
  const root = fixture(t);
  const lock = path.join(root, '.runtime/launcher.lock');
  const content = JSON.stringify({pid: 2147483647, token: 'previous-launch'});
  fs.writeFileSync(lock, content);
  assert.throws(() => acquireLock(lock), /ロック復旧/);
  assert.equal(fs.readFileSync(lock, 'utf8'), content);
});

test('occupied ports are refused without disturbing the listener', async t => {
  const server = net.createServer();
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  t.after(() => new Promise(resolve => server.close(resolve)));
  await assert.rejects(assertPortFree(server.address().port), /使用中/);
  assert.equal(server.listening, true);
});

test('stop refuses a reused PID and preserves database/storage/state for retry', async t => {
  const root = fixture(t);
  const jar = path.join(root, 'backend/target/takukairo-0.1.0.jar');
  const filename = path.join(root, '.runtime/mac-processes.json');
  fs.writeFileSync(filename, JSON.stringify({workspace: root, backend: {pid: process.pid, start: 'old', argument: jar}, databaseStarted: true}));
  fs.writeFileSync(path.join(root, '.runtime/keep-image.png'), 'image');
  const launcher = createLauncher(root, {inspect: () => ({start: 'new', command: 'another process'}), log: () => {}});
  await assert.rejects(launcher.execute('stop'), /一致しない/);
  assert.equal(fs.readFileSync(path.join(root, '.runtime/keep-image.png'), 'utf8'), 'image');
  assert.equal(JSON.parse(fs.readFileSync(filename)).databaseStarted, true);
  assert.equal(fs.existsSync(path.join(root, '.runtime/mac-launcher.lock')), false);
});

test('stop handles exited processes, preserves data, and can run repeatedly', async t => {
  const root = fixture(t);
  const filename = path.join(root, '.runtime/mac-processes.json');
  fs.writeFileSync(filename, JSON.stringify({workspace: root, backend: {pid: 123}, databaseStarted: false}));
  fs.writeFileSync(path.join(root, '.runtime/keep-data'), 'saved');
  const launcher = createLauncher(root, {inspect: () => null, log: () => {}});
  await launcher.execute('stop');
  await launcher.execute('stop');
  assert.equal(fs.existsSync(filename), false);
  assert.equal(fs.readFileSync(path.join(root, '.runtime/keep-data'), 'utf8'), 'saved');
});
