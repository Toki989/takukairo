# Web制作総合演習1
# TRPG活動履歴管理Webサービス
## PL / PC Visual Design再設計フェーズ 確定事項まとめ
### 2026-09-25

---

# 0. この資料の目的

本資料は、PL / PC画面のVisual Design再設計フェーズで採用・変更した事項を、今後の設計・Figma・実装・引継ぎ用の確定情報として整理したものである。

本資料は既存のデータ設計・CRUD・UC・認証認可等を置き換えるものではない。

今回の主な変更対象は、

- PL / PC画面のVisual Concept
- PC一覧の見せ方
- PC詳細への入り方
- PC Focusの構成
- 登場した卓（Appearances）の見せ方
- PC編集 / PL変更 / PL名編集の導線
- PC画像なし / Empty State
- Responsive / Mobile
- Keyboard / Focus / Reduced Motion
- 卓詳細とのNavigation連携

である。

---

# 1. 最重要原則

## 1.1 中身は既存確定仕様を正本とする

以下は既存の確定済み設計を正本とし、今回のVisual再設計を理由に変更しない。

- User
- Scenario
- ScenarioFavorite
- Person
- PC
- Table
- TableDate
- Participation
- EndPcState
- CRUD
- UC
- 認証・認可
- User所有権
- 保存 / 更新 / 削除ルール
- HO
- セリフ
- PC状態履歴
- 卓登録 / 編集
- PC登録 / 編集
- Import
- SelfPerson

v5.9、試作画面、モックデータ、画像生成上だけに存在する情報を正式仕様へ追加しない。

---

## 1.2 卓詳細v5.9との役割分担

Desktop卓詳細は引き続き、

> v5.9をVisual Design基準とした完全画面切替型

を維持する。

PL / PC側でv5.9のCharacter Select表現をそのまま再利用しない。

役割は以下のように分ける。

### PL / PC

> PCそのものを見る場所

主な意味：

- PC本体
- 現在PL
- Game System
- Character Sheet URL
- 登場した卓
- PC編集
- PL変更
- PL名編集

### 卓詳細v5.9

> その卓に参加したときのPC / Participationを見る場所

主な意味：

- その卓当時のPL
- HO / セリフ
- EndPcState
- SAN / HP / MP
- 成長
- 生還 / ロスト
- 後遺症
- PL Participation Selector

したがって、

> PL / PC = PC本体のPortrait / Archive  
> 卓詳細v5.9 = その卓の記憶へ入り込むCharacter Select

として明確に分離する。

---

# 2. PL / PC全体のVisual Concept【今回採用】

PL / PC画面は、

> Portrait Gallery × Character Catalogue

を基本Conceptとする。

ただし見た目の主軸はPortrait Gallery寄りとする。

## 2.1 目指す画面の意味

PL / PCを一般的な管理画面やSaaS風一覧として扱わない。

> 自分がこれまで演じてきた人物たちのPortrait Collection / Character Archive

として見せる。

## 2.2 Visual方針

- PC画像そのものを主役とする。
- サイト側の装飾は静かにする。
- 白いCard、強いShadow、大きな角丸を大量に使わない。
- 強い世界観は各PC画像自身に持たせる。
- Tealは常時大面積で使わず、Action / Selection / Hover / FocusのAccentとして扱う。
- Game UI的な斜め装飾やSelector表現は卓詳細v5.9へ寄せすぎない。
- Home / Scenario ViewのGallery思想とは共通するが、同じ画面にはしない。

役割分担：

> Home = Scenarioという作品のCollection  
> PL / PC = PCという人物のPortrait Collection  
> Scenario View = 1作品の展示  
> 卓詳細v5.9 = その卓の思い出の中へ入るCharacter Select

---

# 3. PC Collection View【今回採用】

## 3.1 基本表示

PC一覧の基本表示は以下。

- PC画像
- PC名
- 現在PL

一覧状態ではGame Systemを原則表示しない。

既存機能として以下を維持する。

### 検索

- PC名
- PL名

### Filter

- 現在PL

### Sort

- 最近使用
- PC名
- 登録が新しい順
- 登録が古い順

