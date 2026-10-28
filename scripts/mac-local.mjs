// macOS launcher. Node handles paths/arguments without shell interpolation.
import fs from 'node:fs';
import path from 'node:path';
import net from 'node:net';
import {spawn, execFileSync} from 'node:child_process';
import {createHash, randomUUID} from 'node:crypto';
import {parseEnv} from 'node:util';
import {fileURLToPath} from 'node:url';
import {createInterface} from 'node:readline';

const workspace = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

export function readEnvironment(root, inherited = process.env) {
  const filename = path.join(root, '.env');
  const values = fs.existsSync(filename) ? parseEnv(fs.readFileSync(filename, 'utf8')) : {};
  return {...values, ...inherited, MAVEN_OPTS: `${inherited.MAVEN_OPTS ?? ''} -Dfile.encoding=UTF-8`.trim()};
}

export function dependencyHash(root) {
  const hash = createHash('sha256').update(`${process.platform}:${process.arch}\0`);
  for (const name of ['package.json', 'package-lock.json']) hash.update(fs.readFileSync(path.join(root, 'frontend', name)));
  return hash.digest('hex');
}

export function processIdentity(pid) {
  if (!Number.isSafeInteger(pid) || pid <= 1) return null;
  try {
    const start = execFileSync('ps', ['-p', String(pid), '-o', 'lstart='], {encoding: 'utf8'}).trim();
    const command = execFileSync('ps', ['-ww', '-p', String(pid), '-o', 'command='], {encoding: 'utf8'}).trim();
    return start && command ? {start, command} : null;
  } catch { return null; }
}

export function ownsProcess(record, expected, inspect = processIdentity) {
  if (!record || record.argument !== expected) return false;
  const actual = inspect(record.pid);
  return !!actual && actual.start === record.start && actual.command.includes(expected);
}

export function acquireLock(filename) {
  const token = randomUUID();
  try {
    const fd = fs.openSync(filename, 'wx', 0o600);
    try { fs.writeFileSync(fd, JSON.stringify({pid: process.pid, token})); } finally { fs.closeSync(fd); }
    return () => {
      if (JSON.parse(fs.readFileSync(filename, 'utf8')).token === token) fs.unlinkSync(filename);
    };
  } catch (error) {
    if (error.code !== 'EEXIST') throw error;
    // Never unlink an apparently stale lock here: two concurrent launchers could
    // otherwise remove each other's new lock during recovery.
    throw new Error('別の起動・停止処理が進行中です。前回の強制終了後も表示される場合はdocs/MAC.mdのロック復旧手順を確認してください。');
  }
}

export async function assertPortFree(port) {
  // Check both loopback families: localhost can resolve to either on macOS.
  for (const host of ['127.0.0.1', '::1']) {
    await new Promise((resolve, reject) => {
      const server = net.createServer();
      server.once('error', error => {
        if (['EAFNOSUPPORT', 'EADDRNOTAVAIL'].includes(error.code)) resolve();
        else reject(new Error(`${port}番ポートが使用中です。既存のアプリを確認してください。`));
      });
      server.listen({host, port, exclusive: true, ipv6Only: true}, () => server.close(resolve));
    });
  }
}

