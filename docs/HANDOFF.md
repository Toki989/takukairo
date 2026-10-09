# 作業中断・再開記録

## 2026-10-09 ファイルから開く入口の追加完了

ユーザーの追加希望に従ってルート `index.html` と `起動.cmd / 停止.cmd` を追加した。起動.cmdで実アプリを起動して既定ブラウザを開く。index.htmlはfile://で開ける入口。起動停止・二重起動防止・既存プロセス保護・Data件数保持・実Browserからのログイン／Import移動を検証済み。実機能は既存Backend／DBに接続したまま。操作方法はREADME、検査証拠はIMPLEMENTATION_STATUSに記載した。

## 2026-10-09 再開・最終確認完了

中断時の残件を処理し、通常Devの最終Smokeが成功した。Cookie、Gallery Keyboard、Collection Focus Return、PL Tab、ComboBoxを確認し、Browser実行Errorは0。Desktop ScreenshotもSelector画像のロード完了後に目視確認した。Backend起動時に4 Migrationのvalidateが成功。Application Sourceに変更なし。

ローカルアプリは通常Devで起動中： http://localhost:5173/dev-login 。Rate Limit・Cleanup有効。既存DB・画像・Import途中保存を保持し、任意の確認用Import Fixtureの再作成は行っていない。必要な3状態／完了直前のFixture生成手順と実検査の証拠はREADME・IMPLEMENTATION_STATUSに揃っている。

前日のBackend85件、Browser27件＋独立DB2件、Frontend型検査・Buildの成功記録は有効。通常Devの追加検証は `.runtime/final-dev-smoke.log`、起動ログは `.runtime/resume-start-dev.log`。物理端末・他Browser・Screen Reader・Productionの未検証範囲はIMPLEMENTATION_STATUSに記録した。

以下は2026-10-08の中断時点の履歴。現在の未完了作業を示すものではない。

2026-10-08、ユーザーの「きりのいいところで一旦止めてください」により中断。全体の最終引き渡し確認を残して停止する。Productionは使用していない。

## 中断地点

- 最新Backend：85 Test、失敗0・Error0・Skip0。PostgreSQL 18.6 Testcontainers、Flyway 0→V3、Hibernate validate成功。
- 最新Frontend：typecheck・Build成功。
- 最新Browser：27 Test成功、Skip0・Flaky0（16:27:52開始）。独立DBの状態検査2 Test成功（16:29:14開始）。
- Keyboard Smoke：Gallery Focus/Enter、Collection Focus Return、PL Tab Home/End/Arrow、ComboBox Arrow/Enter/Escape成功。`.runtime/qa-keyboard-smoke.log`。
- 最後の修正：Collection取得からUI専用`pc` Queryを除去し、DOM表示後にFocus復帰。初期Session取得失敗を永続Error表示＋明示再取得に変更。上記検査は両修正を含む。
- Desktop v5.9の3レーン、13 Viewport、PL0〜8とmiddle/lastを実表示・撮影確認。SeedはPL0〜7、PL8は実APIでQA用卓を追加。PC未登録の状態も保持。
- 通常Dev Profileで最新jar／Frontendの起動成功を確認（Rate Limit・Cleanup有効）。中断に伴って`stop.ps1`で停止。Dataは削除しない。
- 元の仕様書・Prototype・参照資料・画像は変更なし。実装は新規未追跡ファイル。Git commitは行っていない。

## 再開時に残る作業

1. `scripts/start.ps1 -PortablePostgres`で通常Devを起動し、最新コードで`frontend/qa/smoke.mjs`を1回実行する。QA Profileでの同じSmokeは成功済み。通常Devで短時間に繰り返すとRate Limitが作動するため、繰り返し大量実行しない。
2. 必要なら確認用Import3状態を`qa/import-fixtures.mjs review`で準備する。現在のImportはBrowser QA由来。途中保存を破棄する場合だけ明示の`--discard-current`を使用する。正式Dataは削除しない。
3. 最終起動状態・証拠・READMEを照合し、ユーザーへ最終完了報告する。未実行の物理端末／Safari・Firefox／Screen Reader／Production検査はPASSにしない。

詳細は [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)、実検査結果は [QA_RESULTS.json](QA_RESULTS.json)、起動・停止・検査コマンドは [README](../README.md)。`.tools/`に固定Versionのportable toolchain、`.runtime/pgdata`にDB、`.runtime/storage`に画像を保持する。

```powershell
cd C:\Src\takukairo
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start.ps1 -PortablePostgres
```

Frontend：http://localhost:5173/dev-login 。固定テストユーザーでログイン。
