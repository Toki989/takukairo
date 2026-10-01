# Web制作総合演習1

# TRPG活動履歴管理Webサービス

## PC登録／編集 Visual Design再設計フェーズ 確定事項まとめ

### 2026-09-25

------------------------------------------------------------------------

# 0. この資料の位置づけ

本資料は、TRPG活動履歴管理Webサービスにおける

> **PC登録／編集画面のVisual Design再設計フェーズで確定した事項**

を、今後の設計・実装・引継ぎ時に参照できる形でまとめたものである。

本フェーズは既存のデータ構造・UC・CRUD・認証／認可・保存／削除ルールを変更するものではない。

基本原則は以下とする。

> **既存の確定済み仕様を、現在の「Portrait Gallery / Character
> Catalogue」思想とフォーム系Design Systemへ落とし込む。**

Desktop卓詳細のPC Focus v5.9はVisual Design /
UI実装ベースであり、PC登録／編集のデータ仕様を新たに定義する根拠にはしない。

------------------------------------------------------------------------

# 1. PC登録／編集画面の役割

PL / PC周辺の画面責務を以下のように分離する。

-   **PL / PC Collection**：Portrait GalleryとしてPCを探す場所
-   **PC Focus**：選択したPCそのものを見る場所
-   **PC登録／編集**：PC本体を登録・修正する場所
-   **Desktop卓詳細 v5.9**：その卓におけるPC /
    Participationの記録を見る場所

PC登録／編集のVisual Conceptは、

> **Character Entry / Character Profile Editor**

とする。

ただし「ゲーム内でキャラクターを作成するキャラメイク画面」ではない。

本サービスでは、すでに存在しているPCを自分のCollectionへ登録する画面として扱う。

したがって画面の意味は、

> **「キャラクターを作る」ではなく「自分のPC Collectionへ収蔵する」**

とする。

------------------------------------------------------------------------

# 2. Visual Designの基本方針

PC登録／編集は、

> **大きなPortrait + 静かなProfile**

を中心構造とする。

PL / PCの「Portrait Gallery × Character
Catalogue」とVisual上のつながりを持たせる一方、通常のSaaS管理フォームには寄せすぎない。

ただし閲覧画面ほど演出を強くしない。

特にDesktop卓詳細 v5.9の、

-   斜めPC Selector
-   Character Select的な強い演出
-   卓詳細専用のVisual表現

をPC登録／編集へそのまま持ち込まない。

Scenario登録／編集と同じフォーム系Design
Systemを共有してよいが、PC画面ではPortraitの存在感をより強くし、単純な画面コピーにしない。

------------------------------------------------------------------------

# 3. 既存データ仕様として維持するPC項目

## 3.1 PC新規登録

新規PC登録で扱う正式項目は以下。

-   PC名：必須
-   現在PLとなるPerson：必須
-   PC画像：任意
-   Game System：任意
-   Character Sheet URL：任意

未登録Personが必要な場合、その場で新規Personを作成できる。

Person専用管理画面は新設しない。

------------------------------------------------------------------------

## 3.2 既存PC編集

PC編集は、

> **PC本体のプロフィール編集**

として扱う。

編集対象：

-   PC名
-   PC画像
-   Game System
-   Character Sheet URL

現在PLは確認表示してよいが、

> **PC編集フォーム内では現在PLを直接変更しない。**

PL変更はPC Focusから開始する専用PL変更フローを使用する。

------------------------------------------------------------------------

# 4. PC編集とPL変更の責務分離

PC編集とPL変更を混同しない。

## PC編集

PC本体の以下を変更する。

-   PC名
-   PC画像
-   Game System
-   Character Sheet URL

## PL変更

PCに現在紐づいているPersonを変更する専用フロー。

PC編集InputへPL Selectを戻さない。

PC.personが必須であるデータ仕様自体は維持する。

また、現在PL変更によって過去のParticipation.personを自動上書きしない。

Person表示名編集もPC編集とは別責務とする。

