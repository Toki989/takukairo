# 卓回廊 Frontend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-09-30

最終改訂：2026-10-01 11:09:17 JST (UTC+09:00) / R1（修正履歴：§63）

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

参照資料の責務範囲を守り、明示変更された項目だけを後発の確定事項で上書きする。旧資料の未確定一覧を理由に、別資料で採用済みの事項を未確定へ戻さない。添付回答中の提案はUser採用まで正式仕様へ昇格させない。

仕様書を修正する場合は、本文を現行仕様へ訂正し、修正履歴に対象節・旧記述（転記漏れなら「未記載」）・訂正後・根拠・採用区分・修正日時（JST / UTC+09:00）を必ず追記する。過去の修正履歴は上書きしない。

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

> 44〜48px程度は推奨目安。全操作の機械的な必須最低値にはしない。

Header主要Icon等は可能なら44px前後。小さいText Action / Micro Switch / `他N日`等はVisualを壊してまで拡大せず、隣接操作とのSpacingを確保する。Invisible Hit Areaを隣要素へ侵食させず、Hit Area同士を重複させない。

アイコン自体を巨大化するのではなくhit areaを確保し、実機で押しやすさとVisualを確認する。

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
- Login / Logoutで旧CSRF Tokenが無効になった場合は、新Tokenを取得してから次のState-changing requestへ送る
- Multipart画像UploadでもCSRF Headerを送る
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

## 16.1 SelfPerson初回設定

認証済みUserにSelfPersonがない場合は `/setup/self-person` で「TRPGで使う名前を設定」を表示する。

- 入力は表示名のみ。必須 / 空文字不可 / 同名Person可。
- スキップ不可。利用しない場合はLogoutできる。
- Person作成とUser.selfPerson設定の成功後、短い成功Feedbackを経てHomeへ進む。
- Google表示名をTRPG表示名として自動確定しない。
- 長い初回Tutorialを追加しない。

この機能は既存確定仕様。作成・紐付けを確定するBackend API Contractは未補完のため、§64のAPI補完対象として扱い、仮APIを正式採用済みとしない。

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

HamburgerはGlobal Navigationのみ。順序は `ホーム / PL-PC / インポート / 区切り / アカウント設定` とする。Back / Current Scenario / 卓編集 / セッションを振り返る / Logout / Terms / Privacyは含めない。Quiet Overlay / Panelで、Escape Close / Focus Trap / Focus Restoreを維持する。

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

PCなしSlotはabstract表現 + `PC 未登録`。人型Silhouetteを主方式にしない。

Selector末尾のGhost Action Slot `＋` は通常Slotより弱く表示し、Hover / Focus / Tapで `＋ PLを追加` を示す。架空Participationを作らず、`selectedParticipationId`対象外とする。Clickで既存Table Editの＋PL Flowへ進む。

選択中PCなしParticipationには `＋ PCを設定` を用意し、既存PC選択 / 新規PC作成の両方へつなぐ。これらはVisual仕上げNo.44の一旦採用事項であり、明確な操作性問題が確認された場合のみVisualを再調整する。

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

両方 → `HO ｜ セリフ` のMicro Switch。選択中のみAccent Strongの細いUnderline / Rule、非選択はNeutral。大きなRounded Tab / Segmented Controlにしない。

なし → 非表示

固定 `QUOTE` Labelを勝手に付けない。

Quote本文へ自動括弧を付けない。

## 19.9 EndPcState

Game System Profileに応じてStatusを可変表示。

SAN / HP / MP固定UIとして実装しない。

`statusValues`をProfile順に描画。

Growth / Aftereffectsは主要STATUSより弱く表示。改行を保持し、`pre-wrap` / `overflow-wrap` を用いる。空欄は非表示。短文は全文、長文はCompact Preview + `全文を見る`、展開後は `折りたたむ`。Skill / before / afterへの構造化、自動Bullet / Parsing / 要約は行わない。Exact clampはPrototype調整とする。

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

等のActionからTable.recordingUrlへ移動できる。

**Recording URLなしの場合はActionをrenderしない。Desktop / Mobile卓詳細ともに同じ条件とする。**

URLなしのDisabled Actionをテスト用の仮仕様として表示しない。

Mobile / Appearancesの非表示は9/30 Visual採用済み仕様。Desktop卓詳細にも非表示を適用する点は、2026-10-01のUser回答で採用された仕様統一として記録する。

---

# 20. Mobile卓詳細

Desktop 3 Laneを縮小せず、Mobile専用に再配置する。

基本順序：

```text
Header
Full-width Active Portrait Hero / Participation Poster
Horizontal Participation Selector / Flick
PC名 / 卓当時PL
Status / Growth / Aftereffects のVertical Flow
Recording Action（URLあり時のみ）
```