初期Sort：

> 登録が新しい順

---

## 3.2 Desktop Grid

Desktopでは、

> 4列のPortrait Grid

を基本とする。

幅が狭くなった場合は列数を減らす。

目安：

- Wide Desktop：4列
- Narrow Desktop：3列
- Tablet：2〜3列

最終的なBreakpointやpx値はPrototypeで調整する。

PC数が1〜3件しかない場合でも、各Itemを画面幅いっぱいまで不自然に引き伸ばさない。

Gridのサイズ・リズムを維持し、余白をGalleryとして活用する。

---

## 3.3 PC Itemの構成

1件のPC Itemは、Card UIとして囲い込まない。

基本構造：

```text
PC画像

PC名
現在PL
```

画像 → 余白 → PC名 → PL名、という順で成立させる。

---

## 3.4 Hover / Focus

Hover / Focus時は静かな演出とする。

候補：

- 細いAccent Rule
- ごく小さい画像変化
- 明確なFocus Ring

大きなOverlayや、

- VIEW CHARACTER
- DETAIL
- 大きな英字CTA

等は使用しない。

Hoverで得られる情報はKeyboard Focusでも同等に確認できるようにする。

---

# 4. PC詳細への入り方【今回変更・採用】

従来は、

> 複数PC詳細を同時展開可能

という仕様が存在した。

今回のVisual再設計により、この仕様は廃止する。

## 4.1 新方式

PL / PCは1つの画面として扱い、

```text
Collection View
↓ PCを選択
PC Focus View
```

へ同一画面内で切り替える。

PC Detail専用の独立ページへ遷移しない。

内部状態の概念例：

```text
selectedPcId = null
→ Collection View

selectedPcId = PC ID
→ PC Focus View
```

## 4.2 同時表示

詳細表示できるPCは常に1件のみ。

複数PC詳細の同時展開は行わない。

## 4.3 Collectionへ戻った場合

以下の状態を可能な限り維持する。

- Search
- PL Filter
- Sort
- Scroll位置
- 元のPC ItemへのKeyboard Focus

---

# 5. PC Focus View【今回採用】

PC Focusは、

> PCそのもののPortrait / Profile

として設計する。

卓詳細v5.9とは別のVisual Identityとする。

---

## 5.1 Desktop基本レイアウト

Desktopは、

> 左 = 大きなPC Portrait  
> 右 = PC Profile / Metadata / Activity

を基本構成とする。

12-column Gridを使用する場合の目安：

- Portrait側：約7 columns
- Profile側：約5 columns

最終比率はPrototypeで調整する。

---

## 5.2 PC Focusに表示する正式情報

- PC画像
- PC名
- 現在PL
- Game System
- Character Sheet URL
- 登場した卓
- PC編集
- PL変更
- PL名編集

PC本体に現在SAN / HP / MPは持たせない。

SAN / HP / MP等の状態値はTableごとのEndPcStateとして扱う。

---

## 5.3 PC名

PC名はFocus内で強く見せる。

Display TypographyにはShippori Mincho系を使用する方向。

Desktopでは48〜64px程度を候補とするが、最終値はPrototypeで調整する。

---

## 5.4 現在PL

PC Focusで表示するPLは、

> PCに現在紐づいているPerson

である。

卓詳細v5.9で表示する、

> その卓当時のParticipation.person

とは分ける。

現在PLの変更によって過去卓のPL表示を自動上書きしない。

---

## 5.5 Game System

Collection Viewでは原則非表示。

PC Focusでは表示する。

Badgeを強く使わず、

```text
SYSTEM
クトゥルフ神話TRPG
```

のような静かなMetadata表現とする。

---

## 5.6 Character Sheet URL

生URLは常時表示しない。

表示文言：

> キャラクターシートを見る ↗

URLドメインを安全に判別できる場合は、外部サービス名表示を行ってよい既存仕様を維持する。

---

# 6. 登場した卓 / Appearances【今回採用】

PC Focus内の「登場した卓」は、

> Activity Archive / 出演履歴

として扱う。

単なる件数表示やTable Cardの再利用にはしない。

---

## 6.1 表示形式

