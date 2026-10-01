# Web制作総合演習1
# TRPG活動履歴管理Webサービス
## Visual / Responsive / Accessibility仕上げフェーズ 確定事項まとめ
### 2026-09-30

---

# 0. この資料の位置づけ

本資料は、TRPG活動履歴管理WebサービスのVisual / Responsive / Accessibility仕上げフェーズで、
No.39〜No.63についてユーザーが採用・一旦採用した内容を整理した追加確定資料である。

既存のEntity、Data属性、CRUD、UC、認証・認可、保存・削除Rule等を置き換えるものではない。

最重要原則は引き続き、

> **見た目はv5.9。中身は既存の確定済み設計。**

とする。

PCフォーカスビュー v5.9 は、

> **確定済み設計を表示するための Visual Design / UI実装ベース**

として扱う。

v5.9のMock Data・仮Button・仮状態・仮値を理由に、
新しいEntity、Data属性、State、CRUD、業務Ruleを自動的に正式仕様へ追加しない。

---

# 1. 採用状態の区分

本資料では以下の2種類を区別する。

## 採用

正式に採用した方針。
実装時に既存仕様と矛盾しない限り、この内容を基準とする。

## 一旦採用

基本方針として採用するが、
Prototype / 実ブラウザ / 実機で明確なVisual破綻・操作性問題が確認された場合に、
Visual Detailを再調整可能とする。

---

# 2. No.39 Breakpoint【一旦採用】

- Mobile：0〜767px
- Tablet：768〜1199px
- Desktop：1200px以上
- Device名ではなくViewport幅で判定する
- TabletをDesktopの単純縮小として扱わない
- v5.9のDesktop 3レーン構成は原則1200px以上で成立させる
- 低いViewport Height対策は横幅Breakpointとは別に維持する
- 実機で明確な破綻が出た場合は調整可能とする

---

# 3. No.40 Focus Ring【採用】

共通Focus表現：

- `2px solid Accent Strong #3F7D79`
- `2px offset`
- 原則 `:focus-visible`
- 色だけでFocusを伝えない
- `outline: none`だけで消さない
- 特殊形状UIは同等以上の視認性を持つ代替表現を許容
- Dialog / Overlay終了後のFocus Restoreを維持

---

# 4. No.41 Motion exact値【一旦採用】

## 4.1 共通Motion Token

- Fast / Micro：160ms
- Standard：260ms
- Character Emphasis：320ms
- View Transition：420ms
- 保存後Highlight：約1200ms
- Skeleton cycle：約1600ms前後
- Main Ease：`cubic-bezier(.2,.7,.2,1)`

## 4.2 PC切替

Portrait：

- Fade：約240ms
- Transform：約320ms
- opacityを弱める
- scaleをわずかに縮小
- 横方向へごく小さく移動

中央Information：

- 約260ms
- Portraitより40〜60ms程度遅れて追従
- Fade + 6〜10px程度の微細な移動

Selector：

- 約260ms
- Mobile Snapは220〜260ms程度を目安

## 4.3 HO / セリフ Spotlight

- 全体約280〜320ms
- Fade主体
- Position変化は2〜4px程度に抑える
- Spotlight通常Opacityは0.38〜0.42程度を初期候補
- 実画像との競合を見てOpacityは再調整可能

## 4.4 Gallery / View Transition

- Gallery Hover：約260ms
- Collection → PC Focus：約420ms
- Scenario → 卓詳細：約420ms
- Popover：約160ms
- Menu / Dialog：約220〜260ms
- Form系軽微Motion：約160〜260ms

## 4.5 Reduced Motion

`prefers-reduced-motion: reduce`では機能自体を削除しない。

- Shared-element的移動 → Fadeまたは即時
- Portrait Slide / Scale → Fade中心
- Flick Snap →短縮
- Shimmer →停止
- Hover Scale →停止可
- 保存後Highlight →色変化中心

---

# 5. No.42 最終px / Grid Gap / Font Size【一旦採用】

## 5.1 共通Spacing Scale