HO / Quote SpotlightとMicro SwitchはPortrait周辺へ配置する。SelectorはPortrait直後に置き、先にParticipationを選んでからそのPC / EndPcState情報を見る操作構造を維持する。

HeaderはBack / SESSION / Scenario名 + External Link / Table編集 / Hamburger / TableName / TableDate / KP contextを扱う。StatusはProfile-drivenのwrap gridとし、横Scrollにしない。PCなしではSTATUS / EndPcStateを表示しない。

Character Flickは `PROVISIONAL_BUT_IMPLEMENT_FOR_TEST`：

- Active Participation中央、前後各1件Peek。
- Gesture開始目安約12px。横移動量が縦移動量の約1.25倍以上なら横Swipe扱い。
- Slot幅約25%以上移動で切替。明確な高速Flickなら短距離でも切替可能。
- 1 Swipe = 最大1 Participation。Activeを中央Snapし、Loopしない。
- 左右PeekはTap可能。`‹ / ›`をGesture代替として併設し、Keyboard相当も維持する。
- PC画像あり / PC画像なし / PCなし / ＋PL Ghost Slotを扱う。
- Reduced Motion対応。Peek幅・Snap感・Gesture閾値はPrototype / 実機で明確な問題があれば再調整可能。

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

MobileはDesktop 2Columnをそのまま縮小せず、戻る → Portrait → PC名 → 現在PL → Game System → Character Sheet → Actions → Appearancesの縦構成とする。

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
- TableDate。複数日は全件確認可能、0件は日付表示なし。
- 現在PLと卓当時Participation.personが異なる場合のみ、当時PLを補足表示。

並び順は最近遊んだTableから古いTableへ。各Tableの最新TableDateを降順基準とし、日付なしは後方に置く。

KP全員 / 他PL・PC全員 / HO / Quote / 生のRecording URL詳細 / EndPcStateを一覧の基本情報へ追加しない。詳細は卓詳細へ。

初期表示：

- 0件 → Empty State。
- 1〜5件 → 全件。
- 6件以上 → 最新5件 + `すべて表示（N）`。
- 展開後は同一PC Focus内で全件表示し、`表示を減らす`で再折りたたみ。
- Desktop / Mobileで件数を変えない。展開状態をAccountへ永続保存しない。

Row本体の選択は対象Tableの卓詳細へ移動し、該当Participationを初期選択する。PC Entity IDだけで選択を決めず、Participation Contextを渡す。

Recording URLあり → Secondary Action `▶ 振り返る`。Row本体の卓詳細Navigationとは別に、直接外部録画URLへ進む。

Recording URLなし → このSecondary Actionをrenderしない。これは生のRecording URL詳細を一覧へ追加することとは区別する。

卓詳細から戻る場合は元PC Focusへ戻り、可能な範囲で選択PC / Appearances展開状態 / Scroll位置を維持する。別一覧Pageを作らない。

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

初期状態は関連Participation全件チェックON。UserはON / OFFを変更し、ONは新Personへ更新、OFFは元のPersonを維持する。初期ONは保存前の選択状態であり、画面を開いただけで過去Dataを変更しない。

変更件数 / 元のPersonのまま残す件数をリアルタイム要約する。PL変更だけではPC / EndPcStateを変更しない。別操作の明示detachを選択した場合のみEndPcState削除を伴うため、失われるDataを事前確認する。

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

未採用のExternal CandidateはForm内の一時Stateだけに保持し、Page離脱 / Reload / Cancelで破棄する。正式Scenario Data / DB Draft / localStorage / IndexedDBへ保存しない。

Uploadを採用した場合は未採用のExternal Candidateを破棄し、Upload削除後のFallbackにしない。必要なら再取得する。

Userが`この画像を使用`を選択しScenarioを保存した時だけ、正式External Image URLとして保存する。外部Image Fileを卓回廊のObject Storageへ複製しない。

外部画像をScenario画像として実際に採用しようとした最初の1回に説明・同意を行う。情報取得 / 候補取得 / Previewでは要求しない。同意済みUserにScenarioごとに繰り返さない。同意はRights Holderの許諾を意味しない。§42にも同じTimingを適用する。同意状態のAPI契約は§64で補完待ちとする。

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

同一画面内でDrag position / Zoom / 中央に戻す / Change / Deleteを行う。別Pageを作らない。

DesktopではZoom ControlsをPortrait直下に置き、順序は `− → Slider → ＋ → 中央に戻す`。Slider幅はPortrait幅の55〜65%程度、Desktop目安200〜260px。±は小さな補助Buttonとし、画像上へOverlayしない。画像変更 / 削除はZoom操作から一段分離する。

`中央に戻す`はPositionだけを中央に戻す。Zoomまで完全Resetしない。

Keyboard代替：

