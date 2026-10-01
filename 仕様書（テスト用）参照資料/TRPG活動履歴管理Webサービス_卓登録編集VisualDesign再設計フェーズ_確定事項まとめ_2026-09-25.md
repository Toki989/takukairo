# Web制作総合演習1
# TRPG活動履歴管理Webサービス
## 卓登録／編集 Visual Design再設計フェーズ 確定事項まとめ
### 2026-09-25

---

# 0. この資料の目的

本資料は、卓登録／編集画面のVisual Design再設計フェーズで、2026-09-25のチャット内にて新たに採用・整理・変更した事項を、今後の設計・Figma・実装・引継ぎ用の情報源としてまとめるものである。

本資料は、既存のデータ設計・CRUD・UC・認証認可を置き換えるものではない。

最重要原則：

> **既存確定仕様を維持し、Visual / Interactionを現在のDesign Systemへ落とし込む。**

---

# 1. 今回の基本判断

卓登録／編集について、入力工程や入力内容を大きく作り直さず、

> **旧v5相当の「一画面で軽く入力できる感覚」を維持する**

方針を採用した。

つまり、

> UX構造を全面再設計するのではなく、現在のVisual Concept / Design Systemで描き直す。

## 維持する方向

- 1ページの連続フォーム
- 上から入力して最後に保存
- Step Wizardにしない
- 不要なAccordionを増やさない
- 入力項目を別画面へ過度に分割しない
- 既存Person / PC再利用を優先
- 新規作成は補助導線

## Visual上改善する方向

- Typography
- Grid
- Spacing
- Rule
- 情報階層
- Focus
- Error
- Saving
- Responsive
- 再利用候補の見せ方

---

# 2. 卓登録／編集画面の意味

閲覧系画面ではGallery / Character Select等の演出を強めるが、卓登録／編集は、

> **入力作業を軽く・安全に行う場所**

とする。

ただし、汎用Bootstrap / SaaS FormのCard群へ戻しすぎない。

アプリ全体のBrandは、

- Typography
- Grid
- Spacing
- Rule
- Metadata
- subtle Accent

で接続する。

Visual Conceptの比喩としては、

> **Session Sheet / Archive Entry**

に近い方向。

紙風・羊皮紙風等の装飾を意味しない。

---

# 3. Desktop全体構造【採用】

1カラム主体の連続フォームとする。

```text
Scenario Context
↓
TableName / TableDate
↓
参加者
  KP
  PL Tabs
↓
セッション録画URL
↓
保存
↓
編集時のみ Danger Zone
```

Cardを大量に使わない。

Scenarioは新規時すでに決まっているため、再選択させない。

Scenario Contextは確認情報として静かに見せる。

---

# 4. 卓基本情報【既存仕様維持＋Visual整理】

## TableName

- 任意
- 新規時に `卓1` 等をInputへ自動入力しない
- `卓1` 等は閲覧時FallbackでありDB保存しない

## TableDate

- 任意
- 複数可
- カレンダー
- `今日` Shortcut
- 実際に遊んだ日として扱う
- 登録日時と混同しない

Visual上は必要以上にCardで囲わず、見出し・Rule・Spacingで整理する。

---

# 5. 参加者入力：KP / PLの構造【今回更新・採用】

## 5.1 KP

KPはPLタブへ含めない。

独立した軽い入力行として扱う。

SelfPersonは初期候補だが、自動確定はしない。

## 5.2 PLはタブ形式へ変更

当初案ではPLブロックを人数分縦に繰り返す方向を検討したが、ユーザー提案により変更。

最終採用：

> **複数PLはタブ形式で切り替える。**

理由：

- 3〜5人以上でもフォーム全体が過度に縦長にならない
- 入力項目数は変えず、心理的な「入力が多い」印象を軽減できる
- 1 Participation = 1タブとしてデータ構造と一致

## 5.3 タブ表示

選択済み：

> `Person名 / PC名`

PC未選択：

> `Person名 / PC未選択`

Person未選択の追加直後：

> `新しいPL`

`PL1 / PL2 / PL3` のような番号のみの表現は使用しない。

