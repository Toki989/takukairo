import {ApiError} from '../api/client';
import type {Pc, Person, Profile, Scenario, TableDto} from '../api/types';
import {readStore, writeStore, resetDemo, type DemoData} from './store';
import profileData from './profiles.json';
import type {ImportRow} from './csv';
import {demoBoothPreview} from './booth';

const profiles = profileData as Profile[];
type Body = Record<string, any>;
const normalize = (text: unknown) => String(text ?? '').normalize('NFKC').toLocaleLowerCase('ja');
const nullable = (text: unknown) => typeof text === 'string' && text.trim() ? text.trim() : null;
function fail(status: number, message: string, path?: string): never {
  throw new ApiError(status, {code: status === 409 ? 'DEMO_CONFLICT' : 'DEMO_VALIDATION', message, fieldErrors: path ? [{path, code: 'INVALID', message}] : []});
}
function required(value: unknown, path: string, label: string) {
  const text = nullable(value);
  if (!text) fail(422, label + 'を入力してください。', path);
  if (text.length > 500) fail(422, label + 'は500文字以内で入力してください。', path);
  return text;
}
function find<T extends {id: number}>(items: T[], id: number): T {
  return items.find(item => item.id === id) ?? fail(404, '対象が見つかりません。サンプルを初期化した場合は、ホームから開き直してください。');
}
function version(item: {version: number}, expected: unknown) {
  if (expected !== undefined && Number(expected) !== item.version) fail(409, '別の画面で変更されています。最新の内容を読み込んでください。');
}
function packScenario(db: DemoData, s: Scenario): Scenario {
  return {...s, tableCount: db.tables.filter(t => t.scenarioId === s.id).length};
}
function packPc(db: DemoData, pc: Pc): Pc { return {...pc, person: find(db.persons, pc.personId)}; }
function packTable(db: DemoData, t: TableDto): TableDto {
  return {...t, scenario: packScenario(db, find(db.scenarios, t.scenarioId)), participations: t.participations.map(p => ({...p, person: p.personId ? find(db.persons, p.personId) : undefined, pc: p.pcId ? packPc(db, find(db.pcs, p.pcId)) : null}))};
}
function appearances(db: DemoData, id: number) {
  return db.tables.flatMap(t => t.participations.filter(p => p.pcId === id).map(p => ({table: packTable(db, t), participationId: p.id, person: find(db.persons, p.personId!)})));
}
function resolve(input: unknown) {
  const text = normalize(input);
  const exact = profiles.find(p => normalize(p.displayName) === text || normalize(p.canonicalSystemKey) === text || normalize(p.profileKey) === text);
  const candidates = exact ? [exact] : profiles.filter(p => text && (normalize(p.displayName).includes(text) || (text.includes('クトゥルフ') || text.includes('coc')) && p.canonicalSystemKey.startsWith('coc') || text.includes('エモクロア') && p.canonicalSystemKey === 'emoklore'));
  return {state: exact ? 'EXACT' : candidates.length ? 'SUGGESTED' : 'UNKNOWN', input, canonicalSystemKey: exact?.canonicalSystemKey ?? null, profileKey: exact?.profileKey ?? null, displayName: exact?.displayName ?? null, candidates: candidates.map(p => ({canonicalSystemKey: p.canonicalSystemKey, profileKey: p.profileKey, displayName: p.displayName}))};
}
function validateUrl(value: unknown, path: string) {
  const text = nullable(value);
  if (text) {
    try { const url = new URL(text); if (text.length > 2048 || !['http:', 'https:'].includes(url.protocol) || url.username || url.password) throw new Error(); }
    catch { fail(422, 'http または https のURLを入力してください。', path); }
  }
  return text;
}
function externalImage(db: DemoData, body: Body, previous: string | null = null) {
  const url = validateUrl(body.externalImageUrl, '/externalImageUrl');
  if (url && url !== previous && !db.externalImageConsentGiven) {
    if (body.externalImageConsentAccepted !== true) fail(422, '外部画像の利用について確認してください。', '/externalImageConsentAccepted');
    db.externalImageConsentGiven = true;
  }
  return url;
}
function tablePayload(db: DemoData, body: Body, scenarioId: number, existing?: TableDto): TableDto {
  const scenario = find(db.scenarios, scenarioId);
  const participations = (body.participations ?? []) as TableDto['participations'];
  if (participations.filter(p => p.role === 'PL').length > 8) fail(422, 'PLは8人まで登録できます。');
  const parts = participations.map((p, i) => {
    if (!p.personId || !db.persons.some(person => person.id === p.personId)) fail(422, '参加する人物を選んでください。', `/participations/${i}/personId`);
    if (!['PL', 'KP'].includes(p.role)) fail(422, '参加区分を選んでください。');
    if (p.pcId) find(db.pcs, p.pcId);
    const quote = nullable(p.displayQuote);
    if (quote && [...new Intl.Segmenter('ja', {granularity: 'grapheme'}).segment(quote)].length > 24) fail(422, '飾り台詞は24文字以内で入力してください。', `/participations/${i}/displayQuote`);
    return {...p, id: p.id ?? db.nextId++, version: (p.version ?? -1) + 1,
      pcId: p.role === 'KP' ? null : p.pcId, ho: p.role === 'KP' ? null : nullable(p.ho), displayQuote: p.role === 'KP' ? null : quote,
      spotlightType: p.spotlightType ?? (p.ho ? 'HO' as const : quote ? 'QUOTE' as const : null),
      endPcState: p.role === 'PL' && p.pcId && p.endPcState ? {...p.endPcState, id: p.endPcState.id ?? db.nextId++} : null};
  });
  const tableDates = ((body.tableDates ?? []) as TableDto['tableDates']).map((d, i) => {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(d.playedOn) || !Number.isFinite(Date.parse(d.playedOn)) || new Date(d.playedOn).toISOString().slice(0, 10) !== d.playedOn) fail(422, '有効な開催日を入力してください。', `/tableDates/${i}/playedOn`);
    return {...d, id: d.id ?? db.nextId++};
  });
  return {id: existing?.id ?? db.nextId++, version: existing ? existing.version + 1 : 0, scenarioId, scenario,
    tableName: nullable(body.tableName), recordingUrl: validateUrl(body.recordingUrl, '/recordingUrl'), createdAt: existing?.createdAt ?? new Date().toISOString(), tableDates, participations: parts};
}

