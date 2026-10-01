# Web制作総合演習1
# TRPG活動履歴管理Webサービス
## Scenario登録／編集 Visual Design再設計フェーズ 確定事項まとめ
### 2026-09-25

---

# 0. この資料の目的

本資料は、TRPG活動履歴管理Webサービスにおける
**Scenario登録／編集画面のVisual Design再設計フェーズ**
で、2026-09-25の検討により採用・整理・更新した事項を、
今後のFigma・実装・引継ぎ・設計確認用の情報源としてまとめるものである。

本資料は既存のデータ設計・UC・CRUD・認証認可を置き換えるものではない。

最重要原則：

> **既存確定仕様を維持し、現在のPersonal Gallery / Collection思想へVisual / Interactionを落とし込む。**

また、PC Focus v5.9はDesktop卓詳細のVisual Design / UI実装ベースであり、
Scenario登録／編集へCharacter Select表現をそのまま流用するものではない。

Scenario登録／編集は、

> **入力の分かりやすさ・軽さ・安全性を優先する画面**

として扱う。

---

# 1. 既存仕様との関係

Scenario登録／編集の中身・データ・機能については、
既存の確定済み設計を正本とする。

今回のVisual Design再設計によって、以下の既存仕様を変更しない。

- User
- Scenario
- ScenarioFavorite
- Person
- PC
- Table
- TableDate
- Participation
- EndPcState
- 9エンティティ / 14UC
- CRUD
- 認証・認可
- User所有権
- 保存 / 更新 / 削除ルール
- Scenario登録 / 編集
- Table登録 / 編集
- その他既存の確定仕様

Mock・試作コード・画像生成・v5.9上だけに存在する情報を、
今回のVisual Designを理由として正式仕様へ追加しない。

---

# 2. アプリ全体のVisual Conceptとの関係

現在のサービス全体は、

> **TRPGの思い出を収蔵する Personal Gallery / Collection**

として扱う。

Scenarioごと・PCごとに異なる世界観が存在するため、
サービス側から強すぎる共通TRPGモチーフを被せない。

Brandは主に、

- Typography
- Grid
- Spacing
- Rule
- Metadata placement
- Focus
- Motion
- Interaction
- subtle Accent

で形成する。

以下のような装飾を全画面へ乱用しない。

- 魔法陣
- 羊皮紙
- Horror Texture
- 意味のない数字・英語
- 過剰な斜線
- AI風筆記体
- 過剰なゲームHUD

---

# 3. 現在の画面役割

## Home

> **ScenarioのCurated Personal Gallery / 美術館**

複数Scenarioを作品として眺める場所。

## Scenario View

> **1作品の個展 / 作品展示室**

1件のScenarioと、
そのScenarioに属するSession Archive / Table一覧を見る場所。

## Desktop卓詳細

> **その卓の記憶の中へ入り込むCharacter Select**

PC Focus v5.9をVisual Design基準とする専用閲覧画面。

## PL / PC

> **自分が演じてきた人物たちのPortrait Collection**

## Scenario登録／編集

> **Personal Galleryへ作品情報を追加・修正するための静かな入力画面**

閲覧系画面ほど演出を強めず、
入力作業の分かりやすさ・軽さ・安全性を優先する。

ただし、汎用SaaS Formへ戻しすぎず、

- Typography
- Grid
- Spacing
- Rule
- Scenario Image
- Metadata
- Accent

によってGallery系Brandとの接続を維持する。

---

# 4. 現在のNavigation

旧設計の、

```text
Home
└ Scenario Accordion
   └ Table
      └ Table詳細
```

は廃止済み。

現在は、

```text
Home Scenario Gallery
↓
Scenario View
↓
Table選択
↓
Desktop卓詳細 v5.9
```

を基本Navigationとする。

Scenario View内のTable一覧は、

> **Session Archive**

として扱う。

---

# 5. Scenario登録／編集画面のVisual Concept

Scenario登録／編集画面は、

> **Personal Galleryへ新しい作品を収蔵するためのCollection Entry**

に近い意味を持たせる。

ただし、

- 額縁
- 展示札
- 美術館風背景
- 過剰な展示演出

等を直接的に付与するものではない。

意味のつながりは、

```text
Home
Scenarioを作品として眺める
↓
Scenario登録
作品情報をCollectionへ加える
↓
Scenario View
登録した1作品を見る
```

として成立させる。

---

# 6. 基本フォーム構造

