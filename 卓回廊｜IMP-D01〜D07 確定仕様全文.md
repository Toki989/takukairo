# 卓回廊｜Import共通推論 追加確定事項・Codex向け機能仕様追補

**確定日**：2026-10-13  
**状態**：ユーザーの「このまま決めてください」による採用判断を反映した、**機能・業務ルールの確定追補**。コード実装・テスト合格の記録ではない。  
**目的**：2026-10-13の三視点レビューで残ったRole未判定／SKP-only、未解析ブロック、任意情報の正式保存、CHECKED、SourceTrace、登録対象初期状態の6差分について、Codexが業務ルールを推測しなくて済むよう動作を固定する。

## 0. 正本との関係・適用範囲

1. 本書は2026-10-08の**Codex実装正本 R5.1.12**と2026-10-13引継ぎの**既存採用事項**に対する**限定追補**。R5.1.12原本を置き換えたり、全文改訂版を名乗ったりしない。
2. 変更するのは本書に明示した6つのImport機能の動作、およびそれを成立させるImport Support Data／画面と登録条件のみ。既存R5.1.12と本書が同一論点で異なるときは、**この追補の明示変更箇所に限り本書を適用**し、それ以外はR5.1.12を維持する。
3. 正式9 Entity、14UC、RoleのFormal Enum（`KP`／`PL`）、`display_order`、Profile、認証・User所有権、Version、Source Lock、Resolution、REGISTERED不可変、Bulk／Split／Merge Preview→Apply、Candidate単位Atomic Register、FILE Atomic Replacement（R5.1.12 Part A A18）は変更しない。
4. 入力形式は**CSV／Markdown／HTML／TXT等／直接貼り付けの5形式**で同等に機能させる。PDF・OCR、LLM必須化、ユーザー横断学習、新しい必須Wizardは追加しない。
5. 採用済みのG-01〜08、H01-C＋H01-B、H02-B、H03-B、ROLE-C01〜12（最新C05）および積極仮入力を維持する。E3/E2/C1〜C3とRole推論A/B/C、Backend Status、CHECKED、登録対象は独立した意味を持つ。
6. 本書の「Support情報／操作／契約」は**必要な機能上の意味**を定めるもの。具体的なクラス名、DBカラム名、新Endpoint名、DTO名はCodexの技術設計対象であり、本書で未記載の名前を自動的に正式仕様と解釈しない。技術的な構成は、既存R5の公開Contractとの互換性とテスト可能性を優先する。

## 1. 決定一覧

| ID | 採用結論 | 区分 |
|---|---|---|
| IMP-D01 | Role不明はImport一時情報として保持し、明示解決まで正式Participationにしない。SKP-onlyを無断で主KPにしない | 今回確定 |
| IMP-D02 | 未解析の**意味のある活動記録**が未処理ならComplete不可。明示除外またはCandidate化後の処理完了を要する | 今回確定・旧Complete条件を限定拡張 |
| IMP-D03 | Quote／EndPcState／作者・URL等の採用済み抽出情報を正式属性へ接続。保存不可は無通知で捨てず、修正または項目単位の明示除外を要する | 今回確定 |
| IMP-D04 | Candidateの実質変更で旧CHECKEDを失効。編集と同時の明示再CHECKEDは許容 | 今回確定 |
| IMP-D05 | 項目別SourceTrace、推論根拠、ユーザー編集値の区別を保証 | 今回確定 |
| IMP-D06 | 登録対象は確認状態と別。**新規Candidateの初期登録対象をtrue**、ただし未確認／未修正なら登録できない | 今回確定・旧UIと整合 |
| IMP-D07 | 任意値の新Bulk修正、一括CHECKED、恒久学習をMVPには追加しない | 今回不採用 |

## 2. IMP-D01｜Role未判定とSKP-only

### 2.1 抽出・仮入力

