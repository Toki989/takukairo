# 卓回廊 Frontend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-09-30

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

1. 本書
2. `TRPG活動履歴管理Webサービス_VisualResponsiveAccessibility仕上げフェーズ_確定事項まとめ_2026-09-30.md`
3. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md`
4. `TRPG活動履歴管理Webサービス_最新統合正本_2026-09-28_確定版_フロント制作補強修正版(3).md`
5. 2026-09-25各Visual Design資料
6. `TRPG活動履歴管理Webサービス_Desktop卓詳細_v5.9統合フェーズ_確定事項まとめ_2026-09-24.md`
7. `TRPG_PCFocus_v5_9(1).zip` Visual Reference
8. `TRPG_GAME_LIBRARY_FrontendPrototype_v5.zip` Interaction / QA Reference

ZIP内Mock Dataは仕様根拠にしない。

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

Tealを常時大面積で使用しない。

Action / Selection / Hover / FocusのAccentとして使う。

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
- CSRF TokenをState-changing requestへ送る
- JSON Error共通Parse
- 401 Session Expired処理
- 409 Optimistic Lock処理
- 429 Rate Limit処理
- AbortController対応

JWTを保存しない。

localStorageへ認証情報を保存しない。

---

# 15. Session Expired UX

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

Outcome：

```text
SURVIVED / LOST
```

LostはBurgundy Accentを使用可。

## 19.10 Recording Action

Recording URLあり：

```text
セッションを振り返る
```

等のAction。

URLなし時にAction自体を隠すかDisabled表示するかは正式未確定。

Test実装では **Disabled表示**を採用してよいが：

```text
PROTOTYPE_TUNABLE / TODO_SPEC_CONFIRMATION
```

をCommentへ残す。

Disabled：

- Hoverなし
- pointer actionなし
- aria-disabled
- 色だけに依存しない

---

# 20. Mobile卓詳細

Desktop 3 Laneを縮小しない。

Mobile専用再配置。

基本：

```text
Header
Active Portrait / Participation Poster
PC名 / PL
Spotlight
Status / Growth / Aftereffects
Character Selector / Flick
Recording Action
```

Character Flick：

- Active Participation中央
- 前後Peek
- 1 Swipe = 最大1 Participation
- Swipeだけに依存しない
- Tap / Selector / Keyboard相当を用意

正確なGesture ThresholdはPrototype調整可能。

---

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

Profile：

- PC名
- Current PL
- Game System
- Character Sheet URL
- Edit PC
- Change PL
- Edit PL display name
- Appearances

Table当時のHO / EndPcStateをPC本体Profileへ混ぜない。

## 22.1 Return to Collection

維持：

- Search
- Filter
- Sort
- Scroll
- original item focus

## 22.2 Appearances

PC Focus内の縦型Activity Archive。

基本一覧：

- Scenario名
- TableName
- TableDate
- 卓当時PL等必要Context

一覧へ追加しない：

- KP全員
- 他PL / PC全員
- HO
- Quote
- Recording URL詳細
- EndPcState

詳細は卓詳細へ。

件数が多い場合：

```text
すべて表示（N）
```

PC Focus内で展開。

別一覧Pageを作らない。

---

# 23. PC Change PL Mode

PC Focusから同一PL / PC画面内の専用Modeへ。

別の完全独立管理画面にしない。

表示：

- PC
- Current PL
- New PL Searchable ComboBox
- `＋ 新しい人物を追加`
- Related past Participations
- Update target checkbox
- Detach PC option where needed
- Impact summary

明示：

> PCの現在PLを変更しても、過去の卓当時PLは自動では変更されない。

対象ParticipationはUserが選択。

保存後：PC Focusへ戻る。

Mobileでは縦Layout。

過去Participationを横Scroll表にしない。

---

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

## 25.3 Image

Radio設定画面化しない。

直接操作：

- Preview
- Upload
- External Candidate
- Change
- Delete

Upload削除後にExternal Candidateへ自動復帰しない。

External ImageはBrowserから外部Host参照。

元Scenario Pageへの導線を維持。

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

## 26.1 New Fields

- PC名 required
- Current Person required
- PC image optional
- Game System optional
- Character Sheet URL optional

Person：Searchable ComboBox。

末尾：

```text
＋ 新しい人物を追加
```

Small DialogでPerson作成。

作成後Formへ戻り自動選択。

Draftを保持。

SelfPersonを自動確定しない。

## 26.2 Edit

Edit対象：

- PC名
- PC image
- Game System
- Character Sheet URL

Current PLはRead Only。

PL変更は専用Modeへ。

## 26.3 Image Editor

同一画面内：

- Drag position
- Zoom
- Reset
- Change
- Delete

別Pageを作らない。

Keyboard代替：

- ArrowでPosition調整
- Zoom Slider
- Reset Button

Mobile：

- 1 finger drag
- Pinch Zoom
- Slider fallback
- + / - Zoom Buttonは表示しない

画像の透明Marginを勝手にTrimしない。

## 26.4 Error Summary

Error 1件：Inline中心。

Error 2件以上：Form上部にSummary。

Summary item選択 →該当FieldへFocus。

---

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

## 27.7 PC optional

Label：

```text
PC（任意）
```

PCなし保存可。

PCなし：EndPcStateを表示しない。

## 27.8 HO / Quote

Accordionへ隠さない。

Desktop横並び可。

Mobile縦積み。

Quote max 24 chars。

括弧を自動追加しない。

## 27.9 EndPcState

PC選択時に自動表示。

`状態を追加` Buttonは置かない。

Current saved valueとPrevious candidateを混同しない。

Structure：

```text
今回終了時 [inputs]
前回の参考値 [read-only assist]
```

Game System ProfileからStatus inputを生成。

Growth / Outcome / AftereffectsもProfileに応じ表示。

## 27.10 CCFOLIA

Button：

```text
ココフォリア駒データから入力
```

Paste Dialog。

Preview結果をCurrent EndPcState Formへ候補反映。

Userが編集可。

失敗してもManual入力継続。

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

## 28.2 Entry

Current Sessionなし：

→ STEP01を直接表示。

Current Sessionあり：

```text
前回の途中データがあります
Current Step
Checked count
Expiration
[続きから再開]
[新しくインポート]
```

優先：

```text
続きから再開
→ Session情報
→ 新しくインポート
```

New Import選択時は破棄確認。

## 28.3 INPUT

Desktop：

- Drag & Drop
- File Select
- Pasted Text

FileとTextを同Sessionで併用可。

Radioで二者択一にしない。

Mobile：通常File Select中心。

対象：CSV / MD / HTML / TXT等。

PDFはMVP外。

## 28.4 Analysis Loading

全画面を不要にLockしない。

Long processで実Progressが取得できる場合だけ%表示。

偽%禁止。

## 28.5 REVIEW Summary

上部：

- Candidate total
- Registerable
- Needs review
- Needs fix
- Checked / total
- Auto Save status

Filter：

- すべて
- 登録可能
- 要確認
- 修正必要
- 未確認
- 確認済み

## 28.6 Candidate Card

ここは意味のあるSurfaceとしてCard可。

表示：

- Scenario
- TableDate
- KP
- PL
- PC
- Candidate Status
- Register target ON/OFF
- Checked state

StatusはColorのみで表現しない。

## 28.7 Register target vs checked

別State。

Initial：

```text
REGISTERABLE -> target ON
NEEDS_REVIEW -> target OFF
NEEDS_FIX    -> target OFF
```

## 28.8 Auto Save

Header付近Persistent Status：

```text
保存しています…
保存済み
保存できませんでした・再試行
```

毎回Toastにしない。

失敗してもLocal Draft保持。

## 28.9 DETAIL

必要Candidateだけ。

- Candidate / parsed data
- Original source
- Source file name
- Source position / trace
- Unassigned values
- Split / Merge preview

元SourceはRead Only判断材料。

未割当を勝手に捨てない。

Split / MergeはPreview後明示操作。

修正後はREVIEWへ戻る。

## 28.10 REGISTER

正式Data化直前。

Primary：

```text
18件を登録
```

件数を表示。

Double Submit防止。

Candidate単位でPartial Failureを表示。

失敗してもImportSession Stateを失わない。

## 28.11 Complete

表示：

- 登録Table数
- 新規Scenario / Person / PC
- 再利用Scenario / Person / PC

Action：

- ホームで確認
- 続けてインポート

「既存情報を何件再利用できたか」を成果として見せる。

---

# 29. Searchable ComboBox ARIA

Person / PC / Game System等。

基本：

```text
role=combobox
aria-expanded
aria-controls
aria-activedescendant
```

Popup：

```text
role=listbox
```

Option：

```text
role=option
aria-selected
```

Keyboard：

- Arrow Up / Down
- Enter
- Escape
- Home / End必要時

Openした時点で候補が見える。

Search必須にしない。

---

# 30. PL Tabs ARIA

```text
role=tablist
role=tab
role=tabpanel
aria-selected
aria-controls
```

Arrow keyでTab移動。

FocusとSelectionの挙動を一貫させる。

ErrorあるTabにはText / Icon等で示す。

---

# 31. Dialog

必須：

- `role=dialog`
- `aria-modal=true`
- accessible title
- initial focus
- focus trap
- Escape close（重大処理途中を除く）
- close後triggerへfocus restore

Backdrop clickだけをClose手段にしない。

重大判断：Toastで代用しない。

---

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

# 36.1 Scenario / PC Save Orchestration

Backend契約に合わせ、Scenario / PC Formの画像はBrowser側Draftとして保持し、保存時は次の順に統一する。

```text
1. Metadata JSON Save
2. Entity ID / new version受領
3. Pending image changeがある場合だけImage API
4. 全処理成功後に通常Navigation
```

画像処理が失敗し、Metadataだけ保存済みになった場合：

- 「保存全体が失敗した」と偽らない
- `基本情報は保存されましたが、画像の保存に失敗しました。` と区別する
- 選択済みFile Draftを可能な限り保持する
- 画像だけ再試行できる
- Existing image replaceではBackendが旧画像を維持するため、Previewも旧保存状態へ戻せる

Scenario / PCで異なるSave方式を混在させない。

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

Dialog / Inline Noticeで：

```text
別の更新が行われています。
最新の内容を読み込み直して確認してください。
```

Userの入力を即破棄しない。

可能ならCurrent Draftを保持し、Reload前にCopy / Compareできる余地を残す。

自動上書きしない。

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
1366 x 768
1440 x 900
1672 x 941
1680 x 800
1920 x 1080
390 x 844
```

