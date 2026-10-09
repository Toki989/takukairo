# R5.1.12 実装・検証記録

正本: `卓回廊_Codex実装正本_2026-10-08_R5.1.12_最終Contract補強版.md`。Part A > B/C。D/EのPASSは実装テストのPASSではない。

初期状態: 既存アプリコードなし、Git差分なし。参照資料、Prototype、画像assetsは保全。適用AGENTS.mdなし（workspace配下およびC:/、C:/Srcで確認）。

## 作業チェックリスト

- [x] 正本全文の読解（UTF-8、10588行）、適用AGENTS確認
- [x] 既存ファイル・Git・実行環境確認
- [x] 指定系列Stable版固定・Wrapper・package-lock
- [x] PostgreSQL / Flyway / Formal 9 Entity / Support Structure / Hibernate validate
- [x] Dev Session / Cookie / fresh CSRF / Logout / Setup Gate
- [x] Ownership / Membership / Optimistic Lock / concurrent mutation
- [x] Scenario / Favorite / BOOTH Mock / duplicate candidates / search / filters
- [x] Person / Recent / Impact / PC / Settings / change-person
- [x] Table Aggregate / EndPcState / Previous / Spotlight / Activity
- [x] 画像検証・Local Storage・安全な差し替え・Cleanup Retry
- [x] Import INPUT / parser / REVIEW / DETAIL / Autosave / Resume
- [x] Resolution / Bulk / Split / Merge / Preview・Apply競合
- [x] Partial Register / 二重登録防止 / REGISTERED lock / Complete / Cleanup
- [x] FILE Atomic Replacement / Business limits / outcome unknown recovery
- [x] CCFOLIA任意補助 / Tekey・Udonarium Stub
- [x] Rate limit / Structured logging / Common Error
- [x] React Routing / API Client / common UI / form focus / keyboard
- [x] v5.9 3レーン / Mobile・Tablet / QA seed・viewport（Chromium）
- [x] Backend Build・Test / Frontend typecheck・build / Browser主要Flow
- [x] README / env example / 起動停止・再検証手順

チェックはローカル／テスト実装範囲についての記録。実機・Production・未実行検査の範囲は末尾で区別する。MVP UC14件の実装経路とMethod / Path / DTO / Error / Versionの照合は [API_CONTRACT.md](API_CONTRACT.md)。Formal Entity9件とSupport Structureの内訳・固定Version・操作手順は [README](../README.md)。

## 実行した検査と証拠

### 2026-10-09 ローカルファイルからの確認用入口

ユーザーの追加希望「保存・Importなど実機能も確認したい（ローカルサーバーを簡単に起動する入口）」に従い、ルートの `index.html`、`起動.cmd / 停止.cmd`、`scripts/open-local.ps1 / close-local.ps1`を追加。HTMLはブラウザからローカルアプリを開く入口であり、業務機能をFrontend Mockへ置換していない。Formal Entity／Field／DTO／Endpoint／UC／Business Ruleの追加はない。

起動済みアプリの再利用と二重起動防止、停止状態からの起動、実cmdによる起動・停止、繰り返し停止、別Workspaceでポートが使用中の場合の拒否と既存プロセス保持を検証した。停止・再起動の前後でScenario／PC／Table／Person／ImportSession／ImportSource／ImportCandidate件数の一致を確認。Spring Sessionはログイン時に変わるため件数比較の対象にしない。初回起動検査で終了コード取得により誤った失敗表示が出たため、Windows PowerShellのプロセスHandle保持で修正し再検証した。

`frontend/qa/launcher.mjs`で `file://` の入口HTMLを実Chromiumで開き、1440×900／390×844、Page横Scrollなし、Keyboard Enter、実Dev Login、既存Import画面への移動、Browser実行Error0を確認。画像も目視確認した。証拠：`.runtime/launcher-browser.log`、`.runtime/launcher-cold-cmd.log`、`.runtime/launcher-stop-cmd.log`、`.runtime/launcher-stop-cmd-again.log`、`.runtime/launcher-guard.log`、`.runtime/launcher-preservation-before.txt / after.txt`、`.runtime/qa/local-entry-*.png`。アプリは通常Devで起動中。

2026-10-09の再開後、通常Dev（`test`なし）で最新jar／Frontendを起動し、最終Smokeを完了した。Cookie属性、Home Gallery Keyboard、Collection Focus Return、PL Tab Home/End/Arrow、ComboBox Arrow/Enter/Escapeが成功、Browser実行Errorは0。Selector画像のロード完了を待って撮影し、Desktop3レーンを目視確認した。証拠は `.runtime/final-dev-smoke.log`、`.runtime/qa/detail-final-dev.png`、`.runtime/resume-start-dev.log`。Rate Limit・Cleanupは通常Dev設定で有効。既存DB・Storage・Import途中保存は保持し、確認用Sessionの強制差し替えはしていない。

