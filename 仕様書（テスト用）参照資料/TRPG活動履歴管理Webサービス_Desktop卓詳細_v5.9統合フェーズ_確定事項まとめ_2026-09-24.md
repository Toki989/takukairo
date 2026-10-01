# Web制作総合演習1
# TRPG活動履歴管理Webサービス
## Desktop卓詳細 / PC Focus v5.9統合フェーズ 確定事項まとめ
### 2026-09-24

---

# 1. この資料の位置づけ

本資料は、PCフォーカスビュー v5.9 を基準としてDesktop卓詳細画面の構成を再検討した結果、今回新たに採用・更新した事項を整理したものである。

既存の確定済み仕様・データ設計を置き換えるものではない。

今後も最重要原則は以下とする。

> **見た目はv5.9。中身は既存の確定済み設計。**

v5.9はVisual Design / UI実装ベースとして扱い、v5.9上のモック情報を正式なデータ仕様へ自動的に追加しない。

以下は従来どおり、既存の確定済み設計資料を正本とする。

- Scenario
- Table
- TableDate
- Participation
- Person
- PC
- EndPcState
- User
- ScenarioFavorite
- CRUD
- UC
- 認証・認可
- 保存・更新・削除ルール
- HO
- セリフ
- PC状態履歴
- 卓登録・編集
- PC登録・編集

---

# 2. Desktop卓詳細の画面方式【今回確定】

Desktopの卓詳細は、

> **A案「完全画面切替型」**

を採用する。

基本遷移は以下。

```text
Home / Scenario画面
↓
Tableを選択
↓
v5.9をVisual Design基準とした卓詳細専用画面
```

卓詳細では通常のApp Shellを常時表示せず、PC Focusを主役とする専用表示へ切り替える。

---

# 3. 複数Table詳細の同時展開【今回変更】

従来の、

> 同一Scenario内で複数Table詳細を独立して同時展開可能

という仕様は、ホーム内アコーディオン型・階層型UIを前提とした表示仕様であった。

Desktop卓詳細を完全画面切替型へ変更したため、

> **複数Table詳細を同時に開くUI仕様は廃止する。**

ただし、以下のデータ・業務仕様は変更しない。

- Scenarioは複数Tableを保持できる
- 各Tableは独立した活動履歴として存在する
- 各Tableは独立したTableDate / Participation / EndPcStateを持つ
- 別Tableを閲覧する場合はScenario / Table一覧へ戻って選択する

つまり、廃止するのは「複数Table詳細を同時表示するUI状態」のみであり、ScenarioとTableのデータ関係は変更しない。

---

# 4. Mobileの扱い

今回の変更対象はDesktop卓詳細。

Mobileはv5.9 Desktopレイアウトをそのまま縮小して使用せず、これまでのResponsive / Mobile方針を別途維持する。

今回のDesktop変更を理由に、Mobileを勝手に大幅再設計しない。

---

# 5. 卓詳細Header【今回確定】

Desktop卓詳細では、v5.9上部の細いHeader領域を、卓全体のContext表示とNavigationへ使用する。

概念構成：

```text
← | SESSION | Scenario名 ↗ | TableName | TableDate | KP | 編集 | メニュー
```

## 5.1 戻る

Headerの最も左側に、

> **戻る矢印**

を配置する。

戻る操作では、卓詳細へ入る前のScenario / Table一覧へ戻る。

可能な範囲で以下の一覧状態を維持する方向とする。

- 元のScenario展開状態
- スクロール位置
- 検索条件
- Filter
- Sort

## 5.2 Scenario名

Headerに現在のScenario名を表示する。

Scenario名は現在地を表すContext情報として扱う。

## 5.3 Scenario URL

Scenario URLが登録されている場合、卓詳細Headerから外部Scenarioページへ移動できる導線を設ける。

Scenario名の近くに、

```text
Scenario名 ↗
```