卓詳細は：

- 1 / 2 / 3 / 4 / 5 / 6 / 7+ PL
- active left edge / right edge
- low height desktop

を確認。

横スクロールを発生させない。

---

# 47. Interaction QA

Keyboard：

- Home Gallery Focus
- ComboBox Arrow / Enter / Esc
- PL Tab Arrow
- Dialog Tab / Shift+Tab / Esc
- 卓詳細 Selector ArrowLeft / ArrowRight / Home / End
- PC image position keyboard

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

# 52. TableName Fallback

未命名Tableを表示するときのみ：

```text
卓1
卓2
...
```

現在の未命名Table一覧から画面側で採番。

削除で詰め直す。

Backendへ保存しない。

Inputへ自動セットしない。

---

# 53. TableDate Presentation

卓詳細Header：

```text
0 -> non-display
1 -> date
2 -> both
3+ -> 主要表示 + 他N日 + all dates popover
```

Session Archive：全件表示。

複数日を期間 `9/1-9/3` と勝手にまとめない。

`PART 1`等補助Visualは日付より弱く、Badge/Pill乱用しない。

---

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
- Session renewal final decision
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

# 61. Frontend Definition of Done

テスト実装として完成とみなす条件：

- Dev固定UserでAppへ入れる。
- Home GalleryがBackend Seedを表示する。
- Search / Filter / Sortが動く。
- Scenario View / Session Archiveが動く。
- Scenario Create / Edit / Delete conditionが動く。
- PC Collection / PC Focusが動く。
- PC Create / Edit / Change PL / Person name editが動く。
- Table Create / Edit / Deleteが動く。
- PL Tabs / KP-SKP UI / PC optional / EndPcStateが動く。
- CCFOLIA previewが動く。
- Desktop卓詳細がv5.9 Visualを維持してAPI Dataで動く。
- PCあり / imageなし / PCなし / Participation 0を表示できる。
- HO / Quote Spotlight切替が動く。
- Game System Profileに応じStatusが可変表示される。
- Import INPUT→REVIEW→DETAIL→REGISTER→COMPLETEを操作できる。
- Offline / Network / 401 / 409 / 429 / 500状態を再現できる。
- Keyboard主要操作が成立する。
- Focus Ring / Dialog Focus Trap / Restoreが成立する。
- Reduced Motionが成立する。
- 390x844で横Scroll破綻しない。
- Desktop 1366x768〜1920x1080で卓詳細中央レーンが衝突しない。
- Production Deferred項目を正式完成扱いしない。

---

# 62. Codexへの最終指示

本書は「一般的なReactアプリを作るための参考」ではなく、卓回廊Frontendの実装仕様である。

既存Visualを「もっと一般的なUI」に改善しようとしない。

特にDesktop卓詳細v5.9は、React化を理由にVisual構造を変更しない。

不明点を独自判断で仕様化せず、コード内へ：

```text
TODO_SPEC_CONFIRMATION
```

を残す。

実装都合でComponent分割・Hook・Utilityを追加することは可能だが、Formal Data / UI意味 / Navigation / Business Ruleは変更しない。