- 4px
- 8px
- 12px
- 16px
- 24px
- 32px
- 48px
- 64px

## 5.2 共通基準

- Desktop左右Page Padding：40px
- Tablet左右Padding：24px
- Mobile左右Padding：16px
  - 必要なら14px程度まで調整可
- 通常本文：15〜16px
- 補助本文：13〜14px
- Micro Label：11〜12px
- Section見出し：18〜20px
- Page Title：28〜32px
- Desktop主役PC名：40〜44px
- Mobile主役PC名：32〜34px
- Focus Ring：No.40を維持

## 5.3 Desktop卓詳細 v5.9

v5.9の現行Visual値を優先し、
一般的なGrid Tokenへ無理に丸めない。

目安：

- Header：約78px
- Stage max：約1672px
- Portrait Lane：約44%
- Center Lane：約19%
- Center Lane min-width：約300px
- PC名：約44px
- STATUS数値：約42px
- Spotlight：大Desktopで概ね112px
- Selector通常Slot：約96〜112px
- Active Slot：約116〜132px
- Slot Gap：約3px
- Selector傾斜：約-9deg

## 5.4 PL / PC Collection

Desktop：

- 4列基本
- Grid Gap：24px

Mobile：

- 2列：Gap 14px / PC名16px
- 3列：Gap 10px / PC名14px
- 4列：Gap 8px / PC名12px

## 5.5 Home Gallery

- Desktop外側Padding：40px
- Gallery Gap：24px
- Scenario名：18〜20px / Shippori Mincho
- 作者：13〜14px / Noto Sans JP
- Image → Metadata：約12px

## 5.6 PC登録 / 編集

- max-width：約1200px
- Portrait : Profile = 5 : 7を基本
- Column Gap：48px
- Input間：24〜32px
- Input高さ：約44px

## 5.7 Scenario / Table Form

- max-width：880〜960px程度
- Input間：24px
- Section間：48px
- Label：13〜14px
- Input：15〜16px

※No.42は実ブラウザ・実画像・実フォント表示を確認し、
数px単位でVisual Tuning可能とする。

---

# 6. No.43 PCなし専用View【一旦採用】

PC自体なしは、PC画像なしPlaceholderとは別状態とする。

Desktop v5.9の3レーン構造をできるだけ維持し、

- 左：Participation Poster
- 中央：PL / Participation情報
- 右：Participation Selector

とする。

主役はPL表示名。

`PC 未登録`はNeutralな補助表示。

PCなし時：

- EndPcStateを表示しない
- STATUSを表示しない

表示可能：

- PL名
- Role
- Scenario
- TableName
- TableDate
- HO
- 録画URL
- その他PCなしでも成立するParticipation情報

Mainに巨大な「PCを登録」CTAを置かない。

---

# 7. No.44 PCなしSelector【一旦採用】

- v5.9のSelectorの細さ・傾斜・密度を維持
- PCなしSlotはabstract表現 + `PC 未登録`
- 人型Silhouetteを主方式にしない

Selector末尾：

> Ghost Action Slot `＋`

- 通常Participation Slotより弱くする
- Hover / Focus / Tapで `＋ PLを追加`
- 架空Participationとして扱わない
- `selectedParticipationId`対象外
- Clickで既存Table Editの＋PL Flowへ

選択中PCなしParticipation：

> `＋ PCを設定`

- 「PCを登録」ではなく「PCを設定」
- 既存PC選択 / 新規PC作成の両方へつなぐ

---

# 8. No.45 PC画像なしPlaceholder【採用】

人型Silhouetteではなく、

> **PC名Typography主体のNeutral Character Placeholder**

を正式方向とする。

- Portrait領域維持
- Neutral Gray背景
- PC名の大きなTypography
- ごく薄いRule / Grid / abstract decoration
- 控えめな画像未登録表現
- 巨大な`NO IMAGE`は使わない
- v5.9 Accent / Textureと調和

状態を明確に分ける：