等の形で外部リンクを組み込む方向とする。

Scenario URLが存在しない場合は、不要な無効リンクを常時表示しない方向。

Scenario画面側でも従来どおり、

> シナリオページを見る ↗

等の形でScenario URLへアクセス可能とする。

## 5.4 TableName

TableNameをHeaderに表示する。

TableName未入力時は、既存仕様どおり画面表示上のフォールバック名を使用する。

例：

```text
卓1
卓2
卓3
```

これらはDBへTableNameとして保存しない。

## 5.5 KP

KPは卓全体の情報としてHeaderに表示する。

複数KPが存在する場合も対応可能な構造とする。

KP Participationは原則としてPC Selectorへ含めず、Header側で扱う。

---

# 6. TableDateの表示【今回確定】

TableDateは、

> **その卓を実際に遊んだ日**

を表す。

1つのTableは0件以上のTableDateを持つ。

複数日にわたる一連の活動でも、同じTableであれば複数TableDateとして保持する。

## 6.1 卓詳細Headerでの表示

卓詳細Headerは表示幅が限られるため、以下の省略ルールを採用する。

```text
0件
→ 表示しない

1件
→ 2026.09.12

2件
→ 2026.09.12 / 09.19

3件以上
→ 2026.09.12 他2日
```

3件以上の場合は、Hover / Focus等で全TableDateを確認できるようにする。

以下のような期間表示は採用しない。

```text
2026.09.12 - 10.10
```

理由：

実際には間隔を空けて開催している可能性があり、連続期間であるかのように誤解されるため。

## 6.2 Scenario画面での表示【今回更新】

Scenario画面では、画面構成変更によりTable一覧・確認の役割が強くなったため、

> **TableDateは省略せず全件表示する。**

例：

```text
第二陣

2026.09.12
2026.09.19
2026.09.26
2026.10.03
```

件数が多い場合も「他N日」へ省略せず、折り返し・日付ラベル等のVisualで全件確認できるようにする。

TableDateが0件の場合は、既存方針どおり、

> 実施日不明

等の補完表示を無理に出さない。

---

# 7. Header右上の操作【今回確定】

v5.9右上に存在していた仮の歯車・ハンバーガーについて、以下へ整理する。

## 7.1 編集

歯車アイコンは使用しない。

歯車が存在していた位置には、

> **ペンアイコン**

を配置し、

> **現在の卓を編集する導線**

とする。

PC編集と誤認されないよう、PC名付近ではなくHeader右上へ配置する。

Hover / Focus時には、

> 卓を編集

と分かるTooltip等を表示する方向。

この導線は既存UC14の卓編集へ接続する。

## 7.2 ハンバーガーメニュー

ハンバーガーメニューは残す。

役割は、

> **卓詳細の没入感を維持しながら、サービス内の他画面へ直接移動するNavigation**

とする。

卓詳細内で通常のGlobal Navigationを常時横並び表示せず、必要時のみハンバーガーメニューから主要画面へ移動できるようにする。

具体的なメニュー項目の順序・最終文言は、今回の確定事項には含めない。

---

# 8. 右下アクション【今回確定】

v5.9右下に存在していた、

> 卓情報を見る

という仮導線は使用しない。

代わりに、

> **▶ セッションを振り返る**

等のアクションを配置する。

これは、

> **Table.録画URL**

への導線として扱う。

## 8.1 録画URLあり

クリック可能。

外部の録画・動画URLへ移動できる。

## 8.2 録画URLなし

「セッションを振り返る」自体は表示してよいが、

- グレーアウト
- Hover変化なし
- pointer cursorなし
- 操作不可
- aria-disabled等を適切に使用

とし、利用できないことを色だけに依存せず表現する。

---

# 9. 卓情報Panel【今回判断】

従来検討していた、

> 右側の卓情報Panel

は現時点では採用しない。

今回の整理により、