- Portrait編集領域をFocus可能にする。
- Arrowで表示枠約1%相当移動、Shift + Arrowで約5%相当移動。
- Drag / Keyboardは同じPosition Metadataを更新する。移動方向は画像そのものの方向。
- 空白が出ない位置までClampする。
- Focus Ringと短い操作説明を表示する。方向Buttonを大量Overlayしない。
- Zoom Sliderと中央に戻すButtonを使用可能にする。

Mobile：1 finger drag / Pinch Zoom / Slider / 中央に戻す / Change / Delete。＋／− Zoom Buttonと方向Buttonを常設しない。Keyboardを利用できる場合は同じ位置操作を許容する。

画像の透明Marginを勝手にTrimせず、原画像を変更しない。

## 26.4 Error Summary

常設しない。

- Error 1件：Inline Errorのみ。該当FieldへFocus。Summaryなし。
- Error 2件以上：Inline Errorを維持しProfile上部へ簡潔なSummaryを表示。SummaryへFocus。各項目からFieldへ移動可能。

Save / Network ErrorとValidation Error Summaryを混同しない。

## 26.5 Entry Context / Save Navigation

- PL / PC Collection起点のNew → 保存成功後、新PCのPC Focus。
- Table Form起点のNew → 元Table Formへ復帰し新PCを自動選択。元Form Draftを保持し、通常のPC Focus遷移よりReturn Contextを優先する。
- PC Focus起点のEdit → 保存後、元の対象PC Focus。
- Delete → 成功後PL / PC Collection。削除済みPC Focusへ戻らない。

Table Form内の作成をModal / Overlayとする具体的な表示形態は、別途実装契約の整理対象とする。上記のDraft保持・復帰先は確定仕様として守る。

## 26.6 PC Delete / Safe Image Replacement

PC編集画面最下部に通常Contentから離して削除入口を置く。Saveの隣やPC Focusに強い削除Actionを常設しない。

関連Participationが0件のPCだけ削除可。参照がある場合は削除不可を説明しActionをDisabledにする。削除のためにParticipation / EndPcStateを自動削除・解除しない。

削除可能時のみ対象PC名と元に戻せないことを示すConfirm Dialogを表示する。PC名再入力は要求しない。

画像変更 / 削除はForm内の変更予定状態として扱う。新状態の保存成功後に正式採用し、必要に応じ旧画像を安全に削除する。保存失敗時は旧状態を維持する。画像とMetadataの保存API順序は§64の補完対象。

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

Person候補末尾に`＋ 新しい人物を追加`を置き、小Dialogで表示名を作成する。完了後元Formへ戻り作成Personを自動選択し、元Draftを保持する。

PC候補から`＋ 新しいPCを追加`へ進める。作成後は元Table Formへ戻り新PCを自動選択し、元Draftを保持する（§26.5）。

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

PC変更 / 解除時にEndPcState等が存在する場合は、User確認前に削除しない。PC-Aの卓終了時状態をPC-Bへ引き継がない。対象PC名を明示し、`PCを変更すると、この卓での「PC名」の卓終了時状態が削除されます。`等で確認する。詳細Status値一覧をDialogへ載せない。

PL→KPでは存在するPC / HO / Quote / EndPcStateが解除・削除対象。Dataが失われる場合のみ事前確認し、PersonとParticipation自体は維持する。KP→PLはPC未設定でも保存可。

Participation.personの変更は卓当時記録の修正であり、PC本体の現在Personを変更しない。

## 27.12 Save Navigation

New → Scenario View、New TableをHighlight。

Edit Scenario unchanged →元Scenario Viewで対象Tableを見える状態にして短くHighlight。

Scenario changed →変更後Scenario Viewで対象Tableを見える状態にして短くHighlight。

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

正式登録済みCandidateはImportSession側から再編集しない。修正は正式Data側で行う。

Auto Saveはversionで古い画面 / 別Tabからの更新競合を検知し、新しい保存内容を古いDraftで上書きしない。Session / CandidateそれぞれのAPI契約は§64の補完対象。

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

未割当値はPL / PC / HO等へUserが設定する、またはUserが明示して使用しないと判断する。システムが勝手に破棄しない。

同名Scenario / Personを自動統合しない。Person候補が1件でも無言で確定しない。既存Scenarioを選択してもCandidate情報で既存Scenario本体を上書きしない。PC照合はPC名 + Person候補を強い判断材料とする。

同一条件候補への一括適用は明示操作で行う。対象件数と適用内容を事前確認し、正式登録前なら取消可能とする。自動適用しない。

重複Table候補は自動統合しない。`登録しない / 別Tableとして登録`をUserが選択する。

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

## Person / PC

Inputは`role="combobox"` / `aria-expanded` / `aria-controls` / `aria-autocomplete="list"` / `aria-activedescendant`。