縦型Archive Listとする。

1件の基本表示：

- Scenario名
- TableName
- TableDate

例：

```text
カン・カカリ
第一陣
2026.09.15
```

以下は基本表示へ追加しない。

- KP
- 他PL / PC
- HO
- セリフ
- 録画URL
- EndPcState

詳細は卓詳細v5.9で確認する。

---

## 6.2 TableName

TableNameが入力済みならその値を使用。

未入力の場合は既存仕様どおり、

- 卓1
- 卓2

等の表示用Fallbackを使用してよい。

FallbackはDBへ保存しない。

---

## 6.3 TableDate

TableDateがある場合は表示する。

複数存在する場合は基本的に全件確認できるようにする。

例：

```text
2026.09.04 / 09.05 / 09.12
```

件数が多い場合は折り返し等で対応する。

TableDateが0件の場合、

> 実施日不明

等を勝手に補完せず、日付表示自体を出さない。

---

## 6.4 並び順

基本：

> 最近遊んだ卓 → 古い卓

TableDateがある場合、そのTableの最新TableDateを基準に降順とする。

TableDateなしは後方に配置する。

Tableの登録日時を実施日として扱わない。

---

## 6.5 過去のPLが現在PLと異なる場合

通常はPL名を追加表示しない。

ただし、

> PCの現在PL  
> その卓当時のParticipation.person

が異なる場合のみ、

```text
当時PL　○○
```

等の補助表示を行う。

これにより現在PLと過去履歴を混同しない。

---

## 6.6 Appearance選択時

Appearanceを選択すると、

> 対象Tableの卓詳細v5.9へ直接移動

する。

Scenario Viewを必ず経由させない。

さらに、

> そのPCに対応するParticipationを初期選択した状態

で卓詳細v5.9を開く。

概念上、

```text
tableId
participationId
```

を利用して対象を指定する。

---

## 6.7 卓詳細から戻る場合【今回拡張】

従来の卓詳細は、

> Scenario / Table一覧へ戻る

を基本としていた。

今回のPC Focus連携により、

> 卓詳細の戻り先 = 卓詳細へ入る直前の閲覧元

へ拡張する。

### Scenario Viewから入った場合

```text
Scenario View
→ 卓詳細v5.9
→ Scenario View
```

### PC Focusから入った場合

```text
PC Focus
→ 卓詳細v5.9
→ 元のPC Focus
```

PC Focusへ戻った場合は、

- 選択PC
- Appearanceの展開状態
- Scroll位置

を可能な限り維持する。

---

## 6.8 件数が多い場合

別の「登場卓一覧ページ」へ遷移させない。

初期表示件数を絞る場合は、

> すべて表示（N）

等でPC Focus内にそのまま展開する。

再度折りたためる形としてよい。

---

# 7. PC編集 / PL変更 / PL名編集【今回整理・採用】

3操作は意味が異なるため、同じ強さの編集ボタンとして並べない。

---

# 7.1 PCを編集

PC本体のプロフィール編集として扱う。

編集対象：

- PC名
- PC画像
- Game System
- Character Sheet URL

現在PLは確認表示してよいが、

> 既存PC編集フォーム内でPLを直接変更しない。

PLを変更する場合は専用の「PLを変更」フローへ進む。

これは今回のVisual / 操作整理による変更であり、PC.personが必須というデータ仕様自体は変更しない。

---

## 7.2 PC新規登録

新規登録時は既存仕様を維持する。

- PC名：必須
- PL：必須
- PC画像：任意
- Game System：候補＋自由入力
- Character Sheet URL：任意

新規作成時はPL選択が必要。

---

# 7.3 PL名を編集

PL名編集は、

> Person.displayNameのUpdate

である。

PCの現在Personを別Personへ変更する操作とは別。

PC Focus上では、現在PL名のすぐ近くに導線を置く。

例：

```text
PLAYER
土岐   名前を編集
```

大きな独立Actionとして見せない。

---

## 7.4 PL名編集UI

入力項目が少ないため、小さなDialogで扱う。

変更前に影響範囲を表示する。

例：

```text
この名前は、この人物を使用している
PC 3件・過去の卓12件にも反映されます。
```