- Scenario名
- Scenario URL
- TableName
- TableDate
- KP
- 編集
- Navigation
- 録画URL
- Participation
- PC
- HO / セリフ
- EndPcState

を卓詳細画面内に直接配置できる見込みとなったため、追加Panelを設ける必要性が低くなった。

今後、本当に表示できない正式情報が発生した場合のみ再検討する。

---

# 10. PC Focusの内部選択単位【今回確定】

画面上は引き続き、

> **PC Selector**

のようなVisualを維持する。

ただし内部状態としては、

> **1スロット = 1件のPL Participation**

を基本とする。

選択状態は概念上、

```text
selectedParticipationId
```

を基準に管理する。

理由：

Participationが、その卓当時の以下の情報を結びつける単位だからである。

- Person
- Role
- PC
- HO
- セリフ
- EndPcState

## 10.1 KP Participation

KPはHeaderへ表示するため、原則として右側Selectorへ含めない。

KPは通常PCを設定しない既存仕様とも整合する。

## 10.2 PL Participation

右側SelectorではPL Participationを扱う。

PL Participationは、

```text
PCあり
PCなし
```

の両方を取り扱う。

同じ人物が同じ卓で複数Participationを持つ場合も、それぞれ別スロットとして扱える構造を維持する。

---

# 11. PC表示の3状態【今回確定】

卓詳細では、PL Participationの状態によって表示を明確に分ける。

## 11.1 PCあり・PC画像あり

通常のv5.9 PC Focusを表示する。

主な表示：

- PC画像
- PC名
- その卓当時のPL名
- HOまたはセリフ
- SAN
- HP
- MP
- 成長
- 生還 / ロスト
- 後遺症

## 11.2 PCあり・PC画像なし

PC自体は存在するため、通常のPC Focus構造を維持する。

PC画像部分のみ、

> **PC画像なし用のスタイリッシュなシルエット / プレースホルダー**

を表示する。

ここでは、

> PCの登録がありません

とは表示しない。

PC名・PL名・HO / セリフ・EndPcState等は通常どおり表示する。

このシルエットはPC画像の代替Visualであり、新しいデータ属性ではない。

## 11.3 PC自体なし

PC画像用シルエットは表示しない。

代わりに、

> **PCなし専用ビュー**

へ切り替える。

表示の基本メッセージ例：

> PCの登録がありません

専用ビューは単なる空状態・エラー画面にはせず、

> **PCが登録されていなくても、その人がその卓へ参加していた記録を格好よく見せる画面**

とする。

v5.9の、

- Background
- Accent
- Typography
- 余白
- 斜め装飾
- Texture
- GAME LIBRARYらしい世界観

を維持しながら、PC情報ではなく卓・Participation情報を主役として見せる。

---

# 12. PCなし専用ビューで扱う情報【今回確定】

PCなしParticipationでも存在する情報は表示可能とする。

主に：

- PL名
- Role
- Scenario名
- TableName
- TableDate
- HO
- 録画URL

等。

一方、PCが存在しないParticipationではEndPcStateを保持できないため、以下は表示しない。

- SAN
- HP
- MP
- 成長
- 生還 / ロスト
- 後遺症

PCなし専用ビューの最終Visualは今後作成するが、

> 卓情報・参加記録をポスター的 / ゲーム画面的に見せる

方向とする。

---

# 13. PCなしParticipationのSelector表現

PCなしParticipationもSelectorから選択できるようにする。

ただし、

> PC画像なし時のシルエット

とは明確に区別する。

PCなしParticipationのSelectorでは、

- PC未登録を示す専用記号
- 抽象的なグラフィック
- 専用Typography
- `NO PC`等の短い補助表現

等を候補とし、人物シルエットをそのままPC画像代替として使用しない方向。

具体的なVisualは今後確定する。

---

# 14. EndPcState表示【今回更新】

v5.9中央のSTATUS表示を、既存の正式EndPcStateへ合わせる。

