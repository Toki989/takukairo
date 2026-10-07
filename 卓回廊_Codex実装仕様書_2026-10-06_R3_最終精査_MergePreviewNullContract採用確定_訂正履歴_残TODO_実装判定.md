# 卓回廊 Codex実装仕様書
## 2026-10-06 横断精査・訂正履歴・残TODO・実装判定 R3 / Merge Preview Null Contract採用確定

初回改訂記録時刻：`2026-10-06T10:34:09+09:00`（Asia/Tokyo）

R2追補記録時刻：`2026-10-06T10:49:25+09:00`（Asia/Tokyo）

R3追補記録時刻：`2026-10-06T11:32:57+09:00`（Asia/Tokyo）

---

# 1. この文書の目的

本書は、2026-10-01の「再改訂版」Frontend / Backend実装仕様書を土台として、ユーザー監査で残った8項目と、その横断確認中に追加で発見した不整合を整理した監査記録である。

本書では、以下を混同しない。

- 【確定・復元】：既存確定資料へ直接根拠がある内容の再転記
- 【一旦採用】：2026-10-06の三視点横断判断で、実装Contractとして具体化した新規判断
- 【未確定 / TODO_SPEC_CONFIRMATION】：資料から確定できず、ユーザー判断が必要な内容
- 【資料照合残件】：実装仕様自体ではなく、過去文書の版・履歴を完全照合するための残件

最重要原則は維持する。

> **見た目はv5.9。中身は既存の確定済み設計。**

---

# 2. 直接比較元

今回の直接比較元は9/30版ではなく、10/01再改訂版である。

## Frontend比較元

`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-01_再改訂版.md`

SHA-256：

`0fa1d087a5236ab60e283029788bf82f7625b119587f94ce14556ac0c47af512`

## Backend比較元

`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-01_再改訂版.md`

SHA-256：

`1a28ed9e66200d924049b0eca183989d78bb94fbcc559a7e18ed208f8704f01b`

9/30版・R1・各確定資料は、10/01再改訂版から落ちた既存確定事項を検出・復元するための参照資料として使用した。

---

# 3. 今回作成した修正版

## Frontend

`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版.md`

- 行数：3999
- SHA-256：`ef59d69d72b4a583440a29d53cbad254465dbc08a0a5faccb779c388d13b2282`
- 10/01再改訂版との差分：`+626 / -141`

## Backend

`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版.md`

- 行数：3501
- SHA-256：`6ffeb1c67109fe49cc5de30de0fe70ec1dad8cf06820eaefb418a08e21fd4cd0`
- 10/01再改訂版との差分：`+552 / -118`

---

# 4. ユーザー監査8項目への対応

| No. | 監査指摘 | 今回の状態 | 採用区分 |
|---|---|---|---|
| 1 | Import Registerが`candidateIds`だけで、確認版を固定できない | `expectedSessionVersion + candidateId + expectedVersion`へ変更。Register前Autosave Barrierを追加。全Candidate Versionを処理前検証し、1件でも不一致なら登録0件で409 | 【一旦採用】 |
| 2 | 部分登録後のRe-Analyze、Register/Reset並行競合が未定義 | REGISTERED 1件以降のRe-Analyze禁止。全Import mutationにSession Row Lock + expectedSessionVersionを適用 | 【一旦採用】 |
| 3 | OwnershipだけでResource Membershipが不足 | TableDate→Table、Participation→Table/PC、EndPcState→Participation、System Setting→PC、Import child→Session等の所属検証を追加 | 【一旦採用】 |
| 4 | Profile定義が概念のみ | Generic JSON Schema、Field、SELECT OptionSource、REPEATER、Validationを具体化。ただしMVP初期Active Profile集合は資料から確定不能 | Schema：【一旦採用】 / 初期集合：【未確定】 |
| 5 | Appearances録画導線が原資料と違う | 最新5件、`すべて表示（N）`、`表示を減らす`、Row→卓詳細、`▶ 振り返る`→直接録画URLへ復元 | 【確定・復元】 |
| 6 | R1系UI再転記漏れ | PC画像1%/5%/Clamp、Hamburger、Ghost Slot、ARIA、Date Popover、PART、外部画像候補保持/初回同意を復元 | 【確定・復元】 |
| 7 | Candidate / Resolution / Register Result / Image HTTP Contract不足 | CandidateData Shape、Resolution Slot、Result、Image multipart field、Transform JSON、DELETE query versionを固定 | 【一旦採用】 |
| 8 | 修正日時・訂正履歴不足 | 2冊へ比較元Hash、改訂日時、旧→新→根拠→採用区分→修正日時のAppendixを追加 | 【文書運用修正】 |

---

# 5. 横断精査中に追加で発見・修正した事項

## 5.1 External Image同意のScope【確定・復元】

`TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md` §5-6 とPhysical Designを再確認した結果、以下は既に確定していた。

- 同意は「外部画像候補をScenario画像として正式採用する最初の1回」
- User単位で1回。Scenarioごとには繰り返さない
- 未採用候補はScenario Formの一時Stateだけ
- Page離脱 / Reload / Cancelで破棄
- Scenario正式Data / DB Draft / localStorage / IndexedDBへ保存しない
- Userには`external_image_consent_at`が存在

したがって、以前残していた「外部画像初回同意の記録Scope」はTODOから除外した。

HTTP連携だけは従来未定義だったため、2026-10-06一旦採用として次を追加した。

- `GET /api/session` → `user.externalImageConsentGiven`
- 未同意Userが外部画像を正式採用してScenario保存 → `externalImageConsentAccepted: true`
- Scenario保存と`external_image_consent_at`設定を同一Transaction
- command-only fieldでありScenario属性にしない

## 5.2 Preview → ApplyのSession Version【一旦採用】

Bulk / Split / MergeのPreviewに`expectedSessionVersion`がある一方、Apply側が「同じRequest」とだけ書かれていた。

修正後はApplyにも明示的に、

- `expectedSessionVersion`
- Candidate Version snapshot
- `previewRevision`

を要求する。