- **Role推論A**：実施済み卓でPersonと使用PCの対応が明確なら、`PL`と書かれていなくてもROLE-C05に従って`PL`を仮入力。KPも明確なゲーム進行文脈があれば仮入力する。推論しただけでCHECKEDにしない。
- **Role推論B**：複数の合理的なRole候補がある場合、Person／PCの抽出結果、元原文、候補Roleと根拠をImportの一時情報として保持し、REVIEWで候補を選べるようにする。
- **Role推論C**：実参加やRoleの根拠が不足・矛盾しているとき、Roleを創作せず未判定とする。分かっているPerson／PC／原文は失わない。
- 上記B/Cの情報を、Formal Roleが未設定のまま正式Participationへ渡してはいけない。未解決一時項目が残るCandidateはREGISTERABLEとみなさず、内容確認済みであっても登録を拒否する。
- Roleの候補は`KP`／`PL`のみ。`SKP`は推論・原文表示に残せるが、Formal Roleに`SKP`、`isMainKP`を追加しない。

### 2.2 ユーザーが選べる解決方法

REVIEW（必要時DETAIL）で、未解決の参加記録ごとに次のいずれかを**明示決定**する。

1. **KPとして登録**：そのPersonをKPのParticipationとして扱う。SKP-only由来の場合、単独KPが画面上「KP」と表示され得ること、元の「SKP」と完全に同義ではないことを明記してから選択する。これはユーザーが意味を変更して記録する選択であり、自動変換しない。
2. **PLとして登録**：その卓でPLとして参加したというユーザーの判断に基づく。PCが不明ならPCなしPLを許容する。
3. **その参加記録を今回は保存しない**：原文から抽出した当該部分だけを**ユーザーの明示除外**として記録し、他の正常な参加記録は保持してよい。該当部分に紐づくHO／Quote／EndPcState等を別の参加者へ自動転用しない。未保存部分をREGISTER前に通知する。
4. **未決のまま保留**：Candidateは未解決のため登録不可とし、他の独立Candidateの部分登録は妨げない。Candidate全体のEXCLUDEDは既存の明示除外操作に従う。

主KPとSKPが原文で明確なときは、両者をFormal KPのParticipationとして保持し、主KP=先頭、SKP=2番目以降になるよう既存表示順で表現する。主KPをユーザーが後から明示追加した場合も同様。**SKP-onlyから架空の主KPを補充しない。**

### 2.3 条件と受入例

- `実施済み卓／参加者・使用PC：田中／青い月` → `PL`仮入力（A）。T17の文脈なし`氏名,PC` → 未判定（C）。
- `SKP：田中`のみ → **主KP不明**。田中を自動で「KP」と正式保存しない。本人が「KPとして登録」を選べば、表示の意味を知らせた上で保存可能。
- `KP：佐藤、SKP：田中` → KP表示順1:佐藤、2:田中。
- 未判定情報だけを「今回は保存しない」とした場合、元文と除外意思が追跡でき、残りのCandidateが他の登録条件を満たせば登録可能。

## 3. IMP-D02｜未解析ブロック・部分登録後・Complete

### 3.1 対象の判別

- 単なる空行・装飾記号・構文上の区切りなど、情報ではない構造要素は完了を妨げる未解析ブロックにしない。
- **活動履歴情報を含む可能性があり、卓Candidateへ割り当てられない原文範囲**はCandidate外の未解析ブロックとして、元Source、原文範囲、取り込めなかった理由を保持する。
- 活動記録か無害な文字列か判定できない場合、無害だと決めつけて黙って捨てず、REVIEWで確認対象とする。候補内`unassignedValues`とは別物である。

### 3.2 ブロックの機能的状態

| 意味 | 動作 | Complete |
|---|---|---|
| 未処理 | 元記録を保全。候補化するか明示除外するまで判断待ち | **不可** |
| 明示除外 | ユーザーがこのブロックを今回取り込まないと選択した | **可** |
| Candidate化済み | ユーザー確認済みの分割範囲を新Candidateへ移行し、元ブロックを消費済みとして保持 | 生成CandidateがすべてREGISTERED/EXCLUDEDなら**可** |

上記は**Support上の業務状態の意味**であり、Formal Entityや固定の新Enum名称を要求しない。

### 3.3 Candidate化の操作と部分登録後の制限