export function createLauncher(root = workspace, options = {}) {
  const runtime = path.join(root, '.runtime');
  const frontend = path.join(root, 'frontend');
  const stateFile = path.join(runtime, 'mac-processes.json');
  const jar = path.join(root, 'backend/target/takukairo-0.1.0.jar');
  const vite = path.join(frontend, 'node_modules/vite/bin/vite.js');
  let env = readEnvironment(root);
  let activeCommand = null;
  let interrupted = false;
  const inspect = options.inspect ?? processIdentity;
  const log = options.log ?? console.log;

  const run = (program, args, cwd = root, capture = false) => new Promise((resolve, reject) => {
    if (interrupted) return reject(new Error('処理を中断しました。'));
    const child = spawn(program, args, {cwd, env, stdio: capture ? ['ignore', 'pipe', 'inherit'] : 'inherit'});
    activeCommand = child;
    let output = '';
    if (capture) child.stdout.on('data', chunk => { output += chunk; });
    child.once('error', error => { activeCommand = null; reject(new Error(`${program}を実行できません: ${error.message}`)); });
    child.once('exit', code => {
      activeCommand = null;
      if (code === 0 && !interrupted) resolve(output.trim());
      else reject(new Error(`${program}の処理が完了しませんでした。ログを確認してください。`));
    });
  });

  function ensureEnvironment() {
    fs.mkdirSync(runtime, {recursive: true});
    try { fs.copyFileSync(path.join(root, '.env.example'), path.join(root, '.env'), fs.constants.COPYFILE_EXCL); }
    catch (error) { if (error.code !== 'EEXIST') throw error; }
    env = readEnvironment(root);
  }

  function readState() {
    if (!fs.existsSync(stateFile)) return null;
    const state = JSON.parse(fs.readFileSync(stateFile, 'utf8'));
    if (state.workspace !== root) throw new Error('保存された起動情報のフォルダーが異なります。docs/MAC.mdを確認してください。');
    return state;
  }

  function writeState(state) {
    const temp = `${stateFile}.tmp`;
    fs.writeFileSync(temp, JSON.stringify(state, null, 2), {mode: 0o600});
    fs.renameSync(temp, stateFile);
  }

  async function javaCommand() {
    if (!env.JAVA_HOME && process.platform === 'darwin') {
      env.JAVA_HOME = await run('/usr/libexec/java_home', ['-v', '25'], root, true);
    }
    const java = env.JAVA_HOME ? path.join(env.JAVA_HOME, 'bin/java') : 'java';
    const version = await run(java, ['--version'], root, true);
    if (!/^(?:openjdk|java) 25[.\s]/m.test(version)) throw new Error('JDK 25が必要です。JAVA_HOMEとdocs/MAC.mdを確認してください。');
    return java;
  }

  async function dependencies() {
    const stamp = path.join(runtime, 'mac-frontend-dependencies.sha256');
    const hash = dependencyHash(root);
    const entries = ['vite/bin/vite.js', 'typescript/bin/tsc', 'react/index.js', 'react-dom/client.js'];
    const ready = fs.existsSync(stamp) && fs.readFileSync(stamp, 'utf8').trim() === hash
      && entries.every(name => fs.existsSync(path.join(frontend, 'node_modules', name)));
    if (ready) { log('フロントエンドの依存パッケージを再利用します。'); return; }
    await run('npm', ['ci'], frontend);
    fs.writeFileSync(stamp, hash);
  }

  async function launch(program, args, cwd, name, argument) {
    const out = fs.openSync(path.join(runtime, `${name}.log`), 'w');
    const err = fs.openSync(path.join(runtime, `${name}-error.log`), 'w');
    let child;
    try { child = spawn(program, args, {cwd, env, detached: true, stdio: ['ignore', out, err]}); }
    finally { fs.closeSync(out); fs.closeSync(err); }
    await new Promise((resolve, reject) => { child.once('spawn', resolve); child.once('error', reject); });
    child.unref();
    const identity = inspect(child.pid);
    if (!identity) {
      child.kill('SIGTERM');
      throw new Error(`${name}の起動を確認できません。.runtime/${name}-error.logを確認してください。`);
    }
    return {pid: child.pid, start: identity.start, argument};
  }

  async function terminate(record, expected) {
    if (!record || !inspect(record.pid)) return;
    if (!ownsProcess(record, expected, inspect)) throw new Error(`記録されたPID ${record.pid}は今回のアプリと一致しないため停止しませんでした。`);
    process.kill(record.pid, 'SIGTERM');
    for (let attempt = 0; attempt < 40; attempt++) {
      await delay(250);
      if (!ownsProcess(record, expected, inspect)) return;
    }
    if (ownsProcess(record, expected, inspect)) process.kill(record.pid, 'SIGKILL');
  }

  async function stopState(state) {
    const errors = [];
    for (const [name, argument] of [['frontend', vite], ['backend', jar]]) {
      try { await terminate(state[name], argument); state[name] = null; }
      catch (error) { errors.push(error.message); }
    }
    // Do not stop the database while an application process could still be running.
    if (!errors.length && state.databaseStarted) {
      try { await run('docker', ['compose', 'stop', 'db']); state.databaseStarted = false; }
      catch (error) { errors.push(error.message); }
    }
    if (errors.length) { writeState(state); throw new Error(errors.join('\n')); }
    fs.rmSync(stateFile, {force: true});
  }

  async function appReady(state) {
    if (!ownsProcess(state?.backend, jar, inspect) || !ownsProcess(state?.frontend, vite, inspect)) return false;
    try {
      const response = await fetch('http://localhost:5173/api/session', {signal: AbortSignal.timeout(2000)});
      return response.ok && typeof (await response.json()).authenticated === 'boolean';
    } catch { return false; }
  }

  async function waitReady(check) {
    const deadline = Date.now() + 90_000;
    while (Date.now() < deadline) {
      if (interrupted) throw new Error('処理を中断しました。');
      if (await check()) return;
      await delay(500);
    }
    throw new Error('起動確認がタイムアウトしました。.runtime/のログを確認してください。');
  }

  async function openBrowser(url) {
    if (!options.noBrowser && !options.checkOnly) await run(process.platform === 'darwin' ? 'open' : 'xdg-open', [url]);
  }

  async function start() {
    ensureEnvironment();
    const previous = readState();
    if (previous) {
      if (await appReady(previous)) {
        if (previous.qa !== !!options.qa) throw new Error('異なる検査設定で起動中です。先に停止.commandを実行してください。');
        log('起動済みの卓回廊を開きます。');
        await openBrowser('http://localhost:5173/');
        return;
      }
      // Clean up only verified processes from a previous incomplete startup.
      await stopState(previous);
    }
    await assertPortFree(8080);
    await assertPortFree(5173);
    const java = await javaCommand();
    const state = {workspace: root, qa: !!options.qa, databaseStarted: false};
    try {
      if (!options.externalDb) {
        if (env.DB_URL !== 'jdbc:postgresql://localhost:5432/takukairo'
          || env.DB_USER !== 'takukairo' || env.DB_PASSWORD !== 'local-test-only') {
          throw new Error('独自のDB設定を使う場合は --external-db を指定してください。docs/MAC.mdを参照してください。');
        }
        const existing = await run('docker', ['compose', 'ps', '--status', 'running', '-q', 'db'], root, true);
        if (!existing) await assertPortFree(5432);
        // Record ownership before startup, including failed or interrupted compose up.
        state.databaseStarted = !existing;
        writeState(state);
        await run('docker', ['compose', 'up', '-d', '--wait', 'db']);
      }
      writeState(state);
      await run('sh', [path.join(root, 'backend/mvnw'), '-B', '-ntp', '-f', path.join(root, 'backend/pom.xml'),
        `-Dmaven.repo.local=${path.join(root, '.tools/m2')}`, '-DskipTests', 'package']);
      await dependencies();
      state.backend = await launch(java, ['--enable-native-access=ALL-UNNAMED', '-Djava.awt.headless=true', '-jar', jar,
        `--spring.profiles.active=${options.qa ? 'dev,test' : 'dev'}`], path.join(root, 'backend'), 'backend', jar);
      writeState(state);
      state.frontend = await launch(process.execPath, [vite], frontend, 'frontend', vite);
      writeState(state);
      await waitReady(async () => {
        if (!ownsProcess(state.backend, jar, inspect) || !ownsProcess(state.frontend, vite, inspect)) {
          throw new Error('アプリが終了しました。.runtime/backend-error.log、frontend-error.logを確認してください。');
        }
        return appReady(state);
      });
    } catch (error) {
      // Cleanup must still work when Ctrl+C interrupted the foreground build.
      interrupted = false;
      try { await stopState(state); } catch (cleanup) { log(cleanup.message); }
      throw error;
    }
    log('卓回廊: http://localhost:5173/ （固定テストユーザーでログイン）');
    log('停止: 停止.command。記録・画像・Import途中保存は保持します。');
    await openBrowser('http://localhost:5173/');
  }

  async function stop() {
    const state = readState();
    if (state) await stopState(state);
    log('卓回廊を停止しました。保存した記録・画像・途中保存は保持しています。');
  }

  async function demo() {
    await assertPortFree(4175);
    await dependencies();
    await run('npm', ['run', 'build:demo'], frontend);
    let server;
    try {
      server = await launch(process.execPath, [vite, 'preview', '--mode', 'demo', '--host', 'localhost', '--port', '4175', '--strictPort'], frontend, 'demo-preview', vite);
      await waitReady(async () => {
        if (!ownsProcess(server, vite, inspect)) throw new Error('体験版が終了しました。.runtime/demo-preview-error.logを確認してください。');
        try {
          const response = await fetch('http://localhost:4175/', {signal: AbortSignal.timeout(2000)});
          return response.ok && (await response.text()).includes('ブラウザ体験版');
        } catch { return false; }
      });
      log('卓回廊 ブラウザ体験版: http://localhost:4175/');
      await openBrowser('http://localhost:4175/');
      if (!options.checkOnly) {
        log('体験版を終了するには、この画面でEnterまたはCtrl+Cを押してください。');
        await new Promise(resolve => {
          const input = createInterface({input: process.stdin, output: process.stdout});
          const timer = setInterval(() => { if (interrupted) input.close(); }, 100);
          input.once('line', () => input.close());
          input.once('close', () => { clearInterval(timer); resolve(); });
        });
      }
    } finally { await terminate(server, vite); }
  }

  async function execute(command) {
    fs.mkdirSync(runtime, {recursive: true});
    const unlock = acquireLock(path.join(runtime, command === 'demo' ? 'mac-demo.lock' : 'mac-launcher.lock'));
    const onSignal = () => { interrupted = true; activeCommand?.kill('SIGTERM'); };
    process.on('SIGINT', onSignal);
    process.on('SIGTERM', onSignal);
    try {
      if (command === 'start') await start();
      else if (command === 'stop') await stop();
      else if (command === 'demo') await demo();
      else throw new Error('操作は start / stop / demo から指定してください。');
    } finally {
      process.off('SIGINT', onSignal);
      process.off('SIGTERM', onSignal);
      unlock();
    }
  }
  return {execute};
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    if (process.platform === 'win32') throw new Error('Windowsでは 起動.cmd / 停止.cmd / 体験版.cmd を使用してください。');
    if (Number(process.versions.node.split('.')[0]) !== 24) throw new Error('Node.js 24が必要です。docs/MAC.mdを確認してください。');
    const [command = 'start', ...flags] = process.argv.slice(2);
    const allowed = ['--no-browser', '--qa', '--external-db', '--check-only'];
    if (flags.some(flag => !allowed.includes(flag))) throw new Error(`使用できるオプション: ${allowed.join(', ')}`);
    await createLauncher(workspace, {noBrowser: flags.includes('--no-browser'), qa: flags.includes('--qa'),
      externalDb: flags.includes('--external-db'), checkOnly: flags.includes('--check-only')}).execute(command);
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  }
}