------------------------------------------------------------------------

# 5. PC本体へ追加しない情報

PC登録／編集Visual Designを理由として、以下をPC本体へ追加しない。

-   SAN現在値
-   HP現在値
-   MP現在値
-   成長
-   生還／ロスト
-   後遺症
-   Table時点のHO
-   Table時点のセリフ
-   その他EndPcState / Participation側の履歴情報

SAN / HP / MP等の状態値はTable /
Participation側のEndPcState履歴として扱う。

------------------------------------------------------------------------

# 6. 情報階層

PC登録／編集では、PCそのものを主役とする。

基本的な視覚優先順位：

1.  PC Portrait
2.  PC名
3.  現在PL
4.  Game System
5.  Character Sheet

「誰のPCか」よりも先に「どのPCを登録・編集しているか」を認識できる構造とする。

PLは新規登録では必須だが、PCの主役性を奪うほど強く見せない。

------------------------------------------------------------------------

# 7. 新規登録時の入力順序

基本順序：

1.  PC Portrait
2.  PC名
3.  PL / Person
4.  Game System
5.  Character Sheet URL
6.  保存

概念構造：

``` text
PC新規登録
├─ Portrait
├─ PC名 *
├─ 現在PL / Person *
├─ Game System
├─ Character Sheet URL
└─ このPCを登録
```

------------------------------------------------------------------------

# 8. 編集時の入力順序

基本順序：

1.  PC Portrait
2.  PC名
3.  現在PL（Read Only）
4.  Game System
5.  Character Sheet URL
6.  保存
7.  PC削除領域

概念構造：

``` text
PC編集
├─ Portrait
├─ PC名 *
├─ 現在PL（Read Only）
├─ Game System
├─ Character Sheet URL
├─ 変更を保存
└─ PC削除
```

現在PLのRead
Only表示には、原則として常時「ここでは変更できません」等の説明文を増やさない。

PL変更はPC Focus側の専用Actionとして分離する。

------------------------------------------------------------------------

# 9. Person選択UI

新規PC登録時のPerson選択は、単純なSelectよりも、

> **検索可能なCombo Box**

を基本候補とする。

Combo
Boxを開いた時点で候補を表示できるようにし、最近使用したPerson等を探しやすくする。

下部に、

> `＋ 新しい人物を追加`

を配置する。

SelfPersonを「自分のPCである可能性が高い」という推測だけで自動確定しない。

候補提示とユーザーによる確定を分離する。

------------------------------------------------------------------------

# 10. 新規Person作成

未登録Personが必要な場合、PC登録フロー内から小さなDialog等で作成できる。

基本項目：

-   表示名：必須

作成成功後：

1.  PC登録画面へ戻る
2.  作成したPersonを自動選択する

Person作成のためにPC登録画面の入力状態を失わない。

Person専用管理画面は作らない。

------------------------------------------------------------------------

# 11. Desktop Visual Layout

Desktopでは、

> **左：大きなPortrait / 右：Profile Input**

を基本とする。

概念レイアウト：

``` text
← 戻る

CHARACTER
PCを登録 / PCを編集
────────────────────────────────────────

        PORTRAIT                  PROFILE

  ┌────────────────┐      PC NAME *
  │                │      [                    ]
  │                │
  │   PC PORTRAIT  │      PLAYER
  │                │      [ Person選択 ]  ※新規
  │                │      現在PL表示       ※編集
  └────────────────┘
                           GAME SYSTEM
   画像操作                [                    ]

                           CHARACTER SHEET
                           [                    ]

────────────────────────────────────────

                           [ 登録 / 保存 ]
```

「CHARACTER」「PROFILE」等の英字補助ラベルはVisual上の候補であり、データ項目ではない。

意味のない英字装飾を増やさない。

------------------------------------------------------------------------

# 12. Portraitの扱い

PCではPortraitをScenario登録画面の作品画像よりも強く扱う。

理由：