同じPersonレコードを参照している、

- PC
- 過去Participation

等にも変更後の表示名が反映される。

---

# 7.5 PLを変更

PL変更はPC Focus内のCURRENT PLAYER領域から開始する。

PC Focusとは別ページへ完全遷移させず、

> 同一PL / PC画面内の専用変更モード

へ切り替える。

---

## 7.6 PL変更モードの内容

表示：

- PC画像 / PC名
- 現在PL
- 変更後PL
- 既存Person選択
- ＋新しい人物を追加
- このPCが登場したParticipation一覧
- 各Participationの変更ON / OFF
- 変更件数のリアルタイム要約
- 保存Action

---

## 7.7 過去Participationの扱い

PCの現在PLを変更しても、

> 過去Participationは自動変更しない

という既存データ原則を維持する。

ただしPL変更UIでは、ユーザーが明示的に過去Participation.personを変更できる。

初期状態：

> 関連Participationは全件チェックON

ON：

> 新Personへ変更

OFF：

> 旧Personのまま

とする。

説明文を配置し、

> 過去の登場記録について、PLを変更する卓を選択してください。

等の意味が分かるようにする。

---

## 7.8 リアルタイム要約

PL変更中は、

```text
3件をBへ変更
1件はAのまま
```

のような要約をリアルタイム表示する。

---

## 7.9 EndPcState

PL変更ではPC自体は変更しない。

EndPcStateも変更しない。

必要に応じて最終確認で、

> PCおよび卓終了時状態は変更されません。

等の補足を表示してよい。

---

## 7.10 新しいPerson作成

対象Personが未登録の場合、

> ＋ 新しい人物を追加

から作成できる。

Person専用管理画面は新設しない。

小さなDialog等で作成後、PL変更画面へ戻り、新Personを自動選択する方向とする。

---

# 8. 保存後の戻り先【今回整理】

### PC編集

```text
PC Focus
→ PC編集
→ 保存
→ 対象PC Focus
```

### PL変更

```text
PC Focus
→ PL変更
→ 保存
→ 対象PC Focus
```

### PL名変更

Dialogを閉じ、そのまま対象PC Focus。

保存成功後は、変更箇所を短くハイライトしてよい。

---

# 9. PC削除【既存仕様の配置整理】

関連ParticipationがあるPCは削除不可。

PC削除の入口は、

> PC編集画面下部のDanger Zone

へ配置する。

PC Focus上に強い赤色Actionを常時表示しない。

関連Participationが存在する場合は削除Actionを有効にせず、理由を表示する。

例：

```text
このPCは登場した卓があるため削除できません。
登場した卓 6件
```

削除可能なのは関連Participationが存在しないPCのみ。

削除のためにParticipationやEndPcStateを自動削除・解除しない。

---

# 10. PC画像なし【今回採用】

PC画像がないことを「エラー」や「欠損」として強く見せない。

## 10.1 Collection

Portrait領域は維持する。

人型シルエットを必須にせず、

> Typography主体のNeutral Placeholder

とする。

候補：

- Neutral Gray背景
- PC名の大きなTypography
- ごく薄いRule / Grid
- 控えめな画像未登録記号

巨大な「NO IMAGE」表示は避ける。

---

## 10.2 PC Focus

大きなPortrait領域を維持する。

画像がないことを理由にProfileだけのレイアウトへ切り替えない。

Placeholder内にPC名Typographyを使用可能。

PC画像追加導線は、

> PC編集

へ接続する。

新しい画像管理機能は作らない。

---

# 11. PC 0件時のEmpty State【今回採用】

PCが1件も存在しない場合は、架空PCやサンプルデータを表示しない。

例：

```text
PL / PC

まだPCは登録されていません。

卓の記録に使うPCを登録すると、
ここにコレクションとして表示されます。

＋ PCを追加
```

大きなIllustration Cardや強すぎる販促表現は不要。

PC登録だけをサービスの目的のように見せない。

---

# 12. Search結果0件【今回採用】

PC自体0件とは区別する。

検索やFilterによって0件になった場合：