主表示：

- SAN
- HP
- MP

補助表示：

- 成長
- 後遺症

重要状態：

- 生還 / ロスト

成長・後遺症は、現在のSTATUS構造を壊さないよう小さな補助情報として追加する。

## 14.1 最大値

v5.9モックに存在した、

```text
SAN 65 / 99
HP 12 / 16
MP 8 / 12
```

等の最大値は正式仕様ではない。

したがって、正式データに最大値が存在しない場合は、

```text
SAN 65
HP 12
MP 8
```

のように表示する。

## 14.2 値なし

任意項目に値が存在しない場合、

- 0
- 未設定
- 不明

等を勝手に補完せず、その項目を省略する方向を維持する。

---

# 15. HO / セリフ表示【既存仕様維持＋今回UI採用】

HOとセリフは別データとして両方保存可能。

PC Focusでは一度に片方のみSpotlight表示する。

表示ルール：

```text
HOのみ
→ HO

セリフのみ
→ セリフ

HO・セリフ両方あり
→ ユーザーが表示対象を切替可能

両方なし
→ Spotlightなし
```

## 15.1 HO / セリフ切替UI【今回採用】

HOとセリフの両方が存在する場合、

> **Spotlight付近に控えめな切替UIを配置する。**

通常の大きなTab UIではなく、v5.9のVisualを壊さない小さな操作として設計する。

具体的な最終文言・アイコンは今後Visual検討で確定する。

セリフ表示そのものには、

- `QUOTE`
- 自動の「」
- 固定ラベル

を追加しない既存仕様を維持する。

---

# 16. Scenario画面と卓詳細の情報密度の役割分担【今回整理】

Scenario画面：

> **一覧・比較・Table選択のための情報確認画面**

卓詳細：

> **選択した1件のTableへ入り込み、PC / Participationを主役として振り返る画面**

と役割を分ける。

そのためScenario画面ではTableDateを全件表示し、卓詳細Headerでは省略ルールを使用する。

同じデータを別の意味へ変換するのではなく、

> **同じTableDateを画面役割に応じて異なる密度で表示する**

という考え方とする。

---

# 17. 卓詳細で常時表示しない情報【確認】

以下は正式データではあるが、卓詳細へすべて常時表示する必要はない。

## Scenario側

- 作者
- ゲームシステム
- Scenario画像
- お気に入り

これらはScenario画面側で確認できる。

## PC本体側

- 現在紐づくPerson
- PCゲームシステム
- 外部キャラクターシートURL
- 登場した卓

これらはPC詳細画面側で確認できる。

卓詳細では、

> **現在のPC所有者ではなく、その卓のParticipation.person**

をPL表示へ使用する。

PCの現在所有者変更によって過去卓の表示を上書きしない。

---

# 18. 登録日時の扱い【確認】

Tableの登録日時は、

> **サービスへそのTableを登録した日時**

であり、実際に遊んだ日時ではない。

そのため卓詳細の通常閲覧画面へ常時表示しない。

TableDateがない場合でも、登録日時を実施日として表示しない。

---

# 19. 参加者0件Table【確認】

Tableは参加者0件でも正式に存在できる。

その場合は、

- 架空のPC
- 架空のParticipation
- 仮のSelector

を生成しない。

v5.9の世界観を維持したEmpty Stateとして、

> この卓には参加者が登録されていません

等の案内を表示する方向。

録画URL等、Table自体に存在する情報は通常どおり利用可能とする。

---

# 20. 卓詳細の現在の情報配置【総合確定】

## Header

- 戻る
- Scenario名
- Scenario URL
- TableName
- TableDate
- KP
- 卓編集
- ハンバーガーメニュー

## メイン：PCあり

- PC画像 / 画像なし用シルエット
- PC名
- その卓当時のPL名
- HO / セリフ
- SAN
- HP
- MP
- 成長
- 生還 / ロスト
- 後遺症
- PL Participation Selector