Scenario登録／編集は、

> **1ページの連続フォーム**

とする。

Step Wizardへ分割しない。

不要なAccordionを追加しない。

Desktopの基本構造：

```text
SCENARIO
シナリオを追加 / シナリオを編集

BOOTHから入力を補助
[ BOOTH商品URL                       ] [情報を取得]

────────────────────────

Scenario Image        Scenario Information

                      シナリオ名
                      作者
                      Game System
                      Scenario URL

────────────────────────

シナリオを追加 / 変更を保存
```

入力画面としての軽さを優先する。

---

# 7. BOOTH補助

## 7.1 位置づけ

BOOTHは、

> **Scenario登録そのものではなく、入力補助**

として扱う。

画面上部に配置するが、
画面の主役にはしない。

「BOOTHを入力しないと登録できない」と誤解されるVisualにしない。

## 7.2 基本UI

例：

```text
BOOTHから入力を補助

BOOTHの商品URLを入力すると、
利用できる情報をフォームへ反映します。

[ https://booth.pm/...              ] [情報を取得]
```

巨大なCardや独立Stepにはしない。

- 小さめのSection Label
- 1行Input
- Secondary Action
- 細いRule

程度の軽い構成とする。

## 7.3 取得対象

既存仕様を維持し、

- Scenario名
- 作者候補
- Game System候補
- 商品画像候補

を取得対象とする。

## 7.4 取得結果

BOOTH取得結果は正式保存データではない。

```text
BOOTH URL
↓
情報取得
↓
フォームへ候補反映
↓
ユーザー確認・修正
↓
正式保存
```

とする。

ユーザー確認前に正式データへ自動確定しない。

## 7.5 取得中

取得中も画面全体をロックしない。

例：

```text
[ BOOTH URL                       ] [取得しています…]
```

BOOTH取得中でも、

- Scenario名
- 作者
- Game System
- Scenario URL
- Scenario画像

の手入力を継続可能とする。

全画面Loading Overlayは原則使用しない。

## 7.6 取得成功

取得成功後は同一画面内でフォームへ候補を反映する。

画面遷移しない。

反映されたInput等へ、
軽いFade / Highlightを使用してよい。

大きなSuccess Bannerは不要。

## 7.7 取得失敗

BOOTH取得失敗はScenario登録失敗ではない。

例：

> BOOTHから情報を取得できませんでした。  
> 内容を確認するか、このまま手入力できます。

局所Errorとして表示し、
既に入力済みのScenario情報を失わない。

## 7.8 編集時

編集画面でもBOOTH補助を利用可能とする。

想定用途：

- 後からBOOTH URLを見つけた
- 作者候補を補完したい
- Game System候補を補完したい
- 画像候補を取得したい

ただし、

> **既存値が存在する項目を、BOOTH取得結果で無言上書きしない。**

BOOTH情報は編集時も候補として扱う。

---

# 8. Scenario入力項目

既存仕様を維持する。

- Scenario名：必須
- 作者：任意
- Game System：任意、候補＋自由入力
- Scenario URL：任意
- Scenario画像：任意

入力順序は、

1. Scenario名
2. 作者
3. Game System
4. Scenario URL
5. Scenario Image

を基本とする。

ただしDesktopでは、
入力順とVisual配置を分離してよい。

---

# 9. Scenario Image

## 9.1 Desktop配置

Desktopでは、

> **左 = Scenario Image Preview  
> 右 = Scenario Information**

を基本構造とする。

例：

```text
┌────────────────┬──────────────────────┐
│                │ シナリオ名 *         │
│                │ [                  ] │
│ Scenario Image │                      │
│    Preview     │ 作者                 │
│                │ [                  ] │
│                │                      │
│                │ Game System          │
│                │ [                  ] │
│                │                      │
│                │ Scenario URL         │
│                │ [                  ] │
└────────────────┴──────────────────────┘
```

Scenario ImageによってGalleryとのVisual接続を作りつつ、
入力画面が画像中心になりすぎないようにする。

## 9.2 画像候補

既存仕様を維持し、

- BOOTH等の外部画像候補
- User Upload
- 画像なし

を扱う。

既存の優先関係：

> **User Upload > 外部画像候補**

を維持する。

## 9.3 画像操作

Upload画像は変更 / 削除可能。

Preview付近に、

- `画像を変更`
- `画像を削除`

を配置する。

Visual強度：