## 5.4 ＋PL

タブ列末尾に、

> `＋ PL`

を配置。

追加すると新しいParticipationを作成し、そのタブを自動選択する。

## 5.5 タブ切替

- 切替時も入力内容を保持
- 保存はタブ単位ではなく卓全体で最後に1回
- 別タブにエラーがある場合はタブ側でも判別可能

## 5.6 タブVisual

一般的な角丸Button群にしない。

Typography + Underline / Rule主体。

選択中だけAccent Strong系の細いUnderline。

---

# 6. 選択中PLタブ内の順序【採用】

```text
PLAYER / PC
↓
HO / セリフ
↓
卓終了時状態
```

考え方：

> **横方向 = 誰を編集するか**

> **縦方向 = その人について何を記録するか**

---

# 7. Person / PC Select【今回採用】

## 7.1 検索可能コンボボックス

Person / PCともに、検索可能なコンボボックスを基本とする。

ただし検索入力を強制せず、開いた時点で候補が見える。

## 7.2 最近使用

既存仕様どおり最近使用候補を上位表示。

別ボタンや別モードとして分離しない。

## 7.3 Person選択後のPC候補

Personを選択すると、関連性の高いPCを候補上位へ出す。

ただし、

- PCを自動選択しない
- 選択Person以外のPCを選択不可にしない

Participation.personとPC.personの一致をDBで強制しない既存仕様を維持する。

## 7.4 PC未設定

PCは任意。

UIラベル：

> `PC（任意）`

PC未設定のままParticipation登録可能。

PC未設定時はEndPcStateを表示・保持しない。

PC未設定専用の確認操作を追加しない。

---

# 8. 新規Person / 新規PC【今回Visual整理】

## 8.1 新しいPerson

Person Select候補末尾に、

> `＋ 新しい人物を追加`

を配置。

小さなDialogでPersonを作成。

作成後：

> 元フォームへ復帰 → 新Person自動選択

Person専用管理画面は新設しない。

## 8.2 新しいPC

PC Select候補末尾に、

> `＋ 新しいPCを追加`

を配置。

DesktopではPersonより情報量が多いため、やや大きめのModal / Overlay Formを基本方向とする。

作成後：

> 元フォームへ復帰 → 新PC自動選択

卓登録途中の入力内容を失わせない。

---

# 9. HO / セリフ【今回Visual整理】

HOとセリフは別データとして両方入力可能。

Desktop：横並び。

Mobile：縦積み可。

Accordionへ隠さない。

セリフ：

- 任意
- 最大24文字
- 自動で「」を付けない
- `QUOTE`等の固定ラベルを追加しない

---

# 10. EndPcState入力【今回更新・採用】

## 10.1 表示

PCが選択された時点で自動表示。

`状態を追加` ボタンやAccordionは使用しない。

PC未設定時は表示しない。

## 10.2 任意表示

> `卓終了時状態　任意`

と見出しで一度だけ示す。

各フィールドに `任意` を繰り返さない。

## 10.3 レイアウト

主情報：

- SAN
- HP
- MP

Desktopでは横3列。

下段：

- 成長
- 生還 / ロスト
- 後遺症等

閲覧v5.9の情報階層を参考にするが、入力画面では演出を弱める。

## 10.4 前回値

前回EndPcStateはInputとは別に補助表示。

例：

```text
SAN
前回 63
[ 今回終了時 ]
```

前回値を自動入力・自動確定しない。

## 10.5 時系列根拠が弱い場合

`前回` と断定せず、

> `参考値`

として表示。

登録日時を実施日時として見せない。

## 10.6 最大値

v5.9 MockのSAN / HP / MP最大値等は正式仕様へ追加しない。

## 10.7 空EndPcState【今回新規採用】

> **EndPcState全項目が空欄の場合、空のEndPcStateレコードを作成しない。**

EndPcState 0〜1件という既存構造と整合する。

## 10.8 未確定実装項目

Visual Design上は確定したが、以下のDB型 / Validation詳細は現行資料から確定できない。

