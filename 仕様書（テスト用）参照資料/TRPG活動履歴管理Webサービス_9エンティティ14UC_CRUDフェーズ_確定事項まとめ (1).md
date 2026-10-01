# Web制作総合演習1
# TRPG活動履歴管理Webサービス
## 9エンティティ × 14UC CRUDフェーズ 確定事項まとめ

---

# 1. この資料の位置づけ

本資料は、既存の最新版正本

`TRPG活動履歴管理Webサービス_最新版確定仕様_9エンティティ14UC.md`

を前提として、その後の **9エンティティ × 14UC CRUD対応表フェーズ** で新たに確定した事項と、正式なCRUD対応表をまとめた追加資料である。

既存の確定仕様を置き換えるものではなく、

> **最新版正本に対して、CRUDフェーズで追加・明確化された確定事項を追補する資料**

として扱う。

既存資料と本資料が衝突する場合は、後から確定した本資料の内容を優先する。

ただし、本資料に記載のない既存仕様は、最新版正本の内容をそのまま維持する。

---

# 2. CRUDフェーズの目的【完了】

今回の工程では、MVP対象14UCが正式9エンティティに対して行う、

- Create
- Read
- Update
- Delete

を横断的に整理した。

対象エンティティは以下9件。

1. User
2. Scenario
3. Person
4. PC
5. Table
6. TableDate
7. Participation
8. EndPcState
9. ScenarioFavorite

対象UCは以下14件。

| UC | 内容 |
|---|---|
| UC01 | 既存の活動履歴を取り込む |
| UC02 | 取り込み結果を確認する |
| UC03 | 取り込み時の欠損・不明箇所を修正する |
| UC05 | 新しく遊んだ卓を登録する |
| UC06 | 登録済みシナリオを新しい卓で再利用する |
| UC07 | 登録済みPCを新しい卓で再利用する |
| UC08 | 最近使用した情報を使って卓を登録する |
| UC09 | シナリオ情報を登録・確認・修正・削除する |
| UC10 | PC情報を登録・確認・修正・削除する |
| UC11 | シナリオから過去の卓を確認する |
| UC12 | 過去の活動履歴を探す |
| UC13 | 活動履歴の詳細を確認する |
| UC14 | 登録済み活動履歴を修正・削除する |
| UC17 | 自分のTRPG活動を振り返る |

---

# 3. CRUD表の記号【確定】

- C = Create
- R = Read
- U = Update
- D = Delete
- ― = そのUCでは正式データに対して操作しない

CRUD表には、

> **正式9エンティティに対して実際に行う操作のみ**

を記載する。

インポート中の一時データ・候補データ・未解析データ等のCRUDは、この表へ混在させない。

また、認可のためにUserIDを条件として使用するだけの場合は、UserエンティティのReadとは数えない。

---

# 4. 今回新たに確定した事項

## 4-1. Scenario削除時のScenarioFavorite削除【新規確定】

Scenarioを削除する場合、

> **そのScenarioに紐づくScenarioFavoriteも削除する。**

ScenarioFavoriteはScenarioへの付随関係データであり、Scenario本体が存在しなくなった後に保持する意味がないため。

ただし、Scenarioに紐づくTableは活動履歴そのものであるため、従来仕様どおり自動削除しない。

整理すると、

```text
Scenario削除時

Table
→ 自動削除しない
→ 関連Tableが1件以上ある場合、Scenario本体を削除不可

ScenarioFavorite
→ Scenario削除時に削除する
```

Scenario削除処理は、

```text
関連Table確認
↓
0件なら削除可能
↓
関連ScenarioFavorite削除
↓
Scenario本体削除
```

とする。

CRUD上は、

> **UC09 × ScenarioFavorite = D**

となる。

---

## 4-2. UC10内で未登録Personを新規作成可能【新規確定】

UC10「PC情報を登録・確認・修正・削除する」で新規PCを登録する際、

> **紐づけるPersonが未登録であれば、その場でPersonを新規作成できる。**

PCは「現在紐づくPerson：必須」であるため、PC登録の流れを中断して別機能へ移動させるのではなく、UC10内で補助的にPersonを作成可能とする。

ただし、

> **UC10をPerson管理機能へ拡張するわけではない。**

UC10におけるPerson操作は、

- C：未登録Personを新規作成
- R：既存Personを選択・確認

まで。

UC10では、

- Person U
- Person D

は扱わない。

CRUD上は、

> **UC10 × Person = C/R**

となる。

---

## 4-3. UserのReadと認可処理を分離【新規確定】