1. 未処理ブロックの原文、元範囲、候補分割をPreviewし、ユーザーが明示的に確定した場合のみ、**0件ではなく1件以上**の新Candidateを作成する。
2. 1つのブロック内に複数卓が明示される場合、Previewで分割構成を確認し、複数Candidateを生成できる。同卓の根拠E3/E2とC1〜C3を守る。
3. 元ブロックの**消費記録**、新Candidate、SourceTraceの接続、Session Version更新は1つの原子的操作として成功／失敗させる。同じブロック・同じ活動記録範囲から重複Candidateを作らない。ただし共通見出しなど同じSource範囲を根拠として複数Candidateが参照することまで禁止しない。
4. REGISTEREDが1件でも存在した後は、**すでにSession内に保持されていた未解析ブロックだけ**を使う。新Source追加・編集・削除、一般のRe-analyze、Analysis Reset、REGISTERED Candidate変更を一切行わない。
5. 既存REGISTERED卓と同一の可能性を検出した場合、同名だけで自動同一視せず、ユーザーに元記録との比較と「別卓として登録／この断片は今回登録しない」の明示判断を求める。REGISTERED卓へ自動追加しない。
6. H01のブロックMutationは既存の`expectedSessionVersion`、User所有権、Row Lock、Version競合の規則に従う。Previewの有効性とApply直前の再検証も保証する。既存Bulk／Split／Mergeの具体Endpointを無断で別用途へ再解釈しない。

### 3.4 Complete判定【R5.1.12 §34.11への限定追補】

BackendはComplete時に次の**両方**を同一Session Lock下で検証する。

- **A**：全Candidateの`registrationStatus`がREGISTEREDまたはEXCLUDEDであること（従来どおり）。
- **B**：活動情報を含む／含む可能性のある全未解析ブロックについて、明示除外またはCandidate化済みであること。Candidate化後の生成物は条件Aで判定する。

A/Bのいずれか未充足ならCompleteを拒否し、残っている理由をREVIEWで説明できるようにする。Frontendだけで完了可否を決めてはならない。Partial Register後もACTIVE Sessionとして未処理分を扱える。Complete後のRaw削除48時間、ACTIVE最終保存から7日等は維持する。

## 4. IMP-D03｜任意情報の正式保存と明示未保存

### 4.1 対象と属先

| 原文から抽出するもの | 正式保存の属先 | 確認・Validation |
|---|---|---|
| Scenario作者／Scenario URL | 新規作成するScenario | `REUSE_EXISTING`を選んだ既存Scenarioを自動更新しない |
| Character Sheet URL | 新規作成するPCの正式参照情報 | 共通URLかPC System Setting専用URLかの誤割当を避け、対応先をユーザーに示す |
| HO | 対応するParticipation | 既存のHOルールを維持 |
| Quote | 対応するParticipation | HOと別保存。最大24 Unicode Extended Grapheme Cluster、勝手な切捨て・正規化・括弧付け禁止 |
| Spotlight | 対応するParticipation | HOのみ→HO、Quoteのみ→QUOTE、両方→User選択、両方なし→NULL |
| EndPcState | PC付きParticipation | Scenario.gameSystemに対応する保存時点のProfile、既存制約、PCなし不可 |
| Growth／Outcome／Aftereffects | EndPcState | Profile準拠、Growth／Aftereffects最大500文字、Outcome SURVIVED／LOST／NULL |

`EndPcState.status_values`は解決済みのScenario.gameSystemに対応するProfileで許可されたKeyのみ、Nullable Integer、負数可、小数不可。ゲームシステム不明時に勝手なProfileやStatus Keyを創作しない。EndPcState全項目空ならレコードを作らない。PC本体の現在値にはしない。

### 4.2 設定・修正・明示除外

1. 抽出した任意情報は所属先・原文・現在候補値を確認可能にする。対応先が不明なら他人物や他卓へ勝手に結びつけない。
2. `CREATE_NEW`のScenario／PCでは、ユーザーが正式登録対象として確認した既存Formal属性のみ、新規Entity作成時に保存する。`REUSE_EXISTING`の場合、抽出された作者・URL・Character Sheet情報を既存Entityへ自動上書きしない。必要ならImport外の通常編集へ案内できる。
3. 正式保存に必要な所属先、文字数、Status Profile等が未解決の任意情報がある場合、Candidateはそのまま正式登録できない。
4. ユーザーは**値／属先を修正して保存**、または**該当の任意情報だけを今回は保存しない**のいずれかを明示選択できる。後者でもほかの正常情報とCandidateの正式登録を妨げない。除外した任意情報を他へ転用しない。
5. 「今回は保存しない」という選択は一時Support上で保持し、原文・対象項目・除外意思を元情報と結びつける。登録前の確認画面で未保存予定を明示し、登録結果にも反映する。黙ってIGNOREしたり、「すべて保存しました」と表示しない。
6. 同一Candidate内の正式登録は引き続き原子的。任意情報だけ失敗してもFormal Tableだけ成功したような**無告知の部分保存は認めない**。ユーザーが明示除外した項目を除く対象全体を保存するか、CandidateをFAILEDとして整合を保つ。

