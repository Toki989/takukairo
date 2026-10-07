# 卓回廊 Frontend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-10-06 横断精査修正版 R3 / Merge Preview Null Contract採用確定 / 最終精査追補

---

# 0. この文書の目的

本書は、TRPG活動履歴管理Webサービス **「卓回廊」** のFrontendをCodexが実装するための実装仕様書である。

今回の実装は **実ユーザーを入れないローカル / テスト用Frontend** を対象とする。

ただしVisual Prototypeではなく、Backend APIへ接続する実装ベースを作る。

最重要原則：

> **見た目は確定済みVisual Design。中身は既存の確定済み設計。**

特にDesktop卓詳細は：

> **PC Focus v5.9をVisual Design / UI実装ベースとする。**

v5.9のMock Data・仮Button・仮状態を理由にFormal Specを変更してはならない。

---

# 1. Codexが守る禁止事項

- v5.9にあるだけの情報を正式Fieldへ追加しない。
- SAN / HP / MP最大値を作らない。
- PC本体に現在SAN / HP / MPを持たせない。
- `SKP` Roleを作らない。
- `isMainKP`を作らない。
- Person専用管理画面を作らない。
- Table詳細をScenario View内Accordionへ戻さない。
- PL / PC Collectionで複数PC Detailを同時展開しない。
- Desktop卓詳細の3レーンVisualを汎用Card UIへ作り直さない。
- Tailwind / 大型UI Frameworkへ勝手に置換しない。
- 巨大なRounded Card / Shadowを画面全体へ乱用しない。
- 「TRPGだから」という理由で魔法陣・羊皮紙・ホラーTexture等を追加しない。
- Hoverだけに情報を依存させない。
- Swipeだけを唯一の操作にしない。
- Offline編集を追加しない。
- Undo / Trashを追加しない。
- 本書にない項目は `TODO_SPEC_CONFIRMATION` とする。

---

# 2. 情報源の優先順位