- `画像を変更`：Secondary Action
- `画像を削除`：弱いText Action

Scenario本体削除ほど強いDanger表現にしない。

## 9.4 Radio Button化しない

画像選択を、

```text
○ BOOTH画像
○ Upload画像
○ 画像なし
```

のような設定画面型UIへ寄せすぎない。

基本は、

- Preview
- Upload
- 外部画像候補
- 削除

を直接操作するUIとする。

## 9.5 Upload画像削除後

Upload画像を削除したとき、
外部画像候補へ自動復帰しない。

外部画像候補が利用できる状態なら、

```text
BOOTHから取得した画像候補があります。
[この画像を使用]
```

等を表示し、
使用するかどうかをユーザーが選択する。

理由：

> 「Upload画像を削除した」ことが
> 「Scenario画像自体をなくしたい」という意図である可能性があるため。

## 9.6 データ構造上の注意

今回、

> User Upload > 外部画像候補

というUI上の優先関係は維持する。

ただし、

> **User Upload画像を使用中にも、外部画像候補を別データとして恒久保存し続ける**

という新しいデータ構造までは今回確定しない。

登録 / 編集中のDraft上で外部候補が残っている場合は再選択可能としてよい。

ページ離脱後も外部画像候補を保持する具体方式は、
実装前のデータ設計確認事項として残す。

Visual Designの都合でScenario属性を追加しない。

---

# 10. BOOTH外部画像の既存方針

BOOTH商品画像については、

> **画像ファイルを自サービスへ複製・保存しない**

ことを基本とする。

保存対象は画像ファイルそのものではなく、
外部画像URL。

元BOOTH商品ページへの導線を設ける既存方針を維持する。

なお、BOOTH外部画像URL表示は、
法務・コンプライアンス上、

> **条件付き採用**

である。

「スクレイピング可能 = 画像二次利用が無条件に許可」
とは扱わない。

---

# 11. 重複候補

Scenario名 / URL等が似ていても自動統合しない。

既存候補を提示し、

- `既存を使う`
- `新規登録を続ける`

をユーザーが選択する。

重複候補は、

> **Validation Errorではない。**

したがって赤いErrorとして扱わない。

Visual上はWarning / Neutral寄りとする。

例：

```text
似たScenarioがあります

カン・カカリ
作者：○○

[既存のScenarioを見る]
[このまま新規登録]
```

---

# 12. 新規登録と編集の関係

新規と編集で別Visualを作らない。

> **同一フォーム構造を共有する。**

新規時：

- 空のフォーム
- 保存Action：`シナリオを追加`
- Danger Zoneなし

編集時：

- 既存値を初期表示
- BOOTH補助利用可
- Scenario画像変更 / 削除
- 保存Action：`変更を保存`
- 最下部にDanger Zone

---

# 13. Scenario編集時の基本Layout

```text
← Scenarioへ戻る

SCENARIO
シナリオを編集

BOOTHから入力を補助
[ BOOTH商品URL                    ] [情報を取得]

────────────────────────

┌────────────────┬──────────────────────┐
│                │ シナリオ名 *         │
│ Scenario Image │ [既存値]             │
│                │                      │
│                │ 作者                 │
│ [画像を変更]    │ [既存値]             │
│ [画像を削除]    │                      │
│                │ Game System          │
│                │ [既存値]             │
│                │                      │
│                │ Scenario URL         │
│                │ [既存値]             │
└────────────────┴──────────────────────┘

────────────────────────

                         [変更を保存]


        大きめの余白


────────────────────────
Danger Zone
```

---

# 14. Scenario削除 / Danger Zone

Danger Zoneは、

> **編集画面最下部**

に配置する。

通常Actionと同じ強さで常時見せない。

フォーム本体と十分な余白で分離する。

Visualは、

- Danger見出し
- 細いDanger Rule
- 説明
- 最終ActionだけDanger Color

程度とし、
画面全体を赤いCardにしない。

---

# 15. 関連Tableが0件の場合

関連Tableが0件の場合、
Scenario削除可能。

例：

```text
Danger Zone

シナリオを削除

このシナリオを削除します。
この操作は元に戻せません。

                         [シナリオを削除]
```

削除Action後に最終確認Dialogを表示する。

例：

```text
「カン・カカリ」を削除しますか？

この操作は元に戻せません。

[キャンセル]      [削除する]
```

論理削除・ゴミ箱・復元はMVPでは採用しない既存仕様を維持する。

---