-   PL / PC Collectionの主役がPC Portraitである
-   PC FocusでもPC画像が主要Visualとなる
-   PC登録／編集でもCollectionへ追加されるキャラクターを視覚的に確認できる方がよい

DesktopのPortrait / Information比率はPrototype開始値としておおむね
`5 : 7` 程度から検証可能だが、固定仕様とはしない。

正確なpx・高さ・比率はPrototype / 実装時に調整する。

Portraitを不要なCard / Shadow / SaaS的Panelで囲みすぎない。

画像と余白を中心に成立させる。

------------------------------------------------------------------------

# 13. PC画像

PC画像は、

> **PC本体に属する任意項目**

である。

TableやParticipationの画像ではない。

同一PCが複数Tableへ登場しても、PC本体の同じ画像を参照する。

画像未登録はError / 欠損扱いにしない。

------------------------------------------------------------------------

# 14. 画像未登録Placeholder

画像がない場合は、PL / PCで採用済みのNeutral Placeholder思想へ接続する。

方向：

-   Neutral Gray背景
-   PC名のTypography
-   ごく薄いRule / Grid等
-   控えめな画像未登録表現

巨大な `NO IMAGE` 表示は避ける。

Placeholder内のPC名など「見る文字」にはShippori Minchoを利用可能。

------------------------------------------------------------------------

# 15. PC画像編集は同一画面内で完結

PC画像の追加・変更・位置調整・Zoom等のために、

> **別の画像編集画面へ遷移しない。**

PC登録／編集画面のPortrait領域内でそのまま編集する。

基本フロー：

``` text
PC登録／編集
↓
画像を追加 / 変更
↓
同一画面でPreview
↓
位置調整 / Zoom
↓
必要なら他のPC情報も編集
↓
登録 / 変更を保存
```

画像編集専用ページは新設しない。

------------------------------------------------------------------------

# 16. Desktopでの画像操作

Desktopでは以下を維持する。

-   Portrait上でDragして位置調整
-   Zoom Slider
-   Zoomの＋／－操作
-   中央 / Reset
-   画像変更
-   画像削除

画像上へ多数の操作ButtonをOverlayしない。

画像上は直接Dragを中心とし、明示操作はPortrait下へ静かに配置する。

------------------------------------------------------------------------

# 17. Mobileでの画像操作

Mobileでは以下を採用する。

-   1本指Drag：画像位置調整
-   Pinch Zoom：主要なZoom操作
-   Zoom Slider：Pinchの代替操作
-   中央に戻す：Reset
-   画像変更
-   画像削除

## Mobileでは削除する操作

> **Zoomの＋／－ButtonはMobileでは表示しない。**

理由：

-   Pinch ZoomがTouch端末では自然
-   Sliderを代替操作として残せる
-   操作項目を減らし、画像編集ツール化を避けられる
-   狭い画面でPortrait周辺をすっきり保てる

Pinchのみを唯一のZoom手段にはしない。

------------------------------------------------------------------------

# 18. 画像保存の安全性

画像を変更・削除した瞬間に本番の旧画像を破壊しない。

フォーム内では変更予定状態として扱い、

1.  新画像選択
2.  Preview
3.  Position / Zoom調整
4.  PC保存
5.  保存成功
6.  新状態を正式採用
7.  必要に応じて旧画像を安全に削除

という考え方を維持する。

保存失敗時には旧状態を失わないようにする。

------------------------------------------------------------------------

# 19. Typography

既存Design Systemを維持する。

> **見る文字 = Shippori Mincho**\
> **操作する文字 = Noto Sans JP**

PC登録／編集では、

Noto Sans JP：

-   Form Label
-   Input
-   Button
-   Combo Box
-   Error
-   操作説明

Shippori Mincho：

-   Portrait Placeholder上のPC名
-   Gallery的に見せる表示要素

等を基本とする。

入力フォームそのものを無理に明朝体へしない。

------------------------------------------------------------------------