## 5.3 Register Candidate Version競合の扱い【一旦採用】

旧記述ではCandidate Version競合を「Candidate別FAILEDまたは409」と読める余地があった。

修正後：

- Session Version不一致 → `409 OPTIMISTIC_LOCK_CONFLICT`
- Candidate Version不一致 → `409 IMPORT_CANDIDATE_VERSION_CONFLICT`
- いずれも処理開始前に全件検証し、1件でも不一致なら**登録0件**
- Candidate別`FAILED`はBusiness Validation Failureだけ

これにより、「確認していない最新版が一部だけ登録される」余地を閉じた。

---

# 6. 三視点評価

## 6.1 Import Register Version / Session Lock

### Webクリエイター

ユーザーが確認したCandidateと、実際に登録されるCandidateを一致させることが最優先。Autosave中・失敗中にRegister Buttonを有効化しないため、UI上も「確認した内容と違うものが登録された」という破綻を防げる。

### マーケター

Importは「既存情報を再入力させない」というサービス価値の中心。ここで誤登録・二重登録・確認版ずれが起きるとサービス全体への信頼が大きく落ちる。多少内部Contractが複雑でも、ユーザー側の追加入力を増やさず安全性を上げる方が価値が高い。

### エンジニア

Candidate optimistic lockだけではReset / Analyze / Registerという別Request同士のraceを閉じられない。ImportSession row lock + expectedSessionVersionを共通mutation boundaryにすることで、条件判定からCommitまでを直列化できる。

### 総合判断

**一旦採用が妥当。**

---

## 6.2 Resource Membership Validation

### Webクリエイター

通常の正規UIではほぼ見えないが、壊れた関係を保存させないことで、後から「別卓のPC状態が混ざる」等の理解不能な画面崩壊を防ぐ。

### マーケター

履歴サービスではDataの信用が継続利用に直結する。同一User所有だから許可するのではなく、対象Aggregate配下かまで保証する必要がある。

### エンジニア

OwnershipとMembershipは別問題。同User内の別Table / PC / Session IDをRequestへ混ぜる攻撃・バグはUser ownershipだけでは排除できない。Parent relationを検証し、version checkを所属確認の代替にしない。

### 総合判断

**一旦採用が妥当。**

---

## 6.3 Game System Profile Generic Schema

### Webクリエイター

Profile定義からFormを生成できれば、Game SystemごとにUI Componentを分岐せず、同じVisual ruleで入力体験を維持できる。

### マーケター

対応System追加のたびに画面を作り直さなくてよく、将来拡張性が高い。一方、MVP初期対応Systemを資料なしで増やすのは対象範囲を不必要に拡大するため避けるべき。

### エンジニア

Field key / input type / OptionSource revision / REPEATER itemFields / Validationを同じConfigからFrontend RendererとBackend Validatorが参照できる形が必要。`coc_7e_v1`等の例を「初期対応一覧」と誤認しない。

### 総合判断

**Generic Schemaは一旦採用。MVP初期Active Profile集合はTODOのまま。**

---

## 6.4 Image Mutation HTTP Contract

### Webクリエイター

Upload / Transformをやり直す事態を避け、保存失敗時にも既存画像を維持できることが重要。

### マーケター

画像はPC/Scenarioの体験価値が高い。失敗時の再入力や画像消失は体感品質を大きく下げる。

### エンジニア

Multipart field、CSRF Header、expectedVersion、Transform JSON、DELETE query locationを固定しないとFrontend / Backendを独立実装できない。Safe Replace sequenceと組み合わせてAtomicityを保つ。

### 総合判断

**一旦採用が妥当。**

---

## 6.5 External Image Consent HTTP連携

### Webクリエイター

候補取得時ではなく、実際に使う瞬間だけ説明することで入力を邪魔しない。既同意Userへ繰り返さない。

### マーケター

同意DialogをScenarioごとに出すと登録体験を損なう。既存確定どおりUser初回1回が妥当。

### エンジニア

別Consent APIを先に成功させてScenario保存だけ失敗すると状態が分離する。Scenario Saveのcommand-only fieldとして受け、`external_image_consent_at`とScenarioを同一Transactionにする方が整合性を保ちやすい。

### 総合判断

User初回同意自体は**確定・復元**。HTTP連携方法だけ**一旦採用**。

---

# 7. 現在のTODO_SPEC_CONFIRMATION

実装仕様上の主なTODOは現在6系統。

1. **Game System ProfileのMVP初期Active Profile集合 / 各Profile実データ**
   - Generic Schema / Renderer / Validator Contractは一旦採用済み。
   - `coc_7e_v1`は資料上の例 / Test Fixtureとして扱い、MVP全対応System一覧とはみなさない。
2. **Previous EndPcState候補取得APIの最終Endpoint / Request Shape**
   - Cutoff、Current Table除外、exact profileKey、Low Confidence Ruleは確定済み。
3. **CCFOLIA Mapping Configの最終Field Mapping**
4. **CCFOLIA Preview APIでprofileKey / resolved canonical systemをどう渡すか**
5. **Tekey / Udonarium共通Adapterの具体Contract**
6. **CCFOLIA `iconUrl`をPC画像として利用する正式仕様**

Import Register version / Re-Analyze / Session mutation lock / Resource Membership / CandidateData & Resolution DTO / Image HTTP Contractは「未記述」状態からは解消した。ただし、今回新しく具体化した部分は**ユーザー明示承認前に既存確定仕様扱いしない**。

---

# 8. 資料照合残件【実装Blockerではない】

ユーザー監査は、9/30 Frontend Codexの2708行版・R1訂正履歴を参照している。

今回の作業環境で直接確認できる同名9/30 Frontend Codexは2561行版であり、R1本文そのものと完全一致しない。

ただし、監査で指摘されたR1系挙動のうち今回復元した主要項目は、`VisualResponsiveAccessibility仕上げフェーズ`および`BackendDataSecurityPlatform_精査修正版`等の9/30確定資料でも根拠を確認できた。

したがって、