- SAN / HP / MPの型・範囲
- 成長の型・長さ
- 生還 / ロストの保存型
- 後遺症等の型・長さ

後続の実装設計で確認する。

---

# 11. PL削除UI【今回採用】

タブ上へ単純な `×` を置かない。

理由：

> タブを閉じるのかParticipationを削除するのか意味が曖昧になるため。

選択中PL領域内に、弱いText Actionとして、

> `このPLを削除`

を配置する。

編集時に既存Participationを削除する場合は確認する。

Person本体・PC本体は削除しない。

画面上では削除予定として扱い、`変更を保存` で確定する方向を採用。

---

# 12. 新規登録と編集の関係【今回採用】

新規と編集を別Visualへしない。

同じフォーム構造を共有する。

編集時の追加差分：

- 既存値初期表示
- Scenario変更可
- Participation削除
- PC変更 / 解除の整合性確認
- `変更を保存`
- Danger Zone

---

# 13. Scenario変更【既存仕様維持】

編集時はScenario変更可。

新規時はScenario Viewから開始するため対象Scenarioは決定済みで、再選択させない。

---

# 14. PC変更 / 解除時確認【今回文言整理・採用】

EndPcState等がある状態でPCを変更・解除する場合、ユーザー確認前に勝手に削除しない。

当初案：

> `五色 探に紐づく、この卓の終了時状態が削除されます。`

ユーザー指摘により、より理解しやすい順序へ変更。

最終方針：

> **PCを変更すると、この卓での「五色 探」の卓終了時状態が削除されます。**

PC解除：

> **PCの設定を解除すると、この卓での「五色 探」の卓終了時状態が削除されます。**

SAN / HP / MP等の詳細値一覧は確認Dialogへ表示しない。

---

# 15. Person変更とPC現在PL変更を分離【確認】

卓編集画面でParticipation.personを変更しても、PC本体の現在Personを変更しない。

PC本体の現在PL変更はPL / PC画面の専用フローで扱う。

卓編集は、

> **その卓当時のParticipation記録を修正する場所**

として扱う。

---

# 16. 既存EndPcState編集【今回整理】

現在編集中のTableに保存されているEndPcState値はInputへ現在値として入れる。

前回値補助とは別情報。

例：

```text
SAN
前回 63
[ 51 ]
```

- 63 = この卓より前の履歴からの入力補助
- 51 = このTableに現在保存されている値

---

# 17. 保存後Navigation【今回変更・採用】

旧仕様：

> 元Scenarioへ戻る / 対象Tableを見える状態

Scenario編集機能により、Scenario変更時は両立しないケースがあるため更新。

## Scenario変更なし

> 元Scenario Viewへ戻り、対象Tableを短くハイライト。

## Scenario変更あり

> **変更後ScenarioのScenario Viewへ戻り、移動した対象Tableを短くハイライト。**

---

# 18. Table削除 / Danger Zone【整理】

編集画面最下部へ配置。

通常Actionより十分に弱く分離する。

Table削除時：

削除：

- Table
- TableDate
- Participation
- Participationに属するEndPcState
- Tableに属する関係

保持：

- Scenario
- Person
- PC

---

# 19. Responsive【ユーザー委任・今回方針確定】

ユーザーから、Responsive詳細はこれまでの確定事項と旧v5系の操作感を参考にして任せる方針を受領。

基本：

- Desktopと機能・入力順を共通化
- 横並び項目はMobileで縦積み
- PLタブは横スクロール
- タブを複数行へ折り返さない
- Mobile専用Step / Accordionを増やさない
- タッチ領域44〜48px程度を目安
- Breakpoint / exact pxは実装時調整

仕様変更が必要な場合はユーザーへ報告・相談する。

---

# 20. Keyboard / Focus / Reduced Motion【今回整理・採用】

- Tab順序は視覚順と一致
- Enter / Space対応
- Esc対応
- Focus可視化
- Accent Strong系Focus Ring
- Hoverのみへ依存しない
- 色だけに依存しない
- Modal終了後は元操作へFocus復元
- Reduced Motion対応