# 20. PC削除

PC削除入口は、

> **PC編集画面最下部**

へ配置する。

PC Focusへ強い赤色削除Actionを常設しない。

------------------------------------------------------------------------

# 21. PC削除条件

削除可能：

> **関連Participationが0件のPCのみ**

関連ParticipationがあるPCは削除不可。

PC削除のために、

-   Participation
-   EndPcState

を自動削除・解除しない。

------------------------------------------------------------------------

# 22. PC削除Visual

画面最下部に通常コンテンツから十分離して配置する。

常時巨大な赤Panelや強い「DANGER ZONE」演出を必須とはしない。

Danger色は主として削除Actionへ使用する。

削除可能例：

``` text
────────────────────────

PCの削除

このPCを削除します。
削除したPCは元に戻せません。

                    [ PCを削除 ]
```

削除不可例：

``` text
────────────────────────

PCの削除

このPCは登場した卓があるため削除できません。
登場した卓 6件

                     PCを削除
                    （disabled）
```

------------------------------------------------------------------------

# 23. PC削除確認Dialog

削除可能なPCで「PCを削除」を選択した場合のみ確認Dialogを表示する。

例：

``` text
「五色 探」を削除しますか？
この操作は元に戻せません。

[ キャンセル ] [ 削除 ]
```

対象PC名を明示する。

PC名再入力等の過剰な確認操作は要求しない。

------------------------------------------------------------------------

# 24. Entry Context

PC新規登録には複数の入口が存在する。

## 24.1 PL / PC Collectionから

``` text
PL / PC Collection
↓
＋ PCを追加
↓
PC新規登録
```

## 24.2 卓登録／編集中から

``` text
卓登録／編集
↓
PC選択
↓
＋ 新しいPC
↓
PC新規登録
```

Entry Contextを保持し、保存後Navigationを混同しない。

------------------------------------------------------------------------

# 25. 通常新規登録後Navigation【今回確定】

PL / PC Collectionから新規PCを登録した場合：

``` text
PL / PC Collection
↓
＋ PCを追加
↓
PC登録
↓
保存成功
↓
新しく登録したPCのPC Focus
```

登録したPCのProfileをそのまま確認できるようにする。

------------------------------------------------------------------------

# 26. 卓フォーム経由の新規PC登録後Navigation

卓登録／編集中から新規PCを作成した場合：

``` text
卓登録／編集
↓
＋ 新しいPC
↓
PC登録
↓
保存成功
↓
元の卓フォーム
↓
新PCを自動選択
```

この場合、

> **元の卓フォームDraftを失わない。**

通常新規登録後のPC Focus遷移よりもReturn Contextを優先する。

------------------------------------------------------------------------

# 27. PC編集後Navigation

``` text
PC Focus
↓
PC編集
↓
変更を保存
↓
元のPC Focus
```

編集したPCをそのまま表示する。

------------------------------------------------------------------------

# 28. PC削除後Navigation【今回確定】

PC削除成功後：

``` text
PC Focus
↓
PC編集
↓
PC削除
↓
PL / PC Collection
```

削除済みPCのPC Focusへ戻らない。

------------------------------------------------------------------------

# 29. 未保存離脱

フォームを開いただけでは離脱確認を出さない。

初期状態から変更が存在する場合のみ、戻る・別Navigation等で離脱確認を行う。

変更判定対象例：

-   PC名
-   新規時のPL選択
-   Game System
-   Character Sheet URL
-   画像追加
-   画像変更
-   画像削除
-   画像位置
-   Zoom

確認例：

``` text
編集内容を破棄しますか？
保存していない変更は失われます。

[ 編集を続ける ] [ 破棄して移動 ]
```

------------------------------------------------------------------------

# 30. Validation

基本はInput直下のInline Errorとする。

例：

``` text
PC NAME *
[                    ]
PC名を入力してください。
```

``` text
PLAYER *
[ Personを選択     ]
PLを選択してください。
```