- PCあり + 画像あり → Portrait
- PCあり + 画像なし → Character Typography Placeholder
- PC自体なし → Participation Poster View

Collection / PC Focus / 卓詳細 / Selectorで同じVisual languageを使う。

---

# 9. No.46 HO / セリフ切替UI【採用】

HOとセリフは別Dataとして両方保存可能。

Spotlight表示は一度に1種類。

表示条件：

- HOのみ → HO表示、Switchなし
- セリフのみ → セリフ表示、Switchなし
- 両方 → `HO ｜ セリフ`
- 両方なし → Spotlight / Switchなし

Switch：

- 大きなRounded Tab / Segmented Controlにしない
- 選択中のみAccent Strongの細いUnderline / Rule
- 非選択はNeutral
- 本文より弱いMicro UI

セリフ：

- `QUOTE`固定Labelなし
- 自動「」なし
- 最大24文字

Spotlight選択状態の永続保存は未確定。

---

# 10. No.47 Desktop卓詳細Header Scenario URL【一旦採用】

- Scenario名本体はContext Text
- Scenario URLがある場合のみ独立した小さなExternal Link Icon
- Arrow-up-right系
- 通常Text Secondary
- Hover / FocusでAccent Strong
- Pill / Button化しない
- Tooltip / accessible name：
  `シナリオページを見る`
- Scenario名が長くてもIconを潰さない
- URLなし時はIcon自体をrenderしない

---

# 11. No.48 Hamburger【採用】

HamburgerはGlobal Navigationのみ。

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

Visual：

- v5.9のQuiet Overlay / Panel
- 巨大SaaS Drawerにしない
- Typography主体
- Hover / FocusはAccent Strongの細いRule等
- Escape Close
- Focus Trap
- Focus Restore

---

# 12. No.49 成長・後遺症【採用】

- 任意
- multiline
- 最大500文字
- 改行保持
- Skill / before / after等へ構造化しない
- 自動Bullet / Parsing / 要約なし

Typography：

- Label：Noto Sans JP / 小さく / Text Secondary
- 本文：Noto Sans JP Regular / Text Primary

表示：

- `pre-wrap`
- `overflow-wrap`
- 短文は全文
- 長文はCompact Preview + `全文を見る`
- 展開後 `折りたたむ`
- 空欄は非表示

Exact clampはPrototype調整。

---

# 13. No.50 TableDate 3件以上Popover【採用】

Header表示：

- 0件 → 非表示
- 1件 → `2026.09.12`
- 2件 → `2026.09.12 / 09.19`
- 3件以上 → `2026.09.12 他2日`

`他N日`のみTrigger。

Visual：

- Pill / Button背景なし
- Text Secondary
- Hover / FocusでText Primary / Accent Strong
- 細いUnderline / Rule
- No.40 Focus Ring

Popover：

- 小さくQuiet
- 全TableDateを時系列表示
- Hover / Focus / Click / Enter / Space
- Esc / Outside ClickでClose
- MobileはTap

---

# 14. No.51 Desktop卓詳細 Mobile Layout【採用】

基本構造：

> Full-width Portrait Hero  
> → Horizontal Participation Selector  
> → PC / EndPcState情報 Vertical Flow

Desktop 3レーンを縮小しない。

Header：

- Back
- SESSION
- Scenario名 + External Link
- Table編集
- Hamburger
- TableName
- TableDate
- KP

Portrait：

- Mobile専用crop / position
- HO / セリフをPortrait周辺に配置
- Micro Switch維持

Participation Selector：

- 選択中中央
- 隣Participationを左右Peek
- Flick / Swipeだけを唯一の操作にしない
- Tap + 左右操作あり
- PC画像あり / PC画像なし / PCなし / +PL Ghost Slotを扱う

Status：

- 縦構成
- Statusはwrap grid
- 横Scrollなし
- Game System Profile-driven

Recording：

- URLあり時のみAction
- PCなしならEndPcStateなし

---

# 15. No.52 Appearance初期表示件数 / 録画導線【採用】

Appearance初期表示：