# 16. 関連Tableが1件以上の場合

関連Tableが存在するScenarioは削除不可。

Scenario削除によってTableをCASCADE DELETEしない。

削除Buttonを押した後にエラーにするのではなく、
削除不可であることが事前に理解できるUIとする。

例：

```text
Danger Zone

シナリオを削除

このシナリオは 3件の活動履歴で使用されています。
活動履歴が残っているため、現在は削除できません。

[関連する卓を見る]
```

削除Buttonは表示しない、
または利用不可状態を明確に表現する。

色だけに依存して利用不可を示さない。

---

# 17. `関連する卓を見る`

Danger Zone内の、

> `関連する卓を見る`

は、

```text
Scenario編集
↓
関連する卓を見る
↓
元ScenarioのScenario View
↓
Session Archive
```

とする。

可能であればScenario View内のSession Archive位置まで移動する。

旧Home Accordionへ戻す設計にはしない。

---

# 18. 保存後Navigation【今回更新・採用】

旧仕様：

> 保存後はHomeへ戻り対象Scenario展開

は、
Home Accordion廃止前の仕様であるため更新する。

## 18.1 新規Scenario保存

```text
Home Scenario Gallery
↓
＋ Scenario追加
↓
Scenario登録
↓
シナリオを追加
↓
新しく作成したScenario View
```

保存後にHomeへ戻さない。

新しく作成したScenarioそのものを表示する。

理由：

- 登録成功を自然に確認できる
- Galleryへ作品を追加した流れと一致する
- 現在のHome → Scenario View構造に整合する

## 18.2 編集保存

```text
Scenario View
↓
Scenario編集
↓
変更を保存
↓
同じScenario View
```

同Scenario Viewへ戻る。

変更後の情報をそのまま確認できる。

必要に応じて、
Scenarioメイン情報へ短いFade / Highlightを使用してよい。

## 18.3 Scenario削除成功

```text
Scenario編集
↓
Scenario削除
↓
Home Scenario Gallery
```

削除されたScenario Viewは存在しなくなるため、
Homeへ戻す。

Home上で、

> `シナリオを削除しました`

程度の短いFeedbackを表示してよい。

---

# 19. 新規Scenario保存後の卓登録導線

旧仕様の、

> `＋このシナリオの卓を登録`

導線は維持する。

ただしScenario登録完了専用画面は作成しない。

新Scenario Viewへ遷移し、
Scenario View内のSession Archiveから卓登録へ進む。

Tableが0件の場合は、

```text
SESSION ARCHIVE

まだ卓の記録はありません。

＋ このシナリオの卓を登録
```

等、初回導線としてやや目立たせてよい。

Tableが存在する場合は、
通常のSession Archive内Actionとして扱う。

---

# 20. 戻る / キャンセル

新規登録時：

```text
Scenario登録
←
Home Scenario Gallery
```

編集時：

```text
Scenario編集
←
元Scenario View
```

とする。

変更なし / 保存済みの場合は確認なし。

未保存変更が存在する場合のみ、

> **変更内容が保存されていません。**

Action：

- `編集を続ける`
- `変更を破棄して移動`

を表示する。

ブラウザ更新・Tabを閉じる操作については、
可能な範囲でブラウザ標準の未保存警告を利用する。

---

# 21. Responsive

DesktopとMobileで、
機能・入力順・保存ルールを変えない。

Desktop：

```text
BOOTH補助

Scenario Image | Scenario Information

保存
```

Mobile：

```text
BOOTH補助

Scenario Image

Scenario名
作者
Game System
Scenario URL

保存
```

とする。

狭幅では横並びを縦積みに変更する。

Mobile専用の、

- Step
- Wizard
- Accordion
- 別ページ分割

を追加しない。

Scenario ImageはMobileでもフォームを圧迫するほど巨大にしない。

タッチ操作領域は、
既存方針どおり44〜48px程度を目安とする。

Breakpointのexact値は実装時調整でよい。

---

# 22. Keyboard / Focus / Accessibility

既存アクセシビリティ方針を維持する。

- Tab順序は視覚順と一致
- Enter / Space対応
- Esc対応
- Focus可視化
- Accent Strong系Focus Ring
- Hoverだけに依存しない
- 色だけに依存しない
- Dialog終了後は元操作へFocusを戻す
- Reduced Motion対応

特に以下をKeyboard操作可能にする。

