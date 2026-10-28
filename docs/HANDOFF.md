# 作業中断・再開記録

## 2026-10-28 Mac対応

ユーザーの「卓回廊をMacでも使えるように」に対応し、`起動.command / 停止.command / 体験版.command`と`scripts/mac-local.mjs`を追加。通常版はMacのNode 24・JDK 25・Docker PostgreSQLを使用し、Maven Wrapperでビルドする。外部管理のPostgreSQLは`--external-db`で指定する。Macの初回準備・保存場所・WindowsのDBをそのままコピーしない移行説明は[MAC.md](MAC.md)。Windowsの既存ランチャーは保持。

Apple Silicon対応のためWebP ImageIOを`com.github.usefulness:webp-imageio:0.11.0`へ変更し、配布jarにMac aarch64 / x86_64のネイティブコードを確認。WebKit検査で、編集フォームの初期取得より先に入力できて後から元の値に戻る問題を再現し、シナリオ・PC・卓の編集に読み込み待ち・失敗時の再取得を追加した。Chromium専用のCPU throttlingはChromiumでのみ実行し、同じ画面検査をWebKitでも実行する。

検査の確定結果・制約は[Mac対応の検証記録](reviews/mac-compatibility-2026-10-28.md)に記載する。Mac実機・実Safari・Mac CIはこのWindows作業環境では未実行。Mac CIの構成は`.github/workflows/mac.yml`。既存の未コミット作業を保持し、commit・push・公開は行わない。体験版BOOTH URL対応の終了指示は引き続き有効で、今回その対応は再開していない。

## 2026-10-28 ユーザーによる体験版BOOTH URL対応の終了指示

ユーザーが「いえ、もう体験版でブースURLはもういいです」と明示。特定のWorker構成の調査中止にとどまらず、体験版のBOOTH URL対応そのものを終了する。別方式の検討・追加実装・Deploy依頼を再開しない。通常ローカル版のBOOTH取得機能は保持する。今回の指示をコード一式・Cloudflareサービスの削除指示とは扱わず、削除や公開操作は行っていない。

## 2026-10-28 BOOTHの接続環境拒否を確認・公開体験版対応を区切る

ユーザーが`2026-10-28-error-page-summary`をDeployし、15:10 JSTに実応答を確認。BOOTHはJSON/HTML両方へ403、タイトルBOOTH、見出し「ブロックされました」、本文「あなたの環境からはBOOTHにアクセスできません。」を返した。証拠は `.runtime/booth-import/deployed-error-page-summary.json`。応答全体373,091 bytesを検査済み。これでWorkerからの接続環境をBOOTH側が拒否していることは確認できた。IP評価・Bot判定・Workers判定など具体的にどの規則かは未特定。分類ロジックは英語定型句だけなのでunclassified-denialだが、日本語の実文言はpageSummaryで確認できた。

ユーザーは「もうむりそうならやめるのでいい」と指示。このWorker構成の公開体験版への自動取得対応はここで中止し、追加のコード変更とDeploy依頼を繰り返さない。公開版への有効化・完成報告は行わない。ローカル版の実取得と外部画像URLだけの保存は保持。関連コードは削除せず調査状態を残す。Pages WorkflowはBOOTH_PREVIEW_URL未設定なら自動取得を表示しない状態を維持。具体的規則の確認が必要ならBOOTH運営側の調査範囲で、日時とRay IDは[Worker README](../services/booth-preview/README.md)に記録済み。外部への問い合わせは送っていない。

## 2026-10-28 公開Workerの403原因調査

ユーザーの「理由を解明しましょう」により原因調査。生成済みWorkerコードのNodeローカル実行は商品3403696を200で取得。公開WorkerはOriginなし・GitHub Pages Originありの両方でJSON/HTMLが403、後者のAccess-Control-Allow-Originは正しい。失敗箇所はWorker→BOOTHで、Pages側CORSや解析処理より前。IP評価・Bot判定・Workers判定のどれなのかは、403だけでは未確定。証拠は `.runtime/booth-import/denial-investigation.json`。

診断版`2026-10-28-denial-diagnostics`はユーザーがDeploy済み。JSON/HTML両方がCloudflareのHTMLエラー（cf-error-details等のマーカーあり）、cf-mitigatedなし、エラー番号なし。ただし本文は16KiBで読み取り打ち切りとなり両方truncated=true。証拠は `.runtime/booth-import/deployed-denial-diagnostics.json`。こちらの診断上限が不足しており、本文後半をまだ確認できていない。正確な拒否規則は未確定。

修正版`2026-10-28-denial-diagnostics-v2`もユーザーがDeploy済み。2026-10-28 15:05 JSTに確認、JSON/HTML両方が403で373,091 bytes、truncated=false。全体を検査しても既知の英語定型句やエラー番号に一致せず、正確な原因はまだ不明。証拠は `.runtime/booth-import/deployed-denial-diagnostics-v2.json`。こちらの診断を定型句だけにしていたため、表示文言そのものを確認できていない。Cloudflare形式のマーカーだけでWAF/Bot規則を断定しない。