- 0件 → Empty State
- 1〜5件 → 全件
- 6件以上 → 最新5件 + `すべて表示（N）`

展開後：

- 同一PC Focus内で全件表示
- `表示を減らす`

Desktop / Mobileで件数を変えない。
Account persistenceなし。

録画URL：

- URLあり → Secondary Action `▶ 振り返る`
- Row本体 → 卓詳細v5.9 + 該当Participation初期選択
- `振り返る` → 直接外部録画URL
- URLなし → Actionをrenderしない

---

# 16. No.53 ComboBox / Tab詳細ARIA【採用】

## Person / PC Searchable ComboBox

Input：

- `role="combobox"`
- `aria-expanded`
- `aria-controls`
- `aria-autocomplete="list"`
- `aria-activedescendant`

Popup：

- `role="listbox"`
- Candidate：`role="option"`
- 選択中：`aria-selected="true"`

DOM FocusはInput。

Keyboard：

- Down
- Up
- Enter
- Esc
- Tab

Search文字列だけではPerson / PC Entity選択とみなさない。

`＋ 新しい人物を追加`：

- optionではない
- 別Button / Action

## PL Tabs

- `role="tablist"`
- `role="tab"`
- `role="tabpanel"`
- roving tabindex
- Left / Right
- Home / End
- Automatic Activation

Validation ErrorをTabの`aria-invalid`へ載せない。
視覚Error + accessibleな`入力エラーあり`。

保存失敗：

1. 最初のError PL TabをActivate
2. Panel表示
3. 最初のInvalid FieldへFocus

## Dialog

- Modal semantics
- Focus Trap
- Esc Close（Cancel可能時）
- Focus Restore

---

# 17. No.54 Scenario Form詳細ARIA【採用】

基本：

- Native semantic HTML優先
- LabelをControlへ正式関連付け
- PlaceholderをLabel代替にしない
- Scenario名はnative `required`
- Optionalへ無意味な`aria-required="false"`を乱用しない

## Game System

editable Combobox。

- Candidate選択可能
- 自由入力も正式値として許可
- `role="combobox"`
- `aria-expanded`
- `aria-controls`
- `aria-autocomplete="list"`

## BOOTH入力補助

表示：

`BOOTHから入力を補助`

- URL Input
- `情報を取得`

取得中：

- `取得中…`
- 対象Region `aria-busy="true"`
- Duplicate Request防止
- polite Live Region

成功：

`BOOTHから候補情報を取得しました`

失敗：

`情報を取得できませんでした。手入力を続けられます`

BOOTH取得失敗をScenario Validation Errorにしない。

候補反映時：

- aggregate Live Message
- 各Field変更を過剰に読み上げない

## Scenario画像

- native file input維持
- Drag & Dropのみにはしない
- Preview自体をActionにしない
- Current SourceをTextでも表示
- 外部候補は普通のButton `この画像を使用`
- 状態をColorだけに依存しない

## Validation

- 実際にInvalidなFieldのみ`aria-invalid="true"`
- ErrorをFieldと関連付け
- Save Validation Failure時はFirst InvalidへFocus

## Duplicate Scenario

Errorではない。

- `aria-invalid`なし
- Neutral / Warning Information
- `既存のScenarioを見る`
- `このまま新規登録`

---

# 18. No.55 404 / Error Copy【採用】

## 404

Heading：

`ページが見つかりません`

Body：

`お探しのページは、移動または削除された可能性があります。`

`どうやら今回の探索では見つけられなかったようです。`

Primary：

`ホームへ戻る`

Optional：

`前のページへ戻る`

## Protected Resource

Heading：

`このページを表示できません`

Body：

`データが存在しないか、アクセスする権限がありません。`

他User所有・存在確認等を漏らさない。

## 500

Heading：

`一時的な問題が発生しました`

Body：

`ページを読み込めませんでした。`

`少し時間をおいて、もう一度お試しください。`

Primary：

`もう一度読み込む`

Secondary：

`ホームへ戻る`

## Network Error

Heading：

`通信できませんでした`

