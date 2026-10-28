# 卓回廊 — TRPGの作品と、卓の記憶を並べる個人の回廊

## ログイン不要のブラウザ体験版

**GitHub Pages版（操作体験用）：[卓回廊 ブラウザ体験版](https://toki989.github.io/takukairo/)**

本リポジトリの本体は、Reactのフロントエンドと **Spring Boot＋PostgreSQL** を使用するWebアプリケーションです。GitHub Pages版は画面や操作を試すための静的な体験版で、本体のバックエンド・データベースには接続しません。

シナリオ画像19枚・PC画像10枚を使い、回廊の検索、お気に入り、シナリオ・人物・PC・卓の追加と編集、定型CSVの登録を試せる静的サイトを用意しています。操作結果は閲覧者のブラウザ内だけに保存し、バックエンドやアカウントは必要ありません。

Windowsでは **[体験版.cmd](体験版.cmd)**、Macでは **[体験版.command](体験版.command)** をダブルクリックすると開きます。Macの初回準備は **[Macでの使い方](docs/MAC.md)** を参照してください。コマンドではfrontendでnpm ci、npm run dev:demoを実行し、<http://localhost:5175/> を開きます。

GitHub Pages公開用のWorkflowも用意しています。起動・公開手順、体験できる機能、本体との差、素材の由来は **[ブラウザ体験版の説明](docs/BROWSER_DEMO.md)** を参照してください。

## 通常版・ローカル実装

Home Scenario Galleryの配置には、追加された[No.29確定事項まとめ](卓回廊_HomeGallery_左右余白と並び替え時の空白対策_No29確定事項まとめ.md)も適用します。Flexboxによる行ごとの中央配置と、残余空間の左右均等分配が採用済みです。

実装正本は `卓回廊_Codex実装正本_2026-10-08_R5.1.12_最終Contract補強版.md`。Part Aを優先し、未変更事項はPart B/Cに従います。既存の仕様書・Prototype・参照資料・画像assetsを元の場所に保全しています。実装と実検査の記録は [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md)、API照合は [docs/API_CONTRACT.md](docs/API_CONTRACT.md) にあります。Part D/Eの監査PASSを実装テストのPASSへ転用していません。

## 固定Version・環境要件

| 技術 | Version |
|---|---|
| Java | Eclipse Temurin 25.0.4.1+1 |
| Spring Boot | 4.1.1 |
| Maven / Wrapper | 3.9.16 / 3.3.4 |
| Node.js / npm | 24.21.0 / 11.19.0 |
| PostgreSQL | 18.6 |
| React / Router | 19.3.0 / 7.18.4 |
| TypeScript / Vite | 7.0.2 / 8.3.3 |
| Testcontainers / Playwright | 2.0.5 / 1.64.0 |

Java 25とJAVA_HOME、Node 24、Docker Desktop（DBテスト用）が必要です。この作業環境の `.tools/` には固定Versionのportable環境があります。Gitには含めません。`scripts/env.ps1` はportableを優先し、存在しなければインストール済みPATHを使用します。Maven Wrapperは配布VersionとSHA256を固定、npmはpackage-lockで固定しています。初回依存取得にはネット接続が必要です。DB検査にH2は使用しません。

Stable系列の具体Versionは公式配布metadataで照合しました。作業環境内の照合元は `.tools/jdk-index.json`、`.tools/node-index.json`、`.tools/maven-index.xml`、実行Versionは検証記録に残しています。

## 初回セットアップ・起動

**Mac（Apple Silicon / Intel）の通常版は [Macでの使い方](docs/MAC.md) に沿って準備し、`起動.command` / `停止.command` を使います。** Node.js 24・JDK 25・起動済みDocker Desktopが必要です。Mavenは同梱Wrapperで準備します。以下はWindowsでの手順です。

Windowsで手動確認する場合は、フォルダー直下の `index.html` をダブルクリックして入口を開けます。同じフォルダーの `起動.cmd` をダブルクリックすると、ローカルアプリを起動して既定ブラウザを開きます。すでに同じアプリが起動済みなら二重起動せず、その画面を開きます。ログイン画面では「固定テストユーザーでログイン」を選びます。

確認後は `停止.cmd` をダブルクリックします。DB・画像・Import途中保存は保持します。`index.html` はローカルアプリへ移動する入口なので、実際の保存・Importには起動が必要です。HTML単体のMockへ置き換えてはいません。

この作業環境では `.tools/` のportable環境を自動選択します。別の場所へ保管する場合は、`停止.cmd` で停止してからフォルダー全体（hiddenの `.tools/`、Dataを保つ `.runtime/`、使用中の `.env` を含む）を保持してください。別のPCでは上記環境要件が必要です。ブラウザを閉じただけではアプリは停止しません。起動に失敗した場合は `.runtime/launcher-start.log / launcher-start-error.log` を確認します。

PowerShellで実行します。既存 `.env` は上書きしません。5432／8080／5173を使用します。

```powershell
cd C:\Src\takukairo
if (!(Test-Path .env)) { Copy-Item .env.example .env }
# この作業環境のportable PostgreSQLを使用
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start.ps1 -PortablePostgres
```

Docker PostgreSQLを使う場合はDocker Desktopを起動し、`-PortablePostgres`を省略します。先に同じ5432ポートを使用するportable PostgreSQLを停止してください。ComposeのPostgreSQL 18用volumeは `/var/lib/postgresql` に固定しています。

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start.ps1
```

Frontend: <http://localhost:5173/dev-login>。固定テストユーザーでログインします。任意のUser IDを指定するログインはありません。名前未設定ならTRPG名を設定します。Dev Migrationはテスト人物・14PC・10Scenario・PL人数別の卓を投入します。別Userのデータは拒否検査用です。実ユーザー、Google実Credential、Productionは使用しません。

portable PostgreSQLは127.0.0.1限定のテスト専用trust認証、Dockerは例示のローカル専用パスワードです。環境変数は `.env.example` の `DB_URL / DB_USER / DB_PASSWORD / STORAGE_PATH`。実Credentialを保存する必要はありません。

初期画像は既存テストassetsをUpload API経由で投入します。元画像は変更しません。

```powershell
Set-ExecutionPolicy -Scope Process Bypass
. .\scripts\env.ps1
Push-Location frontend
node qa/seed-images.mjs
Pop-Location
```

フロントエンドの依存パッケージは初回と`package.json`／`package-lock.json`の変更時に準備します。同じ依存設定で必要なパッケージが揃っている場合は再利用するため、通常起動のたびに`npm ci`で入れ直しません。依存パッケージが欠けている場合は再準備します。

## 停止・保存場所

```powershell
cd C:\Src\takukairo
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stop.ps1
```

起動したPIDとCommandLineを確認して今回のアプリだけを停止します。DB・画像・途中保存は削除しません。portable DBは `.runtime/pgdata`、画像は `.runtime/storage`、ログは `.runtime/backend.log / backend-error.log / frontend.log`、PIDは `.runtime/processes.json`。DBとStorageはセットで保持してください。

## 実装範囲

通常の業務操作はReact → Backend → PostgreSQLで保存します。Scenario・Person・PC・Table Aggregate、Favorite・検索・Filter・Recent・Activity、Game System Profile・Previous・EndPcState、画像Upload・Transform・削除・Cleanup、Import入力・解析・照合・編集・Autosave・Resume・Bulk・Split・Merge・Partial Register・Completeまで接続しています。

Formal Entityは User / Scenario / Person / PC / Table / TableDate / Participation / EndPcState / ScenarioFavorite の9件。`PcSystemSetting`、`ImportSession / ImportSource / ImportCandidate / ImportResolution / ImportCandidateSourceTrace / ImportPreview`、`StorageDeleteTask`、Spring Session JDBCはSupport Structureで、Formal EntityやUCを増やしません。Game System Profile・Resolver・CCFOLIA MappingはVersioned Application Configです。初期Resolver Aliasは空です。

ローカル通常起動では、シナリオ追加・編集画面の「BOOTHから入力を補助」から公開商品ページの情報を取得できます。商品名・作者（記載がなければショップ名）・明示されたゲームシステム・商品URLを候補として表示し、選択した項目だけを入力欄へ反映します。取得中も手入力でき、入力済みの値を上書きするときは確認します。取得や候補反映では自動保存しません。

BOOTH画像は「この画像を使用」を明示選択し、シナリオ保存時に外部画像URLとして記録します。表示時に配信元へアクセスし、画像ファイルをLocal Storageへ複製しません。初回保存時の外部画像利用の説明・同意と、アップロード画像の優先を維持します。ページが非公開・取得失敗の場合は手入力で続けられます。`.env`に`BOOTH_LIVE_FETCH=false`を指定するとMock Adapterへ戻せます。検査Profileでは通常Mockを使い、実取得確認時だけ明示して切り替えます。詳細は [BOOTH実取得の記録](docs/reviews/booth-live-fetch-2026-10-27.md)。

手動アップロード画像はLocal Storage Adapterで保存します。CCFOLIA駒JSONは任意の入力補助で、RawをFormal Dataへ保存しません。Tekey／Udonariumは未対応を返すExtension PointのStubです。CCFOLIAを使わず手入力のみでもPC・卓・終了時状態を登録できます。

## 検査

全Browser検査は固定テストユーザーのDataを追加し、既存のテスト用Import Sessionを破棄してケースを作ります。通常業務用の途中保存を保持したい場合は、先に別のテストDBへ切り替えてください。実ユーザーは使用しません。

```powershell
cd C:\Src\takukairo
Set-ExecutionPolicy -Scope Process Bypass
. .\scripts\env.ps1
# Docker Desktop必須。独立PostgreSQL 18.6 Containerで0→latestとHibernate validate
.\backend\mvnw.cmd -B -ntp -f backend/pom.xml "-Dmaven.repo.local=$Workspace/.tools/m2" test
Push-Location frontend
npm run typecheck
npm run build
npx playwright install chromium
Pop-Location
# Rate Limit / scheduled cleanupを停止した検査Profileでアプリを起動
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stop.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start.ps1 -PortablePostgres -Qa
Push-Location frontend
node qa/seed-images.mjs
npm test
Pop-Location
# 空・1件状態と通信失敗を独立ローカルDBで検査（通常DBを変更しない）
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/qa-states.ps1
```

`-Qa` は `dev,test` Profileです。通常の `dev` 起動ではRate LimitとCleanupが有効です。検査終了後は停止して `-Qa` なしで再起動してください。

通常Devへ戻した後の接続・Gallery Keyboard・Collection Focus Return・PL Tab・ComboBox KeyboardのSmokeは、`frontend`で `node qa/smoke.mjs` を実行します。名前やCookieの属性だけを出力し、Session Cookie値は出力しません。

ファイルから開く入口の検査は、アプリ起動中に `frontend` で `node qa/launcher.mjs` を実行します。`file://`の`index.html`、Desktop/Mobile表示、Keyboard、実ログイン、Importへの移動を確認します。業務Dataの追加・途中保存の破棄はしません。

Backend結果: `backend/target/surefire-reports/`。Browser結果: `.runtime/playwright-report/index.html / playwright-results.json`、状態検査: `.runtime/states-results.json`、画面: `.runtime/qa/`。失敗時のTraceは `.runtime/playwright-output/` と `.runtime/states-output/`。通常BrowserのQAは13 Viewport、767/768・1199/1200境界、Low-height、PL 0〜8とfirst/middle/lastを含みます。

Dev Seedの卓はPL0〜7を含み、PL8はBrowser QAが実APIで専用卓を追加します。PC未登録や画像なしのケースも保持して検査します。Seed MigrationのChecksumを書き換えて既存DBを再利用することはありません。

`qa-states.ps1` は日付付きの専用PostgreSQL DBを作成し、8081／5174で検査後に専用アプリを停止します。DB名を出力し、証拠用Dataを残します。通常の `takukairo` DBは変更しません。Docker構成でこの追加検査を行う場合もPostgreSQL client（psql / createdb）が必要です。

## ImportのQA途中保存

登録可能・確認待ち・修正必須の3状態、完了直前のSessionを、実APIで作成できます。既存の途中保存があれば通常は保全して終了します。破棄を明示する場合だけ `--discard-current` を付けます。Browserテストと同時に実行しないでください。

```powershell
Set-ExecutionPolicy -Scope Process Bypass
. .\scripts\env.ps1
Push-Location frontend
node qa/import-fixtures.mjs review
# node qa/import-fixtures.mjs almost-complete --discard-current
# node qa/import-fixtures.mjs none --discard-current
Pop-Location
```

<http://localhost:5173/import> の「続きから再開」で確認します。`almost-complete` は1候補がREGISTERED、2候補がEXCLUDEDの完了直前です。登録済み候補の変更やSource再解析はBackendが禁止します。

## Production Deferred・非実装項目

Google実ユーザー認証運用・OAuth verification、Render deploy、R2実Credential、PITRと旧Object保持、Account削除Tombstone、Production log retention、Terms／Privacy正式本文、外部画像のProduction権利判断、vips-ffm最終採用はProduction Deferredです。通常版バックエンドの外部公開・Production deployは行いません。上記のブラウザ体験版は、認証・DBを使わない独立した静的ビルドです。

UC15一括編集・UC16卓メモ・UC18共有、Activity Export、Offline編集／Snapshot、Native App、Push／Background Sync、PDF Import、One Tap、Auto Select、Person専用管理画面、SKP Role、PC current値／単一game_system、EndPcState最大値、PL変更内のPC Detach、Undo／Trash、AI Import必須化は追加していません。