### 4.3 例

`PL：田中／PC：青い月／HO：HO1／セリフ：まだ帰らない／終了SAN：53` → 明確な所属があればそれぞれ候補化。SpotlightはHOとQuoteが両方あるのでUserが選ぶ。SAN=53は対応Profileが許可する場合、PC付きParticipationのEndPcStateへ保存。PC本体へSAN現在値を追加しない。

Quoteが25 Extended Grapheme Clusterの場合は自動削除・24文字切詰めをせず、修正または明示未保存を求める。PC未設定でEndPcStateを抽出した場合も、PCを紐づけるか、該当Stateを明示未保存とする。

## 5. IMP-D04｜CHECKEDの再確認契約

### 5.1 基本

- CHECKEDはユーザーが**現在のCandidate内容**を確認したことを意味する。推論AやE3があるという理由では自動CHECKEDにしない。
- 値、卓所属範囲、Participation関係、未割当の扱い、Resolution判断、除外予定の任意情報など**正式登録内容の意味に影響する変更**は実質変更とする。旧CHECKEDを失効させUNCHECKEDへ戻す。
- 表示切替、Filter、Sort、スクロール、DETAIL開閉等の表示専用操作ではCHECKEDを保持。別Candidateの変更で無関係なCandidateを一律UNCHECKEDにしない。
- Split／Mergeの結果Candidateは既存契約どおり必ずUNCHECKED。

### 5.2 同一PATCHでの変更と明示確認

R5.1.12 §34.5のCandidate PATCHでは`candidateData`と`confirmationStatus`を同時に送信可能であり、これを維持する。

- **通常の編集・Auto Save**：Frontendは古いCHECKEDを惰性的に送らない。実質変更があればBackendはUNCHECKEDへ戻す。
- **ユーザーの明示的な「変更内容を確認して保存」操作**：その変更後の値を画面上で確認でき、本人が明示CHECKEDしたときは、同じPATCHで変更＋CHECKEDを許容。BackendはVersion確認後、その同一操作の最終内容に対する確認として扱う。
- Backendがユーザーに見せた内容と異なる実質的な補正を行う必要がある場合、確認済みとせず修正後の内容を再表示してUNCHECKEDにする。
- 内容が変わらない再送や、登録対象のON/OFFだけの切替ではCandidate内容の確認状態を解除しない。ただし除外中に内容を変更していた場合は旧CHECKEDを維持しない。
- 同じResolutionへ再Bindするだけで実質意味が変わらなければ再確認不要。別のResolutionへの変更は再確認を要する。
- Requestが古いSession／Candidate Versionを持つ場合、既存409を返し、そのリクエストによってCHECKEDも変更しない。エラー時のFrontend Draftは既存Auto Save方針に従う。

**契約上の注意**：CHECKED同梱は明示確認操作に限るというFrontend義務を設ける。Backendは`confirmationStatus=CHECKED`が明示Requestされた場合でも、Version・Validation・内容補正の結果を検証し、単なる旧CHECKEDのサーバー状態を編集後に自動継承しない。新しいFormal Confirmation Entityは作らない。

## 6. IMP-D05｜SourceTrace・ユーザー編集の出自