すべてのUser所有データについて、

> **ログインUserがそのデータの所有者であることを確認する。**

ただし、

> **所有権確認のためにUserIDを条件として利用することと、UserエンティティそのものをReadすることは別**

として扱う。

例：

```text
ScenarioID = 対象ID
AND UserID = ログインUserID
```

のように所有条件で絞り込むだけであれば、

> User Rとは数えない。

したがって、CRUD表の全UCへ機械的にUser = Rを付けない。

Userエンティティそのものを処理上参照するUCのみRとする。

現MVPでは、

- UC12：SelfPersonを使って「自分がPL / KP」を判定
- UC17：SelfPersonを使って自分の活動を集計

でUserをReadする。

よって、

```text
UC12 × User = R
UC17 × User = R
```

とする。

---

# 5. 全UC共通の認可ルール【新規確定】

CRUD表とは別の横断的なセキュリティルールとして、

> **User所有データを取得・作成・更新・削除する場合は、ログインUserの所有データであることを必ず確認する。**

対象は少なくとも、

- Scenario
- Person
- PC
- Table

である。

子エンティティについては親データを通して所有Userを確認する。

```text
TableDate
→ Table
→ User

Participation
→ Table
→ User

EndPcState
→ Participation
→ Table
→ User

ScenarioFavorite
→ User / Scenario
```

ユーザー体験上は「自分専用のページ」として通常どおり利用できる。

所有権確認は原則としてバックエンド・DBアクセス側で自動的に行い、

> **ユーザーへ毎回確認ダイアログや再認証を求めるものではない。**

つまり、

```text
表側
→ 自分専用の画面として自然に利用

裏側
→ 各データ操作で所有権を厳格に確認
```

という設計とする。

---

# 6. User CRUDに含めないもの【確認】

以下は現在の14UCへ無理に含めない。

- Googleログイン時のUser Create
- 初回SelfPerson作成・設定
- Userプロフィール更新
- Google認証情報の更新
- アカウント削除
- アカウント削除時の関連データ削除

これらは後続の、

> **認証・アカウント・セキュリティ設計**

で別途整理する。

---

# 7. 9エンティティ × 14UC 正式CRUD対応表【確定】

| UC | User | Scenario | Person | PC | Table | TableDate | Participation | EndPcState | ScenarioFavorite |
|---|---|---|---|---|---|---|---|---|---|
| **UC01** 既存履歴を取り込む | ― | **C/R** | **C/R** | **C/R** | **C** | **C** | **C** | **C** | ― |
| **UC02** 取り込み結果を確認する | ― | **R** | **R** | **R** | ― | ― | ― | ― | ― |
| **UC03** 欠損・不明箇所を修正する | ― | **R** | **R** | **R** | ― | ― | ― | ― | ― |
| **UC05** 新しく遊んだ卓を登録する | ― | **R** | **C/R** | **R** | **C** | **C** | **C** | **C** | ― |
| **UC06** 登録済みScenarioを再利用する | ― | **R** | ― | ― | ― | ― | ― | ― | ― |
| **UC07** 登録済みPCを再利用する | ― | ― | ― | **R** | **R** | **R** | **R** | **R** | ― |
| **UC08** 最近使用した情報で卓を登録する | ― | **R** | **R** | **R** | **R** | **R** | **R** | ― | ― |
| **UC09** Scenario情報を管理する | ― | **C/R/U/D** | ― | ― | **R** | ― | ― | ― | **D** |
| **UC10** PC情報を管理する | ― | ― | **C/R** | **C/R/U/D** | **R** | ― | **R/U** | **D** | ― |
| **UC11** Scenarioから過去卓を確認する | ― | **R** | ― | ― | **R** | ― | ― | ― | ― |
| **UC12** 過去の活動履歴を探す | **R** | **R** | ― | ― | **R** | ― | **R** | ― | **C/R/D** |
| **UC13** 活動履歴の詳細を確認する | ― | **R** | **R** | **R** | **R** | **R** | **R** | **R** | ― |
| **UC14** 活動履歴を修正・削除する | ― | **R** | **C/R** | **R** | **R/U/D** | **C/R/U/D** | **C/R/U/D** | **C/R/U/D** | ― |
| **UC17** 自分の活動を振り返る | **R** | ― | ― | ― | **R** | ― | **R** | ― | ― |

---

# 8. UC01〜03のCRUD整理【確定】

## UC01

UC01は、

> **インポートから正式登録までを統括する親UC**

である。

既存データとの照合が必要なため、

- Scenario R
- Person R
- PC R