下表の自動検査日：2026-10-08（JST）。実行環境はWindows、PostgreSQL 18.6、Temurin 25.0.4.1+1、Chromium 156 / Playwright 1.64.0。H2による代替検査は行っていない。再開後はApplication Sourceに変更なく、起動Buildと通常DevのSmokeを実行した。Smokeの撮影待機だけを補強した。

| 検査 | 結果・証拠 |
|---|---|
| Backend `mvn test` | 85件、失敗0・Error0・Skip0。15:50:23 BUILD SUCCESS。`backend/target/surefire-reports/`、`.runtime/backend-test.log` |
| Suite内訳 | ContractIntegrationTest 71、LimitsParserImageTest 9、RateLimitTest 3、DevProfileGuardTest 1、ResolverConfigTest 1 |
| Flyway 0→latest / Hibernate validate | 独立Testcontainers PostgreSQL 18.6で空DBからMain Migration V1/V2/V3を適用しvalidate成功。各Integration Testの初期DataはV1000 SQLを投入。通常Dev DBはFlywayでV1000も適用。空状態用独立DBはMain V1/V2/V3のみ |
| Backend package | `scripts/start.ps1`で最新Sourceのjar生成成功。`.runtime/final-start-qa.log` |
| Maven Wrapper | 3.9.16配布取得・SHA256検証・Java25で`--version`成功。`.runtime/wrapper-version.log` |
| Frontend typecheck / Build | `npm run typecheck`、`npm run build`成功。`.runtime/final-typecheck.log`、`.runtime/final-frontend-build.log` |
| Keyboard Smoke | `frontend/qa/smoke.mjs`でHome GalleryのFocus/Enter、CollectionへのFocus Return、PL Tab Home/End/Arrow、ComboBox Arrow/Enter/Escape成功。`.runtime/qa-keyboard-smoke.log` |
| 通常Browser | 27件、失敗0・Skip0・Flaky0。16:27:52開始、92.0秒。`.runtime/playwright-results.json`、HTMLは `.runtime/playwright-report/index.html`。検査ケースは `frontend/qa/*.spec.ts` |
| 空・1件／通信失敗Browser | 2件、失敗0・Skip0。16:29:14開始、15.7秒。専用DB `takukairo_qa_20261008162839`、通常DBの変更なし。`.runtime/states-results.json`、`.runtime/test-states.log` |
| 起動・停止 | portable PostgreSQL／Backend／Viteを実起動・停止し、Data保持・API Ready・PID確認を検証。`scripts/start.ps1 / stop.ps1` |
| Assets・Git | 元の仕様書・資料・Prototype・画像に変更なし。実装は新規ファイル。`.tools`と`.runtime`はGit対象外 |

Backendの実Test Method単位の一覧とBrowserの各結果は [QA_RESULTS.json](QA_RESULTS.json) にも保存する。新規の画像応答検査の初回実行はテストの期待URL誤りで失敗したため、APIの実際の `/api/dev-images/{token}` に修正して再実行した。人数別撮影で取得前の画面が混ざったため、実GETの完了・PL人数・画像ロードを待って撮影し、各人数のmiddle/last選択を追加検証した。修正前の撮影を人数別QAの証拠には使用しない。

## 重点Contractの検証内容

| 対象 | 実行内容 |
|---|---|
| Security | 固定Dev Login、Session ID変更、旧CSRF拒否、Fresh CSRF、Logout、Cookie HttpOnly/Lax、Setup Gate、他User・親子所属の拒否。Dev以外の固定Sessionは禁止 |
| DB・Version | 所有者付きFK、Deferred reorder、NULL canonical非Deduplicate、同一PCの非NULL canonical重複拒否、Quote TEXT、PCなしState禁止、同時Mutation／Cross-Table競合All Rollback |
| Table | PC A→B／解除で旧EndPcState削除、新State Atomic保存、PL→KP、Children所属、Parent Version、保存済みProfile保持、Quote Grapheme、Previousの日付・Fallback・Current除外・exact Profile、Touched／保存済み値保持 |
| Library | CRUD・削除条件、重複名許可、Search AND・role OR・Favorite、Recent／Impact distinct、Activity、PC Settings省略／[]／full list、現在PL変更と選択した過去Participationのみ更新 |
| Profiles・Character | 3 Active Profile、Generic Schema、Emoklore47 Option、EXACT／明示Alias SUGGESTED／CoC UNKNOWN、Inactive維持・Migration、CoC6/7／Emoklore Mapping、Memo NFKC、警告・Mismatch・Ambiguity、Raw非保存、2MiB UTF-8・Escape worst-case・413分離、Stub範囲 |
| Import | 各Parser、Source変更Analysis Reset、共通Session Lock、Candidate Version、Resolution所属／共有・Reload、Partial Register・二重登録防止・登録後Source/Candidate Lock、Complete条件、Expiry/Cleanup |
| Preview | Bulk Apply/Clear/Exclude/Include、Split未Mappingと明示Binding、Merge判断競合・Discard・Target不一致null・一致false継承、Duplicate Participant Key、古いPreview RequestとApply SnapshotのError優先順位 |
| FILE・Limits | 20件満杯時の1FILE差し替え、50MiBちょうどのcurrent−old+new、ID/order保持、失敗時旧Data／Analysis保持、複数fieldErrors、HTTP Transport 413との区別、Storage Cleanup失敗・Retry・DEAD |
| Image | 実Format判定、サイズ・pixels・animation制限、早期Versionと更新前再確認、Metadata保持・画像のみRetry、Old Object保持／Compensation Queue、Transform Keyboard・Clamp・Position reset、Touch Pinch |
| Outcome Unknown | Browserの実PUTをBackendでCommit後に応答だけ切断。Draft/File保持、PUT1回、自動再送なし、既存GET再同期、Escape後もFile保持を検証 |
| Frontend | 手入力だけでScenario→卓→新規PC→終了時状態まで実保存、Draft保持、Autosave失敗と明示再開、遷移前Flush、Logout取消、Dialog Focus Trap／Escape復帰、SummaryのField Focus、Session再Login・413／429／Offline表示 |