- BOOTH URL Input
- BOOTH取得Action
- Scenario各Input
- Game System候補
- Scenario画像Upload
- Scenario画像変更 / 削除
- 外部画像候補の採用
- 重複候補
- 保存
- Danger Zone
- 削除確認Dialog

---

# 23. Saving

## 23.1 保存中

新規：

```text
[シナリオを追加]
↓
[保存しています…]
```

編集：

```text
[変更を保存]
↓
[保存しています…]
```

とする。

保存処理中は二重送信を防止する。

ただし画面全体を不要にLoading Overlayで覆わない。

## 23.2 保存成功

新規：

```text
保存成功
↓
新Scenario View
```

編集：

```text
保存成功
↓
同Scenario View
```

短いSuccess Feedbackのみ使用する。

巨大な完了画面を挟まない。

## 23.3 保存失敗

保存失敗時、

> **入力中のフォーム内容を保持する。**

例：

> 保存できませんでした。  
> 入力内容は保持されています。もう一度お試しください。

その場に留まり、
最初から入力し直させない。

---

# 24. Validation / Error

Scenario名のみ必須。

Scenario名未入力の場合は、
対象Input付近へErrorを表示する。

例：

```text
シナリオ名 *

[                     ]
シナリオ名を入力してください
```

可能であれば保存失敗時、
最初のValidation ErrorへFocusを移す。

以下は任意項目であり、
空欄自体をErrorにしない。

- 作者
- Game System
- Scenario URL
- Scenario画像

URLの厳密なValidation仕様は、
現時点で既存資料から確定できないため、
Visual Design段階で勝手に制約を追加しない。

---

# 25. 状態の役割分離

実装上は少なくとも、

```text
idle
fetchingBooth
boothError
saving
saveError
```

等の状態を分離する方向。

特に、

> **BOOTH取得中 ≠ Scenario保存中**

として扱う。

BOOTH取得中だからといって、
フォーム全体を操作不能にしない。

また、

- 保存済みScenarioデータ
- Draft Form State
- BOOTH候補データ

を混同しない。

編集画面でBOOTH情報を取得しても、
保存済み既存値を無言上書きしない。

---

# 26. 今回変更していない重要仕様

今回のVisual / Interaction再設計によって、
以下は変更していない。

- ScenarioはUser所有
- Scenario名必須
- 作者任意
- URL任意
- Game System任意
- Game Systemは候補＋自由入力
- Scenario名 / URLはUniqueにしない
- 重複候補を自動統合しない
- ScenarioFavoriteはScenario本体属性ではない
- User所有権確認
- 認証 / 認可
- 関連TableがあるScenarioは削除不可
- Scenario削除でTableを自動削除しない
- 論理削除 / ゴミ箱 / 復元はMVPでは採用しない
- BOOTH取得結果は正式データへ自動確定しない
- BOOTH取得失敗後も手入力可能
- BOOTH商品画像ファイルを自サービスへ複製・保存しない
- BOOTH外部画像URL利用は条件付き採用

---

# 27. 旧仕様から今回更新した内容

## 27.1 保存後Navigation

旧：

> 保存後はHomeへ戻り対象Scenario展開

新：

### 新規

> **新しく作成したScenario Viewへ移動**

### 編集

> **同じScenario Viewへ戻る**

### 削除

> **Home Scenario Galleryへ戻る**

---

## 27.2 新規保存後の卓登録導線

旧：

> 保存後に `＋このシナリオの卓を登録` を表示可能

新：

> **Scenario登録完了専用画面は作らず、新Scenario ViewのSession Archiveから卓登録へ進む。**

---

## 27.3 `関連する卓を見る`

旧Home Accordion前提ではなく、

> **Scenario View / Session Archive**

へ戻す。

---

# 28. 今回新たに採用したInteraction

既存仕様を変更せず、
今回Visual / Interactionとして以下を採用した。

- Scenario登録画面をCollection Entryとして扱う
- BOOTH補助を画面上部へ軽く配置
- BOOTHを必須Stepに見せない
- Desktopで左Image / 右Information
- 新規 / 編集で同一Layout共有
- 編集時もBOOTH補助利用可
- 編集時、BOOTH候補で既存値を無言上書きしない
- Upload画像削除後に外部候補へ自動復帰しない
- 外部候補を使うかユーザーが選択
- 重複候補をErrorではなくWarning / Neutralとして扱う
- Danger Zoneを編集画面最下部へ分離
- 関連Tableがある場合は削除不可状態を事前表示
- 保存成功後に完了専用画面を挟まない
- 未保存変更がある場合のみ離脱確認
- BOOTH取得中も手入力継続可能
- 保存失敗時に入力内容を保持
- Mobile専用Step / Accordionを増やさない