- **実装仕様の挙動復元は進めてよい**
- **2708行版R1の歴史的文言・訂正行を一字一句継承したとは主張しない**

とする。

2708行版R1が将来利用可能になった場合は、挙動を再決定せず、文書履歴だけを照合する。

---

# 9. 機械検査結果

最終検査対象：2026-10-06横断精査修正版2冊。

## Markdown

- Frontend code fence：282 / 偶数・閉じ忘れなし
- Backend code fence：270 / 偶数・閉じ忘れなし

## Frontend ↔ Backend Endpoint

簡易Parserによる明示Method + `/api/**`抽出：

- Frontend：73参照 / 40 unique
- Backend：102参照 / 77 unique
- FrontendにありBackendへ存在しないMethod + Endpoint：**0件**
- Queryを除いたPath正規化後の不足：**0件**

この検査はDTO・状態遷移・Transaction意味の一致を保証しないため、今回の手動横断監査と併用した。

## 旧記述Scan

以下の旧Conflict文字列は残っていないことを確認した。

- Register Requestの`candidateIds` JSON
- Appearance → 卓詳細 → 録画という旧導線
- 再折りたたみ任意表現
- `/image/content`という未定義URL
- `__Host-` Cookieを`Secure=false`で使う記述
- Candidate Version競合を「FAILEDでも409でもよい」とする曖昧表現

---

# 10. Security公式情報との照合

今回のCookie / CSRF修正は、次の公式仕様と整合することを再確認した。

## Cookie

`__Host-` prefixは、

- `Secure`
- `Path=/`
- `Domain`なし

が必要。そのためHTTP Local Dev/TestではProductionの`__Host-TAKUKAIRO_SESSION`を流用せず、別Cookie名を使用する現在仕様を維持する。

## Spring Security CSRF

SPAではAuthentication success / Logout success後に以前のCSRF tokenがclearされるため、新しいTokenを取得する必要がある。またJavaScriptを利用するMultipart RequestではCSRF TokenをHeaderへ含める方式が推奨される。

現在のFrontend / Backend仕様の、

- Login前`GET /api/csrf`
- Login成功後再取得
- Logout成功後再取得
- Multipart Image UploadへCSRF Header

を維持する。

---

# 11. 最終実装判定

## A. 基盤実装

> **GO：開始可**

対象例：

- Project skeleton
- Core 9 Entity schema / repository base
- Auth / Session / CSRF基盤
- Core CRUDの確定部分
- Responsive / Accessibility基盤
- v5.9 Visual構造
- Common Error処理

## B. 既存確定事項として復元済みの機能

> **GO：実装可**

例：

- Recording URLなし時Action非表示
- Mobile卓詳細順序
- Appearances 5件 / 展開 / 折りたたみ / 録画Secondary Action
- PL変更初期全件ON
- PC保存起点別Navigation / Draft保持 / Delete条件
- PC画像Keyboard / Clamp / Reset
- Hamburger / Ghost Slot / ARIA / Date Popover / PART
- External Image候補Draft保持 / User初回同意の業務Rule
- Cookie / CSRF確定部分

## C. 2026-10-06に具体化した一旦採用Contract

> **実装候補：ユーザー承認後に正式固定して実装するのが安全**

対象：

- Import Session Row Lock + expectedSessionVersion
- Register candidate expectedVersion / Autosave Barrier
- Register Version Conflict全体409
- Re-Analyze禁止
- Resource Membership Validation
- Generic Profile Schema / Validation
- CandidateData / Resolution DTO
- Image Mutation HTTP DTO
- External Image Consent HTTP連携
- 未命名卓`createdAt ASC → id ASC`採番

これらは「未定義のまま実装者へ投げる」状態からは脱したが、既存資料の確定事項ではないためユーザー承認なしで【確定】へ昇格させない。

## D. TODO依存機能

> **HOLD**

第7章の6系統が必要な箇所は、インターフェース / TODOまでに留める。

## E. 全体完成判定

> **HOLD**

現在の最終判断は、

> **「基盤実装開始可。既存確定事項は実装可。2026-10-06の新Contractは承認待ち。TODO依存機能と全体完成判定は保留。」**

とする。


---

# 12. 2026-10-06 10:45再監査へのR2対応

修正日時：`2026-10-06T10:49:25+09:00`。

今回の直接土台は10/06「横断精査修正版」2冊。過去仕様へ戻さず、監査で残った5項目だけを追加修正した。

| No. | 再監査指摘 | R2対応 | 採用区分 |
|---|---|---|---|
| R2-1 | ResolutionがID参照だけでReload復元不能 | Candidate GETへ`resolutions[]`同梱、Session Resolution一覧GET、entity別`draftData`、`createdEntityId`再利用、明示`useResolutionId`を追加 | 【確定・2026-10-06 11:02採用】 |
| R2-2 | Split/MergeでResolution・確認状態・登録対象の引継ぎ未定義 | Splitは明示`resolutionBindings`、未MappingならApply不可。MergeはResolution conflict / discard / registrationTarget conflictをPreviewし、TargetはUNCHECKEDへ | 【確定・2026-10-06 11:02採用】 |
| R2-3 | Preview staleと汎用Optimistic Lockが衝突 | Preview作成Requestの古いVersion=`OPTIMISTIC_LOCK_CONFLICT`、成功Preview後ApplyのSnapshot変化=`IMPORT_PREVIEW_STALE`を優先 | 【確定・2026-10-06 11:02採用】 |
| R2-4 | PC Error Summary、成長・後遺症の既存確定UI欠落 | VisualResponsive No.63 / No.49を原文に沿って復元 | 【確定・復元】 |
| R2-5 | 外部画像R1照合が機能TODOに混入 | 機能仕様は確定済みとしてHOLDから除外。R1完全照合だけを資料照合残件・非Blockerへ移動 | 【分類修正】 |

# 13. R2三視点評価

## 13.1 Resolution再開復元 / Split / Merge

### Webクリエイター
Reload後も「再利用する／新規作成する／どのEntityを選んだか」が画面へ戻ることを優先する。Split / Merge時はPreviewでResolutionの引継ぎ・衝突・破棄を見せ、暗黙変更を禁止する。