1. Scenario、Table、TableDate、Person、Role、PC、HO／Quote、EndPcState等の**推論されたフィールドと参加者単位**で、根拠となったSource、実際に分かる原文位置、原文抜粋を関連付ける。
2. 分類根拠（なぜRoleと認識したか）、同卓根拠（E3/E2）、競合・保留根拠（C1/C2/C3等）は意味を分けて保存・返却できるようにする。固定の数値Confidence閾値は新設しない。
3. 跨Source推論では関与した全Sourceへ辿れること。重複コピーを独立証拠として数えない。
4. ユーザー編集前の**Source原文と出自情報は不変**とし、現在の候補値が編集されたものなら**ユーザー編集由来**と区別する。編集後値に元Sourceの値という虚偽の根拠を付けない。
5. Sourceの一部しか位置特定できない場合、分かる範囲のみ示し、架空の行番号・セル座標を補わない。Candidate分割・統合、H01からの候補化でも元Sourceへの関係を再関連付けする。
6. REVIEWでは推論の要点と必要な注意を簡潔に、DETAILでは原文・位置・推論理由・競合と編集履歴上の区別を見られるようにする。Hover以外にKeyboard／Mobileでも確認できる導線を用意する。
7. HTML原文はBrowserで実行せず、安全なテキストとして表示する。ユーザー所有境界、原文保持期限、REGISTERED不変を守る。
8. 実装するSupport／公開DTOの具体形はCodexが既存`import_candidate_source_trace`／`source_location JSONB`／`sourceContexts[].traces[]`を基準に最小限で設計してよい。ただし**各フィールドへ正確に対応できること**を受入条件とし、固定`/table/scenarioName`を全項目のTraceとして使ってはならない。

## 7. IMP-D06｜初期登録対象・Status・確認状態の分離

- **新規に解析生成されるCandidateの初期`registrationTarget`は`true`、`registrationStatus`は`PENDING`、`confirmationStatus`は`UNCHECKED`** とする。REGISTERABLE／NEEDS_REVIEW／NEEDS_FIXのどれであっても、未確認を理由に勝手に`false`へしない。
- `registrationTarget=false`は**ユーザーが明示的に今回の登録対象外とした意思**を意味し、既存契約どおり`registrationStatus=EXCLUDED`へ遷移する。
- 対象に再度含める場合は明示INCLUDEし`PENDING`へ戻せる（REGISTEREDは対象外）。内容が変わっていないCHECKEDは適切に維持、内容変更済みなら再確認。
- Backendの`status`、CHECKED、登録対象、登録結果は独立。`registrationTarget=true`のCandidateでも、未確認・要修正・Resolution未解決などのときはRegister不可。
- FrontendはBackend Responseの状態値を正として表示。要確認／修正必要を「登録対象外」と同一視する旧UI説明のみ廃止。ARCHIVE INTAKEの外観、4 STEP、Candidate Cardを維持する。
- Source作成後に未解析ブロックから新Candidateを生成した場合も初期true／PENDING／UNCHECKEDとする。ただし既存Split／Merge ResultについてはR5.1.12の`registrationTarget`継承／競合解決規則を優先し、自動でtrueに上書きしない。

## 8. IMP-D07｜追加Bulk・学習機能は採用しない

- 既存Bulkは`APPLY_RESOLUTION`／`CLEAR_RESOLUTION`／`EXCLUDE`／`INCLUDE`だけ。
- 任意フィールド一括修正、同型エラーの自動伝播、一括CHECKED、Source間で学習を永続保存する機構は**現MVPに追加しない**。
- 反復構造・文脈を解析中の推論根拠として利用する採用済み方針は維持。任意の表記を機械的に恒久学習する新機能とは区別する。

## 9. 既存安全境界／不変更事項

- 全Mutation：`expectedSessionVersion`、User所有権、Session Row Lock、Snapshot確認、Version更新・409を維持。既存Bulk／Split／Merge Applyは`IMPORT_PREVIEW_STALE`優先。
- Partial Register：Candidate単位Atomicity、通常の個別FAILED許容、二重登録防止。成功CandidateはREGISTEREDとなりImportから更新不可。
- REGISTEREDが存在した後：Source変更、Reset、通常Analyzeは禁止。H01の保持済みブロック限定Candidate化だけを新規の許可操作として追加する。
- Formal Identity：Scenario／Person／PCは文字列一致だけで自動REUSEしない。ImportResolutionの明示判断と同一`resolutionId`による共有を維持。
- Existing R5.1.12 Part A A18のFILE Atomic Replacement、R5既存Limits、7日／48時間の保持、Auth／CSRF／Versionはそのまま。
- Role不明や任意情報の未保存選択を、Candidate全体の`registrationTarget=false`と混同しない。対象部分の明示除外とCandidateの明示EXCLUDEDは別である。