そこで`2026-10-28-error-page-summary`を生成。タイトル・見出し・短い表示本文を返すpageSummaryを追加。HTML/Script/CSS/SVGを除去し、IP・メール・URL・長いトークンを伏せる。全文の生HTMLやCookie・訪問者ヘッダーは返さない。12件のUnit検査と共有TSのstrict型検査が成功。その後ユーザーが公開反映し、冒頭の接続環境拒否を確認。追加Deployを依頼する段階は終了。詳細と一次資料は[Worker README](../services/booth-preview/README.md)。

## 2026-10-28 GitHub Pages体験版へのBOOTH取得追加（公開実取得は未完了）

ユーザーは、GitHub Pagesを見た人が任意のBOOTH商品URLから取得でき、訪問者の登録・追加設定・手間を増やさない構成を希望。無料Workersの追加を明示採用し、Cloudflareで `takukairo-booth-preview.tokibs.workers.dev` を作成・Deploy済み。ローカル体験版だけという一時的な解釈はユーザーが訂正した。費用ゼロ、画像は外部の埋め込みURLのみという指示を維持する。

体験版に候補確認・明示反映・外部画像URLだけの保存・再表示・初回同意を実装し、20件のBrowser検査が成功。公開Worker向けURLでビルドした状態の関連3件も成功。ただしこれは中継応答をStubした画面検査。Nodeからの実取得は成功しているが、Cloudflare上の実取得ではBOOTHが403を返すため公開機能は未完了。

Workers非対応のfetch option `credentials`を除去済み。JSONが拒否された場合に通常公開HTMLを確認するFallbackも追加し、ユーザーが最新版を再Deployした。公開rootのversion `2026-10-28-html-fallback`を確認したうえで実取得を実施したが、`upstreamStatuses={json:403,html:403}`。このWorker構成での実取得は未達。Pages Workflowの失敗するWorkerへの既定接続を外し、Repository Variable `BOOTH_PREVIEW_URL`が未設定なら公開版では自動取得を表示しない。詳細は [Workerの手順と現状](../services/booth-preview/README.md)。成功を確認するまで完成・公開反映済みと報告しない。Windowsのブラウザ自動操作は現在URLを安全に判定できず停止したため、コードの貼り替えとDeployはユーザーが行った。ユーザーのDeploy漏れとして扱わず、同じ再Deployを依頼し続けない。

## 2026-10-28 通常ローカル版の起動復旧

BOOTH実取得対応後の通常起動を復旧した。前回の`npm ci`中断で欠けた依存関係を再準備し、同じ依存設定のパッケージは次回以降再利用するよう`start.ps1`を修正。通常Devは `http://localhost:5173/` で起動済み。独立ブラウザからシナリオ追加画面・BOOTH実商品取得を確認し、シナリオ一覧は取得前後で一致、画像の自動採用なし、Browser Error0。詳細は [BOOTH実取得記録](reviews/booth-live-fetch-2026-10-27.md) の末尾。通常DBのリセット・確認用シナリオの登録は行っていない。次の再開時は実際の稼働状態を確認すること。Homeのユーザーによる見た目の確認と、以下のImport解析不足は別途残る。

## 2026-10-27 ローカル版の完成へ方針変更・BOOTH実取得

ユーザーは開発・運用とも費用ゼロが前提と明示し、翌日までにローカル版を完成させる方針へ変更した。Renderの登録と作成画面の入力を案内したが、エージェントは有料DB・Web Serviceの作成や公開を行っていない。本番公開の準備を自動的に再開しない。

「URLも起動できる」は追加確認と指定URLにより、シナリオ追加・編集画面でBOOTH商品情報を実取得する機能と確定した。実取得Adapterと候補確認画面を追加。指定URLで実取得から保存・表示まで確認した。画像はユーザーの明示指示どおり、外部の埋め込みURLだけを保存し、画像ファイルをStorageへ複製しない。詳細・証拠・使用手順は [BOOTH実取得記録](reviews/booth-live-fetch-2026-10-27.md)。通常DBを変更せず専用環境で確認。変更は未コミット。

Homeの見た目は下記メモの調整後もユーザー未確認。Importの直近の受入記録 `reviews/round1-acceptance-2026-10-26/テスト結果.txt` では、手動修正後の全5卓の正式登録は成功した一方、CSV集約・複数日付、Markdownの所属、HTMLのラベルと値の解析不足R1〜R4は未修正。このBOOTH対応だけでローカル版全体の完成とは報告しない。

## 2026-10-27 Home GalleryのVisual調整への引継ぎ

再開後、行内を上下中央に揃え、FavoriteをMetadataに近づけ、Homeの導入部を詰めた。通常版・体験版ビルドと18件のブラウザ検査が成功。詳細は [Visual調整記録](reviews/home-gallery-visual-balance-2026-10-27.md)。公開へのpushとユーザーによる見た目の確認は未実施。

最新の引継ぎは [HANDOFF_HOME_GALLERY_2026-10-27.md](HANDOFF_HOME_GALLERY_2026-10-27.md) を読むこと。No.29の中央配置とテスト同期修正は実装済みで、公開への反映もユーザー確認済み。ただし、ユーザーは「すごいバランスが悪い」と評価しており、Visualは未完了・未承認。次は仕様書・追加No.29資料・v5参考実装・実画像を照合して見た目を改善する。今回の依頼は引継ぎメモの作成のみで、追加のUI変更は行っていない。

以下の2026-10-09以前の起動状態や残件は過去の記録であり、現在の稼働状況として扱わない。

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