### マーケター
卓回廊の中心価値「同じ情報を何度も入力させない」に対し、途中再開で照合判断が消える、同じScenarioを再作成する挙動は重大な逆行。共有Resolutionと`createdEntityId`再利用を維持する価値が高い。

### エンジニア
ImportResolutionをSession-scoped Support DataとしてCandidateから参照し、同一Resolution IDだけを共有Identityにする。Split / MergeはTarget Slot→Resolution IDを明示し、Temporary Keyや文字列一致で暗黙推測しない。

### 総合判断
**2026-10-06 11:02 JSTにユーザーが明示採用。正式仕様として確定。**

## 13.2 Preview Error優先順位

### Webクリエイター
「Preview内容が古くなった」場合は再Previewを案内できる専用Errorの方が操作回復が明確。

### マーケター
一般的な競合表示より、ユーザーの次行動が分かることでImport離脱を減らせる。

### エンジニア
Preview作成前のVersion不一致と、Preview成功後のSnapshot失効は意味が異なる。Apply Endpointで`IMPORT_PREVIEW_STALE`を優先することでProtocolが一意になる。

### 総合判断
**2026-10-06 11:02 JSTにユーザーが明示採用。正式仕様として確定。**

# 14. R2後の残TODO / Blocker分類

## 機能TODO_SPEC_CONFIRMATION【HOLD】

1. Game System ProfileのMVP初期Active Profile集合 / 各Profile実データ
2. Previous EndPcState候補取得APIの最終Endpoint / Request Shape
3. CCFOLIA Mapping Configの最終Field Mapping
4. CCFOLIA Preview APIでProfile / Canonical System情報をどう渡すか
5. Tekey / Udonarium共通Adapterの具体Contract
6. CCFOLIA `iconUrl`のPC画像利用

## 資料照合残件【非Blocker】

- ユーザー監査が参照した9/30 Frontend 2708行R1版そのものとの全文一致照合。
- External Image初回同意はBackendDataSecurityPlatform §5-6とPhysical DesignでUser単位初回1回まで根拠確認済みのため、**機能HOLDへ戻さない**。R1原文が後日入手できた場合は文言履歴だけ照合する。

# 15. R2実装判定

- **基盤実装：GO**
- **既存確定・復元済みUI：GO**
- **R2のImport Resolution/Preview 4点：2026-10-06 11:02 JSTにユーザー明示採用済み・確定**
- **その他の10/06 R1/R2新規具体化Contract（Profile / Image HTTP / 未命名卓採番等）：一旦採用のまま。今回の採用対象外**
- **上記6系統TODO依存箇所：HOLD**
- **資料照合残件だけを理由に機能実装をHOLDしない**
- **全体完成判定：HOLD**

# Appendix B. R2訂正履歴

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| FE-R3-01 | Candidate DetailはResolution IDのみ | Resolution DTO同梱 + Session Resolution一覧GET + Reload復元 | 三視点再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-02 | Split/MergeはCandidate/Traceだけ | Resolution Binding / Conflict / Confirmation / Target StateまでPreview | 三視点再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-03 | PC Error Summaryが概略 | 1件Field Focus・Summaryなし / 2件以上Profile上部Summary Focus | VisualResponsive No.63 / 【確定・復元】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-04 | Growth/Aftereffects表示が弱表示のみ | 改行保持・pre-wrap・空欄非表示・全文表示/折りたたみ | VisualResponsive No.49 / 【確定・復元】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-05 | External Image R1照合を機能TODO扱い | 資料照合残件・非Blockerへ移動 | 既存確定資料再確認 / 【分類修正】 | `2026-10-06T10:49:25+09:00` |
| BE-R3-01 | ResolutionのReload Responseなし | Candidate `resolutions[]` + `/resolutions` GET + `useResolutionId` | 三視点再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| BE-R3-02 | Split/Merge Resolution継承なし | 明示Binding、Conflict、Unmapped、State Reset Rule | 三視点再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| BE-R3-03 | Preview staleと共通409の優先順位不明 | Preview Request stale=OPTIMISTIC、Apply snapshot stale=IMPORT_PREVIEW_STALE | 三視点再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |

# 16. Import Contract正式採用記録

採用日時：`2026-10-06T11:02:00+09:00`。

ユーザーの明示採用対象は、直前に説明した以下4点に限定する。

1. Resolution再開復元
2. Split時Resolution引継ぎ
3. Merge時Resolution衝突処理
4. Preview競合Error優先順位

これらは本日時点で **【確定】**。

一方、Profile Schema、Image HTTP Contract、未命名卓採番その他の10/06一旦採用事項は、この発言だけでは確定へ昇格させない。

## 採用履歴

| ID | 旧区分 | 新区分 | 採用根拠 | 採用日時 |
|---|---|---|---|---|
| ADOPT-IMP-01 | Resolution再開復元：一旦採用 | 確定 | ユーザー明示採用 | `2026-10-06T11:02:00+09:00` |
| ADOPT-IMP-02 | Split Resolution引継ぎ：一旦採用 | 確定 | ユーザー明示採用 | `2026-10-06T11:02:00+09:00` |
| ADOPT-IMP-03 | Merge Resolution衝突処理：一旦採用 | 確定 | ユーザー明示採用 | `2026-10-06T11:02:00+09:00` |
| ADOPT-IMP-04 | Preview Error優先順位：一旦採用 | 確定 | ユーザー明示採用 | `2026-10-06T11:02:00+09:00` |

# 17. R2 Artifact Integrity（Import Contract採用反映後）

## Frontend R2

- File：`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版_R2.md`
- 行数：4149
- SHA-256：`1a4aa77df4aedb95a37f4170c13ac3c11dfa7c5b485bbb293609d4d3e46ddfd7`
- 10/06 横断精査修正版との差分：`+178 / -28`

## Backend R2