Popupは`role="listbox"`、Candidateは`role="option"`、選択中は`aria-selected="true"`。DOM FocusはInputに保つ。

Down / Up / Enter / Esc / Tabで操作可能にする。Open時点で候補を表示し、Search必須にしない。Search文字列だけではPerson / PC Entity選択とみなさない。

`＋ 新しい人物を追加`はoptionではなく別Button / Actionとする。

## Scenario Form / Game System / BOOTH / Image

Native semantic HTMLを優先しLabelをControlへ関連付ける。PlaceholderをLabel代替にしない。Scenario名はnative `required`。Optionalへ無意味な`aria-required="false"`を乱用しない。

Game Systemはeditable Combobox。Candidate選択と自由入力の両方を正式値として許可し、`role="combobox"` / `aria-expanded` / `aria-controls` / `aria-autocomplete="list"`を設定する。

BOOTH取得中は対象Regionの`aria-busy="true"`・Duplicate Request防止・polite Live Regionを使用する。成功時の候補反映はaggregate Live Messageとし、各Field変更を過剰に読み上げない。取得失敗は手入力継続の案内としScenario Validation Errorにしない。

画像はnative file inputを維持しDrag & Dropだけにしない。Preview自体をActionにせず、Current SourceをTextでも表示し、外部候補の採用は普通のButton `この画像を使用`とする。

実際にInvalidなFieldだけに`aria-invalid="true"`を付け、Errorを関連付ける。Scenario Save Validation FailureはFirst InvalidへFocus。Duplicate ScenarioはErrorではなくNeutral / Warning Informationとし`aria-invalid`を付けない。PC Formの件数別Focusは§26.4に従う。

---

# 30. PL Tabs ARIA

`role="tablist"` / `role="tab"` / `role="tabpanel"` / `aria-selected` / `aria-controls`を設定する。

roving tabindex、Left / Right / Home / End、Automatic Activationを使用する。

Validation ErrorをTabの`aria-invalid`へ載せない。視覚Errorとaccessibleな`入力エラーあり`で伝える。

Save Validation Failure：最初のError PL TabをActivate → Panel表示 → 最初のInvalid FieldへFocus。

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

`他N日`だけをPopover Triggerとし、全TableDateを時系列表示する。Hover / Focus / Click / Enter / Spaceで開き、Esc / Outside Clickで閉じる。MobileはTap。背景付きPillにせず、細いUnderline / RuleとFocus Ringを使用する。

`PART 1 / PART 2 / ...`は同一Table内に複数TableDateがある場合だけ、時系列順に表示時導出する。DBへpartNumber等を追加しない。1日だけなら表示しない。

表示場所は卓登録 / 編集、Scenario View / Session Archive、TableDate全件Popover。Desktop / Mobile卓詳細Header本体では表示しない。PARTは日付より弱い小さなTypographyとし、Badge / Pill化しない。

---

# 54. Placeholder

PC image missing：

- Neutral
- PC自体は存在することが分かる
- PC名等を維持
- PC名Typography主体とし、人型Silhouetteを主方式にしない。
- PC名は基本左下寄り。大Portraitでは大胆に、Collectionは少し中央寄りへ補正、Selectorは最小限。
- 画面ごとに座標調整しつつ共通Visual Languageを維持する。`画像未登録`等の補助Textは弱く表示する。

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
- SelfPerson初回設定の必須入力 / スキップ不可 / 完了後Homeを確認する。API契約は§64 A-01の補完が必要。
- Appearancesの0 / 1 / 5 / 6件境界、展開/再折りたたみ、Rowの該当Participation初期選択、録画Secondary Actionを確認する。
- URLなしの録画ActionはDesktop / Mobile卓詳細・Appearancesのいずれにも表示されない。
- Mobile卓詳細のPortrait → Selector → PC / EndPcState情報の順序を確認する。
- PL変更の初期全件ONとON / OFFの履歴維持、PC作成の起点別復帰と元Table Draft保持を確認する。
- PC画像のArrow約1% / Shift約5% / Clamp / Positionのみ中央に戻す、PC削除条件と削除後Collection復帰を確認する。
- §64の実装に必要な未補完契約をTODOのままテスト実装完成として扱わない。

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

---

# 63. 修正履歴

## R1 — 2026-10-01 11:09:17 JST (UTC+09:00)

原資料照合による監査後の訂正。実装は行っていない。DesktopのURLなし録画Action非表示は今回のUser回答で採用し、それ以外の新提案API / DTO / 集計方式は保留とした。

旧記述欄は修正前の該当節の要約。転記漏れは「未記載」として示す。本文を訂正したうえでこの履歴を追記し、後続改訂でも過去履歴は保持する。

