export interface ImportRow {
  scenarioName: string;
  gameSystem: string;
  tableName: string;
  playedOn: string;
  kp: string;
  pl: string;
  pc: string;
}
export const columns: {key: keyof ImportRow; label: string}[] = [
  {key: 'scenarioName', label: 'シナリオ名'}, {key: 'gameSystem', label: 'ゲームシステム'},
  {key: 'tableName', label: '卓名'}, {key: 'playedOn', label: '開催日'},
  {key: 'kp', label: 'KP'}, {key: 'pl', label: 'PL'}, {key: 'pc', label: 'PC'},
];
export const sampleCsv = 'シナリオ名,ゲームシステム,卓名,開催日,KP,PL,PC\n硝子温室の残響,クトゥルフ神話TRPG 7版,ふたたび温室へ,2026-10-25,白凪,あなた（体験用）,鴉羽 律\n夜明けの郵便局,エモクロアTRPG,最後の配達,2026-10-26,灯,雨音,青葉 こよみ';

// Quoted commas, escaped quotes, CRLF and newlines within quoted fields are supported.
export function parseCsv(raw: string): ImportRow[] {
  if (raw.length > 256_000) throw new Error('体験版では256KB以内のCSVを指定してください。');
  const text = raw.replace(/^\uFEFF/, '');
  const records: string[][] = [];
  let row: string[] = [], field = '', quoted = false, closed = false;
  const finishField = () => { row.push(field.trim()); field = ''; closed = false; };
  const finishRow = () => { finishField(); if (row.some(Boolean)) records.push(row); row = []; };
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (quoted) {
      if (ch === '"') {
        if (text[i + 1] === '"') { field += '"'; i++; }
        else { quoted = false; closed = true; }
      } else field += ch;
    } else if (ch === ',' || ch === '\n' || ch === '\r') {
      if (ch === ',') finishField();
      else { finishRow(); if (ch === '\r' && text[i + 1] === '\n') i++; }
    } else if (ch === '"') {
      if (field || closed) throw new Error('引用符の位置を確認してください。値全体を半角の二重引用符で囲みます。');
      quoted = true;
    } else {
      if (closed && ch.trim()) throw new Error('閉じた引用符の後には、カンマか改行を入れてください。');
      if (!closed) field += ch;
    }
  }
  if (quoted) throw new Error('閉じられていない引用符があります。');
  finishRow();
  const headers = records.shift();
  if (!headers || !columns.every(c => headers.includes(c.label)) || new Set(headers).size !== headers.length || headers.length !== columns.length) throw new Error('見出しは「シナリオ名,ゲームシステム,卓名,開催日,KP,PL,PC」の7列を指定してください。列の順序は変更できます。');
  if (!records.length || records.length > 100) throw new Error('1〜100件のデータ行を指定してください。');
  return records.map((record, i) => {
    if (record.length !== headers.length) throw new Error(`${i + 2}行目の列数を確認してください。`);
    return Object.fromEntries(columns.map(c => [c.key, record[headers.indexOf(c.label)]])) as unknown as ImportRow;
  });
}

export function rowError(row: ImportRow): string | null {
  if (!row.scenarioName.trim()) return 'シナリオ名を入力してください。';
  if (row.playedOn && (!/^\d{4}-\d{2}-\d{2}$/.test(row.playedOn) || !Number.isFinite(Date.parse(row.playedOn)) || new Date(row.playedOn).toISOString().slice(0, 10) !== row.playedOn)) return '開催日は実在する日付をYYYY-MM-DDで入力してください。';
  if (row.pc.trim() && !row.pl.trim()) return 'PCを登録する場合はPL名も入力してください。';
  return null;
}