を行う。

新規データが必要な場合は、

- Scenario C
- Person C
- PC C

を行う。

活動履歴として、

- Table C
- TableDate C
- Participation C
- EndPcState C

を行う。

お気に入りはインポート対象ではないためScenarioFavorite操作なし。

---

## UC02

UC02では正式登録前の取り込み候補を確認する。

正式9エンティティについては、

- Scenario R
- Person R
- PC R

のみ。

これは既存データとの照合・再利用候補確認のため。

Table等の正式活動履歴はまだ作成しない。

---

## UC03

UC03で編集する主対象はインポート一時データ。

正式9エンティティは、

- Scenario R
- Person R
- PC R

のみ。

UC03で正式Scenario・Person・PCをUpdateするわけではない。

修正後の正式登録はUC01として処理する。

---

# 9. UC05〜08のCRUD整理【確定】

## UC05

UC05は新しい活動履歴を成立させる中心UC。

- Scenario R
- Person C/R
- PC R
- Table C
- TableDate C
- Participation C
- EndPcState C

ScenarioやPCの新規作成そのものは、

- Scenario → UC09
- PC → UC10

の責務とする。

Personには独立管理UCがないため、UC05内で新規作成可能。

---

## UC06

登録済みScenarioを新しい卓で再利用するため、

- Scenario R

のみ。

Table作成はUC05。

---

## UC07

登録済みPCを継続利用する際、

- PC R
- Table R
- TableDate R
- Participation R
- EndPcState R

を行う。

過去EndPcStateから前回状態候補を提示するため、過去履歴を参照する。

今回のParticipation作成はUC05。

---

## UC08

最近使用した、

- Scenario
- Person
- PC

等を候補表示する。

候補判定のため、

- Scenario R
- Person R
- PC R
- Table R
- TableDate R
- Participation R

を行う。

EndPcStateは「最近使った情報」の候補抽出そのものには必須ではないため、現CRUDでは操作なしとする。

「最近」の具体的な判定ルール・候補件数は引き続き未確定。

---

# 10. UC09のCRUD整理【確定】

UC09：

> **Scenario情報を登録・確認・修正・削除する**

Scenario：

- C
- R
- U
- D

すべて対象。

Scenario削除時は、

- Table R

で関連Tableの存在を確認。

関連Tableが1件以上ある場合はScenario削除不可。

関連Tableが0件の場合は、

- ScenarioFavorite D
- Scenario D

を行う。

Scenario削除によるTable Dは行わない。

---

# 11. UC10のCRUD整理【確定】

UC10：

> **PC情報を登録・確認・修正・削除する**

PC：

- C
- R
- U
- D

すべて対象。

PC登録時・Person選択時：

- Person R
- 必要ならPerson C

PC削除可否確認：

- Participation R

関連Participationが1件以上ならPC削除不可。

PCのPerson変更時に、ユーザーが特定の過去卓からPCを外す場合：

- Table R
- Participation R/U
- EndPcState D

を行う。

PC本体削除のために、

- Participation D
- Participation.PCIDの自動NULL化
- EndPcStateの自動削除

は行わない。

---

# 12. UC11のCRUD整理【確定】

UC11：

> **Scenarioから過去のTableを確認する**

- Scenario R
- Table R

を行う。

TableNameがある場合はその名前を表示。

未設定の場合は「卓1」「卓2」等を画面表示時に生成する。

---

# 13. UC12のCRUD整理【確定】

UC12：

> **過去の活動履歴を探す**

SelfPersonを使ったPL / KP絞り込みのため、

- User R

を行う。

検索・絞り込み・並べ替えのため、

- Scenario R
- Table R
- Participation R

を行う。

お気に入りについては、

- ScenarioFavorite C：お気に入り登録
- ScenarioFavorite R：お気に入り状態確認・絞り込み
- ScenarioFavorite D：お気に入り解除

を行う。

Scenario本体はお気に入り登録・解除ではUpdateしない。

---

# 14. UC13のCRUD整理【確定】

UC13：

> **活動履歴の詳細を確認する**

以下をReadする。

- Scenario
- Person
- PC
- Table
- TableDate
- Participation
- EndPcState

正式データのCreate / Update / Deleteは行わない。

---

# 15. UC14のCRUD整理【確定】

UC14：

> **登録済み活動履歴を修正・削除する**

参照：

- Scenario R
- Person R
- PC R
- Table R
- TableDate R
- Participation R
- EndPcState R

新しいPersonが必要な場合：

- Person C

活動履歴修正：