| ID | 対象 | 旧記述 | 訂正後 / 訂正方法 | 根拠 | 採用区分 | 修正日時 |
|---|---|---|---|---|---|---|
| F01 | §2 情報源の優先順位 | 資料順位のみ。修正記録の必須項目は未記載。 | 資料ごとの責務と後発差分を明記。今後の修正に旧記述・訂正後・根拠・区分・JST日時を必須化。 | 統合正本 §0.1–0.2 / Userの2026-10-01指示 | 編集運用の明文化 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F02 | §14 API Client Rule | CSRF送信のみ記載。Login / Logout後の再取得・Multipart送信が未記載。 | Token再取得とMultipart画像へのCSRF Headerを明記。 | S1–S6 §4.7 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F03 | §16.1 SelfPerson初回設定（追加） | §6にRouteのみ。入力条件・完了Flowが未記載。 | 必須表示名・スキップ不可・Logout・作成/紐付け後Homeを転記。API不足は別記。 | 統合正本 §6 A-07 / 画面詳細 §5 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F04 | §11 Touch Target | 44〜48px程度・hit area確保のみ。小Target時の例外と重複禁止が未記載。 | 推奨目安・Spacing・隣要素侵食/重複禁止を明記。 | Visual仕上げ §24 No.61 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F05 | §19.3 Selector | 通常PL Participationの選択のみ。PCなしSlot・Ghost Action・PC設定導線が未記載。 | 一旦採用済みのPCなしSlot・＋PL Ghost・＋PC設定を転記。 | Visual仕上げ §7 No.44 | 既存一旦採用仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F06 | §19.2 Header / Hamburger | Hamburgerの存在のみ。項目・除外項目・Focus動作が未記載。 | Global Navigationの項目・順序・除外項目・Focusを転記。 | Visual仕上げ §11 No.48 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F07 | §19.8 Spotlight | 「両方 → 切替UI」のみ。 | 採用済みのMicro Switchと選択表現を転記。 | Visual仕上げ §9 No.46 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F08 | §19.9 EndPcState | 主要STATUSより弱く表示することのみ。 | 改行保持・空欄非表示・長文展開/折りたたみ・非構造化を転記。 | Visual仕上げ §12 No.49 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F09 | 19.10 Recording Action | URLなしは正式未確定。TestではDisabled表示可。 | URLなしはDesktop / Mobileとも非表示へ統一。Disabled可の旧記述を削除。 | Visual仕上げ §14–15 / Userの2026-10-01回答 | 既存確定仕様の転記 + User採用 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F10 | 20. Mobile卓詳細 | Portrait → PC名/PL → Spotlight → Status → Selector。Gesture閾値は調整可能とだけ記載。 | Portrait → Selector → PC/EndPcState情報へ訂正。SpotlightはPortrait周辺。採用済みFlick初期値・代替操作・URLあり時のみActionを転記。 | Visual仕上げ §14 No.51 / §23 No.60 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F11 | 22.2 Appearances | 件数が多ければ「すべて表示（N）」のみ。Recording URL詳細は一覧へ追加しない。初期件数・録画Action・選択Participationが未記載。 | 最新5件・再折りたたみ・日時順・当時PL条件・Secondary録画Action・該当Participation初期選択を明記。生URL詳細の禁止とActionを区別。 | Visual仕上げ §15 No.52 / 統合正本 §10 P-08–P-08B | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F12 | §23 PC Change PL Mode | 対象をUserが選択することのみ。初期ONとリアルタイム件数が未記載。 | 初期全件ON・ON/OFFの意味・件数要約・PL変更とdetachの区別を転記。 | PL-PC Visual §7.7–7.9 / 統合正本 §5 C-10–C-12 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F13 | §25.3 Image / §42への適用 | Upload削除後に外部候補へ自動復帰しないことのみ。候補保持範囲・初回同意Timingが未記載。 | 未採用候補のDraft限定・Upload採用時破棄・正式保存条件・初回採用時の同意を転記。 | Backend追加確定 §5–6 / 9月29日修正版 §10 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F14 | 26.3 Image Editor | Reset / ArrowでPosition調整 / Slider fallbackのみ。操作量とReset対象が未記載。 | 中央に戻すはPositionのみ。Arrow約1%・Shift約5%、Clamp、Desktop操作順、Mobile代替を転記。 | Visual仕上げ §20 No.57 / §21 No.58 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F15 | 26.4 Error Summary | Error 1件Inline中心・2件以上Form上部Summaryのみ。PC保存/削除後NavigationとPC削除UIが未記載。 | Error件数別Focus・Summary位置を訂正。起点別Navigation、Draft保持、PC削除条件/Danger Zoneを転記。 | Visual仕上げ §26 No.63 / 統合正本 §18.3 / PC登録編集 Visual §20–28 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F16 | §27.6 Person / PC ComboBox | 候補表示のみ。新規Person / PC作成導線と復帰が未記載。 | 小DialogでPerson作成・新PC作成・自動選択・元Draft保持を転記。 | 統合正本 §12 T-06–T-07 / §18.3 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F17 | §27.11 Remove Participation / Relation変更 | Participation削除確認のみ。PC変更時の旧EndPcState解除・Role変更確認が未記載。 | PC-A状態をPC-Bへ引き継がないこと、解除/Role変更時の確認と履歴分離を転記。 | 基準確定仕様 §22 / 統合正本 §5 C-14 / 卓登録編集 Visual §14–15 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F18 | §27.12 Save Navigation | NewだけHighlight明記。Editは戻り先のみ。 | Editにも保存対象Tableの可視化と短いHighlightを転記。 | 統合正本 §18.3 / 卓登録編集 Visual §17 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F19 | §28.8 Auto Save | Draft保持のみ。登録済みCandidate再編集禁止とAuto Save競合が未記載。 | 登録済みCandidateは正式Data側で修正。versionによる古いAuto Save更新の検知を転記。 | 統合正本 §14 I-19 / 9月29日修正版 §13 Auto Save競合 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F20 | §28.9 DETAIL / 照合・一括適用 | 元Source比較とSplit / Mergeのみ。照合安全性、一括適用取消、未割当の操作が未記載。 | 既存Entityの自動統合/上書き禁止、未割当設定、明示一括適用/取消、Table重複判断を転記。 | 統合正本 §14 I-10–I-11 / I-17–I-18 / Import Visual §28–39 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F21 | 29. Searchable ComboBox ARIA | ARIA属性の基本のみ。Input Focus、文字列とEntityの区別、追加Actionのsemantics、Game System自由入力が未記載。 | 確定済みの詳細ARIA、Entity選択条件、Game System自由入力、Scenario/BOOTH/画像semanticsを転記。 | Visual仕上げ §16 No.53 / §17 No.54 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F22 | 30. PL Tabs ARIA | Arrow移動・FocusとSelectionを一貫させるとのみ記載。 | roving tabindex・Home/End・Automatic Activation・TabのError semanticsを転記。 | Visual仕上げ §16 No.53 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F23 | §53 TableDate Presentation | 主要表示+他N日・PARTは弱い補助Visualのみ。 | Popover Trigger/開閉操作、PARTの表示場所・複数日のみ・時系列導出・非永続化を転記。 | Visual仕上げ §13 No.50 / §22 No.59 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F24 | §54 Placeholder | Neutral・PC存在/PC名維持のみ。 | PC名Typography主体・Silhouette主方式なし・基本左下・画面別補正を転記。 | Visual仕上げ §8 No.45 / §25 No.62 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F25 | §22 PC Focus | Desktop構成のみ。Mobile PC Focusの順序が未記載。 | 採用済みのMobile縦構成を転記。 | 統合正本 §10 P-13 / PL-PC Visual §16 | 既存確定仕様の転記 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F26 | §61 Definition of Done | 上記転記事項の境界条件と未補完APIによる完成判定の制約は未記載。 | 転記済み動作の確認項目を追加。必要なAPI未補完のまま完成扱いしない。 | F03 / F09–F15 / F25の各本文と原資料 | 既存仕様の検証条件の明文化 | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F27 | §64 監査引継ぎ（追加） | 確定事項の転記不足と新規判断・API契約不足を分けた一覧は未記載。 | A-01〜A-09で区分と未採用案を整理。旧未確定一覧による採用済み仕様の巻き戻しを防止。 | 今回の原資料照合 / 添付ChatGPT回答 / User回答 | 監査結果の記録（新提案は保留） | 2026-10-01 11:09:17 JST (UTC+09:00) |
| F28 | 文書冒頭 | 作成日2026-09-30のみ。最終改訂日時は未記載。 | 原作成日を維持しR1の最終改訂日時と修正履歴参照を追加。 | Userの2026-10-01指示 | 改訂情報の明記 | 2026-10-01 11:09:17 JST (UTC+09:00) |