- File：`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版_R2.md`
- 行数：3632
- SHA-256：`91b5194c25a2fdd14c8b10a3764dbbb7da46529d806f710c61bc1285437032b0`
- 10/06 横断精査修正版との差分：`+156 / -25`

## Endpoint照合

- Frontend Method + `/api/**` unique：41
- Backend Method + `/api/**` unique：73
- FrontendにありBackendに存在しないEndpoint：**0**

Endpoint一致だけではDTO意味の一致を保証しないため、Resolution DTO / Split / Merge / Preview Error優先順位は本文ContractとTest条件でも個別確認した。


## Import Contract採用反映版 Artifact Integrity

### Frontend
- File：`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版_R2_ImportContract確定版.md`
- 行数：4167
- SHA-256：`830330aff3bc109ea96748e1769e52cc1801300c32de168267257c1cadb18613`

### Backend
- File：`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版_R2_ImportContract確定版.md`
- 行数：3650
- SHA-256：`8bcb117f5667caa8996e1d97aff342f1aaf32c2a083c2ef86a803ccbbba0a38a`


# 18. 2026-10-06 最新再監査へのR3対応

修正日時：`2026-10-06T11:32:57+09:00`。

直接土台は`R2_ImportContract確定版`3資料。既に確定したR2 Import Contractを戻さず、今回残った2点だけを追補した。

| No. | 再監査指摘 | R3対応 | 採用区分 |
|---|---|---|---|
| R3-1 | 照合判断を未設定へ戻す送信方法がない | Candidate PATCHへ`clearResolution: true`、Bulkへ`CLEAR_RESOLUTION`を追加。Candidate Slot参照だけ解除し、共有Resolution本体/他Candidate/既作成Formal Entityは削除しない | 取消可能という業務Ruleは【確定・復元】、Wire Shapeも【確定・ユーザー採用】 |
| R3-2 | Mergeで`resultRegistrationTarget`省略時が曖昧 | Source全件一致なら共通値継承、不一致ならConflictで明示必須。Preview Responseは解決後Targetを返す | 【確定・ユーザー採用】 |

# 19. R3三視点評価

## 19.1 Resolution取消 Contract

### Webクリエイター
一括適用後にユーザーが判断を戻せる必要がある。取消で他Candidateの照合判断まで消えるのは予測不能なので、対象Slotだけ未設定へ戻す。Previewで対象件数と取消後状態を確認する。

### マーケター
誤った一括判断を正式登録前に修正できることは、Import離脱と再入力を減らす。共有判断そのものを削除すると別Candidateへ再入力が発生するため、参照解除が中心価値「何度も入力させない」と整合する。

### エンジニア
Candidate→ImportResolutionのBinding解除とImportResolution削除を分離する。`clearResolution` / `CLEAR_RESOLUTION`はFK/RefだけをNULL化し、Support Resolutionと`createdEntityId`は保持する。これにより部分登録済み共有Resolutionにも安全。

### 総合判断
業務要件「正式登録前なら取り消し可能」は既存確定。Wire ShapeはR3で具体化し、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】。

## 19.2 Merge `resultRegistrationTarget`省略 Rule

### Webクリエイター
全Sourceが同じ登録対象状態なのに毎回再選択させる必要はない。一方、不一致時の暗黙決定は避ける。

### マーケター
不要な再入力を減らしつつ、意味が分かれるケースだけユーザー判断を求めることでImport負担を抑える。

### エンジニア
全Source一致なら決定的に共通値を算出できる。不一致なら`registrationTargetConflict`でApplyを止める。Preview Revisionへ解決済み値を束縛することでApply時も一意になる。

### 総合判断
R3で具体化し、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】。

# 20. R3後の残TODO / Blocker分類

## 機能TODO_SPEC_CONFIRMATION【HOLD】

既存6系統を維持する。

1. Game System ProfileのMVP初期Active Profile集合 / 各Profile実データ
2. Previous EndPcState候補取得APIの最終Endpoint / Request Shape
3. CCFOLIA Mapping Configの最終Field Mapping
4. CCFOLIA Preview APIでProfile / Canonical System情報をどう渡すか
5. Tekey / Udonarium共通Adapterの具体Contract
6. CCFOLIA `iconUrl`のPC画像利用

## R3追加Import Contract【確定・GO】

ユーザーが`2026-10-06T11:41:14+09:00`に明示採用。

- `clearResolution: true` / Bulk `CLEAR_RESOLUTION`のWire Shape
- Merge `resultRegistrationTarget`省略時の共通値継承 Rule

## 資料照合残件【非Blocker】

R2までの分類を維持。資料照合残件だけを理由に機能実装をHOLDしない。

# 21. R3実装判定

- **基盤実装：GO**
- **既存確定・復元済みUI：GO**
- **R2で正式採用済みImport Contract 4点：GO**
- **「正式登録前なら一括適用結果を取り消し可能」という業務Rule：確定・GO**
- **R3の具体Wire Shape 2点：`2026-10-06T11:41:14+09:00`にユーザー明示採用済み・確定・GO**
- **既存6系統TODO依存箇所：HOLD**
- **全体完成判定：HOLD**

# Appendix E. R3訂正履歴

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| FE/BE-R4-01 | 一括適用後のResolution取消Wireなし | Slot参照解除`clearResolution` + Bulk `CLEAR_RESOLUTION` | 既存確定要件復元 + API具体化【確定・ユーザー採用】 | `2026-10-06T11:32:57+09:00` |
| FE/BE-R4-02 | Merge Target省略時不明 | 全Source一致なら継承、不一致なら明示必須 | 三視点Contract補完【確定・ユーザー採用】 | `2026-10-06T11:32:57+09:00` |


# 22. R3 Artifact Integrity（採用前Snapshot）

## Frontend R3

- File：`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版_R3.md`
- 行数：4208
- SHA-256：`4bbd216ec499c7028e9cd9b5f662d22cc3b7c46aaa77a175a1109c7ba889155b`
- R2 ImportContract確定版との差分：`+47 / -6`

## Backend R3