PLタブ、Person / PC SelectもKeyboard操作可能とする。

---

# 21. Error / Validation【今回整理・採用】

- エラーは該当Input付近に表示
- 色だけに依存しない
- PLタブ内エラーはタブ側でも認識可能
- 保存時、別タブにエラーがある場合はそのタブへ切替
- 最初のエラー項目へFocus
- Visual都合で必須項目を増やさない

---

# 22. Saving / Failure【今回整理・採用】

## 保存中

- 入力内容を保持
- Primary Actionを一時無効化
- 二重送信防止
- `保存中…` 程度の簡潔な表示
- 画面全体を不要にLoading画面へ置換しない

## 保存失敗

- 入力内容を保持
- 同じフォームへ留まる
- 保存失敗通知
- 必要ならField Errorも表示

---

# 23. 未保存離脱【今回新規採用】

入力・変更がある状態で別画面へ離れようとした場合のみ確認。

文言：

> **変更内容が保存されていません。**

Action：

- `編集を続ける`
- `変更を破棄して移動`

変更なし / 保存済みなら確認しない。

ブラウザ更新・Tab終了は、可能な範囲でブラウザ標準の未保存警告を使用する。

---

# 24. このフェーズで変更していない重要仕様

- 9エンティティ構造
- User所有権
- 認証 / 認可
- Scenario / Person / PCの本体仕様
- TableName任意
- TableDate任意・複数可
- TableDateと登録日時の分離
- 1 Participation = 1 Person / 1 Role / 0〜1 PC
- PC未設定Participation可
- PC未設定時EndPcState不可
- EndPcState = PC本体現在値ではなく卓終了時履歴
- HO / セリフ別データ
- 最近使用Person / PC再利用
- 新規Person / PC作成
- Table削除ルール
- 保存時User所有権確認

---

# 25. 旧資料との世代差【最終レビューで確認】

## PC画像

古い2026-09-15資料にはPC画像MVP対象外の記述が残るが、その後の確定設計では、

> **PC画像は任意の正式項目**

として扱う。

最新資料を優先する。

## PL / PC複数詳細

古い各画面詳細設計にある、

> 複数PC詳細を同時展開

は、2026-09-25 PL / PC再設計で廃止済み。

現在：

> Collection View → 1PC Focus View

を採用。

---

# 26. 旧v5についての注意

ユーザーは、卓登録／編集について、

> **デザインは変更しても、入力工程や内容はほとんど旧v5と変えない方がよい**

という方針を採用した。

ただし、今回確認できた添付資料内では旧v5卓登録画面そのものの実装ソースを直接特定できていない。

そのため、

- 旧v5の細かな配置
- exact pixel
- exact interaction

を推測で正式仕様扱いしない。

維持するのは、ユーザーが明示した、

> **旧v5の入力工程・入力内容・軽さを大きく壊さない**

という方針である。

---

# 27. 最終整合性レビュー結果

Webクリエイター視点：

- 入力項目を減らさず心理的負荷を軽減できている
- PLタブにより複数PL時の縦長化を抑制
- Card過多を避けGallery系Brandと接続
- v5.9の閲覧専用の特別感を奪っていない

エンジニア視点：

- Entity / Relationship変更なし
- Participation / EndPcState整合性維持
- PC未設定ルール維持
- Scenario変更後Navigation整合
- Table削除CRUD整合
- User所有権 / 認証認可維持

結論：

> **卓登録／編集 Visual Designフェーズは確定可能。**

大きなデータ構造・CRUD・Navigation矛盾なし。

---

# 28. 後続実装で確認する残件

Visual Designを再検討する必要はないが、実装前に以下を確認する。

- EndPcState SAN / HP / MPの厳密な型・範囲
- 成長の型 / 最大文字数
- 生還 / ロストの保存型
- 後遺症等の型 / 最大文字数
- Breakpoint exact values
- Focus Ring exact px
- Motion duration / easing
- Combobox / Tabの詳細ARIA実装

---

以上を、2026-09-25時点の  
**「卓登録／編集 Visual Design再設計フェーズ 確定事項」**  
とする。