- Table U
- TableDate C/U/D
- Participation C/U/D
- EndPcState C/U/D

Table削除：

- Table D
- TableDate D
- Participation D
- EndPcState D

ただし、

- Scenario本体
- Person本体
- PC本体

は削除しない。

Scenario・PC本体の編集もUC14では行わない。

---

# 16. UC17のCRUD整理【確定】

UC17：

> **自分のTRPG活動を振り返る**

MVP簡易版では、

- User R
- Table R
- Participation R

を用いて集計する。

User.SelfPersonを基準として、

- 遊んだシナリオ数
- 参加した卓数
- PL参加回数
- KP参加回数

を算出する。

「遊んだシナリオ数」はTableが参照するScenarioIDを重複排除して数えられるため、

> 現MVP簡易版ではScenario本体のRを必須としない。

将来、シナリオ名別ランキング等を表示する場合はScenario Rが必要になる可能性があるが、現時点では追加しない。

---

# 17. 3視点での最終確認

## Webクリエイター

CRUD上の責務はユーザー体験と矛盾していない。

特に、

```text
UC05
├ UC06 Scenario再利用
├ UC07 PC再利用
└ UC08 最近使用情報
```

という役割分担により、内部ではUCを分離しつつ、画面上では一続きの「卓登録」として見せられる。

PC登録時に未登録Personをその場で追加できるため、不要な画面往復も避けられる。

また、認可処理はバックエンド側で行うため、ユーザーへ毎回セキュリティ確認を求めずに済み、UXを損なわない。

---

## エンジニア

正式9エンティティについてCRUD責務を整理できた。

大きく、

```text
認証・所有
User

再利用データ
Scenario
Person
PC

活動履歴
Table
TableDate
Participation
EndPcState

ユーザー固有関係
ScenarioFavorite
```

として責務を分離できている。

特に、

- UserのReadと認可を分離
- インポート一時データと正式データを分離
- Scenario削除とScenarioFavorite削除の関係明確化
- PC削除と卓内PC解除の違いを維持
- UC05 / UC14を活動履歴CRUDの中心に集約

したことで、実装時の責務が明確になった。

---

## 法務・コンプライアンス

User所有権による認可を全データ操作の横断ルールとして明示した。

一方で、

> セキュリティ確認をユーザーへ過剰に要求しない

ため、セキュリティとUXの両立が可能。

Personについては引き続き、

- Discord ID
- SNS ID
- メール
- 本名
- 詳細プロフィール

等をMVPでは保存せず、必要最小限の第三者情報のみ扱う。

ScenarioFavoriteもScenario削除時に不要データを残さない方向となった。

---

# 18. CRUDフェーズ総合判断【確定】

9エンティティ × 14UC CRUD対応表は、

> **正式確定した状態**

とする。

現時点で、

- CRUD責務を成立させられない重大な仕様不足
- エンティティ追加が必要な矛盾
- UC追加が必要な矛盾
- 既存確定仕様との重大な衝突

は確認されていない。

インポート一時データ、Google認証・アカウント管理、外部サービス連携等は、意図的にこのCRUD表から分離して後続工程で設計する。

---

# 19. CRUDフェーズ終了時点で残る未確定事項

今回のCRUD表確定によって、以下は確定していない。

- Person本体の削除仕様
- Google認証で保存するUser属性
- Google認証の具体的な技術方式・ライブラリ
- Googleログイン時のUser作成処理
- 初回SelfPerson設定の具体処理
- アカウント削除時の関連データ削除方針
- UC08「最近」の厳密な判定方法
- UC08の候補件数
- Scenario一覧の具体的な検索対象
- Scenario一覧の具体的な並べ替え選択肢
- 卓一覧の具体的な並べ替え選択肢
- UC11の卓一覧に表示する具体項目
- UC17の月別・年別UI
- 月別・年別集計時の日付不明卓の扱い
- インポート一時データの正式構造
- 外部キャラクターシート対応サービス
- 各外部サービスAPI・利用規約
- 子エンティティへUserIDを物理的に重複保持するか

これらをCRUDフェーズで勝手に確定したものとして扱わない。

---

# 20. 次工程【確定】

CRUDフェーズ完了後は、

> **主要画面一覧**

の作成へ進む。

今後の予定：

```text
9エンティティ × 14UC CRUD対応表【完了】
↓
主要画面一覧【次工程】
↓
画面遷移
↓
各画面詳細
↓
処理設計
↓
インポート詳細設計
↓
外部サービス連携・Google認証調査
↓
セキュリティ・データ保持
↓
実装優先順位
```

---