- File：`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-06_横断精査修正版_R3.md`
- 行数：3685
- SHA-256：`2d9aa53c8ecc7148a4e77439cc3159c7b589d05b2d529a239f9332bf21026e03`
- R2 ImportContract確定版との差分：`+40 / -5`

## Endpoint照合

- Frontend Method + `/api/**` unique：42
- Backend Method + `/api/**` unique：79
- FrontendにありBackendに存在しないEndpoint：**0**

Endpoint数だけでは今回の2件を検出できないため、`clearResolution` / `CLEAR_RESOLUTION`とMerge Target省略Ruleは本文ContractとQA/Test条件でも個別確認した。

## Markdown構造

- Frontend code fence：284（偶数・閉じ忘れなし）
- Backend code fence：276（偶数・閉じ忘れなし）


# 23. R3追加Import Contract 採用確定

採用日時：`2026-10-06T11:41:14+09:00`（Asia/Tokyo）

ユーザーの「採用でいいです」という明示判断により、次の2点を正式仕様へ昇格した。

| ID | 一旦採用 | 採用後 | 根拠 | 採用日時 |
|---|---|---|---|---|
| ADOPT-R3-01 | `clearResolution: true` / `CLEAR_RESOLUTION` | 【確定】Candidate Slot参照だけ解除。共有Resolution本体・他Candidate・既作成Formal Entityは維持 | ユーザー明示採用 | `2026-10-06T11:41:14+09:00` |
| ADOPT-R3-02 | Merge `resultRegistrationTarget`省略Rule | 【確定】Source全件一致なら共通値継承、不一致なら明示必須 | ユーザー明示採用 | `2026-10-06T11:41:14+09:00` |

この採用は上記2点だけを対象とする。Profile Schema、Image HTTP Contract、未命名卓採番その他の10/06一旦採用事項は今回の採用対象外。

## 採用後実装判定

- 基盤実装：GO
- 既存確定・復元済みUI：GO
- R2で確定済みImport Contract 4点：GO
- R3追加Import Contract 2点：**確定・GO**
- 既存6系統TODO依存箇所：HOLD
- 全体完成判定：HOLD

# 24. 採用確定版 Artifact Integrity

## Frontend
- 行数：4220
- SHA-256：`f82d7b18bc870dca9266ffe7f887d5fb1c50db4cc1178aeb5ea11bcfa64fc28a`

## Backend
- 行数：3697
- SHA-256：`ee569f3233ad5b37c6d838b4ca1abb0dd96636f2106fe838625ea223e1898caf`

## 訂正履歴・実装判定
- 行数：686（本節追記前）
- SHA-256：`c755b0f5a2db7c562dca6fc920d37d044f1634a986b7b025b8dc9b9c3eb54813`（本節追記前）

# 25. 2026-10-06 仕様書最終精査・修正フェーズ

修正日時：`2026-10-06 11:49 JST`。

直接監査対象はR3追加ImportContract確定版3資料。9/30版・10/1版は直接修正元にしていない。

今回のCodex原文2点を原資料で再確認した結果、両方とも再現した。

1. FrontendのMerge Previewは`mergedCandidate.registrationTarget`へ解決済み値を必ず返すよう読めるが、Sourceがtrue/false混在し明示値もないConflict中は解決値が存在しない。
2. Frontend / Backendの情報源優先順位に旧`R2`表記が各1件残っていた。

## 25.1 Merge Preview未解決Responseの三視点評価

### Webクリエイター

未解決状態を`false`で代用すると「登録対象外を選択済み」に見え、Previewとして誤解を招く。`null`を未解決専用状態とし、UIでは「登録対象を選択してください」等として明示する方が、Conflictの原因と次操作を理解しやすい。

### マーケター

Source全件一致時は従来どおり自動継承し、混在時だけ判断を求めるため、不要な再入力を増やさない。未解決を`false`へ寄せて誤って除外するよりも、必要な場合だけ明示選択させる方が「すでに存在するTRPG情報を、何度も入力させない」という中心価値と、誤登録防止を両立する。

### エンジニア

Merge Preview Responseだけ`registrationTarget: boolean | null`とし、`false`と未解決を型上分離する。Field省略方式よりResponse Schemaが安定し、TypeScript / Java DTO / Contract Testで三状態を明示できる。Requestの`resultRegistrationTarget`はoptional booleanのまま、未選択はField省略とする。Persisted `import_candidate.registration_target`と実Merge Resultはbooleanのまま維持し、Preview DTOの`null`をDBへ伝播させない。

### 総合判断

採用候補は次とする。

```json
{
  "mergedCandidate": {
    "registrationTarget": null
  },
  "registrationTargetConflict": true,
  "canApply": false
}
```

- Source全件一致 / 明示値あり：`registrationTarget`はboolean。
- Source混在 / 明示値なし：`registrationTarget=null`。
- `null`はPreview Response DTO専用。
- `canApply=false`だけでTarget Conflictを推測せず、`registrationTargetConflict`を併用する。
- Conflict解決前はApply不可。Apply後に作られるCandidateはboolean。

> **採用区分：【確定】。** 最終精査で具体化したContractを、ユーザーが`2026-10-06T12:11:27+09:00`に明示採用したため正式仕様へ昇格する。

# 26. 今回の訂正履歴

| 修正日時 | 対象 | 旧記述 | 訂正後 | 根拠 | 採用区分 |
|---|---|---|---|---|---|
| 2026-10-06 11:49 JST | Frontend / Backend 情報源優先順位 | 現行本書を`R2`と表記 | `R3 追加ImportContract確定版`へ訂正 | 現行直接正本名 | 【確定・訂正】 |
| 2026-10-06 11:49 JST | Merge Preview Response | 未解決Targetの返却値なし / Frontendは解決値必須に読める | 未解決時`registrationTarget:null`、Conflict true、Apply false | 三視点評価 + FE/BE DTO整合 | 【当時一旦採用 → 2026-10-06 12:11確定】 |
| 2026-10-06 11:49 JST | Frontend / Backend 状態区分 | 3区分のみ | 5区分を明記 | 今回引継ぎルール | 【確定・文書整理】 |
| 2026-10-06 11:49 JST | Example JSON | 一部構文上JSONでないfence | 完全JSONまたはtextへ整形 | 機械検査 | 【確定・文書整形】 |