通常：

`インターネット接続を確認して、もう一度お試しください。`

Form中：

`通信できませんでした。入力内容は保持されています。`

`接続を確認して、もう一度保存してください。`

※実際に入力保持される場合のみ。

## Offline Save

Heading：

`オフラインです`

Body：

`現在は変更内容を保存できません。`

`インターネットに接続してから、もう一度保存してください。`

TRPG Flavorは404のみ弱く使う。

---

# 19. No.56 一覧Skeleton【採用】

採用条件：

> **v5.9のVisual languageを崩さず再現すること。**

汎用SaaS角丸Card Skeletonにしない。

原則：

> **Skeletonは共通Cardではなく、各一覧の最終Layoutを簡略化した専用形状にする。**

## Home

- Gallery Layout維持
- Image
- Scenario名
- 作者
- Favorite / Table count / Hover UI等はSkeleton化しない

## PL / PC

- Portrait block
- PC名
- PL名
- Search / Filter / Sortまで灰色化しない
- 再検索時は既存内容を維持してLocal Loadingを優先

## Scenario View / Session Archive

- Card Skeletonにしない
- Archive Row / Session Strip形状
- TableName
- TableDate
- KP Context
- Neutral Participant Shape

## 件数目安

- Desktop：3〜6件
- Mobile：3〜4件
- Viewportに合わせて調整
- 実Data件数を意味しない

その他：

- Neutral only
- Fake textなし
- PlaceholderとSkeletonを混同しない
- Reduced Motionでは静的Skeleton

---

# 20. No.57 PC画像Position Keyboard操作【採用】

- Portrait編集領域をKeyboard Focus可能にする
- Arrow：表示枠約1%相当移動
- Shift + Arrow：約5%相当移動
- Drag / Keyboardは同じPosition Metadataを更新
- 移動方向は画像そのものの方向
- 空白が出ない位置までClamp
- No.40 Focus Ring
- 画面上に短い操作説明
- Direction Buttonを大量Overlayしない
- MobileでKeyboard利用可能なら同操作を許容
- Mobile UIへ方向Button常設不要
- 原画像は変更しない

---

# 21. No.58 PC Portrait / Profile / Zoom Controls【採用】

Desktop：

- Portrait : Profile = 5 : 7基本
- Zoom ControlsはPortrait直下
- 順：
  `− → Slider → ＋ → 中央に戻す`
- Slider幅：Portrait幅の55〜65%程度
- Desktop目安：200〜260px
- ±は小さな補助Button
- Portrait上へOverlayしない
- `中央に戻す` = Positionのみ中央
- Zoomまで完全Resetしない
- 画像変更 / 削除はZoom操作から一段分離

Mobile：

- 1本指Drag
- Pinch Zoom
- Slider
- 中央に戻す
- 画像変更
- 画像削除
- ± Buttonなし

---

# 22. No.59 TableDate PART Visual【採用】

同一Table内で複数TableDateがある場合のみ、

`PART 1 / PART 2 / ...`

を表示。

- TableDateを時系列順に並べて表示時に導出
- DBへ`partNumber`等を追加しない
- 1日だけの場合はPART表示なし

表示場所：

- 卓登録 / 編集 → 表示
- Scenario View / Session Archive → 表示
- TableDate全件Popover → 表示
- Desktop / Mobile卓詳細Header本体 → 表示しない

Visual：

- 小さなTypography主体
- PARTは日付より弱い
- Badge / Pill化しない

---

# 23. No.60 Mobile Session Archive / Character Flick【一旦採用】

基本：

- Active Participation中央
- 前後各1件Peek
- Gesture開始目安：約12px
- 横移動量が縦移動量の約1.25倍以上で横Swipe扱い
- Slot幅約25%以上移動で切替
- 明確な高速Flickなら短距離でも切替
- 1 Swipe = 最大1 Participation
- Activeを中央Snap
- Loopなし
- 左右Peek Tap可能
- `‹ / ›`をGesture代替として併設

Scenario View：

