import {useEffect, useRef, useState} from 'react';
import {Link} from 'react-router-dom';
import {mutate} from '../api/client';
import {useSession} from '../app';
import {ErrorPanel, useDirty, useErrors} from '../components/common';
import {columns, parseCsv, rowError, sampleCsv, type ImportRow} from './csv';
import {IMPORT_KEY} from './store';

function savedDraft() {
  try { const draft = JSON.parse(localStorage.getItem(IMPORT_KEY) ?? 'null'); if (draft && typeof draft.raw === 'string' && (draft.rows === null || Array.isArray(draft.rows) && draft.rows.length <= 100 && draft.rows.every((row: ImportRow) => columns.every(c => typeof row[c.key] === 'string')))) return draft as {raw: string; rows: ImportRow[] | null}; } catch { /* A draft is optional. */ }
  return {raw: '', rows: null as ImportRow[] | null};
}

export function DemoImport() {
  const [initial] = useState(savedDraft);
  const [raw, setRaw] = useState(initial.raw), [rows, setRows] = useState<ImportRow[] | null>(initial.rows);
  const [busy, setBusy] = useState(false), [result, setResult] = useState<number[] | null>(null), [draftSaved, setDraftSaved] = useState(false);
  const {error, report, clear} = useErrors(), {toast} = useSession();
  const resultHeading = useRef<HTMLHeadingElement>(null);
  const releaseDirty = useDirty(!!(raw || rows) && !draftSaved && !result);
  useEffect(() => {
    try { if (raw || rows) localStorage.setItem(IMPORT_KEY, JSON.stringify({raw, rows})); else localStorage.removeItem(IMPORT_KEY); setDraftSaved(true); }
    catch { setDraftSaved(false); }
  }, [raw, rows]);
  useEffect(() => { if (result) resultHeading.current?.focus(); }, [result]);
  const updateRaw = (value: string) => { setRaw(value); setRows(null); setResult(null); setDraftSaved(false); clear(); };
  function preview() { clear(); try { setRows(parseCsv(raw)); } catch (e) { report(e); } }
  async function register() {
    if (!rows || busy) return;
    clear(); setBusy(true);
    try {
      const saved = await mutate<{tableIds: number[]}>('/demo/import', 'POST', {rows});
      releaseDirty(); setResult(saved.tableIds); setRows(null); setRaw(''); toast(`${saved.tableIds.length}件の卓をブラウザ内に登録しました`);
    } catch (e) { report(e); } finally { setBusy(false); }
  }
  return <main className="page form-page demo-import">
    <p className="kicker">IMPORT / BROWSER DEMO</p><h1>記録を、回廊へ。</h1>
    <p>CSVの記録を確認して、シナリオ・人物・PC・卓としてまとめて登録できます。</p>
    <aside className="demo-note"><strong>体験版で試せること</strong><p>定型CSVの読み取り・内容の修正・登録を体験できます。1行を1卓として、KPとPLを各1人登録します。同じ名前とゲームシステムのシナリオ、同名の人物、同じPLの同名PCは再利用します。本体の自由文解析・推論・照合・分割や結合は体験版の対象外です。</p></aside>
    <ErrorPanel error={error}/>
    {result ? <section className="demo-import-result"><p className="kicker">COMPLETE</p><h2 ref={resultHeading} tabIndex={-1}>{result.length}件の記録が加わりました</h2><p>保存先はこのブラウザです。ほかの閲覧者には共有されません。</p><p><Link to={'/tables/' + result[0]}>登録した卓を見る →</Link></p><p><Link to="/">回廊を見る →</Link></p><button onClick={() => setResult(null)}>別の記録を取り込む</button></section> : <>
      <div className="demo-import-steps" aria-label="登録の流れ"><span aria-current={!rows ? 'step' : undefined}>01 入力</span><span aria-current={rows ? 'step' : undefined}>02 確認・修正</span><span>03 登録</span></div>
      {!rows ? <section><h2>記録を入力</h2><p>サンプルを使うと、入力から登録までをすぐに試せます。</p><div className="demo-actions"><button onClick={() => updateRaw(sampleCsv)}>サンプルCSVを使う</button><a download="takukairo-demo.csv" href={'data:text/csv;charset=utf-8,' + encodeURIComponent('\uFEFF' + sampleCsv)}>サンプルをダウンロード</a></div>
        <label htmlFor="demo-csv-file">CSVファイルを選択（UTF-8・256KB以内）</label><input id="demo-csv-file" type="file" accept=".csv,text/csv" onChange={e => { const file = e.target.files?.[0]; if (!file) return; if (file.size > 256_000) { report(new Error('256KB以内のCSVを選んでください。')); return; } void file.text().then(updateRaw).catch(report); }}/>
        <label htmlFor="demo-csv">CSVを貼り付け</label><textarea id="demo-csv" rows={10} spellCheck={false} placeholder={sampleCsv} value={raw} onChange={e => updateRaw(e.target.value)}/>
        <p className="save-status" role="status">{draftSaved && raw ? '入力はこのブラウザに途中保存されています。' : '入力内容を外部へ送信しません。'}</p>
        <button className="primary" disabled={!raw.trim()} onClick={preview}>内容を確認する →</button>
      </section> : <section><div className="page-heading"><h2>{rows.length}件の記録を確認</h2><button onClick={() => { setRows(null); clear(); }}>入力に戻る</button></div><p>必要な項目を修正してから登録してください。登録するまでは回廊のデータは変わりません。</p>
        {rows.map((row, index) => <fieldset key={index}><legend>記録 {index + 1}</legend><div className="demo-import-grid">{columns.map(c => <label key={c.key} htmlFor={`row-${index}-${c.key}`}>{c.label}<input id={`row-${index}-${c.key}`} type={c.key === 'playedOn' ? 'date' : 'text'} required={c.key === 'scenarioName'} value={row[c.key]} onChange={e => { setRows(rows.map((r, i) => i === index ? {...r, [c.key]: e.target.value} : r)); setDraftSaved(false); }}/></label>)}</div>{rowError(row) && <p className="inline-error" role="status">{rowError(row)}</p>}<button onClick={() => setRows(rows.filter((_, i) => i !== index))}>この記録を除外</button></fieldset>)}
        <div className="save-bar"><button className="primary" disabled={busy || !rows.length || rows.some(rowError)} onClick={() => void register()}>{busy ? '登録しています…' : `${rows.length}件をブラウザ内に登録`}</button></div>
      </section>}
    </>}
  </main>;
}