PC画像・Game System等の任意項目を未入力Warningにはしない。

Character Sheet
URL等で形式確認が必要な場合は対象Input付近へ具体的に表示する。

------------------------------------------------------------------------

# 31. 同一Person + 同一PC名

同一Personに同名PCが存在しても登録禁止にはしない。

必要に応じて、

> `同じPLに同名のPCが登録されています。`

等の警告を行うが、ユーザーは登録を続行可能とする。

------------------------------------------------------------------------

# 32. Saving

保存中は二重送信を防止する。

例：

-   `このPCを登録` → `登録中…`
-   `変更を保存` → `保存中…`

画面全体を不要なLoading Overlayで覆わない。

保存失敗時：

-   同じ画面に留まる
-   入力値を失わない
-   画像調整状態を失わない
-   保存Action付近等へErrorを表示する

原因を安全に特定できる場合は、

-   PC情報を保存できない
-   画像を保存できない

等、対象を具体化してよい。

------------------------------------------------------------------------

# 33. Desktop Responsive

Desktopでは、

``` text
Portrait | Profile
```

の2カラムを基本とする。

画面幅が狭くなった場合は、

``` text
Portrait
   ↓
Profile
```

へ再配置する。

Responsiveによってデータ項目を削除しない。

------------------------------------------------------------------------

# 34. Mobile Layout

Mobileでは1カラムとする。

概念：

``` text
← 戻る

PCを登録 / PCを編集

PORTRAIT
[ PC画像 ]

画像操作
Position / Zoom / Reset

PC NAME
[             ]

PLAYER
[             ]

GAME SYSTEM
[             ]

CHARACTER SHEET
[             ]

[ 登録 / 保存 ]

編集時のみ
PC削除
```

MobileでもPortraitを単なる小さなThumbnailへ落としすぎない。

ただしPortraitだけでViewportを占有しすぎないよう、最終高さはPrototypeで調整する。

------------------------------------------------------------------------

# 35. Responsiveの原則

> **Responsive = 情報削除ではなく再配置**

DesktopとMobileで扱う正式情報は同じ。

新規：

-   PC名
-   PL
-   PC画像
-   Game System
-   Character Sheet URL

編集：

-   PC名
-   現在PL確認
-   PC画像
-   Game System
-   Character Sheet URL
-   PC削除領域

を維持する。

------------------------------------------------------------------------

# 36. Keyboard

Keyboardのみでも主要操作を完結可能にする。

最低限：

-   TabでInteractive要素へ移動
-   Combo Box操作
-   Slider操作
-   保存
-   戻る
-   削除
-   Dialog操作
-   Reset

をKeyboard対応する。

画像Positionについては、

> **Mouse / Touch Dragのみを唯一の位置調整方法にしない**

ことを要件とする。

Arrow
key等による具体的な微調整方法・移動量は実装設計時に調整可能とし、現時点で固定しない。

------------------------------------------------------------------------

# 37. Focus

既存Design Systemに従い、

> **Accent Strong #3F7D79 + Ring / Outline**

を基本とする。

色だけでFocusを表現しない。

Dialogを開いた場合はFocusをDialog内へ移動する。

キャンセル時には起点となったActionへ適切にFocusを戻す。

------------------------------------------------------------------------

# 38. Person作成DialogのFocus

`＋ 新しい人物を追加` からDialogを開く。

キャンセル時：

-   起点Action付近へFocusを戻す

作成成功時：

-   PC登録画面へ戻る
-   新Personを選択状態にする
-   Person選択UIへ自然にFocusを戻す

------------------------------------------------------------------------

# 39. 削除DialogのFocus

PC削除確認Dialogを開いた場合、FocusをDialog内へ移動する。

キャンセル時は、

> `PCを削除`

ActionへFocusを戻す。

削除成功時はPL / PC Collectionへ遷移する。

------------------------------------------------------------------------

# 40. Validation時のFocus