## メイン：PCなし

- PCなし専用ビュー
- PL名
- PC未登録表示
- 卓 / Participation情報
- HO等、PCなしでも成立する情報
- PL Participation Selector

## 右下

- セッションを振り返る
- Table.録画URLへ接続
- URLなし時は無効表示

---

# 21. Webクリエイター視点で維持する事項

今後のDesktop卓詳細実装では必ず以下を維持する。

- v5.9のVisual Designを不用意に作り直さない
- PCが存在する場合はPCを主役にする
- 左＝大きなPC
- 中央＝PC情報
- 右＝細く傾斜したSelector
- Selectorの細さ・密集感・傾斜・配置を維持
- 背景装飾を維持
- TRPG GAME LIBRARYの世界観を維持
- 普通の管理SaaSへ戻しすぎない
- PCなし専用ビューも同一世界観内で成立させる
- 情報追加によって中央レーンを過密化させない
- 成長・後遺症は主STATUSより弱く扱う
- HeaderはContext / Navigationに徹し、PC Focusより強くしない

---

# 22. エンジニア視点で維持する事項

今後の実装では必ず以下を維持する。

- `selectedParticipationId` を基準とした状態管理
- Participation.personとPC.personの違いを壊さない
- 過去卓表示に現在のPC所有者を誤使用しない
- PCなしParticipationにEndPcStateを生成しない
- PC画像なしとPCなしを混同しない
- Responsive
- Keyboard操作
- Focus
- Reduced Motion
- Selector選択追従
- PC切替時の破綻防止
- 低いDesktop Viewportでの中央レーン衝突防止
- 横スクロール防止
- v5.6以降で改善された中央レーン構造を退行させない
- Header追加によってPC立ち絵・中央情報・Selectorを押し潰さない
- 外部URLはScenario URLとTable録画URLを混同しない

---

# 23. 今回の変更によって廃止・置換された旧UI事項

以下は今回のDesktop卓詳細再設計によって置き換えられた。

## 廃止

- 同一Scenario内で複数Table詳細を同時展開するDesktop UI
- v5.9右下の「卓情報を見る」という汎用導線
- 右側卓情報Panel案
- 右上の歯車アイコン
- PCなしParticipationをPC画像なしと同じシルエットで表現する案

## 置換

```text
歯車
→ 卓編集用ペンアイコン

卓情報を見る
→ セッションを振り返る

PCなしPL
→ PCなし専用ビュー

PC画像なし
→ PC画像なし用シルエット

Table詳細アコーディオン
→ Desktop完全画面切替

PC Selector内部単位
→ PL Participation単位
```

---

# 24. 現時点で未確定として残すもの

以下は今回まだ最終確定していない。

- Header内の各要素の正確なpx位置・間隔
- HeaderでScenario URLを示す最終アイコン / Hover表現
- ハンバーガーメニュー内の最終項目・順序・文言
- HO / セリフ切替UIの最終Visual
- PC画像なし用シルエットの最終Visual
- PCなしParticipation用Selectorの最終Visual
- PCなし専用ビューの最終レイアウト
- 成長・後遺症の最終Typography / 改行ルール
- TableDate 3件以上時のPopover最終Visual
- Mobile側への最終反映方法
- 各モーションの最終duration / easing

未確定事項を、ユーザー承認なく正式仕様へ追加しない。

---

# 25. 現時点の総合判断

Desktop卓詳細は、

> **v5.9をVisual Design基準とした完全画面切替型**

として整理された。

卓情報Panelを追加せず、

- Header
- PC Focus
- PL Participation Selector
- PCなし専用ビュー
- セッション録画導線
- 編集
- Navigation

へ情報を分散することで、必要な正式データを卓詳細画面内で確認できる構成とする。

最重要原則：

> **見た目はv5.9。中身は既存の確定済み設計。**

を今後も維持する。
