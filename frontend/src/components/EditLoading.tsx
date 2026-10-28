import {ApiError} from '../api/client';
import {ErrorPanel} from './common';

export function EditLoading({label, error, onRetry}: {label: string; error: unknown; onRetry: () => void}) {
  return <main className="page form-page">
    <h1>{label}を編集</h1>
    {error ? <>
      {error instanceof ApiError ? <ErrorPanel error={error}/> : <p role="alert">内容を読み込めませんでした。</p>}
      <button onClick={onRetry}>再取得</button>
    </> : <p role="status">保存済みの内容を読み込んでいます…</p>}
  </main>;
}