```text
一致するPCがありません。

検索条件を変更してください。

検索をクリア
```

等とする。

この状態では「＋PC追加」を主CTAにしない。

---

# 13. Responsive【今回採用】

Desktop用とMobile用でデータや機能を分けない。

同じ、

- PC
- Person
- Search
- Filter
- Sort
- selectedPcId
- Appearances

を利用し、Layoutのみ変更する。

---

# 14. Mobile Collection【今回変更・採用】

Mobile Collectionは、

> デフォルト2列

とする。

さらにユーザーが、

> 2列 / 3列 / 4列

を任意に切り替えられるようにする。

目的：

- 2列：PC Portraitを大きめに眺める
- 3列：閲覧と一覧性の中間
- 4列：多数PCを俯瞰する

列数によって、

- Portraitサイズ
- Gap
- PC名文字サイズ
- 折り返し量

を調整する。

ただし情報構造は維持し、

- PC画像
- PC名
- 現在PL

を基本表示する。

---

## 14.1 Mobile Grid切替UI

大きな設定Panelにはしない。

Toolbar内に小さなGrid Density切替として配置する方向。

例：

```text
2 | 3 | 4
```

またはGrid Iconを利用する。

---

## 14.2 Grid設定の永続化

2 / 3 / 4列のユーザー選択を端末やアカウントへ永続保存するかは、

> 現時点では未確定

とする。

MVP実装時に必要性と工数を見て判断する。

---

# 15. Mobile Search / Filter / Sort【今回採用】

MobileでもSearch / Filter / Sort機能を削除しない。

例：

```text
[ PC・PLを検索 ]

[ PL ▼ ]   [ 並べ替え ▼ ]
```

Desktop Toolbarをそのまま縮小せず、Mobile用Layoutへ再配置する。

---

# 16. Mobile PC Focus【今回採用】

Desktopの左右2Columnをそのまま縮小しない。

Mobileでは縦構成とする。

順序：

1. 戻る
2. PC Portrait
3. PC名
4. 現在PL
5. Game System
6. Character Sheet URL
7. PC編集 / PL変更等
8. Appearances

概念：

```text
← PC一覧

PC PORTRAIT

PC名
現在PL

SYSTEM
Game System

キャラクターシートを見る ↗

PCを編集
PLを変更

Appearances
...
```

Portraitだけで初期Viewportを埋め切らず、PC名等も早い段階で確認できる高さとする。

正確な高さはPrototypeで調整する。

---

# 17. Mobile PL変更【今回採用】

Mobileでは縦Layoutへ再構成する。

過去Participation一覧を横Scrollさせない。

各項目を縦に並べ、Check操作と要約を確認しやすくする。

---

# 18. Keyboard / Focus【今回採用】

Keyboard操作を維持する。

Desktop CollectionではTab順序を、

```text
Search
→ PL Filter
→ Sort
→ PC Item 1
→ PC Item 2
→ ...
```

等の自然な順序にする。

PC Item自体をKeyboardで選択可能にする。

Hoverで確認できる状態はKeyboard Focusでも確認できるようにする。

Focus Ringは既存Accent Strong系を使用する。

---

## 18.1 Focus復元

Collection → PC Focusへ入った場合、

PC Focus側の先頭または戻る導線へFocusを移動する。

PC Focus → Collectionへ戻った場合、

> 元のPC ItemへFocusを戻す

ことを目指す。

---

# 19. Motion / Reduced Motion【今回採用】

Animationは、

> 状態変化や情報のつながりを理解させる目的

で使用する。

飾りだけの長いMotionは避ける。

候補：

> CollectionのPC画像が少し拡大してPC Focus Portraitへつながる

等。

ただし必須機能にはしない。

Reduced Motion時：

- Animation短縮
- Animation省略
- Fade
- 即時切替

等で対応する。

---

# 20. 今回明確に廃止・変更した旧仕様

## 20.1 複数PC詳細同時展開

旧：

> 複数PC詳細を同時展開可能

新：

> 詳細表示は1PCのみ。Collection ViewとPC Focus Viewを同一PL / PC画面内で切り替える。

---

## 20.2 PC Detail独立ページ