通信Error／Catalog空／Inactive Profileの一部UIケースは明示的なHTTP応答Stubを使用する。主要の業務作成・照合・登録・差し替え・競合は実Backend／DBへ接続して検証している。Rate LimitのBoundaryは専用RateLimitTestで検査し、Browser大量実行時のみ `dev,test` を使う。

最終Smokeで判明したCollectionへのFocus復帰不良は、API QueryからUI専用の選択状態`pc`を除き、CollectionのDOM表示後に元PCへFocusを戻すことで修正した。実DevでRate Limitが作動した際の初期Session取得失敗も、永続Error表示とUser明示のCSRF／Session再取得へ修正した。Mutationの自動再送は追加していない。初期Session429→明示再取得のBrowserケースを追加している。

## Visual・Interaction確認

`TRPG_PCFocus_v5_9`のHTML/CSSとScreenshotを参照し、DesktopのPortrait／中央情報／斜めSelectorの3レーンをReactに移植。既存背景assetsを参照し、Tailwindや大型UI Frameworkへ置換していない。Formalの実Dataと画像を表示するため、参照Mockの値・人数・画像内容の完全一致は要求していない。

Chromiumで13条件を表示・撮影し、画像を目視確認した：390×844、767×844、768×1024、820×1180、1024×768、1199×800、1200×800、1366×768、1440×900、1672×941、1680×800、1920×1080、1440×600。境界767/768・1199/1200で、Mobile `Portrait→Selector→情報`、Tablet上段Portrait/Selector＋下段情報、Desktop3レーンを確認。意図しないPage横ScrollはDOM寸法でも検査している。

PL0〜8、first/middle/last、画像・EndPcStateなし、空／1件Collection、Import3状態／完了直前を撮影。Low-heightでは長い成長記録の中央Scrollと固定Navigation、Keyboard Home/End・Focus、Reduced Motion、GhostのFocusと文字コントラストを検査。Mobile Swipeの1名単位移動・縦Scrollで不変・画像PinchはChromiumのTouch入力で検証した。Screenshotは `.runtime/qa/`。

## 未検証・環境制約と再実行

- ローカル／テスト対象について既知の未実装機能はない。Tekey／Udonariumは仕様上のStubであり、Concrete Parser実装済みとは表示しない。
- iOS／Android物理端末、Safari／Firefox、実Screen Readerでの読み上げは未実行。このWindows環境に対象端末・Browser／支援技術がないため、ChromiumでのViewport／Touch／ARIA／Focus確認と区別する。対象端末から同等のローカルテスト環境を開き、仕様§47のKeyboard・Touch・Focus Return・Reduced Motionと上記Viewportを再確認する。
- Production CookieのSecure／Prefixは構成・Security Testの対象。HTTP Dev BrowserでProduction認証・HTTPS運用を実行したとは扱わない。
- Google実認証、R2、Render、実BOOTH接続、Productionの負荷・運用・権利判断は実行していない。ユーザー指定に従って外部公開しない。
- 正本§61.4の旧R1原文の完全一致照合は非Blockerの資料履歴残件。解消済みの6系統TODOをHOLDへ戻していない。

再検証の具体コマンド・環境変数・専用DB・Dev Login・起動停止はREADMEの「検査」「初回セットアップ・起動」に記載。自動検査は固定Dev UserにテストDataを追加するため、必要な途中保存がある場合は別DBで実行する。

## Production Deferred

Google実ユーザー認証運用、R2実Credential、Render deploy、PITRと旧Object保持、Account削除Tombstone、Production log retention、Terms/Privacy正式本文、外部画像Production権利判断、vips-ffm最終採用。外部公開しない。