1. 本書（2026-10-06横断精査修正版 R3 追加ImportContract確定版）
2. `最終横断チェック後 引継ぎメモ兼プロンプト 2026-10-01`
3. `TRPG活動履歴管理Webサービス_VisualResponsiveAccessibility仕上げフェーズ_確定事項まとめ_2026-09-30.md`
4. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md`
5. `卓回廊_S1-S6_BackendDataSecurityPhysicalDesign_確定事項まとめ_2026-09-30.md`
6. `TRPG活動履歴管理Webサービス_最新統合正本_2026-09-28_確定版_フロント制作補強修正版(3).md`
7. 2026-09-25各Visual Design資料
8. `TRPG活動履歴管理Webサービス_Desktop卓詳細_v5.9統合フェーズ_確定事項まとめ_2026-09-24.md`
9. `TRPG_PCFocus_v5_9(1).zip` Visual Reference
10. `TRPG_GAME_LIBRARY_FrontendPrototype_v5.zip` Interaction / QA Reference

2026-10-01までにユーザーが明示採用した内容は、9/30版の古い記述より優先する。

ZIP内Mock Dataは仕様根拠にしない。

状態区分は必ず維持する。

- **【確定】**：ユーザーが明示採用済み、または既存確定資料から直接確認できる事項。
- **【確定・復元】**：過去資料で確定済みだったが、新仕様書への転記から落ちていたため戻した事項。
- **【一旦採用】**：実装Contractを閉じるため新たに具体化したが、ユーザーの正式採用前である事項。
- **【TODO_SPEC_CONFIRMATION】**：資料から確定できず、ユーザー判断が必要な事項。
- **【資料照合残件】**：機能仕様自体は確定済みで、歴史的資料・旧訂正履歴等の完全照合だけが残る事項。

`資料照合残件`だけを理由に機能実装をHOLDしない。

## 2.1 今回の改訂基準【2026-10-06】

本版は **2026-10-01再改訂版を直接の比較元** とし、9/30版へ巻き戻して再作成しない。

比較元SHA-256：

```text
0fa1d087a5236ab60e283029788bf82f7625b119587f94ce14556ac0c47af512
```

改訂記録時刻：`2026-10-06T10:34:09+09:00`。

9/30以前の資料は、10/1再改訂版で落ちた既存確定事項を検出・復元するために参照する。新しい判断を古い資料から推測追加しない。

今回の変更は次の3区分で記録する。

- **【確定・復元】**：既存確定事項の再転記。再採用判断を行わない。
- **【一旦採用・2026-10-06提案】**：実装契約を閉じるため今回新たに具体化した内容。ユーザーの明示承認前は確定扱いしない。
- **【未確定 / TODO_SPEC_CONFIRMATION】**：資料から確定できず、推測で埋めない内容。

詳細な旧記述 / 訂正後 / 根拠 / 採用区分 / 修正日時は、同日作成の横断精査・訂正履歴ファイルにも対応付ける。

---

# 3. Technology Stack

- React 19.x
- TypeScript
- Vite
- React Router
- Node.js LTS
- npm + `package-lock.json`
- CSS Modules
- Global Design Tokens
- SPA

使用しない：

- SSR
- React Server Components
- Tailwindへの全面移行
- UI FrameworkによるVisualの置換

APIはBackend REST `/api/**`。

---

# 4. Project Structure

推奨：

```text
frontend/
├─ package.json
├─ vite.config.ts
├─ src/
│  ├─ main.tsx
│  ├─ app/
│  │  ├─ router.tsx
│  │  ├─ AppShell.tsx
│  │  └─ providers/
│  ├─ api/
│  │  ├─ client.ts
│  │  ├─ auth.ts
│  │  ├─ scenarios.ts
│  │  ├─ persons.ts
│  │  ├─ pcs.ts
│  │  ├─ tables.ts
│  │  ├─ import.ts
│  │  └─ images.ts
│  ├─ components/
│  │  ├─ common/
│  │  ├─ forms/
│  │  ├─ gallery/
│  │  ├─ table-detail/
│  │  └─ import/
│  ├─ features/
│  │  ├─ home/
│  │  ├─ scenario/
│  │  ├─ pc/
│  │  ├─ table/
│  │  ├─ import/
│  │  └─ account/
│  ├─ styles/
│  │  ├─ tokens.css
│  │  ├─ globals.css
│  │  └─ reset.css
│  ├─ assets/
│  └─ test/
└─ public/
```

Page ComponentへAPI詳細や複雑Business Ruleを直書きしない。

---

# 5. Test Implementation Positioning

今回のFrontendは、BackendのDev固定User Sessionを使う。

Dev時：

```text
POST /api/dev/session
```

を呼べるDeveloper Entryを用意してよい。

例：

```text
/dev-login
```

ただし：

- Production BuildではRouteを生成しない。
- 正式Google Login画面と混同しない。
- App内部ではDev Userでも通常Session / CSRF / Ownership APIを利用する。

正式Production LoginはGoogle Identity Services Buttonである。

One Tap / Auto Selectは使用しない。

---

# 6. Route Map

React Routerの基本Route：

```text
/                         -> Home
/login                    -> Google Login / test placeholder
/dev-login                -> dev only
/setup/self-person        -> 初回SelfPerson設定
/scenarios/new            -> Scenario登録
/scenarios/:scenarioId    -> Scenario View
/scenarios/:scenarioId/edit
/scenarios/:scenarioId/tables/new
/tables/:tableId          -> 卓詳細（Responsive）
/tables/:tableId/edit
/pcs                      -> PL / PC Collection or PC Focus
/pcs/new
/pcs/:pcId/edit
/import                   -> Import
/account                  -> Account Settings
/terms                    -> Public
/privacy                  -> Public
/*                        -> 404
```

PL / PCのPC Focusは `/pcs/:id` という独立Detail Pageを基本にしない。

推奨：

```text
/pcs?pc={pcId}
```

または内部stateで同一画面切替。

Browser Back / Forwardに対応するためQuery Param利用を推奨。

---

# 7. Navigation

Top Level Navigation：

```text
HOME
PL / PC
IMPORT
```

Google LoginはNav外。

通常画面はApp Shellを使う。

Desktop卓詳細だけは、通常App Shellを常時出さない没入型専用View。

基本遷移：

```text
Home
→ Scenario View
→ Table選択
→ 卓詳細
```

卓詳細Back：

- Scenario Viewから来た → Scenario View
- PC Focus Appearanceから来た → 元PC Focus

戻り先ContextをRouter State等で持つ。

---

# 8. Global Visual Concept

正式Service名：

> **卓回廊**

Design Concept：

> Personal Gallery / Collection / 美術館 / 回廊

役割：

```text
Home              = Scenario Collection / 美術館
Scenario View     = 1作品の個展 / 展示室
PL / PC           = PC Portrait Collection
Desktop卓詳細     = 卓の記憶へ入るCharacter Select
Scenario Form     = Collection Entry
PC Form           = Character Entry / Profile Editor
Table Form        = Session Sheet / Archive Entry
Import            = ARCHIVE INTAKE
```

Service側の装飾がScenario / PC固有の世界観を奪わない。

---

# 9. Global Design Tokens

## 9.1 Color

```css
--bg: #F4F4F2;
--surface: #FFFFFF;
--surface-light: #FAF9F7;
--surface-muted: #E6E9E9;
--border: #D1D3D4;
--text-primary: #191919;
--text-secondary: #797A7C;
--decorative: #B3B8B8;
--spotlight: #898D8E;
--accent-teal: #4F8F8A;
--accent-strong: #3F7D79;
--favorite: #E3B341;
--success: #5F7D62;
--warning: #D97A2B;
--danger: #C14343;
--lost: #8A4C55;
```

既存v5.9 Colorを全面置換しない。

【一旦採用】通常Text / Interactive用途には、実ブラウザのContrast確認を前提としてAccessible Tokenを追加する。

概念名：

```text
text-secondary-accessible
accent-text
success-text
warning-text
control-border
```

最終HEXは既存Visualを維持しながらWCAG 2.2 AAを満たすよう調整し、数値を勝手に固定しない。

Tealを常時大面積で使用しない。

Favoriteは色だけに依存せず、`☆ / ★`、accessible name等も併用する。

薄いSpotlight演出層と、ユーザーが実際に読むHO / Quote本文はContrast責務を分ける。

## 9.2 Typography

見る文字：

```text
Shippori Mincho
```

操作する文字：

```text
Noto Sans JP
```

基本Token：

| Use | Desktop | Mobile | Font |
|---|---:|---:|---|
| PC名 | 44px | 32px | Shippori Mincho 700 |
| Scenario名 / 大見出し | 36px | 28px | Shippori Mincho 700 |
| 中見出し | 28px | 24px | Shippori Mincho 600 |
| STATUS数値 | 38px | 32px | Shippori Mincho 600 |
| Section | 22px | 20px | Noto Sans JP 700 |
| PL名等重要情報 | 18px | 16px | Noto Sans JP 500 |
| 本文 | 16px | 16px | Noto Sans JP 400 |
| 補助 | 14px | 14px | Noto Sans JP 400 |
| STATUS Label | 13px | 12px | Noto Sans JP 500 |
| Button | 16px | 15px | Noto Sans JP 600 |
| Nav | 15px | 14px | Noto Sans JP 500 |
| 英字Micro Label | 12px | 11px | Noto Sans JP 500 |

Prototype Tuning値は画面ごとに数px調整可。

---

# 10. Breakpoints

`PROVISIONAL_BUT_IMPLEMENT_FOR_TEST`：

```text
Mobile  : 0 - 767px
Tablet  : 768 - 1199px
Desktop : 1200px+
```

Viewport幅で判定する。

TabletをDesktop単純縮小にしない。

Desktop卓詳細3レーンは原則1200px以上。

低Height対応は別途CSSで行う。

---

# 11. Spacing / Common Size

Spacing Scale：

```text
4 / 8 / 12 / 16 / 24 / 32 / 48 / 64px
```

Page Padding：

```text
Desktop 40px
Tablet  24px
Mobile  16px
```

Mobileは必要時14pxまで調整可。

Touch Target：

> 44〜48px程度

アイコン自体を巨大化するのではなくhit areaを確保する。

---

# 12. Focus / Keyboard / Accessibility

Focus Ring：

```css
outline: 2px solid #3F7D79;
outline-offset: 2px;
```

原則 `:focus-visible`。

`outline: none`だけで消さない。

主要操作はKeyboard完結可能にする。

共通：

- Enter
- Space
- Escape
- Tab / Shift+Tab
- Dialog Focus Trap
- Dialog Close後Focus Restore

Gesture UIはClick / Tap / Keyboard代替を必ず用意。

---

# 13. Motion

`PROVISIONAL_BUT_IMPLEMENT_FOR_TEST`：

```text
Micro             160ms
Standard          260ms
Character         320ms
View Transition   420ms
Save Highlight    1200ms
Skeleton          1600ms前後
Ease              cubic-bezier(.2,.7,.2,1)
```

Reduced Motion：

- Shared movement → Fade / instant
- Portrait slide / scale → Fade
- Shimmer停止
- Hover scale停止可
- Flick snap短縮
- 情報・機能を削除しない

---

# 14. API Client Rule

API Wrapperを1箇所へ集約する。

```ts
apiClient<T>(...)
```

必須：

- `credentials: 'include'`
- State-changing RequestへCSRF Tokenを送る
- JSON Error共通Parse
- 401 `SESSION_EXPIRED` 処理
- 409 `OPTIMISTIC_LOCK_CONFLICT` 処理
- 429 `RATE_LIMITED` 処理
- AbortController対応
- `fieldErrors[]` のJSON Pointer PathをField RegistryへMapping

JWT / Session Tokenを保存しない。

localStorage / IndexedDBへ認証情報を保存しない。

## 14.1 Common API Response【確定】
成功Responseを一律Envelope化しない。

- Create → `201` + 作成DTO
- Update → `200` + 更新後DTO
- Version付きEntity → 最新`version`を返す
- 完全Delete → `204`
- Image Delete等、親Entity Versionを更新する操作 → 最新Versionを返す

Frontendは `{ success: true, data: ... }` を前提にしない。

## 14.2 Common API Error【確定】

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力内容を確認してください。",
  "fieldErrors": [
    {
      "path": "/participations/2/personId",
      "code": "REQUIRED",
      "message": "人物を選択してください。"
    }
  ],
  "traceId": "...",
  "context": {}
}
```

`fieldErrors`はObjectではなくArray。

PathはJSON Pointer形式。

代表例：

```text
/name
/systemSettings/1/gameSystemName
/systemSettings/1/profileValues/syndrome
/participations/2/personId
/participations/2/endPcState/statusValues/san
/tableDates/1/playedOn
```

主なError Code：

```text
VALIDATION_ERROR
MALFORMED_REQUEST
SESSION_EXPIRED
FORBIDDEN
NOT_FOUND
CONFLICT
OPTIMISTIC_LOCK_CONFLICT
IMPORT_CONFLICT
IMPORT_SOURCE_LOCKED_AFTER_REGISTRATION
IMPORT_ANALYSIS_LOCKED_AFTER_REGISTRATION
IMPORT_CANDIDATE_VERSION_CONFLICT
IMPORT_CANDIDATE_REGISTERED
IMPORT_PREVIEW_STALE
SELF_PERSON_SETUP_REQUIRED
RATE_LIMITED
NETWORK_EXTERNAL_ERROR
INTERNAL_ERROR
```

## 14.3 Game System Config API【確定】

```text
GET  /api/game-system-profiles
GET  /api/game-system-profiles/{profileKey}
POST /api/game-systems/resolve
```

Profile一覧はActive Profileのlightweight summary + `configRevision`。

Profile個別取得はExact Versionを取得でき、Inactive / Old Versionも過去Data表示のため取得可能とする。

必要なOptionSourceはProfile Detailへbundleされる。

`POST /api/game-systems/resolve`の状態：

- `EXACT`：自動Canonical化可能
- `SUGGESTED`：候補提示のみ。ユーザー選択後に確定
- `UNKNOWN`：自由入力維持。Canonical KeyはNULL可

曖昧な`CoC`等から版を勝手に決めない。

### 14.3.1 Profile Detail DTO【一旦採用・2026-10-06 Backend対向Contract】

Frontend Generic RendererはBackendと同じProfile Shapeだけを解釈し、System名でComponent分岐しない。

```json
{
  "profileKey": "coc_7e_v1",
  "canonicalSystemKey": "coc_7e",
  "displayName": "クトゥルフ神話TRPG 7版",
  "active": true,
  "configRevision": 1,
  "pcFields": [],
  "endStateStatuses": [
    {"key": "san", "label": "SAN", "displayOrder": 10, "required": false}
  ],
  "showOutcome": true,
  "growthLabel": "成長",
  "aftereffectsLabel": "後遺症",
  "optionSources": []
}
```

PC Field：

```json
{
  "key": "field_key",
  "label": "表示名",
  "inputType": "TEXT",
  "required": false,
  "displayOrder": 10,
  "validation": {"maxLength": 255},
  "optionSourceRef": null,
  "itemFields": null
}
```

Input Type：`TEXT / INTEGER / SELECT / BOOLEAN / REPEATER`。MVPでNested REPEATERは作らない。

SELECTは：

```json
{
  "optionSourceRef": {"sourceKey": "example_options", "revision": 1}
}
```

を参照し、RequestにはOption LabelではなくOption Keyを送る。`selectable=false`のOptionは過去値表示には使えるが、新規選択肢へ出さない。

REPEATERは`itemFields`定義から1段の配列Inputを生成する。React表示用Local Keyを保存`profileValues`へ混ぜない。

Backend Validationに合わせ、ProfileにないKeyを新規生成しない。Unknown Input Typeを受け取った場合はSystem別Fallbackを捏造せず、Profile表示不能Errorとして扱う。

**MVP初期Active Profile集合は未確定。** `coc_7e_v1`は既存資料で使われる具体例なのでTest Fixtureとして利用できるが、これだけを正式対応一覧と解釈しない。

## 14.4 Import API【Endpoint一覧は確定 / Request詳細は一旦採用を含む】

```text
GET    /api/import/session/current
POST   /api/import/session
GET    /api/import/session/{id}
PATCH  /api/import/session/{id}
DELETE /api/import/session/{id}

POST   /api/import/session/{id}/sources/files
POST   /api/import/session/{id}/sources/text
PATCH  /api/import/session/{id}/sources/{sourceId}
DELETE /api/import/session/{id}/sources/{sourceId}
POST   /api/import/session/{id}/analysis/reset
POST   /api/import/session/{id}/analyze
GET    /api/import/session/{id}/review
GET    /api/import/session/{id}/candidates/{candidateId}
PATCH  /api/import/session/{id}/candidates/{candidateId}
GET    /api/import/session/{id}/resolutions
POST   /api/import/session/{id}/bulk-apply/preview
POST   /api/import/session/{id}/bulk-apply
POST   /api/import/session/{id}/candidates/{candidateId}/split-preview
POST   /api/import/session/{id}/candidates/{candidateId}/split
POST   /api/import/session/{id}/candidates/merge-preview
POST   /api/import/session/{id}/candidates/merge
POST   /api/import/session/{id}/register
POST   /api/import/session/{id}/complete
```

Candidate Status等のBackend-owned FieldをFrontendから書き換えない。

---

## 14.5 CSRF Contract / Token Lifecycle【2026-10-01再監査で確定】

`GET /api/csrf` は未認証でも取得可能とする。

Response：

```json
{
  "token": "...",
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf"
}
```

Frontendは`headerName`を固定文字列として再定義せず、Responseの値を使用する。

CSRF TokenはFrontend memoryのみに保持し、`localStorage` / `IndexedDB`へ保存しない。

State Changing Request：

```text
POST / PUT / PATCH / DELETE
```

では、現在のCSRF Responseで得たHeader名へTokenを設定する。

Lifecycle：

```text
App / Login画面初期化
→ GET /api/csrf
→ Login / Dev Login POST
→ Login成功
→ 旧Tokenを破棄
→ GET /api/csrf を再取得
→ SelfPerson Setup等の次のPOST
```

Logout：

```text
現在Tokenで POST /api/logout
→ 成功後、Frontend memoryのTokenを破棄
→ Login画面を継続利用する場合 GET /api/csrf を再取得
```

再認証後も同様にTokenを再取得する。

401後にState Changing Requestを旧Tokenで自動再送しない。

Spring Securityでは認証成功・Logout成功時に以前のCSRF Tokenが無効化される前提で実装する。

# 15. Session / SelfPerson Setup UX

## 15.1 Session Expired

401 / `SESSION_EXPIRED` 時：

- 入力を即破棄しない。
- 即 `/login` へ強制replaceしない。
- Session Expired UIを表示する。
- 再Login導線を出す。
- 可能なら認証後に元Routeへ戻す。

Form Stateが残っている場合：

- 自動POST再送しない。
- 認証後に入力を維持。
- UserがSaveを再実行。

Dev Loginでも同じFlowをTest可能にする。

## 15.2 SelfPerson Setup Gate【確定】

Route Guard：

```text
authenticated=false
→ /login

authenticated=true + selfPerson=null
→ /setup/self-person

authenticated=true + selfPersonあり
→ app
```

SelfPerson未設定中に許可されるAPI：

```text
GET  /api/session
GET  /api/csrf
POST /api/self-person
POST /api/logout
```

それ以外で`SELF_PERSON_SETUP_REQUIRED`を受けた場合、通常画面へ留めずSetupへ誘導する。

`GET /api/session`では、SelfPersonに加えて外部画像初回同意の派生状態も受け取る。

```json
{
  "authenticated": true,
  "user": {
    "id": 1,
    "selfPerson": {"id": 10, "displayName": "テストPL"},
    "externalImageConsentGiven": false
  }
}
```

`externalImageConsentGiven`はScenario外部画像の初回採用時確認だけに利用し、Scenarioごとの同意Stateを作らない。

SelfPerson作成：

```text
POST /api/self-person
```

```json
{
  "displayName": "..."
}
```

Frontendから`userId`や`isSelf`を送らない。

---

# 16. App Shell

通常画面：

```text
Header
  Brand: 卓回廊
  Nav: HOME / PL-PC / IMPORT
  Account entrance
Main
Footer
  利用規約 | プライバシーポリシー
```

Headerを主役にしない。

Account入口はSelfPerson表示名中心。

Google email / pictureを通常UIへ不要に出さない。

---

# 17. Home

Concept：

> Curated Personal Gallery / Scenario美術館

Desktop基本：

```text
Header
Collection Summary
Search / Filter / Sort
Scenario Gallery
Footer
```

## 17.1 Summary

SelfPerson基準：

- 遊んだScenario数
- 参加したTable数
- PL参加回数
- KP参加回数

4枚Dashboard Cardへ分割しない。

Typography主体。

## 17.2 Search

対象：

- Scenario名
- 作者
- PL名
- PC名

Search中の再Renderで入力Focus / Caretを失わない。

## 17.3 Filter

- PL参加
- KP参加
- お気に入り

複数組合せ可。

## 17.4 Sort

- 最近遊んだ順
- Scenario名
- 登録順

初期：最近遊んだ順。

## 17.5 Gallery

一般的なBox Card一覧にしない。

通常表示：

- Scenario画像
- Scenario名
- 作者
- Favorite済み状態は小さく常時表示可

補助：

- Table数
- Favorite Action等

はHover / Focusで静かに提示可。

画像：

- 不必要にCropしない
- 全Scenarioを同一比率へ強制しない
- 強いShadow / Roundedを乱用しない

## 17.6 Empty

Data 0：

- Collectionが空であること
- `Scenarioを追加`
- `Import`

を案内。

Search 0：

- Data 0と区別
- Filter解除 / Search変更を案内

---

# 18. Scenario View

Concept：

> 1作品の個展 / 展示室 + Session Archive

構成：

```text
Back / Header
Scenario Hero
Scenario Metadata
Session Archive
+ このシナリオの卓を登録
```

## 18.1 Metadata

- Scenario名
- 作者
- Game System
- Scenario URL
- Favorite
- Scenario Edit

URLは生URLではなく：

```text
シナリオページを見る ↗
```

## 18.2 Hero

Homeより大きく扱う。

`object-fit: contain`を基本。

External Imageでも不要なCrop / Overlay加工をしない。

## 18.3 Session Archive

白Card大量配置にしない。

> Archive Row / Session Strip

各Table：

- TableName
- TableDate全件
- KP
- PL / PC全員
- PC小画像

PL / PC表示：

```text
PC image
PC name  主
PL name  従
```

KPはPL/PC群と分離したContext。

TableDate 0件で「実施日不明」を勝手に表示しない。

## 18.4 Table Select

Row選択 → 卓詳細Routeへ。

Scenario View内でAccordion Detailを開かない。

---

# 19. Desktop卓詳細 v5.9

Desktop 1200px+ではv5.9をVisual基準として維持する。

## 19.1 3 Lane
```text
LEFT   = Selected PC large Portrait
CENTER = PC / Participation information
RIGHT  = PL Participation Selector
```

主役はPC。

維持：

- Portraitの大きさ
- 左のVisual重心
- 細いSelector
- Selectorの傾斜
- Selector密集感
- 背景装飾
- 中央レーン衝突防止
- low-height desktop対策

目安：

```text
Stage max       約1672px
Portrait Lane   約44%
Center Lane     約19%
Center min      約300px
Selector angle  約-9deg
```

一般Grid Tokenへ無理に丸めない。

## 19.2 Header

- Back
- SESSION micro label
- Scenario名
- Scenario URL（ある場合のみExternal Link）
- TableName
- TableDate
- KP / SKP context
- Table Edit icon
- Hamburger

HeaderをPCより強くしない。

### 19.2.1 Hamburger【確定・復元】

Hamburgerは **Global Navigationのみ** とする。

```text
MENU

ホーム
PL / PC
インポート

────────

アカウント設定
```

含めない：

- Back
- Current Scenario
- 卓編集
- セッションを振り返る
- Logout
- Terms
- Privacy

Visual / Interaction：

- v5.9のQuiet Overlay / Panelを維持する。
- 巨大なSaaS Drawerへ変更しない。
- Typography主体。
- Hover / FocusはAccent Strongの細いRule等。
- `Escape`でClose。
- Open中はFocus Trap。
- Close後はHamburger TriggerへFocus Restore。

## 19.3 Selector

内部単位：

> 1 slot = 1 PL Participation

PC Entityではない。

KPはSelectorに入れない。

selected key：

```text
selectedParticipationId
```

Roving tabindex。

Keyboard：

- ArrowLeft / ArrowRight
- Home / End
- Enter / Space

Active変更時Focusも追従。

### 19.3.1 +PL Ghost Action Slot【確定・復元】

Selector末尾にGhost Action Slot `＋`を置く。

- 通常Participation Slotより視覚的に弱くする。
- Hover / Focus / Tapで `＋ PLを追加` を示す。
- 架空Participationとして扱わない。
- `selectedParticipationId`の対象外。
- Click / Enter / Spaceで既存Table Editの `＋PL` Flowへ接続する。
- PC画像あり / PC画像なし / PCなしParticipationとは別状態。

PCなしParticipationを選択中の場合の補助Actionは `＋ PCを設定` とし、「PCを登録」だけに限定しない。既存PC選択 / 新規PC作成の両方へ接続する。

## 19.4 PCあり / Imageあり

表示：

- PC Portrait
- PC名
- 卓当時PL
- HO / Quote Spotlight
- EndPcState
- Growth / Aftereffects

## 19.5 PCあり / Imageなし

PC情報は通常表示。

PortraitだけNeutral Placeholder。

画像なしをErrorにしない。

## 19.6 PCなしParticipation

PC画像なしとは別状態。

Desktopでは3レーンをできるだけ維持：

```text
Left   = Participation Poster
Center = PL / Participation information
Right  = Participation Selector
```

主役：PL表示名。

表示可：

- PL
- Role
- Scenario
- TableName
- TableDate
- HO
- Recording URL

表示不可：

- STATUS
- EndPcState

巨大な「NO PC」を主役にしない。

## 19.7 Participation 0

架空PC / 架空Selectorを生成しない。

Table ContextとEmpty Stateを表示する。

Recording URLがあればTable-level Actionは利用可能。

## 19.8 Spotlight

HO / Quoteは1度に1種類のみ。

HOだけ → HO

Quoteだけ → Quote

両方 → 切替UI

なし → 非表示

固定 `QUOTE` Labelを勝手に付けない。

Quote本文へ自動括弧を付けない。

## 19.9 EndPcState

Game System Profileに応じてStatusを可変表示。

SAN / HP / MP固定UIとして実装しない。

`statusValues`をProfile順に描画。

Growth / Aftereffectsは主要STATUSより弱く表示。

### Growth / Aftereffects表示【既存確定・No.49復元】

両方とも任意・multiline・最大500文字。入力時の改行を保持し、Skill / before / after等へ自動構造化しない。自動Bullet / Parsing / 要約もしない。

表示は：

- `white-space: pre-wrap`相当で改行保持
- `overflow-wrap`で長い語を折り返す
- 短文は全文表示
- 長文はCompact Preview + `全文を見る`
- 展開後は`折りたたむ`
- 空欄はLabelごと非表示

Exact clamp行数はPrototype / 実機調整対象で、固定値を本仕様から捏造しない。

Outcome：

```text
SURVIVED / LOST
```

LostはBurgundy Accentを使用可。

## 19.10 Recording Action【2026-10-01再監査で確定】

Recording URLあり：

```text
セッションを振り返る
```

等のTable-level Actionを表示する。

Recording URLなし：

> **DesktopでもAction自体を表示しない。**

Disabledの空Actionを残さない。

PCなしParticipation / Participation 0の場合も同じRuleを適用し、Recording URLが存在する場合だけTable-level Actionを表示する。

この項目は今回新規に推測したものではなく、2026-10-01再監査でユーザーから「録画URLなしはDesktopも非表示」という既存修正の再転記指示を受けたため復元したものとする。

# 20. Mobile卓詳細【2026-10-01再監査で順序復元】

Desktop 3 Laneを縮小しない。

Mobile専用再配置。

確定した主順序：

```text
Header
Active Portrait / Participation Poster
Character Selector / Flick
PC名 / PL
Spotlight
Status / Growth / Aftereffects
Recording Action（Recording URLがある場合のみ）
```

つまり、主情報階層は：

> **Portrait → Selector → PC / EndPcState情報**

とする。

SelectorをPC / EndPcState情報の後ろへ戻さない。

Character Flick：

- Active Participation中央
- 前後Peek
- 1 Swipe = 最大1 Participation
- Swipeだけに依存しない
- Tap / Selector / Keyboard相当を用意

Recording URLがない場合はMobileでもAction自体を表示しない。

正確なGesture ThresholdはPrototype調整可能。

# 21. PL / PC Collection

Concept：

> Portrait Gallery × Character Catalogue

基本表示：

```text
PC image
PC name
Current PL
```

一覧ではGame Systemを原則表示しない。

## 21.1 Search

- PC名
- PL名

## 21.2 Filter

- Current PL

## 21.3 Sort

- 最近使用
- PC名
- 登録が新しい順
- 登録が古い順

初期：登録が新しい順。

## 21.4 Desktop

4列基本。

Gap目安24px。

1〜3件でもItemを画面幅いっぱいに引き伸ばさない。

## 21.5 Mobile

初期2列。

Userが：

- 2列
- 3列
- 4列

へ切替可。

この列数PreferenceだけlocalStorage保存可。

4列でもCurrent PLを隠さない。

## 21.6 Item

Box Cardで囲い込まない。

```text
image
space
PC name
PL name
```

Hover / Focus：

- thin Accent rule
- small image change
- clear focus ring

`VIEW CHARACTER`等の巨大Overlay CTAは使わない。

---

# 22. PC Focus

同じPL / PC画面内でCollectionから切替。

詳細表示は常に1件。

Desktop：

```text
Left  = large Portrait
Right = Profile / Metadata / Activity
```

Profile【2026-10-01更新】：

- PC名
- Current PL
- Common / Default Character Sheet URL（任意）
- 0..N Game System Settings
- Edit PC
- Change PL
- Edit PL display name
- Appearances

単一`Game System`をPC本体の唯一のSystemとして表示しない。

Game System SettingはPC Aggregate配下のSupport Structureであり、PCをSystemごとに複製しない。

各Settingの表示対象：

- Game System名
- Canonical System情報（存在する場合）
- Profileに基づくPC固有Field
- System専用Character Sheet URL（存在する場合）

Character Sheetを開く場合：

1. 選択中SettingのSystem専用URL
2. なければPC共通 / Default URL

の順で使用する。

Profile / OptionSourceに応じるUIをSystem名ごとのReact Componentとしてハードコードしない。

Table当時のHO / EndPcStateをPC本体Profileへ混ぜない。

## 22.1 Return to Collection

維持：

- Search
- Filter
- Sort
- Scroll
- original item focus

## 22.2 Appearances【確定・復元】

PC Focus内の縦型Activity Archive。

基本表示：

- Scenario名
- TableName
- TableDate
- 卓当時PL（現在PLと異なる場合等、必要Context）

一覧へ常時追加しない：

- KP全員
- 他PL / PC全員
- HO
- Quote
- Recording URL文字列
- EndPcState

初期表示：

- 0件 → Empty State
- 1〜5件 → 全件
- 6件以上 → 最近5件 + `すべて表示（N）`

展開後は同一PC Focus内で全件を表示し、**`表示を減らす`で再折りたたみ可能**とする。Desktop / Mobileで初期件数を変えない。Account persistenceは行わない。

並び順は、TableDateがある場合は各Tableの最新TableDateを基準に降順。TableDateなしは後方。

Appearance Row本体：

```text
対象Tableの卓詳細v5.9へ移動
→ そのPCに対応するParticipationを初期選択
```

Recording URLがあるAppearanceだけ、Row本体とは別のSecondary Action：

```text
▶ 振り返る
```

を表示し、**卓詳細を経由せず外部録画URLへ直接移動**する。生URL文字列は一覧へ表示しない。Recording URLなしではAction自体をrenderしない。

Secondary ActionのClick / Keyboard操作でRow本体Navigationを誤発火させない。

卓詳細から戻る場合は元PC Focusへ戻り、可能な範囲で選択PC・Appearances展開状態・Scroll位置を維持する。

# 23. PC Change PL Mode【2026-10-01再監査で転記補強】

PC Focusから同一PL / PC画面内の専用Modeへ。

別の完全独立管理画面にしない。

API：

```text
GET  /api/pcs/{pcId}/change-person-context
POST /api/pcs/{pcId}/change-person
```

表示：

- PC
- Current PL
- New PL Searchable ComboBox
- `＋ 新しい人物を追加`
- Related past Participations
- Update target checkbox
- Impact summary

明示：

> PCの現在PLを変更しても、過去の卓当時PLは自動では変更されない。

ただし専用PL変更UIでは、ユーザーが過去Participation.personの更新対象を明示選択できる。

初期状態：
> **関連Participationは全件チェックON**

ON：新Personへ変更。

OFF：旧Personのまま。

リアルタイムで、

```text
3件をBへ変更
1件はAのまま
```

等の要約を表示する。

PL変更で扱うのは：

- PC.current Person変更
- ユーザーが選択した過去Participation.person変更

のみ。

**PC解除をPL変更Flowへ混在させない。**

PL変更だけではParticipation.pc / EndPcStateを変更・削除しない。

Context取得時に以下Versionを保持する。

- PC.version
- 変更候補Participation.version
- 各Participationが属するTable.version

保存時にすべて送信・検証対象とする。

409 `OPTIMISTIC_LOCK_CONFLICT`時：

- 自動再試行しない
- 最新Contextを再取得
- Userに再確認させる
- 旧選択を勝手に再適用しない

保存後：PC Focusへ戻る。

Mobileでは縦Layout。

過去Participationを横Scroll表にしない。

# 24. Person Display Name Edit

PC FocusからDialog。

表示：

- 現在名
- 新表示名
- 関連PC件数
- 過去卓件数

同じPersonを参照する箇所すべてへ反映されることを示す。

Person専用管理Pageは作らない。

---

# 25. Scenario Form

Concept：

> Collection Entry

1ページ連続Form。

Wizard化しない。

Desktop：

```text
SCENARIO
Title
BOOTH input assistance
Rule
Left: Scenario Image
Right: Scenario Information
Rule
Save
Edit only: Danger Zone
```

max-width目安：880〜960px程度。

## 25.1 Fields

- Scenario名 required
- 作者 optional
- Game System optional
- Scenario URL optional
- Scenario image optional

## 25.2 BOOTH

画面主役にしない。

```text
BOOTHから入力を補助
[ URL ] [情報を取得]
```

取得中も他Input編集可。

全画面Lockしない。

成功：候補をFormへ反映。

自動保存しない。

Edit時は既存値を無言上書きしない。

## 25.3 Image【確定・復元 + R1監査反映】

Radio設定画面化しない。

直接操作：

- Preview
- Upload
- External Candidate
- Change
- Delete

優先関係は `User Upload > External Candidate`。Upload削除後にExternal Candidateへ自動復帰しない。候補が残っている場合だけ `この画像を使用` をユーザーが明示選択する。

External ImageはBrowserから外部Host参照とし、画像ファイルを卓回廊Storageへ複製しない。元Scenario / 商品Pageへの導線を維持する。

### 外部画像候補のDraft保持【確定・復元】

未採用のExternal Candidateは **Scenario FormのDraft内だけ** で保持する。

- Save前に正式Scenario属性として永続化しない。
- Form離脱 / Cancel / Reloadで未採用候補は破棄する。
- Upload画像使用中にも、未採用External Candidateを恒久保存する新しいData属性を追加しない。
- External Candidateを正式採用した場合のみ、既存の外部画像URL利用仕様に従って保存対象へ移す。

### 初回採用時の同意【確定・復元】

外部画像候補を正式なScenario画像として **Userが初めて採用する操作時** に説明・同意を求める。BOOTH情報取得・候補取得・Previewだけでは同意を要求しない。

- 同意はUser単位で1回。Scenarioごとに繰り返さない。
- `GET /api/session`の`user.externalImageConsentGiven`で既同意か判定する。
- 未同意Userが外部画像を正式採用してScenarioを保存する場合、Scenario Create / Patchのcommand-only field `externalImageConsentAccepted: true`を同じ保存Requestへ含める。
- BackendはScenario保存と`app_user.external_image_consent_at`初回設定を同一Transactionで処理する。
- `externalImageConsentAccepted`はScenario属性として保存しない。
- この同意はRights Holderからの利用許諾を意味せず、画像ファイルを卓回廊Storageへ複製保存しない。

この復元根拠は `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md` §5-6 と、Physical Designの`app_user.external_image_consent_at`。

Test環境ではExternal Image featureをMock URLで確認可能にする。

## 25.4 Duplicate Warning

Errorではない。

候補を表示し：

- 既存Scenarioを使う
- 新規として続ける

User判断。

## 25.5 Save Navigation

New → 新Scenario View

Edit → 同Scenario View

Delete → Home

## 25.6 Delete

Danger Zone最下部。

Table 0件のみAction enabled。

Tableあり：

- 削除不可説明
- `関連する卓を見る`

Saveの隣にDeleteを置かない。

---

# 26. PC Form

Concept：

> Character Entry / Character Profile Editor

Desktop：

```text
Left  = large Portrait editor
Right = Profile form
```

max-width目安1200px。

Portrait : Profile ≈ 5 : 7。

## 26.1 PC本体Field【確定】

- PC名 required
- Current Person required（新規時）
- PC image optional
- Common / Default Character Sheet URL optional
- Game System Settings 0..N

旧`PC.gameSystem`の単一System前提は使用しない。

Current Person：Searchable ComboBox。

末尾：

```text
＋ 新しい人物を追加
```

Small DialogでPerson作成。

作成後Formへ戻り自動選択。

Draftを保持。

SelfPersonを自動確定しない。

## 26.2 Game System Settings Tabs【確定】

1 System = 1 Tab。

Tab列末尾：

```text
＋ ゲームシステム
```

Visual：

- 選択中のみ細いAccent Underline
- 大きなRounded Segmented Control化しない
- 横幅不足時はHorizontal Scroll可
- Tab切替でDraft保持

Setting概念：

```text
gameSystemName
canonicalSystemKey
profileKey
profileValues
characterSheetUrl   # System専用
displayOrder
```

Settingを1件作る場合、Game System名は必須。

Game System入力後、`POST /api/game-systems/resolve`を利用する。

- EXACT → Canonical確定可
- SUGGESTED → Userが候補を選んだ場合のみ確定
- UNKNOWN → 自由入力を維持

Profileが解決できる場合は`GET /api/game-system-profiles/{profileKey}`の定義から入力Fieldを生成する。

SELECTはLabelではなくOption KeyをRequestへ送る。

Profileごとの専用React Componentを作らない。

System専用Character Sheet URLがない場合は、PC本体のCommon / Default Character Sheet URLを参照先として利用できる。

## 26.3 Save Contract【確定】

PC本体 + 全System SettingsをPC Aggregateとして保存する。

```text
POST  /api/pcs
GET   /api/pcs/{pcId}
PATCH /api/pcs/{pcId}
```

PATCH：

- `systemSettings`省略 → Settings変更なし
- `systemSettings: []` → 全Settings削除
- `systemSettings`指定 → 保存後の完全一覧として送る

親`PC.version`でOptimistic Lock。

System Setting専用CRUD APIは作らない。

## 26.4 Edit

Edit対象：

- PC名
- PC image
- Common / Default Character Sheet URL
- 0..N Game System Settings

Current PLはRead Only。

PL変更は専用Modeへ。

## 26.5 Image Editor【確定・復元】

同一画面内：

- Drag position
- Zoom
- 中央に戻す
- Change
- Delete

別Pageを作らない。

### Keyboard Position操作

Portrait編集領域自体をKeyboard Focus可能にする。

- `Arrow`：表示枠の約1%相当ずつ、**画像そのものを**矢印方向へ移動。
- `Shift + Arrow`：約5%相当ずつ移動。
- Drag / Keyboardは同じPosition Metadataを更新する。
- 画像の空白が表示枠内へ出ない位置までClampする。
- Focus Ringを表示する。
- 画面上に短い操作説明を置く。
- 方向Buttonを大量Overlayしない。

### Zoom Controls

Desktopの順序：

```text
− → Slider → ＋ → 中央に戻す
```

- Zoom ControlsはPortrait直下。
- Slider幅はPortrait幅の約55〜65%、Desktop目安200〜260px。
- ±は小さな補助Buttonで、Portrait上へOverlayしない。
- **`中央に戻す`はPositionだけを中央へ戻し、Zoom値は変更しない。**
- 画像変更 / 削除はZoom操作から一段分離する。

Mobile：

- 1 finger drag
- Pinch Zoom
- Slider fallback
- 中央に戻す
- Change / Delete
- + / - Zoom Buttonは表示しない
- Keyboardを利用できる環境では同じArrow / Shift+Arrow操作を許容する

画像の透明Marginを勝手にTrimしない。原画像は変更しない。

## 26.6 Error Summary【既存確定・No.63復元】

Error Summaryは常設しない。Save / Network ErrorとValidation Error Summaryを混同しない。

### Error 1件

- Inline Errorを表示
- 保存失敗後、該当FieldへFocus
- Summaryは表示しない

### Error 2件以上

- Inline Errorは維持
- **PC Profile / Form上部**へ簡潔なError Summaryを表示
- 保存失敗後はSummaryへFocus
- Summary内の各Errorから該当Fieldへ移動可能

Summary item選択時：

```text
必要なら該当System Tabを開く
→ scrollIntoView
→ focus()
```

System Tabにも視覚的Error Indicatorとaccessibleな`入力エラーあり`を付与する。Tab自体へ`aria-invalid`を付けない。

---

## 26.7 Entry Context / Save Navigation【既存確定仕様の再転記】

PC新規登録の入口を保持する。

### PL / PC Collection起点

```text
PL / PC Collection
→ ＋ PCを追加
→ PC登録
→ 保存成功
→ 新PCのPC Focus
```

### 卓登録 / 編集起点

```text
卓登録 / 編集
→ ＋ 新しいPC
→ PC登録
→ 保存成功
→ 元の卓Form
→ 新PCを自動選択
```

卓Form起点では元の卓Form Draftを失わない。

PC編集保存後は元の対象PC Focusへ戻る。

## 26.8 PC Delete UI【既存確定仕様の再転記】

PC削除入口はPC編集画面の最下部へ置く。PC Focusへ強い赤色Delete Actionを常設しない。

削除可能：

> 関連Participationが0件のPCのみ。

関連ParticipationがあるPCは削除不可とし、PC削除のためにParticipation / EndPcStateを自動解除・削除しない。

削除可能な場合のみ確認Dialogを表示する。

```text
「PC名」を削除しますか？
この操作は元に戻せません。
[キャンセル] [削除]
```

PC名再入力等の過剰な確認は要求しない。

削除成功後はPL / PC Collectionへ戻り、削除済みPC Focusへ戻らない。

# 27. Table Form

Concept：

> Session Sheet / Archive Entry

1ページ連続Form。

Wizard化しない。

基本：

```text
Scenario Context
TableName / TableDate
KP
PL Tabs
Recording URL
Save
Edit only: Danger Zone
```

max-width目安880〜960px。

## 27.1 Scenario

New：Scenario View起点で固定。

再選択させない。

Edit：変更可。

## 27.2 TableName

Optional。

Placeholderへ `卓1` を実値のように自動入力しない。

## 27.3 TableDate

Optional / multiple。

API上は文字列配列ではなくObject配列で統一する。

```json
{
  "tableDates": [
    {"id": 501, "playedOn": "2026-09-20"},
    {"id": null, "playedOn": "2026-09-21"}
  ]
}
```

Create時は`id`省略または`null`。Edit時の既存日付は`id`を保持する。

Validation Pathは`/tableDates/{index}/playedOn`とする。

- Date input / Calendar
- `今日` Shortcut
- Add / Remove
登録日時を入力させない。

## 27.4 KP / SKP

KPはPL Tabに含めない。

1人目：UI上KP。

```text
＋ SKPを追加
```

で追加。

Backend上は全員Role=KP。

## 27.5 PL Tabs

1 Participation = 1 Tab。

Tab Label：

```text
Person名 / PC名
Person名 / PC未選択
新しいPL
```

番号だけ `PL1` は使用しない。

末尾：

```text
＋ PL
```

Tab切替でDraft保持。

保存は卓全体で1回。

別TabにError → TabにもError indication。

Save時最初のError Tabへ切替 + Focus。

## 27.6 Person / PC ComboBox

開いた時点でCandidate表示。

Recent最大5件。

Person選択後：

- 現在そのPersonに紐づくPCを通常候補
- Recentを上位

自動選択しない。

SearchではCurrent User全PCを検索可。

別Person現在PCを選択する場合はCurrent PLを補助表示。

## 27.7 PC optional / PC変更・解除【既存確定仕様の再転記】

Label：

```text
PC（任意）
```

PCなし保存可。

PCなし：EndPcStateを表示・保持しない。

### PC-A → PC-B

既存EndPcStateがある状態でPCをAからBへ変更する場合、AのEndPcStateをBへ引き継がない。

変更前に確認する。

```text
PCを変更すると、この卓での「PC-A」の卓終了時状態が削除されます。
```

確認DialogへSAN / HP / MP等の詳細値一覧を並べない。

Cancel：PC-Aと既存EndPcStateを維持。

Confirm：

1. PC-Aに紐づく今回TableのEndPcState Draft / 保存対象を解除
2. PC-Bを設定
3. PC-A側のTouched / Reference Prefill状態を破棄
4. PC-B用の新しいEndPcState入力状態を初期化
5. 必要ならPC-BのPrevious EndPcState候補を新規取得してReference Prefill

PC-Aの値をPC-Bへコピーしない。

### PC解除

既存EndPcStateがある場合は解除前に確認する。

```text
PCの設定を解除すると、この卓での「PC名」の卓終了時状態が削除されます。
```

Confirm後、PCを`null`にし、該当EndPcStateも保存対象から除外する。

この責務をPL変更Flowへ混在させない。

## 27.8 HO / Quote【Quote文字数は一旦採用】

Accordionへ隠さない。

Desktop横並び可。

Mobile縦積み。

Quote最大24文字の「1文字」はUnicode Extended Grapheme Clusterとして数える。

Frontendは原則：

```text
Intl.Segmenter
{ granularity: "grapheme" }
```

を利用する。

JavaScript `.length`だけで判定しない。

超過時に勝手に切り捨てない。

入力文字列を勝手にUnicode正規化して別文字列へ変換しない。

括弧を自動追加しない。

固定`QUOTE` Labelを追加しない。

## 27.9 EndPcState【確定】

PC選択時に自動表示。

`状態を追加` Buttonは置かない。

EndPcStateのProfileは**TableのScenario.gameSystem**を基準にする。

PC System SettingをEndPcState Profile選択の根拠にしない。

既存EndPcState編集では保存済み`profileKey`を使用し、現在Profileへ勝手にFallbackしない。

Game System Profileの`endStateStatuses[]`からStatus Inputを生成する。

Growth / Outcome / AftereffectsもProfile定義に応じ表示する。

### Previous EndPcState Reference Prefill【2026-10-01再監査で条件補強】

旧「前回の参考値を別Read Only欄に表示」方式は廃止。

前回Status値は、今回終了時Status Inputへ**Reference Prefill**する。

対象：

- `endStateStatuses[]`のみ

引き継がない：

- growth
- outcome
- aftereffects

### 保存済み今回値を最優先

Table編集で今回Tableに既存EndPcStateが保存済みの場合、その値をInputの現在値として表示する。

> **保存済み今回値をPrevious Prefillで上書きしない。**

Previousは「今回値が存在しない新規入力Field」の補助だけに使う。

Reference Prefill中で未編集のField：

- 最初のBackspace / Delete → 値を全消去
- 入力開始 → Reference値を全置換
- 編集後 → 通常Input挙動

TableDate追加・変更で候補再計算してもTouched済みFieldを上書きしない。

### PC変更時のTouched

PC-A → PC-Bを確認して実行した場合、PC-A用EndPcStateとTouched状態を破棄し、PC-B側は新しい入力ContextとしてTouched=falseから開始する。

PC-AのReference値・User編集値をBへ移さない。

### Scenario変更時のTouched / Profile

- 既存保存済みEndPcStateは保存済み`profileKey`と値を維持し、自動Migration / Prefill上書きをしない。
- まだ未保存の新規EndPcStateで、Scenario変更により解決Profileが変わる場合は、値が存在するなら破棄確認を行う。
- Confirm後は旧Profile用Draft / Touchedを破棄し、新ProfileのFieldをTouched=falseで再初期化する。
- Profileが変わらない場合はTouched状態を維持する。
- 別ProfileのStatusを自動Mappingしない。

TableDate 0件時は：

```text
日付未入力のため参考値
```

等と表示可能。ただし「真の前回」と断定しない。

`TODO_SPEC_CONFIRMATION`：Previous EndPcState候補取得APIの最終Endpoint / Request Shapeは未確定。Frontend API層に仮の独自Endpointを固定しない。

## 27.10 CCFOLIA【一部確定 / 一部TODO】

Button：

```text
ココフォリア駒データから入力
```

Clipboard JSONをPasteし、Preview後にUserの明示操作で今回EndPcState Formへ反映する。

反映後は編集可能。

解析失敗してもManual入力継続。

Raw JSONは恒久保存しない。

Skills / Ability / Memo / Chat Palette等の不要Dataを正式保存しない。

Game SystemをCCFOLIA JSONから勝手にCoC6 / CoC7へ判定しない。

方向：

```text
CCFOLIA statuses
→ Mapping
→ Table / ScenarioのprofileKey
→ endStateStatuses[]
→ Preview
```

`TODO_SPEC_CONFIRMATION`：

- Mapping Configの正式Shape
- Preview API Requestの最終Shape
- profileKeyの具体的渡し方
- Tekey / Udonariumとの共通Adapter設計
- `iconUrl`をPC画像として利用する正式仕様

## 27.11 Remove Participation

Tab ×だけで即削除しない。

```text
このPLを削除
```

Edit時は確認。

Saveで確定。

## 27.12 Save Navigation

New → Scenario View、New TableをHighlight。

Edit Scenario unchanged →元Scenario View。

Scenario changed →変更後Scenario View。

---

# 28. Import

Concept：

> ARCHIVE INTAKE

Galleryと業務UIの中間。

通常App Shellを維持。

巨大なWizard Cardへしない。

## 28.1 Steps

```text
01 INPUT
02 REVIEW
03 DETAIL  OPTIONAL
04 REGISTER
```

Typography / Number / Ruleで表示。

## 28.2 Entry / Session【確定】

Current Sessionなし：STEP01を直接表示。

Current Sessionあり：

```text
前回の途中データがあります
Current Step
Checked count
Expiration
[続きから再開]
[新しくインポート]
```

新しく開始する場合、既存ACTIVE Sessionを明示確認後にDELETEしてから新規POSTする。

BackendにACTIVE Sessionが存在する状態で新規POSTを上書き用途に使わない。

COMPLETED / 48h削除待ちSessionは新規開始を阻害しない。

API：

```text
GET    /api/import/session/current
POST   /api/import/session
GET    /api/import/session/{id}
PATCH  /api/import/session/{id}
DELETE /api/import/session/{id}
```

### 28.2.1 Import Session Version Rule【一旦採用・2026-10-06】

ImportSession配下を変更するRequestは原則`expectedSessionVersion`を送る。Mutation成功Responseで返る最新`sessionVersion`を次のMutationへ引き継ぐ。

- Session currentStep PATCH
- Session DELETE
- Source add / edit / delete
- Analysis Reset / Analyze
- Candidate PATCH
- Bulk Apply
- Split / Merge Apply
- Register
- Complete

Preview Requestも確認時点の`expectedSessionVersion`を送り、Apply時に同じPreview Revision + 最新確認版を再検証する。Version競合をFrontendで自動再試行しない。

## 28.3 INPUT / Source【2026-10-01再監査で整合修正】

Desktop：

- Drag & Drop
- File Select
- Pasted Text

FileとTextを同Sessionで併用可。

Mobile：通常File Select中心。

対象：CSV / MD / HTML / TXT等。PDFはMVP外。

API：

```text
POST   /api/import/session/{id}/sources/files
POST   /api/import/session/{id}/sources/text
PATCH  /api/import/session/{id}/sources/{sourceId}
DELETE /api/import/session/{id}/sources/{sourceId}
POST   /api/import/session/{id}/analysis/reset
```

Source変更前に：

```text
入力元を変更すると、現在の未登録の解析結果・確認内容がリセットされます
```

と警告する。

ただしSource変更とAnalysis ResetをFrontendの2 Requestに分けない。

> **Source追加・編集・削除が成功する場合、Backendが同一の論理操作として未登録解析状態の無効化までAtomicに行う。**

ImportSession配下を変更するRequestは、画面が確認したSession版を示す`expectedSessionVersion`を付ける。Source mutationでは必須。

FrontendはMutation成功Responseの`sessionVersion`を次のMutationへ引き継ぐ。StaleなSession Versionを自動で上書き・再送しない。

Request Shape：

```text
POST /sources/files
multipart/form-data: files (1..N), expectedSessionVersion
```

```text
POST /sources/text
{
  "expectedSessionVersion": 3,
  "text": "..."
}
```

PASTED_TEXT編集：

```text
PATCH /sources/{sourceId}
{
  "expectedSessionVersion": 4,
  "text": "...",
  "order": 2
}
```

FILE binary差替えはPATCHせず、新規追加 + 旧Source削除を使用する。

```text
DELETE /sources/{sourceId}?expectedSessionVersion=5
```

正式登録済みCandidateが1件でも存在するSessionではSource追加・編集・削除を許可しない。

Backendの：

```text
409 IMPORT_SOURCE_LOCKED_AFTER_REGISTRATION
```

を表示し、登録済みCandidate / ImportResolution / registrationResultを壊さない。

`POST /analysis/reset`は**一件もREGISTERED Candidateがない段階での明示Reset専用**とする。

Source変更後にFrontendから追加でReset POSTを送らない。

## 28.4 Analysis Loading【確定】

```text
POST /api/import/session/{id}/analyze
```

全画面を不要にLockしない。

実Progressが取得できる場合だけ%表示。偽%禁止。

Candidate StatusはBackendが判定し、Frontendから`REGISTERABLE`等を設定しない。

Analyze Requestにも`expectedSessionVersion`を送る。REGISTERED Candidateが1件でも存在する場合は再Analyzeしない。Backendの`409 IMPORT_ANALYSIS_LOCKED_AFTER_REGISTRATION`を表示し、未登録Candidateは既存解析結果のReview / Detail / Registerを継続する。

## 28.5 REVIEW【確定】

```text
GET /api/import/session/{id}/review
```

Filter / Pagination対応。

Summary：

- total
- registerable
- needsReview
- needsFix
- checked

Candidate詳細：

```text
GET   /api/import/session/{id}/candidates/{candidateId}
PATCH /api/import/session/{id}/candidates/{candidateId}
```

Candidateは`version`を持つ。

Autosave競合はOptimistic Lockとして扱う。

Backend-owned FieldはPatchしない。

Candidate CardではRegister targetとCheckedを別Stateとして表示する。

## 28.6 Bulk Apply【確定】

```text
POST /api/import/session/{id}/bulk-apply/preview
POST /api/import/session/{id}/bulk-apply
```

Previewは非破壊。

Apply前に対象件数を確認する。

## 28.7 DETAIL / Split / Merge【確定】

必要Candidateだけ開く。

- Candidate / parsed data
- Original source
- Source file name
- Source position / trace
- Unassigned values

元SourceはRead Only判断材料。

未割当を勝手に捨てない。

Split / Merge：

```text
POST /api/import/session/{id}/candidates/{candidateId}/split-preview
POST /api/import/session/{id}/candidates/{candidateId}/split
POST /api/import/session/{id}/candidates/merge-preview
POST /api/import/session/{id}/candidates/merge
```

必ずPreview後にUserが明示操作。

## 28.8 Auto Save【確定】

Header付近Persistent Status：

```text
保存しています…
保存済み
保存できませんでした・再試行
```

毎回Toastにしない。

失敗してもLocal Draft保持。

## 28.9 REGISTER【一旦採用・2026-10-06 Contract補完】

```text
POST /api/import/session/{id}/register
```

### Register前Autosave Barrier

STEP04へ進む前、およびRegister Buttonを有効化する前に、登録対象Candidateの未完了Autosaveをすべてflushし、成功Responseで最新`candidate.version`と`sessionVersion`を受領する。

- `保存しています…`中はRegister不可。
- Autosave失敗Candidateが1件でも登録対象に含まれる場合はRegister不可。
- Frontend Local Draftだけを正式登録の根拠にしない。

Requestは **確認済みCandidate ID + その確認版** を送る。

```json
{
  "expectedSessionVersion": 12,
  "candidates": [
    {"candidateId": 11, "expectedVersion": 5},
    {"candidateId": 12, "expectedVersion": 3},
    {"candidateId": 14, "expectedVersion": 8}
  ]
}
```

`candidateIds`だけの旧Requestは使用しない。

Primary Actionには件数を表示し、Double Submitを防止する。

Candidate版またはSession版が変わっていた場合、**未確認の最新版を勝手に登録しない**。競合としてReviewへ戻す。

ResponseはCandidate別Resultと最新`sessionVersion`を受け取り、成功 / 失敗 / staleを同じ画面で区別する。Candidate単位Partial Failureで、既に成功した正式Dataを画面上でも成功として扱う。

ImportResolutionにより、一度`CREATE_NEW`で作成済みになったScenario / Person / PCを後続Candidateで重複作成しない。

## 28.10 COMPLETE【確定】

```text
POST /api/import/session/{id}/complete
```

```json
{
  "expectedSessionVersion": 13
}
```

全Candidateが`REGISTERED`または`EXCLUDED`であることをBackendが確認する。

Frontendから強制Completeしない。

Complete後、生Import Dataは48h後削除。Formal Dataは削除しない。

表示：

- 登録Table数
- 新規Scenario / Person / PC
- 再利用Scenario / Person / PC

Action：

- ホームで確認
- 続けてインポート

「既存情報を何件再利用できたか」を成果として見せる。

---

## 28.11 Detailed Contract【2026-10-01再監査で固定】

### Session PATCH

Frontendが変更できるのは`currentStep`のみ。

```json
{
  "expectedSessionVersion": 7,
  "currentStep": "REVIEW"
}
```

`status` / `savedAt` / `expiresAt` / completion stateはBackend-owned。

### Candidate PATCH【一旦採用・2026-10-06 DTO補完】

CandidateのUser-editable Draftは、MVP Importで推定対象として確定している項目だけを共通Shapeで扱う。

```json
{
  "scenarioName": "狂気山脈",
  "gameSystemName": "クトゥルフ神話TRPG 7版",
  "tableName": null,
  "tableDates": [
    {"playedOn": "2026-06-20"}
  ],
  "recordingUrl": null,
  "participations": [
    {
      "candidateParticipationKey": "p-1",
      "personName": "土岐",
      "role": "PL",
      "pcName": "五色 探",
      "handout": "HO1"
    }
  ],
  "unassignedValues": [
    {
      "itemKey": "u-1",
      "rawValue": "A",
      "assignment": {"type": "UNASSIGNED", "candidateParticipationKey": null}
    }
  ]
}
```

`candidateData`に正式Entity IDを混ぜない。既存Scenario / Person / PCを再利用する判断は`ImportResolution`との参照として別管理する。

Candidate PATCH Request：

```json
{
  "expectedSessionVersion": 8,
  "expectedVersion": 5,
  "candidateData": {"...": "上記Shape"},
  "confirmationStatus": "CHECKED",
  "registrationTarget": true,
  "resolutionChanges": [
    {
      "slot": {"type": "SCENARIO"},
      "decision": "REUSE_EXISTING",
      "existingEntityId": 100
    },
    {
      "slot": {"type": "PERSON", "candidateParticipationKey": "p-1"},
      "decision": "CREATE_NEW",
      "draftData": {"displayName": "土岐"}
    }
  ]
}
```

`resolutionChanges`は省略可。ResponseでBackendが確定した`resolutionRefs`を受け取る。

省略Fieldは変更しない。

Frontendが直接変更しない：

- candidate `status`
- `registrationStatus`
- `registrationResult`
- source trace
- Backendが算出するWarning / classification

`registrationTarget=false`はユーザーの明示的な除外操作として扱い、Backendが`registrationStatus=EXCLUDED`へ遷移させる。

`registrationTarget=true`へ戻した場合、未完了Session内のEXCLUDED Candidateは`PENDING`へ戻せる。

REGISTERED CandidateはImport側から編集・再包含・Split / Mergeしない。

### Candidate Detail / Resolution再開復元 / 原文Trace【確定・2026-10-06採用】

`GET .../candidates/{candidateId}`はCandidate DraftとID参照だけでなく、**そのCandidateが現在参照しているImportResolutionの内容も同梱**する。Reload / 再開後はこのResponseを正とし、Frontend Local Stateだけから照合判断を復元しない。

```json
{
  "candidate": {
    "id": 11,
    "version": 5,
    "candidateData": {},
    "resolutionRefs": {
      "scenarioResolutionId": 71,
      "participations": [
        {
          "candidateParticipationKey": "p-1",
          "personResolutionId": 72,
          "pcResolutionId": 73
        }
      ]
    }
  },
  "resolutions": [
    {
      "id": 71,
      "version": 2,
      "entityType": "SCENARIO",
      "decision": "CREATE_NEW",
      "existingEntityId": null,
      "createdEntityId": null,
      "draftData": {"name": "狂気山脈", "gameSystem": "クトゥルフ神話TRPG 7版"}
    },
    {
      "id": 72,
      "version": 1,
      "entityType": "PERSON",
      "decision": "REUSE_EXISTING",
      "existingEntityId": 220,
      "createdEntityId": null,
      "draftData": null
    },
    {
      "id": 73,
      "version": 1,
      "entityType": "PC",
      "decision": "CREATE_NEW",
      "existingEntityId": null,
      "createdEntityId": null,
      "draftData": {"name": "五色 探"}
    }
  ],
  "sourceContexts": [
    {
      "sourceId": 3,
      "sourceType": "FILE",
      "originalFileName": "history.md",
      "traces": [
        {
          "fieldPath": "/table/scenarioName",
          "lineStart": 12,
          "lineEnd": 14,
          "excerpt": "...元データ抜粋..."
        }
      ]
    }
  ]
}
```

`resolutionRefs`に存在する非NULL IDは、同Responseの`resolutions[]`に必ず1件存在する。

Resolutionの`draftData`はMVPでは次だけを扱う。

- SCENARIO `CREATE_NEW`：`name`必須、`gameSystem`任意
- PERSON `CREATE_NEW`：`displayName`必須
- PC `CREATE_NEW`：`name`必須。Person関係は同じParticipationのPERSON Slotで解決し、`draftData`へ`personId`を混ぜない
- `REUSE_EXISTING`：`existingEntityId`必須、`draftData`は`null`

部分登録で`CREATE_NEW`が正式Entityを作成したら`createdEntityId`を保持する。同じ`resolutionId`を参照する後続Candidateはその`createdEntityId`を再利用し、2件目を作らない。

**同じ文字列だから同じResolutionとみなさない。共有Identityは`resolutionId`そのもの。**

Session内の共有ResolutionをReload後にも選択できるよう、次を追加する。

```text
GET /api/import/session/{id}/resolutions?entityType=SCENARIO|PERSON|PC
```

Responseは`sessionVersion`と、上記と同じResolution DTOの`items[]`を返す。FrontendはCandidate Detail表示時または「既存の照合判断を使う」操作時に必要なEntity Typeだけ取得してよい。

Candidate PATCHの`resolutionChanges[]`は、次の3形式を排他的に扱う。

1. `decision` / `existingEntityId` / `draftData`でそのSlot用Resolutionを作成・置換する
2. `useResolutionId`だけを送り、同一Session・同一`entityType`の既存Resolutionへ明示Bindする
3. `clearResolution: true`を送り、そのCandidateの対象Slotを**未設定へ戻す**

CLEAR例：

```json
{
  "slot": {"type": "PERSON", "candidateParticipationKey": "p-1"},
  "clearResolution": true
}
```

`decision` / `useResolutionId` / `clearResolution`は同一Change内で併用しない。

`clearResolution`は**Candidate Slot → ImportResolutionの参照だけを解除**する。共有ImportResolution本体、他Candidateの参照、`createdEntityId`で既に作成されたFormal Entityは削除しない。参照0件になったSupport ResolutionはSession cleanupまで保持してよい。

これは既存確定要件「正式登録前なら一括適用結果を取り消し可能」を、Wire Contractへ落とすためのR3具体化である。**`clearResolution: true` / `CLEAR_RESOLUTION`のWire Shapeは、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】とする。** 共有Resolutionを1 Candidateから編集して他Candidateへ暗黙波及させない。変更時はそのSlot用に新しいResolutionへRebindする。

原文比較はTraceが示す必要範囲を返し、HTML等を実行しない。

### Bulk Preview / Apply

Preview：

```json
{
  "expectedSessionVersion": 9,
  "operation": {
    "type": "APPLY_RESOLUTION",
    "slot": {"type": "SCENARIO"},
    "decision": "REUSE_EXISTING",
    "existingEntityId": 100
  },
  "targets": [
    {"candidateId": 11, "expectedVersion": 5},
    {"candidateId": 12, "expectedVersion": 3}
  ]
}
```

Response：

```json
{
  "sessionVersion": 9,
  "previewRevision": "opaque-value",
  "affectedCount": 2,
  "targets": [
    {"candidateId": 11, "version": 5, "willChange": true},
    {"candidateId": 12, "version": 3, "willChange": true}
  ]
}
```

ApplyはPreviewで送った **同じ`expectedSessionVersion` / Operation / Targets** に`previewRevision`を追加して送る。

Preview後にSession / Candidate Version、Preview対象、またはPreview Revisionが変わった場合は`409 IMPORT_PREVIEW_STALE`とし、再Previewを要求する。**Apply EndpointではこのPreview固有Errorを汎用`OPTIMISTIC_LOCK_CONFLICT`より優先する。** Preview作成Request自体が開始時点で古い`expectedSessionVersion`なら`409 OPTIMISTIC_LOCK_CONFLICT`。Bulk decisionは一括で適用し、途中だけ成功させない。

> **確定・2026-10-06採用**：Preview作成前の古いSession Versionは`OPTIMISTIC_LOCK_CONFLICT`、成功済みPreviewのApply時Snapshot失効は`IMPORT_PREVIEW_STALE`を優先する。

Bulk `operation.type`は少なくとも：

```text
APPLY_RESOLUTION
CLEAR_RESOLUTION
EXCLUDE
INCLUDE
```

を扱う。

`CLEAR_RESOLUTION`は`slot`と`targets`を指定し、対象CandidateのそのSlotだけを未設定へ戻す。Previewでは「現在の照合判断 → 未設定」を明示し、対象件数を確認してからApplyする。REGISTERED Candidateは対象外。共有Resolution本体や他Candidateの参照は削除しない。

一括適用直後の「取り消す」は、この`CLEAR_RESOLUTION`を同じ対象SlotへPreview → Applyすることで実現する。履歴全体を巻き戻す汎用Undoではない。

### Split Preview / Apply【Resolution引継ぎ規則：確定・2026-10-06採用】

Preview：

```json
{
  "expectedSessionVersion": 10,
  "expectedVersion": 5,
  "parts": [
    {
      "clientPartKey": "part-1",
      "candidateData": {},
      "resolutionBindings": [
        {"slot": {"type": "SCENARIO"}, "resolutionId": 71},
        {"slot": {"type": "PERSON", "candidateParticipationKey": "p-1"}, "resolutionId": 72}
      ]
    },
    {
      "clientPartKey": "part-2",
      "candidateData": {},
      "resolutionBindings": []
    }
  ]
}
```

`resolutionBindings`は**引継ぎ先SlotとSession-scoped Resolution IDを明示**する。同名・同じ`candidateParticipationKey`だからという理由だけでBackendがResolutionを自動コピーしない。

Preview Responseは各Result Candidateについて、

- 正規化後`candidateData`
- `resolutionRefs`
- `resolutions` bundle
- `unmappedSourceResolutionRefs`
- `canApply`

を返す。Source Candidateが参照していたResolutionをどのPartにもBindせず失う場合は`unmappedSourceResolutionRefs`へ出し、`canApply=false`。FrontendはPreview上で引継ぎ先を明示してから再Previewする。

ApplyはPreviewと同じ **`expectedSessionVersion` / `expectedVersion` / `parts`** に`previewRevision`を追加して送る。

Split後は：

- `confirmationStatus = UNCHECKED`
- `registrationTarget`はSource Candidateから継承
- `registrationStatus`はTarget=trueなら`PENDING`、falseなら`EXCLUDED`
- 旧`registrationResult`は新Candidateへコピーしない
- `createdEntityId`を持つ共有Resolutionも、明示Bindingされた場合は同じIDを維持する

BackendはVersionとPreview Revisionを再確認し、Temporary Candidateの置換・Trace再関連付け・Resolution Ref更新を1 Transactionで行う。ApplyでPreview snapshotが変化していれば`409 IMPORT_PREVIEW_STALE`。

REGISTERED CandidateはSplit不可。

### Merge Preview / Apply【Resolution衝突処理：確定・2026-10-06採用】

Preview：

```json
{
  "expectedSessionVersion": 11,
  "sources": [
    {"candidateId": 11, "expectedVersion": 5},
    {"candidateId": 12, "expectedVersion": 3}
  ],
  "mergedCandidateData": {},
  "resolutionBindings": [
    {"slot": {"type": "SCENARIO"}, "resolutionId": 71}
  ],
  "discardResolutionIds": [],
  "resultRegistrationTarget": true
}
```

Merge後`candidateParticipationKey`は**mergedCandidateData内で一意**であること。Source Candidateごとに`p-1`が存在しても、Target側で同一Keyを重複させない。

異なるSourceが同じTarget Slotに異なるResolutionを持つ場合、Backendは勝手に一方を選ばない。Preview Responseに、

- `resolutionConflicts`
- `unmappedSourceResolutionRefs`
- `resolutionRefs` / `resolutions`
- `registrationTargetConflict`
- `canApply`

を返す。Conflictがある場合は`canApply=false`。Frontendは`resolutionBindings`で採用Resolutionを明示する。意図的に捨てるResolutionだけ`discardResolutionIds`へ明示する。

`resultRegistrationTarget`は任意Fieldとし、省略時の意味を次で固定する。**この省略Ruleは、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】とする。**

- Source Candidateの`registrationTarget`が**全件同じ** → その共通値をMerge結果へ継承する
- Source間で値が**異なる** → `registrationTargetConflict=true`、`canApply=false`。`resultRegistrationTarget`を明示するまでApply不可
- `resultRegistrationTarget`を明示した場合 → その値をMerge結果に使用する

### Merge Preview `registrationTarget` Response Contract【確定・2026-10-06 12:11採用】

`mergedCandidate.registrationTarget`はMerge Preview Responseに限り **`boolean | null`** とする。`false`は「登録対象外へ解決済み」、`null`は「Source間で値が不一致かつユーザーがまだ解決していない」を表し、両者を混同しない。

- `resultRegistrationTarget`を明示した場合 → `true` / `false`の明示値を返す。
- `resultRegistrationTarget`を省略し、Source全件が同じ場合 → 共通の`true` / `false`を返す。
- `resultRegistrationTarget`を省略し、Sourceが混在する場合 → `mergedCandidate.registrationTarget = null`、`registrationTargetConflict = true`、`canApply = false`。

未解決例：

```json
{
  "mergedCandidate": {
    "registrationTarget": null
  },
  "registrationTargetConflict": true,
  "canApply": false
}
```

Frontendは`null`を`false`へCoerceせず、「登録対象を選択してください」等の未解決状態として表示する。`canApply=false`はResolution Conflict等でも発生し得るため、`canApply`だけから`registrationTarget`の意味を推測しない。

Request側の`resultRegistrationTarget`は **optional boolean** とし、「未選択」はField省略で表す。Requestへ`null`を送って未選択を表現しない。

この`null`はPreview用DTOだけの表現であり、正式なCandidate / Merge Resultの`registrationTarget`をnullable化するものではない。Conflict解消後のMerge Resultでは必ず`true` / `false`へ解決される。

Merge後の`confirmationStatus`は常に`UNCHECKED`、`registrationStatus`は解決後Targetに応じ`PENDING` / `EXCLUDED`。旧`registrationResult`はコピーしない。

> **採用区分：確定。** このResponse表現は最終精査で新規具体化され、ユーザーが`2026-10-06T12:11:27+09:00`に明示採用したため【確定】とする。

ApplyはPreviewと同じ **`expectedSessionVersion` / Sources / mergedCandidateData / resolutionBindings / discardResolutionIds`** を送り、Previewで`resultRegistrationTarget`を明示した場合のみ同Fieldも送る。さらに`previewRevision`を追加する。Preview時に省略して共通値を継承した場合、Applyでも省略を維持してよい。Source混在かつ未解決のPreviewは`canApply=false`なのでApplyしない。Preview後のSession / Candidate / Resolution snapshot変化は`409 IMPORT_PREVIEW_STALE`。

BackendはSource Candidate群から新しいTemporary Candidateへの置換・Trace統合・Resolution Ref確定を1 Transactionで行う。

REGISTERED CandidateをMerge対象に含めない。

### Register / Partial Register【一旦採用・2026-10-06版固定】

Register Requestは`expectedSessionVersion + candidateId + expectedVersion`を送る。`candidateIds`だけのRequestは使用しない。

Response概念：

```json
{
  "sessionId": 1,
  "sessionVersion": 13,
  "results": [
    {
      "candidateId": 11,
      "requestedVersion": 5,
      "outcome": "REGISTERED",
      "candidateVersion": 6,
      "registrationStatus": "REGISTERED",
      "registeredTableId": 901,
      "error": null
    },
    {
      "candidateId": 12,
      "requestedVersion": 3,
      "outcome": "FAILED",
      "candidateVersion": 4,
      "registrationStatus": "FAILED",
      "registeredTableId": null,
      "error": {
        "code": "VALIDATION_ERROR",
        "message": "登録内容を確認してください。",
        "fieldErrors": []
      }
    }
  ]
}
```

`outcome`は`REGISTERED / FAILED`を基本とし、Request開始時点のSession Version不一致はHTTP 409としてRequest全体を開始しない。Candidate Version不一致は **Request全体を`409 IMPORT_CANDIDATE_VERSION_CONFLICT`で開始前に停止**し、1件も登録しない。通常のBusiness Validation FailureだけをCandidate別`FAILED` Resultとして返す。Frontendは成功済みCandidateを再送しない。

登録済みCandidateはImport側から再編集しない。

一部登録後も未登録CandidateのReview / Detail / Registerは継続可能だが、Source mutation / Analysis Reset / Re-Analyzeは禁止する。

ImportResolutionの`createdEntityId`等、二重登録防止に必要な情報はSession cleanupまで維持する。

# 29. Searchable ComboBox ARIA【確定・復元】

Person / PC Searchable ComboBox：

Input：

```text
role="combobox"
aria-expanded
aria-controls
aria-autocomplete="list"
aria-activedescendant
```

Popup：`role="listbox"`。Candidate：`role="option"`。選択中Option：`aria-selected="true"`。DOM FocusはInputへ残す。

Keyboard：Down / Up / Enter / Esc / Tab。Search文字列だけではPerson / PC Entity選択とみなさない。

`＋ 新しい人物を追加`はOptionにせず別Button / Actionとする。

Game Systemはeditable Comboboxとして同じ基本ARIAを使うが、自由入力も正式値として許可する。

# 30. PL Tabs ARIA【確定・復元】

```text
role="tablist"
role="tab"
role="tabpanel"
roving tabindex
Left / Right
Home / End
Automatic Activation
```

Validation ErrorをTabの`aria-invalid`へ直接載せない。視覚Error + accessibleな`入力エラーあり`を提供する。

Save Validation Failure：

1. 最初のError PL TabをActivate
2. 対応Panelを表示
3. 最初のInvalid FieldへFocus

# 31. Dialog【確定・復元】

- Modal semantics
- accessible title
- initial focus
- Focus Trap
- Esc Close（Cancel可能時）
- Close後TriggerへFocus Restore
- Backdrop clickだけを唯一のClose手段にしない
- 重大判断をToastで代用しない

## 31.1 Scenario Form詳細ARIA【確定・復元】

基本：

- Native semantic HTML優先。
- LabelをControlへ正式関連付け。PlaceholderをLabel代替にしない。
- Scenario名はnative `required`。Optionalへ無意味な`aria-required="false"`を乱用しない。

Game System：editable Combobox。`role=combobox` / `aria-expanded` / `aria-controls` / `aria-autocomplete=list`。Candidate選択と自由入力の両方を許可する。

BOOTH入力補助：

- URL Input + `情報を取得`。
- 取得中は対象Regionを`aria-busy="true"`、Duplicate Request防止、polite Live Regionで`取得中…`。
- 成功は`BOOTHから候補情報を取得しました`。
- 失敗は`情報を取得できませんでした。手入力を続けられます`。BOOTH取得失敗をScenario Validation Errorにしない。
- 候補反映はaggregate Live Messageとし、各Field変更を過剰に読み上げない。

Scenario画像：

- native file inputを残す。Drag & Dropだけにしない。
- Preview自体をActionにしない。
- Current SourceをTextでも表示。
- External Candidateは通常Button `この画像を使用`。
- 状態をColorだけに依存しない。

Validation：実際にInvalidなFieldだけ`aria-invalid=true`。ErrorをFieldと関連付け、Save Failure時はFirst InvalidへFocus。Duplicate ScenarioはError扱いせず`aria-invalid`を付けない。

# 32. Loading

分類する。

## Local Loading

Button / Field単位。

## Content Loading

List / Gallery / Archive。

Skeleton可。

## Long Process

Import等。

実Progressがある時だけ% / count / current step。

---

# 33. Skeleton

Home：Scenario Gallery形状。

PL / PC：Portrait Grid形状。

Scenario View：Archive Row / Session Strip形状。

Generic gray Card大量表示にしない。

Search / Filter / Sort全部を無効灰色化しない。

再Fetchでは既存Contentを残してLocal Loading優先。

---

# 34. Error / Warning / Toast

## Error

処理成立不可。

具体的に何を直すか表示。

Backend `fieldErrors[]` をJSON Pointer PathからField RegistryへMappingする。

Error 1件：Inline中心。

2件以上：Form上部Error Summary。

**PC登録 / 編集は§26.6（VisualResponsive No.63）の具体Ruleを優先する。** すなわち1件なら該当FieldへFocusしてSummaryなし、2件以上ならPC Profile / Form上部SummaryへFocusする。

Summary Item選択：

```text
必要なら該当Tabを開く
→ 該当Sectionを開く
→ scrollIntoView
→ focus()
```

使用：

- `aria-invalid`
- `aria-describedby`
- Tab Error Indicator

Unknown Pathは無視せずForm-level Errorとして表示し、`traceId`を診断用に保持できるようにする。

## Warning

処理可能だが確認推奨。

Warningだけで勝手にSave禁止しない。

## Toast

小さい成功 / 状態通知：

- Save success
- Favorite
- PL change success
- Small delete success
- Connection restored

使わない：

- Validation error
- Delete confirm
- Import detailed error
- Major server error

---

# 35. 404 / Protected / 500

404：

- 対象が見つからない
- Home / Back action

Protected Resource：

他User resourceの存在 / owner情報を漏らさない。

500：

- 処理失敗
- Retry可能ならRetry
- internal stack / idをUser向けへ出さない

---

# 36. Offline

MVP Online前提。

OfflineでCreate / Update / Delete不可。

Copy：

```text
オフラインです
現在は変更内容を保存できません。
インターネットに接続してから、もう一度保存してください。
```

Business DataをlocalStorage / IndexedDBへ永続保存しない。

保存可localStorage：

- Mobile PC Grid Density 2 / 3 / 4

等Security影響のないUI Preferenceだけ。

---

# 36.1 Scenario / PC Save Orchestration【2026-10-01更新】

Scenario / PC Formの画像はBrowser側Draftとして保持する。

MetadataとImageの両方を変更する場合：

```text
1. Metadata JSON Save
2. Responseの最新Entity version受領
3. Pending image changeがある場合だけ、その最新versionをexpectedVersionとしてImage APIへ送信
4. Image API Responseの最新versionを受領
5. 全処理成功後に通常Navigation
```

画像しか変更していない場合、Metadata APIを無駄に呼ばない。

PC画像PUT時はFileとPosition / Zoomを同じ`multipart/form-data`で送信し、不要な`image-transform`往復を避ける。具体Field名はSection 41.1のImage Mutation Contractに従う。

保存中のLocal Previewは保存後もしばらく維持可能。

保存直後に不要なImage再Downloadを行わない。

画像処理が失敗しMetadataだけ保存済みの場合：

- 「保存全体が失敗した」と偽らない
- `基本情報は保存されましたが、画像の保存に失敗しました。` と区別
- File Draftを可能な限り保持
- 画像だけ再試行可能
- 既存画像差し替えでは旧画像を維持

Scenario / PCで異なるSave方式を混在させない。

FrontendはVersion競合時に画像Uploadを自動再試行しない。

---

# 37. Form State

Form Draftは画面Component State / Form Stateとして保持。

未保存変更がある場合だけ離脱確認。

変更対象：

- input values
- selected image File
- image position
- zoom
- added / removed tabs
- EndPcState
- candidate changes

Save success後はdirtyを解除。

Save failure：

- 同一画面維持
- Draft保持
- 再入力要求しない

---

# 38. Optimistic Lock UX

409 `OPTIMISTIC_LOCK_CONFLICT`：

```text
別の更新が行われています。
最新の内容を読み込み直して確認してください。
```

Userの入力を即破棄しない。

可能ならCurrent Draftを保持し、Reload前にCopy / Compareできる余地を残す。

自動上書き・自動再試行しない。

PC Change PLでは、PC / Participation / Tableの複数Versionが関係する。

競合時は最新`change-person-context`を再取得し、対象ParticipationをUserに再確認させる。

---

# 39. Rate Limit UX

429：

```text
リクエストが短時間に集中しています。
少し待ってから、もう一度お試しください。
```

内部Limit値 / 残数を表示しない。

Account Lockのように見せない。

Retry-Afterがあれば内部的にButton再有効化へ利用可。

---

# 40. Favorite

ScenarioFavoriteはScenario本体Fieldではない。

Frontend StoreでもScenario objectへ永続Fieldとして混ぜる必要はない。

API Response ViewModelで `favorite: boolean` を表示用に持つことは可。

Optimistic UI可。

Failure時Rollback。

---

# 41. Image URL Handling

Storage KeyをFrontendへ露出しない。

Backendから短期表示URLを取得。

Componentは：

```text
loading
loaded
expired/retry
missing
error
```

を扱う。

期限切れ / 403時：

- 1回だけURL再取得
- 無限retryしない

Masterを通常表示に使わない。

---

## 41.1 Image Mutation HTTP Contract【一旦採用・2026-10-06 Contract補完】

### PC Image PUT

```text
PUT /api/pcs/{pcId}/image
Content-Type: multipart/form-data
```

Form parts：

- `file`：binary、required
- `expectedVersion`：decimal text、required
- `positionX`：decimal text、required
- `positionY`：decimal text、required
- `zoom`：decimal text、required、`> 0`

Multipartでも通常どおりCSRF Headerを付ける。

### Scenario Image PUT

```text
PUT /api/scenarios/{scenarioId}/image
Content-Type: multipart/form-data
```

Form parts：`file` / `expectedVersion`。ScenarioへPC用Transformを送らない。

### Transform only

```text
PATCH /api/pcs/{pcId}/image-transform
Content-Type: application/json
```

```json
{
  "expectedVersion": 9,
  "positionX": 0.125,
  "positionY": -0.050,
  "zoom": 1.20
}
```

### Image DELETE

DELETE BodyへVersionを埋め込まず、Query Parameterで統一する。

```text
DELETE /api/pcs/{pcId}/image?expectedVersion=10
DELETE /api/scenarios/{scenarioId}/image?expectedVersion=4
```

成功Responseは最新親Entity `version`と画像有無 / PC Transformを返す。画像表示URLが必要な場合は既存のDerivative URL取得Contractを利用し、Storage Keyを組み立てない。Frontendは古いVersionのまま次Mutationを送らない。

# 42. External Scenario Image

正式採用候補URLはScenario Dataとして返る。

Browserが外部Hostへ直接Requestする構成。

画像へ：

- Cropしない
- Color変更しない
- Text Overlayしない
- Watermark追加しない
- Credit / Logoを切り落とさない

元ページ導線を保つ。

Production法務判断は `DEFERRED_PRODUCTION`。

テスト実装ではFixture External URLを使用可能。

---

# 43. Account UI

右上SelfPerson → Menu。

- TRPGで使う名前
- Account Settings
- Logout
- Account Delete

Google email / pictureは不要に表示しない。

## 43.1 Logout

通常Confirm不要。

Form dirtyならUnsaved Confirmを優先。

## 43.2 Account Delete

Test UIとしてFlowを実装してよい。

1. Delete target / count / irreversible説明
2. Confirmation text
3. ProductionではGoogle reauthentication
4. Delete
5. Complete screen

偽Progress %を出さない。

Production Backup表記は `DEFERRED_PRODUCTION`。

---

# 44. Terms / Privacy

RouteとFooter Linkは実装する。

今回のテストFrontendではPlaceholder本文でよい。

ただし明確に：

```text
TEST IMPLEMENTATION / FINAL LEGAL TEXT NOT YET FIXED
```

と分かるようにする。

正式本文をCodexが生成してProduction完成扱いしない。

---

# 45. Test Data States

Frontend QA用にBackend Seedで以下を必ず確認する。

## Home

- Scenario 0
- 1
- 8+
- Search 0
- Filter 0
- Favorite
- image missing
- external image

## Scenario

- Table 0
- Table 1
- Table 7+
- TableDate 0 / 1 / 2 / 3+

## 卓詳細

- PL 0
- 1
- 2
- 4
- 7+
- active first / middle / last
- PC image present
- PC image missing
- PC missing
- HO only
- Quote only
- both
- no spotlight
- EndPcState empty
- SURVIVED
- LOST
- recording present / absent

## PL / PC

- PC 0
- PC 1
- PC 12+
- image missing
- same name PCs
- PL changed historical participation
- Appearances 0 / 1 / many

## Forms

- validation errors
- duplicate warning
- save failure
- optimistic conflict
- network error
- offline
- session expired
- rate limited

## Import

- no session
- saved session
- all three statuses
- autosave fail
- partial register fail
- complete

---

# 46. QA Viewports

最低確認：

```text
390 x 844
767px width
768 x 1024
820px width
1024 x 768
1199 x 800
1200px width
1366 x 768
1440 x 900
1672 x 941
1680 x 800
1920 x 1080
```

Breakpoint境界：

```text
767 / 768
1199 / 1200
```

を重点確認。

TabletをDesktop縮小版として扱わない。

Desktop卓詳細3レーンは原則1200px以上。

卓詳細は：

- 1 / 2 / 3 / 4 / 5 / 6 / 7+ PL
- active left edge / right edge
- low height desktop

を確認。

意図しないPage Horizontal Scrollを発生させない。

---

# 47. Interaction QA

Keyboard：

- Home Gallery Focus
- ComboBox Arrow / Enter / Esc
- PL Tab Arrow
- Dialog Tab / Shift+Tab / Esc
- 卓詳細 Selector ArrowLeft / ArrowRight / Home / End
- PC image position keyboard

Import：

- Candidate Detail reload後にResolution decision / selected Entity / CREATE_NEW draftDataが復元される
- Shared Resolutionの`createdEntityId`が部分登録後の未登録Candidateで再利用表示される
- Split PreviewでResolution未Mapping時`canApply=false`になり、引継ぎ先を指定後にApply可能になる
- Merge Previewで異なるResolutionが衝突した場合、暗黙選択せずConflictを表示する
- Preview Requestの古いSession Versionは通常Conflict、Preview成功後ApplyのSnapshot変化は`IMPORT_PREVIEW_STALE`として再Previewへ誘導する
- Resolution設定済みSlotを`clearResolution: true`で未設定へ戻せ、共有Resolutionを使う他Candidateへ影響しない
- Bulk `CLEAR_RESOLUTION` Previewで対象件数と「未設定へ戻る」ことを確認し、正式登録前の一括適用を取り消せる
- Mergeで全Sourceの`registrationTarget`が同じ場合、`resultRegistrationTarget`省略でも共通値を継承し、Previewの`mergedCandidate.registrationTarget`はそのbooleanになる
- MergeでSourceの`registrationTarget`が異なる場合、省略時は`mergedCandidate.registrationTarget = null`、`registrationTargetConflict=true`、`canApply=false`となり、明示値なしでApplyできない
- Merge Previewの`registrationTarget:null`を`false`として扱わず、Conflict解決後の実Merge Resultでは必ずbooleanになる

Focus Return：

- Dialog close
- Collection ← PC Focus
- Table detail Back

Reduced Motion：

- 全主要画面

---

# 48. Visual Regression Priority

最優先でVisual Regressionを防ぐ画面：

1. Desktop卓詳細 v5.9
2. Home Gallery
3. Scenario View / Session Archive
4. PL / PC Collection / Focus
5. Scenario Form
6. Table Form
7. PC Form
8. Import

卓詳細v5.9は既存ScreenshotをVisual Referenceとして比較する。

`TRPG_PCFocus_v5_9(1).zip` のScreenshot / CSSを参照可能。

ただしMock text / valuesはコピーしない。

---

# 49. Component Candidate

Codexは以下程度に分割する。

## Common

```text
AppHeader
TopNav
AccountMenu
Footer
Button
IconButton
Rule
Dialog
ConfirmDialog
ToastRegion
ErrorSummary
InlineError
EmptyState
Skeleton
ExternalLink
SearchableComboBox
```

## Home / Scenario

```text
CollectionSummary
ScenarioGallery
ScenarioGalleryItem
ScenarioHero
SessionArchive
SessionArchiveRow
TableDateList
```

## PC

```text
PcPortrait
PcPlaceholder
PcCollection
PcCollectionItem
PcFocus
PcProfile
AppearancesList
PersonEditDialog
PlChangeMode
PcImageEditor
```

## Table detail

```text
TableDetailHeader
CharacterStage
ParticipationSelector
ParticipationSlot
PcInfoLane
ParticipationNoPcView
Spotlight
StatusBlock
GrowthBlock
RecordingAction
```

## Table Form

```text
KpEditor
PlTabList
PlParticipationForm
PersonPcSelector
EndPcStateEditor
CcfolliaPasteDialog
```

## Import

```text
ImportStepIndicator
ImportInput
ImportResumePanel
ImportSummary
CandidateCard
CandidateFilters
AutoSaveStatus
ImportDetailEditor
RegisterSummary
ImportComplete
```

---

# 50. State Management

大型Global State Libraryは必須ではない。

基本：

- Router State
- React local state
- Context for Session / Toast
- Fetch cacheは必要に応じ導入

導入する場合もVisualやDomain ModelをLibrary都合に変えない。

重要：

- Form DraftはLocal / Feature state
- Session AuthはGlobal
- ToastはGlobal
- Home Search / Filter / Sortを復帰可能なStateとして保持
- PC Collection Search / Filter / Sort / Scroll / selectedPcIdを復帰可能にする

---

# 51. Data ViewModel Separation

API DTOをそのままVisual Component全体へばら撒かない。

例：

```ts
ScenarioDto
→ ScenarioGalleryItemViewModel
```

```ts
TableDetailDto
→ TableDetailViewModel
```

ただしViewModel都合でFormal Dataを捏造しない。

Fallback text：

- TableName未入力 → 表示時のみ `卓1` 等

DB値ではないことを型 / function名で明確にする。

---

# 52. TableName Fallback【確定 + 一旦採用の採番順補完】

未命名Tableを表示するときのみ：

```text
卓1
卓2
...
```

DBへ保存しない。Inputへ自動セットしない。削除時は現在の未命名卓を再採番して詰め直す。

既存確定事項では採番の基準順が未固定だったため、表示Sortの変更で`卓1`自体が入れ替わらないよう、2026-10-06時点では次を **一旦採用** する。

```text
同一Scenario内のTableName未設定Tableを
createdAt ASC → id ASC
で並べ、その順に1..Nを付与する。
```

Search / Sort / Filterで表示順が変わっても、このFallback番号の基準順は変えない。

# 53. TableDate Presentation【確定・復元】

卓詳細Header：

- 0件 → 非表示
- 1件 → `2026.09.12`
- 2件 → `2026.09.12 / 09.19`
- 3件以上 → `2026.09.12 他2日`

3件以上では **`他N日`だけ** をPopover Triggerとする。Pill / Button背景を付けず、Text Secondary + Hover / FocusでText Primary / Accent Strong、細いUnderline / Rule程度に留める。

Popover：

- 小さくQuiet。
- 全TableDateを時系列表示。
- Hover / Focus / Click / Enter / SpaceでOpen可能。
- Esc / Outside ClickでClose。
- MobileはTap。

Session ArchiveではTableDateを全件表示し、複数日を勝手に期間表記へまとめない。

## PART Visual

同一TableにTableDateが複数ある場合だけ、時系列順から表示時に `PART 1 / PART 2 / ...` を導出する。DBへ`partNumber`を追加しない。

表示する：

- 卓登録 / 編集
- Scenario View / Session Archive
- TableDate全件Popover

表示しない：

- TableDate 1件のみ
- Desktop / Mobile卓詳細Header本体

PARTは日付より弱い小Typographyとし、Badge / Pill化しない。

# 54. Placeholder

PC image missing：

- Neutral
- PC自体は存在することが分かる
- PC名等を維持

PC missing Participation：

- 別State
- `PC未登録` は補助表示
- 主役はPL

Scenario image missing：

- Neutral Placeholder
- 巨大NO IMAGEを使わない

---

# 55. Responsive Form Rule

Desktop横並びはMobileで縦積み。

機能・保存Rule・入力意味は変えない。

Table PL Tabは必要に応じ横Scroll可。

ただしMobile PL変更履歴Listは横Scroll前提にしない。

---

# 56. PWA

`PROVISIONAL_BUT_IMPLEMENT_FOR_TEST`：

- Manifest
- App Icon
- Standalone support

Installは任意。

Browser通常利用を維持。

実装しない：

- Push
- Background Sync
- Offline business data

Service Workerを入れる場合、User business API response / image masterを永続Cacheしない。

---

# 57. Security Frontend Rule

- Auth TokenをStorageへ保存しない。
- API credentials include。
- CSRF headerをState Changing Requestへ付与。
- Raw HTMLを安易に`dangerouslySetInnerHTML`しない。
- Import HTML Previewが必要ならsanitize / text representationを優先。
- External URLはhttp/httpsのみBackend Validation済みを前提にするが、Frontendも不正schemeをAction化しない。
- `target="_blank"` 使用時は `rel="noopener noreferrer"`。
- R2 Credential / SecretをFrontend envへ入れない。
- `VITE_*` はPublic値だけ。

---

# 58. DO NOT IMPLEMENT

```text
UC15 一括編集
UC16 卓メモ・感想
UC18 卓共有
Activity Export
PDF Import
Native App
Offline Edit
Read-only Offline Snapshot
Push Notification
Background Sync
Google One Tap
Google Auto Select
Person管理一覧Page
SKP Role
isMainKP
Undo / Trash
PC current SAN/HP/MP
EndPcState max values
SystemごとのPC Entity複製
Profileごとの専用React Component
PL変更Flow内のPC Detach
CCFOLIA JSONからのCoC6/7自動判定
```

---

# 59. Production Deferred

```text
DEFERRED_PRODUCTION:
- Google production login verification
- Formal Terms text
- Formal Privacy Policy text
- R2 real signed URL
- External image production rights decision
- Render production operation
- Account deletion backup wording
- Login成功時Session ID変更・Idle 7日・Absolute 30日とは別の、追加Periodic Session Rotationを将来導入するかの判断
```

Test UIでProduction対応済みと誤認させない。

---

# 60. Implementation Order

1. Vite / Router / Global CSS / Tokens
2. Dev Session / API Client / CSRF
3. App Shell / Error / Dialog / Toast
4. Home
5. Scenario View
6. Scenario Form
7. PL / PC Collection / Focus
8. PC Form / Change PL
9. Table Form
10. Desktop卓詳細 v5.9移植
11. Mobile卓詳細
12. Import
13. Account / Terms / Privacy placeholders
14. PWA
15. Accessibility QA
16. Responsive / Visual Regression

Desktop卓詳細移植時は、最初にv5.9のHTML/CSS構造をReact Componentへ分解し、Visualを変えてからReact化するのではなく、Visualを保持したままData Bindingへ置換する。

---

# 61. Implementation Readiness / Definition of Done【2026-10-01再監査で分離】
完成条件を1段階にまとめない。

## 61.1 基盤実装開始条件

以下が揃っていれば、Frontend基盤実装を開始してよい。

- Vite / Router / Design Token / App Shell
- Dev Session / CSRF lifecycle
- Common Error `fieldErrors[]` + JSON Pointer
- Formal 9 Entityを壊さないDTO境界
- Home / Scenario / PC / Tableの確定済みRouteとVisual骨格
- Desktop v5.9移植基盤
- Responsive / Keyboard / Focus / Reduced Motion基盤
- Cookie / CSRFについてBackend Contractと一致
- TableDate Object ShapeがBackendと一致

判定：

> **基盤実装開始可**

## 61.2 実装可能範囲【2026-10-06再整理】

### A. 既存確定事項として実装可

- Search / Filter / Sort
- Scenario CRUD / Favorite
- PC Create / Edit / Delete condition / Entry Context Navigation
- PL変更のCross-Table Optimistic Lockと初期全件ON
- Table Aggregate Create / Edit / Delete
- PC変更・解除時のEndPcState破棄Rule
- Recording URLなしAction非表示
- Mobile `Portrait → Selector → PC / EndPcState情報`
- Appearances初期5件 / `表示を減らす` / Row→卓詳細 / `▶ 振り返る`→録画URL直接遷移
- Hamburger / Ghost Slot / TableDate Popover / PART表示条件 / 詳細ARIA
- PC画像Arrow約1% / Shift約5% / Clamp / 中央に戻す=Positionのみ

### B. 2026-10-06一旦採用Contractとして実装可能だが、ユーザー明示承認前は正式確定扱いしない

- Import Session mutation version protocol
- Register `expectedSessionVersion + candidate expectedVersion` Contract
- CandidateData / ResolutionRef DTO
- Image Mutation Multipart / DELETE Query Contract
- TableName fallbackの`createdAt ASC → id ASC`採番基準
- Generic Game System Profile Renderer / ValidatorのSchema Contract（Backend仕様と対）

## 61.3 未確定機能の保留

以下は推測固定しない。

- Game System Profileの **MVP初期Active Profile集合と各Profile実データ**（Generic Schema自体は一旦採用Contractで実装可）
- Previous EndPcState候補取得の最終Endpoint / Request Shape
- CCFOLIA Mapping Config / Preview Requestの未確定部分
- Tekey / Udonarium共通Adapter
- CCFOLIA `iconUrl`のPC画像利用

Previous PrefillのUI挙動自体は確定しているためComponent / State設計は可能だが、最終Endpointを独自固定しない。

CCFOLIAもManual入力へ戻れるUIまでは実装可能だが、未確定Mappingをハードコードしない。

## 61.4 資料照合残件【非Blocker】

- ユーザー監査が参照した9/30 Frontend 2708行R1版そのものは手元のProject Sourceと版が異なるため、R1固有文言の完全一致照合は未完了。
- **外部画像初回同意の機能仕様は、9/30 BackendDataSecurityPlatform §5-6およびPhysical Designの`external_image_consent_at`でUser単位初回1回まで確認済みであり、実装保留ではない。**
- 将来R1原文が入手できた場合に文言差を資料履歴として照合するが、現在の機能実装・全体完成条件をこれだけでBlockしない。

## 61.5 全体完成判定

テスト実装全体を完成とみなすには：

- 61.1 / 61.2が動作
- 61.3の`TODO_SPEC_CONFIRMATION`がユーザー判断で解消
- 61.4の資料照合残件は機能Blockerとして数えない
- BackendとのEndpoint / Method / Request / Response / Error / Version / CSRF契約が一致
- Import Preview後Version競合 / Partial Register / Source Lock Testが通る
- PC変更・解除時のEndPcState消失確認とAtomic保存Testが通る
- Cookie / CSRFを実Browserで確認
- Keyboard / Focus / Reduced Motion / 390x844 / 767-768 / 1199-1200 / Low-height Desktop QA完了
- Production Deferredを正式完成扱いしない

現時点判定：

> **全体完成判定は保留。**

# 62. Codexへの最終指示

本書は「一般的なReactアプリを作るための参考」ではなく、卓回廊Frontendの実装仕様である。

既存Visualを「もっと一般的なUI」に改善しようとしない。

特にDesktop卓詳細v5.9は、React化を理由にVisual構造を変更しない。

2026-10-01改訂で特に禁止する逆戻り：

- PCを単一Game Systemへ戻す
- Previous EndPcStateを別Read Only欄へ戻す
- PL変更にPC Detachを戻す
- Import APIを旧簡略Contractへ戻す
- Common ErrorをObject型`fieldErrors`へ戻す
- Quoteを`.length`だけで数える
- 409を自動再試行する

不明点を独自判断で仕様化せず、コード内へ：

```text
TODO_SPEC_CONFIRMATION
```

を残す。

現時点の主な残TODOは本書末尾の監査で確認すること。

---


# 63. 残TODO_SPEC_CONFIRMATION【2026-10-06再整理】

残TODOは「5項目だけ」としない。現在の未確定は次のとおり。

1. **Game System ProfileのMVP初期Active Profile集合 / 各Profile実データ**
   - Generic JSON Schema / Renderer / Validator Contractは2026-10-06一旦採用として具体化。
   - `coc_7e_v1`は資料上のProfile Key例であり、MVP全体の初期対応System集合を勝手に推定しない。
2. **Previous EndPcState候補取得APIの最終Endpoint / Request Shape**
   - Cutoff / Current Table除外 / exact profileKey / Low Confidence Ruleは確定。
3. **CCFOLIA Mapping Configの最終Field Mapping**
4. **CCFOLIA Preview APIでprofileKey / resolved canonical systemをどう渡すか**
5. **Tekey / Udonariumを同一Adapterへ載せる具体Contract**
6. **CCFOLIA `iconUrl`をPC画像として利用する正式仕様**

ImportのRegister version / Re-Analyze / Session mutation Lock、Resource membership、CandidateData DTO、Image HTTP Contractは本版でContractを具体化したため「未記述TODO」からは外す。ただし **2026-10-06一旦採用** であり、ユーザー明示承認前に「既存確定仕様」と呼ばない。

ただし、R2で追加した **Resolution再開復元 / Split時Resolution引継ぎ / Merge時Resolution衝突処理 / Preview競合Error優先順位** の4点は、`2026-10-06T11:02:00+09:00`にユーザーが明示採用したため **【確定】** とする。その他の10/06一旦採用Contractまで自動的に確定したものではない。

---

# Appendix A. 2026-10-06訂正履歴（Frontend）

改訂日時：`2026-10-06T10:34:09+09:00`。

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| FE-R2-01 | Appearance録画は卓詳細経由、再折りたたみ任意 | `▶ 振り返る`は録画URLへ直接、`表示を減らす`必須 | VisualResponsive No.52 / 【確定・復元】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-02 | PC画像Keyboardが概略のみ | Arrow約1%、Shift約5%、Clamp、中央に戻す=Positionのみ | VisualResponsive No.57-58 / 【確定・復元】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-03 | Hamburger項目省略 | Global Nav 4項目、除外項目、Focus Trap/Restoreを復元 | VisualResponsive No.48 / 【確定・復元】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-04 | Ghost Slot省略 | Selector末尾`＋`、selected対象外、Table Edit +PLへ | VisualResponsive No.44 / 【確定・復元】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-05 | Date Popover/PART条件が概略 | Trigger、Keyboard、Close、PART表示/非表示を復元 | VisualResponsive No.50/59 / 【確定・復元】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-06 | ARIA概略 | ComboBox/Tab/Dialog/Scenario Form詳細ARIAを復元 | VisualResponsive No.53-54 / 【確定・復元】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-07 | 外部画像候補の保持・同意不足 | 未採用はForm Draft限定、離脱等で破棄、User初回正式採用時のみ同意。Session boolean + Save commandで連携 | BackendDataSecurityPlatform §5-6 + Physical Design / 【確定復元 + HTTP連携のみ一旦採用】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-08 | RegisterはcandidateIdsだけ | Session/Candidate versionを送信、Autosave Barrier追加 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-09 | candidateData/Resolution関係不明 | 共通CandidateData Shape + ResolutionChanges/Refsを明記 | Import確定概念をDTOへ具体化 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-10 | Image Mutation Field不明 | Multipart Field、Transform JSON、DELETE queryを固定 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-11 | 未命名卓の採番順不明 | createdAt ASC→id ASCを基準 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-12 | Preview→ApplyのSession Versionが暗黙 | Bulk/Split/Merge Applyにも同じexpectedSessionVersionを明示 | Import競合監査 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| FE-R2-13 | Register Candidate Version競合がCandidate失敗か409か曖昧 | 全Candidate Versionを処理前検証し、1件でも不一致ならRequest全体409・登録0件 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |

## Appendix A.1 9/30 R1・後発確定事項の継承方針

本書は9/30 R1を消去・置換しない。今回直接確認できた9/30確定資料から、少なくとも以下を本文へ再転記した。

- Hamburger Global Nav / 除外項目 / Focus Trap・Restore
- +PL Ghost Action Slot
- TableDate Popover / PART表示条件
- Mobile卓詳細のPortrait → Selector → PC/EndPcState順
- Appearances最新5件 / `表示を減らす` / 直接録画Action
- ComboBox / Tab / Dialog / Scenario Form ARIA
- PC画像Arrow約1% / Shift約5% / Clamp / Position-only reset
- External Image初回User同意 / 未採用CandidateのForm Draft限定保持

手元の`2026-09-30 Frontend Codex`は2561行で、ユーザー監査が参照した2708行版R1そのものではない。そのため、存在を確認できないR1固有文言を推測で複製せず、内容は一次の9/30確定資料とユーザー監査提示で裏付けられる範囲だけ継承する。


# Appendix B. 2026-10-06 R2追補訂正履歴

追補日時：`2026-10-06T10:49:25+09:00`。

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| FE-R3-01 | ResolutionRefsのみでReload後の判断復元不能 | Candidate DetailへResolution DTO同梱、Session Resolution一覧GET、`useResolutionId` | 再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-02 | Split/MergeのResolution/状態継承不明 | 明示Binding、Conflict Preview、UNCHECKED化、Target State規則 | 再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-03 | PC Error Summaryが概略 | No.63どおり1件Field Focus・Summaryなし、2件以上Summary Focus | VisualResponsive No.63 / 【確定・復元】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-04 | Growth/Aftereffectsの表示細則欠落 | No.49どおり改行保持・空欄非表示・展開/折りたたみ | VisualResponsive No.49 / 【確定・復元】 | `2026-10-06T10:49:25+09:00` |
| FE-R3-05 | 外部画像R1照合が機能HOLDに混在 | 資料照合残件・非Blockerへ移動 | 確定資料再確認 / 【分類修正】 | `2026-10-06T10:49:25+09:00` |


# Appendix C. Import Contract採用確定履歴

採用日時：`2026-10-06T11:02:00+09:00`。

ユーザーの明示採用により、R2で一旦採用だった次の4点を正式仕様へ昇格する。

1. ResolutionのReload / 再開復元
2. Split時のResolution明示引継ぎ
3. Merge時のResolution衝突・破棄・Target State処理
4. Preview競合Error優先順位（Preview前stale=`OPTIMISTIC_LOCK_CONFLICT`、成功Preview後Apply snapshot stale=`IMPORT_PREVIEW_STALE`）

この採用は上記4点だけを対象とし、Profile Schema、Image HTTP、未命名卓採番その他の10/06一旦採用事項を自動的に確定しない。


# Appendix D. 2026-10-06 R3追補訂正履歴

R3追補記録時刻：`2026-10-06T11:32:57+09:00`（Asia/Tokyo）

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| FE-R4-01 | Resolutionを設定/Bindできるが未設定へ戻すWire Contractなし | `clearResolution: true`とBulk `CLEAR_RESOLUTION`を追加。Candidate Slot参照だけ解除し共有Resolution本体は削除しない | Import一括適用「正式登録前なら取り消し可能」【確定・復元】 + Wire具体化【当時一旦採用 → 2026-10-06T11:41:14+09:00 確定】 | `2026-10-06T11:32:57+09:00` |
| FE-R4-02 | Mergeで`resultRegistrationTarget`省略時の意味が曖昧 | Source全件一致なら共通値継承、不一致なら明示必須 | Merge Contract補完【当時一旦採用 → 2026-10-06T11:41:14+09:00 確定】 | `2026-10-06T11:32:57+09:00` |

R3の新規Wire表現2点は既存確定要件を実装可能にするための具体化であり、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】として扱う。


# Appendix F. 2026-10-06 R3追加Import Contract 採用確定

採用日時：`2026-10-06T11:41:14+09:00`（Asia/Tokyo）

ユーザーの明示採用により、R3で一旦採用だった次の2点を正式仕様へ昇格する。

1. `clearResolution: true` / Bulk `CLEAR_RESOLUTION` によりCandidate SlotのResolution参照だけを解除し、共有Resolution本体・他Candidate・既作成Formal Entityを削除しない。
2. Mergeの`resultRegistrationTarget`は、Source全件一致時は省略可能で共通値継承、不一致時は明示必須。

この採用は上記2点だけを対象とし、Profile Schema、Image HTTP Contract、未命名卓採番その他の10/06一旦採用事項を自動的に確定しない。


# Appendix G. 2026-10-06 最終精査・訂正履歴

| 修正日時 | 対象 | 旧記述 | 訂正後 | 根拠 | 採用区分 |
|---|---|---|---|---|---|
| 2026-10-06 11:49 JST | 情報源優先順位 | 最優先の「本書」が旧R2表記 | `本書（2026-10-06横断精査修正版 R3 追加ImportContract確定版）` | 現在の直接正本名との一致 | 【確定・訂正】 |
| 2026-10-06 11:49 JST | Merge Preview Response | Conflict未解決でも`mergedCandidate.registrationTarget`を必ず解決済み値として返すよう読めた | Source混在・明示値なしでは`registrationTarget:null`、`registrationTargetConflict:true`、`canApply:false`。解決済みはboolean | Frontend / Backend DTOの意味的閉包、falseと未解決の分離 | 【当時一旦採用 → 2026-10-06 12:11確定】 |
| 2026-10-06 11:49 JST | 状態区分 | 3区分のみ記載 | 【確定】【確定・復元】【一旦採用】【TODO_SPEC_CONFIRMATION】【資料照合残件】の5区分を明記 | 今回の引継ぎルール | 【確定・文書整理】 |
| 2026-10-06 11:49 JST | Example JSON | 一部`json` fenceが断片 / Method行を含み構文上JSONでなかった | 完全JSONへ補正、または`text` fenceへ変更 | 機械検査で本文ContractとExample形式を一致 | 【確定・文書整形】 |

> **実装判定注記：** Appendix Fまでの「R3追加Import Contract 2点＝確定・GO」に加え、最終精査で具体化した`registrationTarget:null` Preview Response表現もユーザーが`2026-10-06T12:11:27+09:00`に明示採用したため【確定・GO】とする。


# Appendix H. Merge Preview Null Contract 正式採用

採用日時：`2026-10-06T12:11:27+09:00`（Asia/Tokyo）

ユーザーの明示採用により、最終精査で【一旦採用】としていた次のContractを正式仕様へ昇格する。

- Sourceの`registrationTarget`が混在し、`resultRegistrationTarget`が未指定のMerge Previewでは、`mergedCandidate.registrationTarget = null`を返す。
- 同時に`registrationTargetConflict = true`、`canApply = false`とする。
- `false`は「登録対象外へ解決済み」、`null`は「未解決」を意味し、Frontendは両者を区別する。
- `null`はMerge Preview Response DTOだけに許可し、正式Candidate / Merge Result / DB `registration_target`はbooleanのままとする。
- Request側の未選択は`resultRegistrationTarget`のField省略で表し、JSON `null`送信を未選択Contractとして追加しない。

採用区分：**【確定】**。実装判定：**GO**。