保存時にValidation
Errorが存在する場合、ユーザーが問題箇所を探し回らないようにする。

複数Errorがある場合は、

-   最初のErrorへFocusを移す
-   または簡潔なError Summaryを設ける

等の方法を採用できる。

各Errorは対象Inputと関連付ける。

色だけでErrorを伝えない。

------------------------------------------------------------------------

# 41. Reduced Motion

PC登録／編集は入力画面であり、大きなMotionを前提としない。

使用可能な軽いMotion：

-   画像Preview反映
-   Validation表示
-   保存成功
-   Dialog
-   View切替

`prefers-reduced-motion: reduce` 時には、

-   Fade短縮 / 除去
-   Slide除去
-   大きなZoom Transition除去

等を行う。

Motionを無効化しても情報理解・操作が成立する構造とする。

------------------------------------------------------------------------

# 42. Scenario登録／編集との差別化

Scenario登録／編集とPC登録／編集は同じフォーム系Design
Systemを共有するが、役割を分ける。

  観点         Scenario           PC
  ------------ ------------------ ----------------------------------
  主Visual     作品画像           PC Portrait
  Visual比重   中                 大
  画面の意味   Collection Entry   Character Entry / Profile Editor
  左側         作品Preview        キャラクターそのもの
  右側         作品情報           PC Profile
  画像操作     補助               主要操作の一つ
  接続先       Personal Gallery   Portrait Gallery

同じ左右構成でも単純コピーにはしない。

------------------------------------------------------------------------

# 43. v5.9との関係

Desktop卓詳細 PC Focus v5.9は、

> **卓におけるPC / ParticipationをゲームのCharacter
> Selectのように見せる閲覧Visual**

として維持する。

PC登録／編集へv5.9のVisualを理由なく移植しない。

特に以下をPC登録／編集の正式仕様へ追加しない。

-   v5.9モック上だけの属性
-   PC名読み仮名
-   SAN / HP / MP最大値
-   モック用状態値
-   仮Header
-   仮Button
-   仮Menu
-   未確定のデータ属性・状態・操作

最重要原則：

> **見た目は各画面の役割に合わせる。中身は既存の確定済み設計。**

------------------------------------------------------------------------

# 44. 今回変更していない重要仕様

本フェーズによって以下は変更していない。

-   PCはUser所有
-   PersonはUser所有
-   PC名必須
-   新規PCでは現在Person必須
-   PC画像は任意
-   Game Systemは任意
-   Character Sheet URLは任意
-   PC本体に現在SAN / HP / MPを持たせない
-   EndPcStateはTable / Participation側の履歴
-   PC編集から現在Personを直接変更しない
-   PL変更は専用フロー
-   現在PL変更で過去Participation.personを上書きしない
-   Person表示名変更は別責務
-   関連ParticipationがあるPCは削除不可
-   PC画像は変更 / 削除可能
-   認証 / 認可
-   User所有権確認
-   Responsive
-   Keyboard
-   Focus
-   Reduced Motion

------------------------------------------------------------------------

# 45. 今回新たにVisual / Interactionとして確定した主な事項

本フェーズで新たに確定した主な事項：

1.  PC登録／編集のVisual Conceptを
    `Character Entry / Character Profile Editor` とする
2.  `大きなPortrait + 静かなProfile` を基本構造とする
3.  Desktopは左Portrait / 右Profileを基本とする
4.  MobileはPortrait → Profileの1カラムへ再配置する
5.  PC画像編集を別画面へ遷移せず同一PC登録／編集画面内で完結する
6.  編集時の現在PLはRead Only表示とする
7.  MobileではZoomの＋／－Buttonを削除する
8.  Mobile ZoomはPinchを主要操作、Sliderを代替操作とする
9.  通常新規登録後は新PCのPC Focusへ進む
10. PC削除成功後はPL / PC Collectionへ進む
11. 変更が存在する場合のみ未保存離脱確認を行う
12. ValidationはInline Errorを基本とする
13. 保存失敗時も入力・画像調整状態を保持する
14. 削除領域は編集画面最下部へ静かに配置する
15. Responsiveでは情報を削除せず再配置する