今回の検討途中では独立ページ化も候補となったが、採用しない。

新：

> PL / PC内の同一画面切替。

理由：

PL / PCは卓管理サービスの補助的Character Archiveであり、独立したCharacter管理サービスのように階層を深くしすぎないため。

---

## 20.3 PC編集内のPL直接変更

旧：

PC編集項目にPLを含む。

新：

- 新規PC登録ではPL必須。
- 既存PC編集ではPLを直接変更しない。
- PL変更は専用フローへ一本化。

---

## 20.4 卓詳細の固定戻り先

旧：

> Scenario / Table一覧へ戻る

新：

> 卓詳細へ入る直前の閲覧元へ戻る

Scenario View経由とPC Focus経由の両方を成立させる。

---

# 21. 今回変更していない重要仕様

今回のVisual再設計によって以下は変更していない。

- PCはUser所有
- PersonはUser所有
- PC名必須
- PCに現在紐づくPerson必須
- PC画像は任意
- Game Systemは任意
- Character Sheet URLは任意
- PC本体に現在SAN / HP / MPを持たせない
- EndPcStateはTable / Participation側の履歴
- 現在PL変更で過去Participation.personを自動上書きしない
- Person.displayName変更は同一Person参照先へ反映
- 関連ParticipationがあるPCは削除不可
- PC画像は変更 / 削除可能
- 新画像保存成功後に旧画像を削除する等、安全な置換を目指す
- Search / Filter / Sort
- Responsive
- Keyboard
- Focus
- Reduced Motion
- 認証 / 認可
- User所有権確認

---

# 22. 実装時調整でよい項目

以下は現時点で仕様として固定しすぎず、Prototype / 実装時に調整する。

- Desktop Breakpoint
- Tablet Breakpoint
- Portraitの正確なpx
- Grid Gap
- PC名の最終Font Size
- Mobile 2 / 3 / 4列それぞれの文字サイズ
- Placeholder Typographyの最終位置
- Hover時の画像変化量
- Focus Ringの最終太さ
- Motion duration
- easing
- Mobile Portrait高さ
- Appearance初期表示件数
- Mobile Grid列数設定の永続化

これらはVisual / Implementation Detailであり、データ仕様を変更するものではない。

---

# 23. 現時点のPL / PC画面全体構造

```text
PL / PC
│
├─ Collection View
│   ├─ Search
│   ├─ PL Filter
│   ├─ Sort
│   ├─ Mobile Grid Density 2 / 3 / 4
│   └─ PC Portrait Grid
│
└─ PC Focus View
    ├─ Portrait
    ├─ PC名
    ├─ 現在PL
    │   └─ PL名を編集
    ├─ Game System
    ├─ Character Sheet URL
    ├─ PCを編集
    ├─ PLを変更
    └─ Appearances
        └─ Table選択
            └─ 卓詳細v5.9
                └─ 対象Participation初期選択
```

補助状態：

```text
PC Focus
├─ view
├─ editPc
└─ changePlayer

Dialog
└─ editPersonName
```

---

# 24. 画面の意味の最終整理

> HomeはScenarioの美術館。  
> Scenario Viewは1作品の個展。  
> PL / PCは自分が演じてきた人物たちのPortrait Collection。  
> PC Focusはその人物自身を眺めるProfile / Archive。  
> AppearancesはPCから過去の活動履歴へ戻る橋。  
> 卓詳細v5.9はその卓の記憶の中へ入り込むCharacter Select。

PL / PCを独立したキャラクター管理サービスへ肥大化させず、

> 卓管理を主軸としながら、PCを美しく再利用・振り返りできる補助Collection

として位置づける。

---

# 25. 最終原則

> **PCはCollectionとして美しく見せる。  
> しかし、このサービスの主役はあくまでTRPGの活動履歴・卓管理である。**

PC Focusはプロフィール表示で終わらせず、

> PC → 登場した卓 → 卓詳細v5.9

へ自然につながることで、活動履歴管理サービスとして成立させる。

---

以上を、2026-09-25時点の  
**「PL / PC Visual Design再設計フェーズ 確定事項」**  
とする。
