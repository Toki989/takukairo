import {useContext, useState} from 'react';
import {Link, NavLink, Outlet} from 'react-router-dom';
import {mutate} from '../api/client';
import {ConfirmContext, Dialog, ErrorPanel, useErrors} from '../components/common';
import {storageNotice} from './store';
import {demoBoothEndpoint} from './booth';

export function DemoChrome() {
  const [about, setAbout] = useState(false), [busy, setBusy] = useState(false);
  const confirm = useContext(ConfirmContext), {error, report} = useErrors();
  async function reset() {
    if (!await confirm('このブラウザで追加・編集した体験データとIMPORTの途中入力を消して、最初のサンプルに戻しますか？')) return;
    setBusy(true);
    try { await mutate('/demo/reset', 'POST'); window.dispatchEvent(new Event('demo-reset')); window.location.replace(window.location.href.split('#')[0]); }
    catch (e) { report(e); setBusy(false); }
  }
  return <>
    <aside className="demo-banner" aria-label="ブラウザ体験版"><div><strong>卓回廊 <span>BROWSER DEMO</span></strong><p>ログイン不要。あなただけのブラウザで、TRPGの記憶を並べる体験を。</p></div><div className="demo-actions"><button className="quiet" onClick={() => setAbout(true)}>体験版について</button><button disabled={busy} onClick={() => void reset()}>サンプルに戻す</button></div></aside>
    {storageNotice && <p className="demo-storage" role="status">{storageNotice}</p>}
    <ErrorPanel error={error}/>
    {about && <Dialog title="卓回廊のブラウザ体験版" onClose={() => setAbout(false)}><p>TRPGのシナリオ、遊んだ卓、PCをつなぎ、物語の記憶を振り返る個人のギャラリーです。</p><ul><li>アカウント作成・ログインは不要です。</li><li>検索、お気に入り、シナリオ・PC・人物・卓の追加と編集、定型CSVの登録を試せます。</li><li>追加・編集した記録はこの端末・ブラウザの保存領域に保存します。ほかの閲覧者とは共有しません。</li>{demoBoothEndpoint()&&<li>BOOTHの商品URLからシナリオ情報を取得できます。取得時は商品URLを情報取得用の処理へ送信します。シナリオの記録や追加画像は送信しません。</li>}<li>外部画像は埋め込みURLだけを保存し、表示時に画像配信元へ通信します。</li><li>「サンプルに戻す」で体験データを初期化できます。ブラウザのサイトデータを消した場合も変更は失われます。</li><li>画像のアップロードはPNG・JPEG・WebPの1.5MB以下に対応します。</li><li>本体のIMPORT推論、実ユーザー認証は体験版の対象外です。</li></ul><p><a href="https://github.com/Toki989/takukairo" target="_blank" rel="noopener noreferrer">GitHubでソースコードを見る ↗</a></p><button className="primary" onClick={() => setAbout(false)}>体験を続ける</button></Dialog>}
  </>;
}

export function DemoShell() {
  return <><header className="app-header"><Link className="brand" to="/">卓回廊</Link><nav aria-label="主な画面"><NavLink to="/" end>HOME</NavLink><NavLink to="/pcs">PL / PC</NavLink><NavLink to="/import">IMPORT</NavLink></nav><Link className="account-entrance" to="/account">体験データ</Link></header>
    <Outlet/><footer className="app-footer"><Link to="/terms">体験版の利用について</Link><Link to="/privacy">データの取り扱い</Link><span>BROWSER DEMO</span></footer></>;
}

export function DemoSettings() {
  return <main className="page form-page"><p className="kicker">BROWSER DEMO</p><h1>体験データについて</h1><p>ログインやアカウントの作成は必要ありません。「あなた（体験用）」を自分の人物として、参加記録の絞り込みやPC管理を試せます。</p><p>追加・編集したデータはこのブラウザだけに保存されます。最初から試すには、画面上部の「サンプルに戻す」を選んでください。</p><Link to="/">回廊へ戻る →</Link></main>;
}