- 縦 = Tableを探す
- 横 = そのTable内のPCを見る

Reduced Motion対応。

※Peek幅・Snap感・Gesture閾値はPrototype / 実機で再調整可能。

---

# 24. No.61 Mobile Touch Target【採用・修正版】

重要原則：

> **44〜48pxを全操作に機械的な必須最低値として適用しない。**

- 44〜48pxは推奨目安
- Header主要Icon等は可能なら44px前後
- 小さいText Action / Micro Switch / `他N日`等はVisualを壊すなら無理に44px化しない
- 小Target時は隣接操作とのSpacingを確保
- Invisible Hit Areaを隣要素へ侵食させない
- Hit Area同士を重複させない
- v5.9の細さ / 密度 / Peekを優先
- Figma + 実機で押しやすさと邪魔にならなさの両方を確認

「広ければ広いほど良い」とは扱わない。

---

# 25. No.62 Placeholder Typography / Hover【採用】

## Placeholder Typography

- PC名は基本左下寄り
- 完全中央固定にしない
- 大Portraitほど大胆
- Collectionは少し中央寄りへ補正
- Selectorは最小限
- 画面ごとに座標調整するがVisual Languageは共通
- `画像未登録`等の補助Textは大Portrait中心に弱く表示
- 巨大`NO IMAGE`なし

## Hover

- Collection画像Scale：1.02〜1.03程度
- Card全体を大きく浮かせない
- Placeholder HoverはTypography / Rule / Decorationで表現
- FocusはNo.40を主
- Reduced Motion対応

---

# 26. No.63 PC登録 / 編集 Error Summary【採用】

Error Summaryは常設しない。

## Error 1件

- Inline Error
- 該当FieldへFocus
- Summaryなし

## Error 2件以上

- Inline Error維持
- Profile上部へ簡潔なError Summary
- SummaryへFocus
- 各ErrorからFieldへ移動可能

Save / Network ErrorとValidation Error Summaryを混同しない。

---

# 27. 本フェーズ完了時点の状態

Visual / Responsive / Accessibility仕上げQueueはNo.39〜No.63まで一巡した。

## 正式採用

- No.40
- No.45
- No.46
- No.48
- No.49
- No.50
- No.51
- No.52
- No.53
- No.54
- No.55
- No.56
- No.57
- No.58
- No.59
- No.61
- No.62
- No.63

## 一旦採用

- No.39 Breakpoint
- No.41 Motion exact値
- No.42 final px / Grid Gap / Font Size
- No.43 PCなし専用View
- No.44 PCなしSelector
- No.47 Scenario URL Icon / Hover
- No.60 Mobile Flick / Snap

一旦採用項目も基本仕様として維持するが、
Prototype / 実ブラウザ / 実機で明確な問題がある場合のみ再調整する。

---

# 28. 今回あえて確定していないこと

本フェーズのVisual整理を理由に、以下を勝手に確定していない。

- SpotlightでHO / セリフのどちらを選んだかを永続保存するか
- 正式Service名
- Activity Exportの採否・詳細
- Browser / Web App / PWA / Native等の最終配布方式
- Platform依存のlocal storage実装方式
- 外部画像利用時の一度だけの同意タイミング
- 未確定のBackend / Library / Storage等の物理実装Detail

これらは別の未確定事項として扱う。

---

# 29. 最終確認原則

今後のPrototype / 実装では毎回、

## Webクリエイター

- v5.9のVisual Designを維持できているか
- PCが主役になっているか
- TRPG GAME LIBRARYの世界観を損なっていないか
- 追加情報で情報階層や操作性が崩れていないか

## エンジニア

- 既存確定Data設計と一致しているか
- Mock上だけのDataを正式仕様へ混入していないか
- CRUD / 認可 / Data整合性を壊していないか
- Responsive / Keyboard / Focus / Reduced Motionを退行させていないか

を最低限確認する。

---

以上を、2026-09-30時点の
**Visual / Responsive / Accessibility仕上げフェーズ確定事項まとめ**
とする。