---

# 29. 実装前に残す未確定事項

以下はVisual Designフェーズを止める問題ではないが、
実装前に確認する。

- Scenario画像Uploadの対応ファイル形式
- Scenario画像Uploadの容量上限
- Scenario画像Uploadの保存先 / 物理ストレージ方式
- Upload画像差し替え時の旧ファイル削除方法
- Upload画像と外部画像候補を恒久的にどう保持するか
- Scenario URLの厳密なValidation
- 重複Scenario候補の具体的な判定基準
- Desktop / Tablet / Mobile breakpoint exact values
- Focus Ring exact px
- Motion duration
- easing
- 各Componentの詳細ARIA実装
- BOOTH外部画像URL表示の最終的な規約 / 実装確認
- BOOTH取得処理の具体技術方式

未確定事項を、
Visual Design上の都合から勝手に正式仕様化しない。

---

# 30. 最終レビュー結果

## Webクリエイター

以下を確認した。

- Home / Scenario ViewのGallery思想と接続している
- Scenario画像を活かしつつ入力画面が重くなっていない
- BOOTH補助が主役になっていない
- 手入力を常に継続できる
- 重複Scenarioの判断がErrorと混同されていない
- 外部画像 / Upload / 画像なしの関係を理解しやすくできる
- Danger Zoneを通常Actionと分離できている
- Mobileで破綻しない構造
- 一般的なCard過多のSaaS Formへ戻っていない
- Personal Gallery / CollectionとのVisual連続性を維持している

## エンジニア

以下を確認した。

- Scenarioの既存属性を勝手に増減していない
- User所有権を維持
- BOOTH候補と保存済み正式データを分離
- 重複候補を自動統合していない
- `User Upload > 外部画像候補` の既存優先関係を維持
- 関連TableがあるScenarioを削除しない
- Scenario → TableのCASCADE DELETEを導入していない
- 保存失敗時の入力保持を考慮
- Keyboard / Focus / Reduced Motionを維持
- Responsiveを既存方針と整合
- Error / Saving状態を分離可能
- Home / Scenario Viewの現在Navigationと整合
- Visual Designを理由に新しいデータ構造を確定していない

---

# 31. 総合判断

> **Scenario登録／編集 Visual Design再設計フェーズは確定可能。**

既存のデータ構造・UC・CRUD・認証認可・User所有権・Scenario削除ルールを壊す重大な矛盾は確認されなかった。

今回の再設計は、

> **既存のScenario登録／編集機能を、現在のPersonal Gallery / Collection思想へVisual / Interactionとして統合したもの**

として扱う。

特に重要な原則：

> **BOOTHは補助。Scenario登録が主役。**

> **閲覧画面は演出、登録／編集画面は軽さ・分かりやすさ・安全性。**

> **Visual Designの都合でデータ仕様を増やさない。**

---

# 32. 現時点のScenario登録／編集全体構造

```text
Home Scenario Gallery
│
├─ ＋ Scenario追加
│   ↓
│   Scenario登録
│   ├─ BOOTH補助
│   ├─ Scenario Image
│   ├─ Scenario名
│   ├─ 作者
│   ├─ Game System
│   ├─ Scenario URL
│   ├─ 重複候補
│   └─ シナリオを追加
│       ↓
│       新Scenario View
│       └─ Session Archive
│           └─ ＋ このシナリオの卓を登録
│
└─ Scenario View
    ├─ Session Archive
    └─ Scenario編集
        ├─ BOOTH補助
        ├─ Scenario Image変更 / 削除
        ├─ Scenario名
        ├─ 作者
        ├─ Game System
        ├─ Scenario URL
        ├─ 変更を保存
        │   ↓
        │   同Scenario View
        │
        └─ Danger Zone
            ├─ 関連Table 0件
            │   └─ Scenario削除
            │       ↓
            │       Home Scenario Gallery
            │
            └─ 関連Table 1件以上
                ├─ Scenario削除不可
                └─ 関連する卓を見る
                    ↓
                    Scenario View / Session Archive
```

---

以上を、2026-09-25時点の  
**「Scenario登録／編集 Visual Design再設計フェーズ 確定事項」**
とする。