------------------------------------------------------------------------

# 46. 実装時調整でよい項目

以下は現時点で固定しすぎず、Prototype / 実装時に調整可能とする。

-   Desktop Portraitの正確な幅・高さ
-   Portrait : Profileの最終比率
-   Breakpoint
-   Grid Gap
-   Form幅
-   PortraitのMobile高さ
-   Zoom Sliderの細かな寸法
-   Desktopの＋／－Buttonの最終配置
-   Position Keyboard代替操作の具体的方法
-   Arrow keyによる画像移動量
-   Focus Ringの最終太さ
-   Motion duration
-   easing
-   Placeholder Typographyの最終サイズ・位置
-   Error Summaryを常設するか、複数Error時のみ表示するか

これらはVisual / Implementation
Detailであり、データ仕様を変更するものではない。

------------------------------------------------------------------------

# 47. 最終整合性確認

本フェーズの確定内容は、既存のPC / Person / Participation /
EndPcState等の責務を変更していない。

特に、

-   PC属性を勝手に増やしていない
-   PC本体へSAN / HP / MP等を追加していない
-   PC編集へPL直接変更を戻していない
-   過去Participation.personを上書きしていない
-   関連ParticipationがあるPCを削除可能にしていない
-   PC削除のためにParticipation / EndPcStateを自動削除していない
-   v5.9モック情報を正式仕様化していない
-   卓フォーム経由PC作成時のReturn Contextを壊していない
-   PC画像をTable / Participation側へ移していない

ことを確認した。

------------------------------------------------------------------------

# 48. 現時点のPC登録／編集全体構造

``` text
PC登録／編集
│
├─ Header / Back
│
├─ Portrait
│   ├─ 画像追加
│   ├─ 画像変更
│   ├─ 画像削除
│   ├─ Position
│   ├─ Zoom
│   └─ Reset
│
├─ Profile
│   ├─ PC名
│   │
│   ├─ 新規
│   │   └─ PL / Person選択
│   │       └─ ＋ 新しい人物を追加
│   │
│   ├─ 編集
│   │   └─ 現在PL Read Only
│   │
│   ├─ Game System
│   └─ Character Sheet URL
│
├─ Save
│
└─ 編集時のみ
    └─ PC削除
```

Navigation：

``` text
PL / PC Collection
→ PC新規登録
→ 新PC Focus
```

``` text
PC Focus
→ PC編集
→ 元PC Focus
```

``` text
卓登録／編集
→ PC新規登録
→ 元卓フォーム
→ 新PC自動選択
```

``` text
PC編集
→ PC削除
→ PL / PC Collection
```

------------------------------------------------------------------------

# 49. 次工程へ引き継ぐ際の注意

今後の画面設計・Prototype・実装では、本資料をPC登録／編集Visual
Designの確定事項として参照する。

ただし本資料は既存データ仕様を置き換えるものではない。

データ・Entity・CRUD・UC・認証／認可・保存／削除ルール等は、それぞれの既存確定資料を正本とする。

Visual上の都合から新しいPC属性や状態値が必要に見えた場合も、自動的に正式仕様へ追加せず、

1.  既存仕様との差分を確認
2.  Visual上必要な理由を整理
3.  データ設計への影響を確認
4.  ユーザーの採用判断を取る

という順序を守る。

------------------------------------------------------------------------

# 50. 最重要原則

> **PC登録／編集は、Portrait GalleryへPCを迎えるための静かなCharacter
> Entry / Profile Editorとする。**

> **PC画像は主役として扱うが、入力画面を過剰なゲームUIにはしない。**

> **PC編集とPL変更を混同しない。**

> **PC本体とTable / Participation / EndPcStateの責務を混同しない。**

> **Visual Designを理由として既存の確定済みデータ仕様を変更しない。**