# 27. 最終精査後の残TODO / 実装判定

## TODO_SPEC_CONFIRMATION【HOLD】

既存6系統を**削除・確定しない**。

1. Game System ProfileのMVP初期Active Profile集合 / 各Profile実データ
2. Previous EndPcState候補取得APIの最終Endpoint / Request Shape
3. CCFOLIA Mapping Configの最終Field Mapping
4. CCFOLIA Preview APIでProfile / Canonical System情報をどう渡すか
5. Tekey / Udonarium共通Adapterの具体Contract
6. CCFOLIA `iconUrl`のPC画像利用

## GO

- 基盤実装
- 既存確定事項
- 復元済みUI
- R2までに正式採用済みImport Contract
- R3で正式採用済み追加Import Contract

## GO（最終精査追加採用）

- Merge Preview未解決時の`registrationTarget:null` Response Contract：**【確定・GO】**

## HOLD

- 上記6系統TODO依存機能
- 全体完成判定

`registrationTarget:null`案はユーザーが`2026-10-06T12:11:27+09:00`に正式採用したため、Formal【確定】ContractとしてCodex実装対象に含める。

# 27.1 Merge Preview Null Contract 正式採用記録

採用日時：`2026-10-06T12:11:27+09:00`（Asia/Tokyo）

ユーザーの「採用とします。」という明示判断により、最終精査で【一旦採用】としていた以下を正式仕様へ昇格した。

| ID | 旧区分 | 新区分 | 採用内容 | 採用日時 |
|---|---|---|---|---|
| ADOPT-FINAL-01 | 一旦採用 | 【確定】 | Source混在・`resultRegistrationTarget`未指定のMerge Previewでは`mergedCandidate.registrationTarget=null`、`registrationTargetConflict=true`、`canApply=false`。`null`はPreview DTO専用で、正式Candidate / Merge Result / DBはbooleanのまま | `2026-10-06T12:11:27+09:00` |

この採用で**既存6系統TODOは解消しない**。またProfile Schema、Image HTTP Contract、未命名卓採番等、別の10/06一旦採用事項を自動的に確定しない。

実装判定：このContractは**GO**。全体完成判定は既存6系統TODOが残るため**HOLD**。

# 28. 修正後再監査チェックリスト

修正後3資料について次を再確認する。機械検査結果の具体値は本節の末尾へ追記する。

- Frontend / Backend Merge Preview DTOのnullable / boolean意味が一致
- Requestの「省略」とResponseの`null`を混同していない
- Actual Merge Result / DB Candidateがbooleanのまま
- Source全件一致 / 混在 / 明示値ありの3分岐が閉じている
- Resolution Conflictで`canApply=false`となるケースとTarget Conflictを混同していない
- `IMPORT_PREVIEW_STALE`と`OPTIMISTIC_LOCK_CONFLICT`の既存優先順位を変更していない
- 既存6系統TODOを維持
- R2旧表記が情報源優先順位から消えている
- Frontend EndpointにBackend未定義Endpointがない
- Markdown code fence / JSON exampleが構文上成立
- Accessibility / Keyboard / Focus / Reduced Motion既存仕様を変更していない


# 29. 機械検査結果【修正後】

検査対象：最終精査修正版 Frontend / Backend / 本訂正履歴。

## 29.1 Markdown / JSON

- Frontend code fence：286、偶数・閉じ忘れなし
- Backend code fence：278、偶数・閉じ忘れなし
- Frontend `json` fence：22件、JSON構文エラー：0件
- Backend `json` fence：38件、JSON構文エラー：0件
- 訂正履歴 `json` fence：1件、JSON構文エラー：0件

既存R3で構文上JSONではなかった断片例 / Method行入りfenceは、内容を変えず完全JSONまたは`text` fenceへ整形した。

## 29.2 Endpoint横断

明示Method + `/api/**`抽出：

- Frontend：75参照 / 42 unique
- Backend：104参照 / 79 unique
- FrontendにありBackendに存在しないMethod + Path：**0件**
- Query除去・Path Parameter正規化後の不足：**0件**

Endpoint一致だけをDTO整合の根拠にはせず、Merge Preview DTOは別途条件分岐監査を行った。

## 29.3 Version / TODO / Contract Scan

- 情報源優先順位の旧`本書（... R2）`完全一致：Frontend 0件 / Backend 0件
- `R2`という文字列自体は、過去版名・訂正履歴・採用履歴として残る。これらは歴史記録であり、現在版名の誤記ではない。
- 旧「Preview Responseでは解決値を必ず返す」文：0件
- Merge Preview `registrationTarget` Contract heading：Frontend 1件 / Backend 1件
- 既存6系統`TODO_SPEC_CONFIRMATION`：Frontend / Backend / 訂正履歴ですべて存在を確認
- `IMPORT_PREVIEW_STALE`：Frontend 7件 / Backend 10件
- `OPTIMISTIC_LOCK_CONFLICT`：Frontend 8件 / Backend 11件
- `clearResolution` / `CLEAR_RESOLUTION`のR3確定Contractを維持

## 29.4 Artifact Integrity

### Frontend 最終精査修正版

- 行数：4264
- SHA-256：`54686dd074940d8ac46b8c8b49925a3c1cdc0016b9120fbdc56eed2d39e11190`
- R3追加ImportContract確定版の抽出テキストとの差分：`+65 / -16`

### Backend 最終精査修正版

- 行数：3747
- SHA-256：`d70feae95543f6491a2a9fbc28376b627642321d943e018892ae8f9421650a80`
- R3追加ImportContract確定版の抽出テキストとの差分：`+65 / -11`

### 訂正履歴・残TODO・実装判定

本節追記前Snapshot：

- 行数：804
- SHA-256：`09d11b00c7375997cc77c0a50341ac8fa79aef299e443e5d06505ad2eb432e64`
- 元R3訂正履歴抽出テキストとの差分：`+104 / -0`（本節追記前）