## 10. Codex向け機能受入テスト（新たな期待値・未実行）

| ID | 入力／操作 | 必須の期待結果 |
|---|---|---|
| CT01 | 実施済み卓の参加者・使用PC行、PLラベルなし | Role AでPL仮入力。自動CHECKEDなし |
| CT02 | 上位文脈なし`氏名,PC`（元T17） | Role未判定。A01と混同しない |
| CT03 | SKP-only | 主KPを捏造せず、KPとして保存には明示説明・選択が必要 |
| CT04 | Role不明の1人＋正常PL2人 | Role未解決ならCandidate登録不可。明示除外で正常2人のみ登録可能、未保存通知あり |
| CT05 | 無害な空行・見出し | 構造として処理し、活動記録未処理のCompleteブロッカーにしない |
| CT06 | 卓境界不明の活動メモ1件 | 未解析ブロックとして保持、未処理ではComplete不可 |
| CT07 | 部分登録後に保持済みブロックからCandidate生成 | REGISTERED不変、Source Lock保持、二重候補化なし、生成候補UNCHECKED |
| CT08 | 未解析ブロックを明示除外 | 全Candidateが終端状態ならComplete可能 |
| CT09 | Quote 25 Grapheme | 勝手に切らない。修正か明示未保存を要求 |
| CT10 | PCなしのEndPcState | 無断保存不可。PC設定か明示未保存を要求 |
| CT11 | 新規Scenarioの作者・URL | 適正にDraft→CREATE_NEWへ反映。REUSE_EXISTINGでは自動上書きしない |
| CT12 | HO＋Quote両方 | 別保存。Spotlightはユーザー選択 |
| CT13 | CHECKED候補をAuto Saveで実質編集 | BackendがUNCHECKEDへ。旧CHECKEDを自動継承しない |
| CT14 | 内容変更＋ユーザー明示確認の同一PATCH | 正常Versionなら最終値にCHECKED可能。Backend補正で実質差があれば再確認 |
| CT15 | 検索／フィルタ／表示切替 | CHECKED維持 |
| CT16 | 別Candidateの編集、SourceTrace複数 | 無関係な確認状態を解除せず、全元Sourceを追跡可能 |
| CT17 | PC名をユーザーが修正 | 元原文・原位置は不変、現在値はユーザー編集として区別 |
| CT18 | NEEDS_FIXで初期生成されたCandidate | registrationTarget=true/PENDING/UNCHECKED。要修正のためRegister不可。明示OFF以外はEXCLUDEDにしない |
| CT19 | 旧VersionでRole補正／H01変換／Register | Version競合で無変更。成功済みREGISTEREDは不変 |
| CT20 | T01〜T24既存Fixture、CSV/MD/HTML/TXT/貼付、E3/E2/C1/C3 | 既存期待値を維持。誤統合や正式Identity自動REUSEが起きない |

**テスト状態**：すべて**未実行の受入期待値**。現在Git HEAD、Codex実装完了、テスト合格を示すものではない。

## 11. Codexへ渡す際の指示

- 本書に記載の6つの追加確定仕様を、2026-10-08 R5.1.12の既存Contractへ**矛盾なく最小限で接続する**こと。
- 追加した機能が必要とするImport Support Data／API／画面は、**機能の意味が本書と一致する範囲で**実装設計してよい。Formal 9 Entity・14UCや正式Roleを変更しない。
- 特に、未解析ブロックの限定Candidate化とComplete、任意情報の明示未保存、Role未決の解決、同時PATCHの明示CHECKEDについて、Backendの検証・原子性・二重処理防止が成り立つことをテストで確認する。
- 仕様外の判断が必要になった場合は、推測で正式Data・既存Contractを変更せず、実装上の具体的な衝突点を報告すること。
- R5.1.12原本、今回の2026-10-13引継ぎ、前段の統合レビュー案、T01〜T24の期待値を併読する。**レビュー案の未承認提案は、本書で採用した部分だけ今回の確定仕様となる。**
