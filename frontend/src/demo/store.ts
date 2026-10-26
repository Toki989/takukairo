import type {Pc, Person, Scenario, TableDto} from '../api/types';

export const DEMO_KEY = 'takukairo-demo-v1';
export const IMPORT_KEY = 'takukairo-demo-import-v1';
export interface DemoData {
  schemaVersion: 1;
  nextId: number;
  selfPersonId: number;
  persons: Person[];
  scenarios: Scenario[];
  pcs: Pc[];
  tables: TableDto[];
  images: Record<string, string>;
}

const titles = [
  '硝子温室の残響', '灯台は夢を見るか', '雪解けの停留所', '天泣の劇場', '星砂の海辺市',
  '夜鴉裁判所', '眠らぬ地下聖堂', '風葬列車', '薄明の花嫁館', '逆さ水族館',
  '月下標本室', '雨宿り駅、午前三時', '玻璃の天球儀', '深海図書館', '失われた遊園地',
  '星食いの塔', '白紙の遺言', '灯なき回廊', '砂上の祝祭',
];

export function createSeed(): DemoData {
  const createdAt = '2026-10-01T12:00:00Z';
  const persons: Person[] = ['あなた（体験用）', '白凪', '灯', '雨音', '栞', '朔'].map((displayName, i) => ({id: i + 1, version: 0, displayName}));
  const scenarios: Scenario[] = titles.map((name, i) => ({
    id: i + 1, version: 0, name, authorName: ['回廊文庫', '宵待ち工房', '波間の書架'][i % 3] + '（架空）',
    sourceUrl: null, gameSystem: i % 4 === 2 ? 'エモクロアTRPG' : 'クトゥルフ神話TRPG 7版',
    externalImageUrl: null, image: {present: true}, favorite: [0, 2, 9].includes(i), tableCount: 0,
    createdAt: `2026-09-${String(i + 1).padStart(2, '0')}T12:00:00Z`,
  }));
  const names = ['鴉羽 律', '水瀬 澪', '朝霧 透', '灯野 紬', '月白 凪', '雨宮 奏', '花守 環', '星見 蓮', '雪代 栞', '遠野 碧'];
  const pcs: Pc[] = names.map((name, i) => ({
    id: i + 1, version: 0, name, personId: persons[i % persons.length].id, person: persons[i % persons.length],
    characterSheetUrl: null, systemSettings: [{id: i + 1, gameSystemName: 'クトゥルフ神話TRPG 7版', canonicalSystemKey: 'coc_7e', profileKey: 'coc_7e_v1', profileValues: {}, characterSheetUrl: null, displayOrder: 1}],
    image: {present: true, positionX: 0, positionY: 0, zoom: 1}, createdAt,
  }));
  let childId = 200;
  const tables: TableDto[] = scenarios.slice(0, 12).map((scenario, i) => ({
    id: i + 1, version: 0, scenarioId: scenario.id, scenario,
    tableName: i === 0 ? '硝子越しの約束' : ['はじめての夜', '週末の物語', 'もうひとつの結末'][i % 3], recordingUrl: null, createdAt,
    tableDates: [{id: ++childId, playedOn: `2026-10-${String(24 - i).padStart(2, '0')}`, version: 0}],
    participations: [
      {id: ++childId, version: 0, personId: persons[(i + 4) % 6].id, person: persons[(i + 4) % 6], pcId: null, pc: null, role: 'KP', displayOrder: 1, ho: null, displayQuote: null, spotlightType: null, endPcState: null},
      ...Array.from({length: i === 0 ? 4 : i % 3 + 1}, (_, j) => {
        const pc = pcs[(i + j) % pcs.length];
        return {id: ++childId, version: 0, personId: pc.personId, person: pc.person, pcId: pc.id, pc, role: 'PL' as const, displayOrder: j + 1,
          ho: ['温室の鍵を持つ人', '忘れられた約束を探す人', '夜明けを待つ人', '最後の手紙を読む人'][j],
          displayQuote: ['「この景色を、覚えていよう。」', '「きっと、また会える。」', '「ここから、もう一度。」', '「帰ろう。みんなで。」'][j],
          spotlightType: (j % 2 ? 'QUOTE' : 'HO') as 'HO' | 'QUOTE',
          endPcState: {id: ++childId, version: 0, profileKey: scenario.gameSystem === 'エモクロアTRPG' ? 'emoklore_v1' : 'coc_7e_v1', statusValues: {san: 62 - j * 7, hp: 12 - j, mp: 14 - j}, growth: '目星 +5\n仲間と交わした約束を、手帳に残した。', outcome: 'SURVIVED' as const, aftereffects: j === 1 ? '雨の音を聞くと、あの夜を思い出す。' : null},
        };
      }),
    ],
  }));
  const images: Record<string, string> = {};
  scenarios.forEach((s, i) => { images['scenarios/' + s.id] = `demo-assets/scenario-${String(i + 1).padStart(2, '0')}.jpg`; });
  pcs.forEach((p, i) => { images['pcs/' + p.id] = `demo-assets/pc-${String(i + 1).padStart(2, '0')}.png`; });
  return {schemaVersion: 1, nextId: 1000, selfPersonId: 1, persons, scenarios, pcs, tables, images};
}

let memory = createSeed();
export let storageNotice = '';
let persistent = true;
try {
  localStorage.setItem(DEMO_KEY + '-probe', '1');
  localStorage.removeItem(DEMO_KEY + '-probe');
} catch {
  persistent = false;
  storageNotice = 'このブラウザでは保存領域を利用できないため、変更はページを閉じるまで保持します。';
}

export function readStore(): DemoData {
  if (!persistent) return structuredClone(memory);
  try {
    const raw = localStorage.getItem(DEMO_KEY);
    if (!raw) return createSeed();
    const value = JSON.parse(raw) as DemoData;
    if (value.schemaVersion !== 1 || !Number.isSafeInteger(value.nextId) || !Number.isSafeInteger(value.selfPersonId) ||
      !['persons', 'scenarios', 'pcs', 'tables'].every(key => Array.isArray(value[key as keyof DemoData])) || !value.images) throw new Error('Invalid demo data');
    return value;
  } catch {
    storageNotice = '保存済みの体験データを読み取れなかったため、サンプルを表示しています。「サンプルに戻す」で保存状態を初期化できます。';
    return createSeed();
  }
}

export function writeStore(value: DemoData) {
  // A failed write must not be reported as a successful save.
  if (persistent) localStorage.setItem(DEMO_KEY, JSON.stringify(value));
  memory = structuredClone(value);
}

export function resetDemo() {
  if (persistent) localStorage.removeItem(DEMO_KEY);
  try { localStorage.removeItem(IMPORT_KEY); localStorage.removeItem('takukairo-demo-pc-density'); } catch { /* Memory-only session. */ }
  memory = createSeed();
}