※Library正本はraw-byte materialize不可だったため、Frontend / Backendの変更行数はFilesから全行取得して再構成した**抽出テキスト正本**とのsemantic diffである。元Libraryファイルの既存記録SHAを改ざん・置換した値ではない。

# 30. 人間的な条件分岐・横断再監査結果

## 30.1 Merge Preview / Apply

次の3分岐がFrontend / Backendで一致することを確認した。

1. Source全件同値 + Request省略 → 共通boolean継承、Conflictなし。
2. Source混在 + Request省略 → `registrationTarget:null`、`registrationTargetConflict=true`、`canApply=false`。【確定】
3. `resultRegistrationTarget`明示 → 明示booleanへ解決。

`false`と`null`は別意味。`canApply=false`はResolution Conflict等でも発生し得るため、Target Conflict判定を`canApply`だけへ依存させない。

実Merge Resultは`resolvedResultRegistrationTarget`を使用し、`PENDING` / `EXCLUDED`へ遷移する。Previewの`null`を正式Candidateへ保存しない。

## 30.2 Resolution / Reload / Split / Merge

- Candidate Reload時は`resolutionRefs`と`resolutions[]`で判断を復元。
- 部分登録後は同一`resolutionId`の`createdEntityId`を再利用し、Formal Entityを重複生成しない。
- `clearResolution`は対象Slot参照だけ解除し、共有Resolution / 他Candidate / 既作成Formal Entityを削除しない。
- Splitは未Mapping ResolutionがあればApply不可、明示Binding後に適用。
- MergeはResolution Conflictを暗黙解決せず、Binding / discardを要求。
- Merge Previewのresolved / unresolved例で`resolutionRefs` Shapeを既存Candidate DTOと同じObject Shapeに維持。

## 30.3 Conflict / Optimistic Lock / 別Tab

- Preview Request開始時の古いSession Version → `OPTIMISTIC_LOCK_CONFLICT`。
- 成功済みPreview後のSession / Candidate / Resolution Snapshot変化 → Apply時`IMPORT_PREVIEW_STALE`を優先。
- Register確認後に別TabでCandidate更新 → Backendが最新版を勝手に登録せずVersion Conflictで停止。
- Candidate Version競合は登録処理前に検出し、対象RequestでFormal Dataを部分生成しない。

## 30.4 Backend / DB境界

- Formal Entity 9件は変更していない。
- ImportSession / ImportCandidate / ImportResolution等はSupport / Temporary Dataの位置づけを維持。
- `import_candidate.registration_target BOOLEAN`をPreview DTOの`null`表現を理由にSchema変更していない。
- Session Row Lock、Ownership、Parent-Child / Same Session Membership Validationを維持。
- Child EntityのUserID、FK、Delete Rule、Optimistic Lock等、今回対象外の物理設計へ差分を入れていない。

## 30.5 UI / Accessibility

今回の機能差分はMerge Previewの未解決表示だけ。以下の既存仕様は変更していない。

- Desktop / Tablet / Mobile Responsive
- Keyboard
- Focus / Focus Restore
- ARIA
- Error Summary
- Reduced Motion
- Empty State / Ghost Slot
- TableDate Popover
- Long Text
- Recording / Appearances
- PC Focus v5.9 Visual

未解決Targetは`null`を`false`へ変換せず、選択が必要な状態として表示する。

## 30.6 最終判定

今回の修正後再監査では、**新たな意味矛盾は確認されなかった**。

Merge Preview未解決Responseの`registrationTarget:null`は最終精査で新規具体化されたが、ユーザーが`2026-10-06T12:11:27+09:00`に明示採用したため**【確定・GO】**とする。

既存6系統TODOはすべて残っているため、全体完成判定は引き続き**HOLD**。

# 31. 最新採用確定版の検証記録・文書管理訂正

修正・検証日時：`2026-10-06T12:17:59+09:00`（Asia/Tokyo）

## 31.1 訂正履歴

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| DOC-FINAL-01 | §29.4はFrontend 4264行・Backend 3747行の最終精査修正版を記録しており、最新のMergePreviewNullContract採用確定版のファイル名・行数・SHA-256が未記録 | §29.4の旧検証記録を履歴として保持し、本節に最新採用確定版の実ファイルから取得した検証記録を追記 | ユーザー指示 + 実ファイル照合 / 【確定・文書管理訂正】 | `2026-10-06T12:17:59+09:00` |

§29.4の行数・SHA-256は過去の検証対象に対応する記録であり、今回の最新添付ファイルを識別する値には使用しない。最新採用確定版の識別には以下を使用する。

## 31.2 Frontend 最新採用確定版

- File：`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-06_R3_最終精査_MergePreviewNullContract採用確定版.md`
- 行数：4279
- SHA-256：`5cb69438d9c143303e537e0a60e6e0cc36ac4fdf06a51ab9184edbe0e7eee5ce`

## 31.3 Backend 最新採用確定版

- File：`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-06_R3_最終精査_MergePreviewNullContract採用確定版.md`
- 行数：3762
- SHA-256：`ca7039414faf59985aba5d00bb0fa2a475d7def9e658f090e4c66eaa676b5295`

## 31.4 訂正履歴資料の追記前Snapshot

- File：`卓回廊_Codex実装仕様書_2026-10-06_R3_最終精査_MergePreviewNullContract採用確定_訂正履歴_残TODO_実装判定.md`
- 行数：943（本節追記前）
- SHA-256：`1c7d8b9899c38f2203766447810b2769b20a9594b5d2fb042d1d3e9fc92eae09`（本節追記前）

行数はUTF-8テキストの改行数（末尾改行を含む）、SHA-256は保存された実ファイルの全バイトから取得した。本資料のSnapshot値は本節追記前の状態を識別する値であり、追記後の本資料全体のハッシュを表すものではない。

今回の訂正は検証対象の識別情報の追記であり、Frontend / Backendの機能仕様、採用区分、既存6系統TODO、実装判定は変更しない。