async function request(path: string, options: RequestInit): Promise<unknown> {
  if (options.signal?.aborted) throw new DOMException('Aborted', 'AbortError');
  const url = new URL(path, 'https://demo.invalid');
  const route = url.pathname.split('/').filter(Boolean), query = url.searchParams;
  const method = options.method ?? 'GET';
  const body: Body = typeof options.body === 'string' ? JSON.parse(options.body) : {};
  const db = readStore();
  const save = (result: unknown) => {
    try { writeStore(db); }
    catch { fail(422, 'ブラウザの保存容量を超えたか、保存が制限されています。画像を小さくするか、不要な体験データを削除して再度お試しください。'); }
    return structuredClone(result);
  };
  if (path === '/csrf') return {token: 'browser-demo', headerName: 'X-DEMO', parameterName: '_csrf'};
  if (path === '/session') return {authenticated: true, user: {id: 1, selfPerson: find(db.persons, db.selfPersonId), externalImageConsentGiven: !!db.externalImageConsentGiven}};
  if (path === '/demo/reset' && method === 'POST') { resetDemo(); return {}; }
  if (path === '/activity/summary') return {scenarioCount: db.scenarios.length, tableCount: db.tables.length,
    plParticipationCount: db.tables.flatMap(t => t.participations).filter(p => p.personId === db.selfPersonId && p.role === 'PL').length,
    kpParticipationCount: db.tables.flatMap(t => t.participations).filter(p => p.personId === db.selfPersonId && p.role === 'KP').length};
  if (route[0] === 'game-system-profiles') return route[1] ? profiles.find(p => p.profileKey === route[1]) ?? fail(404, '設定形式が見つかりません。') : profiles.filter(p => p.active);
  if (path === '/game-systems/resolve') return resolve(body.gameSystem);
  if (route[0] === 'images') {
    const source = db.images[`${route[1]}/${route[2]}`];
    if (!source) fail(404, '画像がありません。');
    return {url: source.startsWith('data:') ? source : import.meta.env.BASE_URL + source};
  }
  if (route[0] === 'persons') {
    if (!route[1]) {
      if (method === 'GET') return db.persons.filter(p => normalize(p.displayName).includes(normalize(query.get('search'))));
      const person: Person = {id: db.nextId++, version: 0, displayName: required(body.displayName, '/displayName', '表示名')};
      db.persons.push(person); return save(person);
    }
    const person = find(db.persons, Number(route[1]));
    const impact = {relatedPcCount: db.pcs.filter(p => p.personId === person.id).length, relatedTableCount: db.tables.filter(t => t.participations.some(p => p.personId === person.id)).length};
    if (route[2] === 'impact') return impact;
    if (method === 'DELETE') {
      if (person.id === db.selfPersonId || impact.relatedPcCount || impact.relatedTableCount) fail(422, '参照されている人物は削除できません。');
      db.persons = db.persons.filter(p => p.id !== person.id); return save(undefined);
    }
    if (method === 'PATCH') { version(person, body.version); person.displayName = required(body.displayName, '/displayName', '表示名'); person.version++; return save(person); }
    return person;
  }
  if (route[0] === 'scenarios' && route[1] === 'duplicate-candidates') return db.scenarios.filter(s => normalize(s.name) === normalize(body.name)).map(s => packScenario(db, s));
  if (['scenarios', 'pcs'].includes(route[0])) {
    const kind = route[0] as 'scenarios' | 'pcs';
    const items = db[kind] as (Pc | Scenario)[];
    const pack = (item: Pc | Scenario) => kind === 'pcs' ? packPc(db, item as Pc) : packScenario(db, item as Scenario);
    if (!route[1]) {
      if (method === 'GET') {
        const search = normalize(query.get('search'));
        let result = items.filter(item => {
          if (kind === 'pcs') { const pc = item as Pc; return (!query.get('personId') || pc.personId === Number(query.get('personId'))) && normalize(pc.name + ' ' + find(db.persons, pc.personId).displayName).includes(search); }
          const s = item as Scenario, tables = db.tables.filter(t => t.scenarioId === s.id);
          const roles = query.getAll('participation');
          const related = tables.flatMap(t => t.participations).map(p => (p.personId ? find(db.persons, p.personId).displayName : '') + ' ' + (p.pcId ? find(db.pcs, p.pcId).name : '')).join(' ');
          return (query.get('favorite') !== 'true' || s.favorite) && (!roles.length || tables.some(t => t.participations.some(p => p.personId === db.selfPersonId && roles.includes(p.role)))) && normalize(s.name + ' ' + (s.authorName ?? '') + ' ' + related).includes(search);
        });
        const latest = (item: Pc | Scenario) => db.tables.filter(t => kind === 'pcs' ? t.participations.some(p => p.pcId === item.id) : t.scenarioId === item.id).flatMap(t => t.tableDates).map(d => d.playedOn).sort().at(-1) ?? '';
        const sort = query.get('sort') ?? (kind === 'pcs' ? 'CREATED_DESC' : 'RECENT_PLAYED');
        result = result.sort((a, b) => sort === 'NAME' ? a.name.localeCompare(b.name, 'ja') : ['RECENT', 'RECENT_PLAYED'].includes(sort) ? latest(b).localeCompare(latest(a)) || b.createdAt.localeCompare(a.createdAt) : (sort === 'CREATED_ASC' ? 1 : -1) * (a.createdAt.localeCompare(b.createdAt) || a.id - b.id));
        return result.map(pack);
      }
      const name = required(body.name, '/name', kind === 'pcs' ? 'PC名' : 'シナリオ名');
      if (kind === 'scenarios') {
        const scenario: Scenario = {id: db.nextId++, version: 0, name, authorName: nullable(body.authorName), sourceUrl: validateUrl(body.sourceUrl, '/sourceUrl'), gameSystem: nullable(body.gameSystem), externalImageUrl: externalImage(db, body), image: null, favorite: false, tableCount: 0, createdAt: new Date().toISOString()};
        db.scenarios.push(scenario); return save(scenario);
      }
      if (!body.personId) fail(422, '現在PLを選んでください。', '/personId');
      const person = find(db.persons, Number(body.personId));
      const pc: Pc = {id: db.nextId++, version: 0, name, personId: person.id, person, characterSheetUrl: validateUrl(body.characterSheetUrl, '/characterSheetUrl'), systemSettings: body.systemSettings ?? [], image: null, createdAt: new Date().toISOString()};
      db.pcs.push(pc); return save(pc);
    }
    const item = find(items, Number(route[1]));
    if (route[2] === 'favorite') { (item as Scenario).favorite = method !== 'DELETE'; return save({}); }
    if (route[2] === 'tables') {
      if (method === 'GET') return db.tables.filter(t => t.scenarioId === item.id).map(t => packTable(db, t));
      const table = tablePayload(db, body, item.id); db.tables.push(table); return save(packTable(db, table));
    }
    if (route[2] === 'appearances') return appearances(db, item.id);
    if (route[2] === 'previous-end-state-candidate') {
      const candidates = appearances(db, item.id).filter(a => a.table.id !== body.targetTableId).sort((a, b) => (b.table.tableDates.at(-1)?.playedOn ?? '').localeCompare(a.table.tableDates.at(-1)?.playedOn ?? ''));
      const previous = candidates.find(a => a.table.participations.find(p => p.id === a.participationId)?.endPcState);
      return {candidate: previous ? {statusValues: previous.table.participations.find(p => p.id === previous.participationId)!.endPcState!.statusValues, sourceTableId: previous.table.id, basis: {confidence: 'LOW'}} : null};
    }
    if (route[2] === 'change-person-context') return {pc: packPc(db, item as Pc), participations: appearances(db, item.id).map(a => ({participationId: a.participationId, participationVersion: a.table.participations.find(p => p.id === a.participationId)!.version, tableId: a.table.id, tableVersion: a.table.version, person: a.person, scenario: a.table.scenario, tableName: a.table.tableName}))};
    if (route[2] === 'change-person') {
      const pc = item as Pc; version(pc, body.pcVersion); pc.personId = find(db.persons, Number(body.newPersonId)).id; pc.version++;
      for (const update of body.participationUpdates ?? []) {
        const table = find(db.tables, update.tableId); version(table, update.tableVersion);
        const part = find(table.participations as (TableDto['participations'][number] & {id: number})[], update.participationId);
        version({version: part.version ?? 0}, update.participationVersion); part.personId = pc.personId; part.version = (part.version ?? 0) + 1; table.version++;
      }
      return save(packPc(db, pc));
    }
    if (route[2] === 'image' || route[2] === 'image-transform') {
      const form = options.body instanceof FormData ? options.body : null;
      version(item, form?.get('expectedVersion') ?? body.expectedVersion ?? query.get('expectedVersion') ?? undefined);
      if (method === 'DELETE') { item.image = null; delete db.images[kind + '/' + item.id]; }
      else {
        if (form) {
          const file = form.get('file');
          if (!(file instanceof File) || !['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) fail(422, 'PNG・JPEG・WebP画像を選んでください。', '/file');
          if (file.size > 1_500_000) fail(422, '体験版では1.5MB以下の画像を選んでください。', '/file');
          db.images[kind + '/' + item.id] = await new Promise<string>((done, reject) => { const reader = new FileReader(); reader.onload = () => done(String(reader.result)); reader.onerror = reject; reader.readAsDataURL(file); });
        }
        item.image = {present: true, positionX: Number(form?.get('positionX') ?? body.positionX ?? 0), positionY: Number(form?.get('positionY') ?? body.positionY ?? 0), zoom: Number(form?.get('zoom') ?? body.zoom ?? 1)};
      }
      item.version++; return save({version: item.version});
    }
    if (method === 'DELETE') {
      if (kind === 'scenarios' ? db.tables.some(t => t.scenarioId === item.id) : appearances(db, item.id).length > 0) fail(422, '卓で参照されているため削除できません。');
      if (kind === 'pcs') db.pcs = db.pcs.filter(p => p.id !== item.id); else db.scenarios = db.scenarios.filter(s => s.id !== item.id);
      delete db.images[kind + '/' + item.id]; return save(undefined);
    }
    if (method === 'PATCH') {
      version(item, body.version); item.name = required(body.name, '/name', kind === 'pcs' ? 'PC名' : 'シナリオ名');
      if (kind === 'pcs') { const pc = item as Pc; pc.characterSheetUrl = validateUrl(body.characterSheetUrl, '/characterSheetUrl'); pc.systemSettings = body.systemSettings ?? []; }
      else { const s = item as Scenario; s.authorName = nullable(body.authorName); s.gameSystem = nullable(body.gameSystem); s.sourceUrl = validateUrl(body.sourceUrl, '/sourceUrl'); if (Object.hasOwn(body, 'externalImageUrl')) s.externalImageUrl = externalImage(db, body, s.externalImageUrl); }
      item.version++; return save(pack(item));
    }
    return pack(item);
  }
  if (route[0] === 'tables') {
    const table = find(db.tables, Number(route[1]));
    if (method === 'DELETE') { db.tables = db.tables.filter(t => t.id !== table.id); return save(undefined); }
    if (method === 'PUT') { version(table, body.version); const updated = tablePayload(db, body, Number(body.scenarioId), table); db.tables = db.tables.map(t => t.id === table.id ? updated : t); return save(packTable(db, updated)); }
    return packTable(db, table);
  }
  if (path === '/ccfolia/character-preview') {
    let data: Body;
    try { const parsed = JSON.parse(body.rawText); data = parsed.data ?? parsed; if (!data || typeof data !== 'object') throw new Error(); }
    catch { fail(422, '有効な駒データJSONを入力してください。', '/rawText'); }
    const statuses: Record<string, number> = {};
    for (const s of Array.isArray(data.status) ? data.status : []) { const key = String(s.label).toLowerCase(); if (['san', 'hp', 'mp'].includes(key) && Number.isFinite(Number(s.value))) statuses[key] = Number(s.value); }
    const key = body.manualProfileKey ?? (body.scenarioId ? resolve(find(db.scenarios, body.scenarioId).gameSystem).profileKey : statuses.san !== undefined ? 'coc_7e_v1' : null);
    const profile = profiles.find(p => p.profileKey === key);
    return {targetSystem: null, detectedProfile: profile ? {profileKey: profile.profileKey} : null, previewProfile: {profileKey: profile?.profileKey ?? null, canonicalSystemKey: profile?.canonicalSystemKey ?? null, source: 'DEMO'},
      pcCandidate: {nameCandidate: nullable(data.name), systemFields: {}, characterSheetUrl: nullable(data.externalUrl), iconUrl: null}, endPcStateCandidate: {statusValues: statuses, applicable: !!profile},
      warnings: [{code: 'DEMO_PREVIEW', path: null, message: '体験版ではPC名・シートURLとSAN / HP / MPを読み取ります。設定形式は必要に応じて指定してください。'}]};
  }
  if (path === '/demo/import' && method === 'POST') {
    if (!Array.isArray(body.rows) || !body.rows.length || body.rows.length > 100) fail(422, '1〜100件の行を指定してください。');
    const results: number[] = [];
    for (const row of body.rows as ImportRow[]) {
      const name = required(row.scenarioName, '/scenarioName', 'シナリオ名');
      let scenario = db.scenarios.find(s => normalize(s.name) === normalize(name) && normalize(s.gameSystem) === normalize(row.gameSystem));
      if (!scenario) {
        scenario = {id: db.nextId++, version: 0, name, gameSystem: nullable(row.gameSystem), authorName: null, sourceUrl: null, externalImageUrl: null, favorite: false, tableCount: 0, image: null, createdAt: new Date().toISOString()}; db.scenarios.push(scenario);
      }
      const person = (name: string) => {
        let p = db.persons.find(p => normalize(p.displayName) === normalize(name));
        if (!p) { p = {id: db.nextId++, version: 0, displayName: required(name, '/personName', '人物名')}; db.persons.push(p); } return p;
      };
      const parts: Body[] = [];
      if (row.kp.trim()) parts.push({personId: person(row.kp.trim()).id, pcId: null, role: 'KP', displayOrder: 1});
      if (row.pl.trim()) {
        const pl = person(row.pl.trim()); let pc = row.pc.trim() ? db.pcs.find(p => normalize(p.name) === normalize(row.pc) && p.personId === pl.id) : undefined;
        if (!pc && row.pc.trim()) { pc = {id: db.nextId++, version: 0, name: row.pc.trim(), personId: pl.id, person: pl, characterSheetUrl: null, systemSettings: [], image: null, createdAt: new Date().toISOString()}; db.pcs.push(pc); }
        parts.push({personId: pl.id, pcId: pc?.id ?? null, role: 'PL', displayOrder: 1});
      } else if (row.pc.trim()) fail(422, 'PCを登録する行にはPL名も入力してください。');
      const table = tablePayload(db, {tableName: row.tableName, tableDates: row.playedOn ? [{playedOn: row.playedOn}] : [], participations: parts}, scenario.id);
      db.tables.push(table); results.push(table.id);
    }
    return save({tableIds: results});
  }
  fail(422, 'この操作はブラウザ体験版では利用できません。外部サービスへの接続や本体の推論処理は行いません。');
}

// Serialize local read/modify/write operations, including image reads.
let pending: Promise<unknown> = Promise.resolve();
export function demoRequest<T>(path: string, options: RequestInit): Promise<T> {
  // Metadata retrieval never holds the local data mutation queue while waiting for the network.
  if (path === '/booth/preview' && options.method === 'POST') {
    const body = typeof options.body === 'string' ? JSON.parse(options.body) : {};
    return demoBoothPreview(body.url, options.signal ?? undefined) as Promise<T>;
  }
  const next = pending.then(() => request(path, options));
  pending = next.catch(() => undefined);
  return next.then(value => structuredClone(value) as T);
}