### 根拠資料の略称

- 統合正本：[TRPG活動履歴管理Webサービス_最新統合正本_2026-09-28_確定版_フロント制作補強修正版(3).md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_%E6%9C%80%E6%96%B0%E7%B5%B1%E5%90%88%E6%AD%A3%E6%9C%AC_2026-09-28_%E7%A2%BA%E5%AE%9A%E7%89%88_%E3%83%95%E3%83%AD%E3%83%B3%E3%83%88%E5%88%B6%E4%BD%9C%E8%A3%9C%E5%BC%B7%E4%BF%AE%E6%AD%A3%E7%89%88%283%29.md)
- Visual仕上げ：[TRPG活動履歴管理Webサービス_VisualResponsiveAccessibility仕上げフェーズ_確定事項まとめ_2026-09-30.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_VisualResponsiveAccessibility%E4%BB%95%E4%B8%8A%E3%81%92%E3%83%95%E3%82%A7%E3%83%BC%E3%82%BA_%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-30.md)
- S1–S6：[卓回廊_S1-S6_BackendDataSecurityPhysicalDesign_確定事項まとめ_2026-09-30.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/%E5%8D%93%E5%9B%9E%E5%BB%8A_S1-S6_BackendDataSecurityPhysicalDesign_%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-30.md)
- Backend追加確定：[TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_%E8%BF%BD%E5%8A%A0%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-30_BackendDataSecurityPlatform_%E7%B2%BE%E6%9F%BB%E4%BF%AE%E6%AD%A3%E7%89%88.md)
- 9月29日修正版：[TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-29_修正版.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_%E8%BF%BD%E5%8A%A0%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-29_%E4%BF%AE%E6%AD%A3%E7%89%88.md)
- PC登録編集 Visual：[TRPG活動履歴管理Webサービス_PC登録編集VisualDesign再設計フェーズ_確定事項まとめ_2026-09-25.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%92%E3%82%99%E3%82%B9_PC%E7%99%BB%E9%8C%B2%E7%B7%A8%E9%9B%86VisualDesign%E5%86%8D%E8%A8%AD%E8%A8%88%E3%83%95%E3%82%A7%E3%83%BC%E3%82%B9%E3%82%99_%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-25.md)
- PL-PC Visual：[TRPG活動履歴管理Webサービス_PL-PC_VisualDesign再設計フェーズ_確定事項まとめ_2026-09-25.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_PL-PC_VisualDesign%E5%86%8D%E8%A8%AD%E8%A8%88%E3%83%95%E3%82%A7%E3%83%BC%E3%82%BA_%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-25.md)
- 卓登録編集 Visual：[TRPG活動履歴管理Webサービス_卓登録編集VisualDesign再設計フェーズ_確定事項まとめ_2026-09-25.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_%E5%8D%93%E7%99%BB%E9%8C%B2%E7%B7%A8%E9%9B%86VisualDesign%E5%86%8D%E8%A8%AD%E8%A8%88%E3%83%95%E3%82%A7%E3%83%BC%E3%82%BA_%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-25.md)
- 画面詳細：[TRPG活動履歴管理Webサービス_各画面詳細設計フェーズ_確定事項まとめ_2026-09-16.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_%E5%90%84%E7%94%BB%E9%9D%A2%E8%A9%B3%E7%B4%B0%E8%A8%AD%E8%A8%88%E3%83%95%E3%82%A7%E3%83%BC%E3%82%BA_%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81_2026-09-16.md)
- 基準確定仕様：[TRPG活動履歴管理Webサービス_最新版確定仕様_9エンティティ14UC.md](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9_%E6%9C%80%E6%96%B0%E7%89%88%E7%A2%BA%E5%AE%9A%E4%BB%95%E6%A7%98_9%E3%82%A8%E3%83%B3%E3%83%86%E3%82%A3%E3%83%86%E3%82%A314UC.md)
- Import Visual：[#-Web制作総合演習1-#-TRPG活動履歴管理Webサービス-##-Import・横断UI-Visual-Design再設計フェーズ-確定事項まとめ-###.txt](%E4%BB%95%E6%A7%98%E6%9B%B8%EF%BC%88%E3%83%86%E3%82%B9%E3%83%88%E7%94%A8%EF%BC%89%E5%8F%82%E7%85%A7%E8%B3%87%E6%96%99/%23-Web%E5%88%B6%E4%BD%9C%E7%B7%8F%E5%90%88%E6%BC%94%E7%BF%921-%23-TRPG%E6%B4%BB%E5%8B%95%E5%B1%A5%E6%AD%B4%E7%AE%A1%E7%90%86Web%E3%82%B5%E3%83%BC%E3%83%93%E3%82%B9-%23%23-Import%E3%83%BB%E6%A8%AA%E6%96%ADUI-Visual-Design%E5%86%8D%E8%A8%AD%E8%A8%88%E3%83%95%E3%82%A7%E3%83%BC%E3%82%BA-%E7%A2%BA%E5%AE%9A%E4%BA%8B%E9%A0%85%E3%81%BE%E3%81%A8%E3%82%81-%23%23%23.txt)

添付回答：2026-10-01にUserが提供したChatGPT回答（貼り付けたテキスト.txt）。User回答：同日の「Desktopも含めて非表示に統一する」。添付回答の提案全体を採用したものではない。

---

# 64. 監査引継ぎ・未採用提案

未解決事項は以下の二種類を区別する。UI / 業務Ruleが確定していても、それを実現するAPI契約が不足する場合がある。添付回答の具体案は、この表で「未採用」とした範囲では正式仕様ではない。

| ID | 区分 | 確認できた確定事項 / 不足 | 次に整理する事項・未採用案 |
|---|---|---|---|
| A-01 | 確定FlowのAPI補完 | SelfPersonは表示名を必須で作成しUserへ紐付ける。スキップ不可。現行API一覧には初回設定を完了する契約がない。 | 作成+紐付けのRequest / Response、既設定時・再送時・並行操作時の扱い。Google名で自動確定する仕様は追加しない。 |
| A-02 | 確定Profile方針の補完 + 編集意味の判断 | Profile-driven、Scenario.gameSystem基準、保存時のSystem Key / Profile Version保持は確定。初期対応System / Profile定義とFrontend取得方法が不足。 | 初期Profile集合、配布API、保存済み履歴の表示・編集と現在Profileの関係を整理。添付の「過去編集は保存時Profile固定」は未採用。 |
| A-03 | 確定Import FlowのAPI補完 | 元Source比較、途中保存、登録済みCandidate再編集禁止、version競合検知は確定。Session更新を要求する本文に対しSession PATCH契約がなく、Source追加・原文取得・DTOも不足。 | File+Textの送信方法、Session / Candidate更新DTOと遷移、Source取得、同一Candidate並行登録時の整合性を具体化。正式Entityを増やさない。 |
| A-04 | PL変更 / 所有権・所属境界のAPI補完 | 選択Participationのみ更新、非選択履歴維持、Table子更新時の親version更新は確定。現行change-personはPC.versionだけを要求する。 | 影響Tableとの競合検知、子IDが対象Table / PCに属すること、更新・detach集合の重複や不正IDの拒否を明記する。添付のexpectedTableVersions追加は未採用の具体案。 |
| A-05 | 確定画像安全性のAPI補完 | Position / Zoomは非破壊Metadata。新保存成功前に旧画像を破壊しない。現行本文はMetadata保存後に専用Image APIへ送る2段階方式。未採用外部候補はDraft限定、初回採用時のみ同意。 | 現行方式での画像PUT / DELETEのexpected version・成功後version、途中失敗の整合復旧、同意状態の取得・更新を整理。全Mutation共通の返却統一とFile+Transform同時保存案は未採用。 |
| A-06 | 確定Previous基準のAPI補完 | 基準確定仕様§23には、対象卓より前の実施日による最新EndPcState、複数日の最新日基準、日付不明時の登録順による低精度参考という既存方針がある。新TableはまだtableIdがなく、現行GET /tables/{tableId}/previous-end-stateをそのまま使用できない。 | 日付・PC・編集中Table除外を渡せる新規用契約と、登録順参考を明確に区別するResponseを具体化。添付の新POST Endpointは未採用。登録日時を実施日時として表示しない。 |
| A-07 | 集計・検索意味の採用判断 | 同一Personが同一Tableに複数Participationを持てる。UC17件数、PL+KP Filter組合せ、変更影響の表示単位の具体定義が不足 / 不一致。 | Role別distinct Table集計、PL+KPはOR・FavoriteはAND、pcCount / tableCount / participationCountを併記する添付案はいずれも未採用。確定まで値を推測しない。 |
| A-08 | テスト環境契約の補完 | Production Cookie設定とCSRF Lifecycleは確定。Local HTTPでSecure=falseを許す記述と__Host-名の組合せを整理する必要がある。GET /csrfのResponse契約も不足。 | テスト用Cookie名 / HTTPS方針、CSRF TokenとHeader名の取得契約、Profile別の適用範囲を具体化。ProductionのSecure / HttpOnly等の確定方針を弱めない。 |
| A-09 | QA条件の補完 / 再検証 | 主要Viewport列挙はあるがTablet / Breakpoint境界、24文字の数え方、未命名Table採番の安定順、エラーFixtureの作り方が不足。Text Secondaryの背景とのContrastも要再検証。 | 境界条件と再現方法を定め、Colorの実際の使用箇所・文字サイズでContrastを確認する。今回Color Token / 文字数計算 / 採番方法は変更しない。 |

Mobileレイアウト順・Appearances初期5件/録画導線・PL変更初期全件ON・PC画像Keyboard操作・起点別保存Navigationは、原資料に存在する事項であり、新規業務判断待ちへ戻さない。

録画URLなしのDesktop Action非表示だけは、2026-10-01のUser回答により今回採用した。Mobile / Appearancesの既存採用事項とは根拠を区別する。

ここに挙げた未補完契約は、実装開始前に該当するBackend API / DTO節とFrontend利用節を対で改訂し、その修正履歴を追記する。今回の改訂ではAPI / Schema / 集計Ruleの具体案を正式採用していない。
