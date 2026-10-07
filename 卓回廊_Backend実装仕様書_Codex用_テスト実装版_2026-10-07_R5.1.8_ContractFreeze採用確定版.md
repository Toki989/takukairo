# 卓回廊 Backend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-10-07 R5.1.8 / Contract Freeze採用確定版

---

# 0. この文書の位置づけ

本書は、

`卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-07_R5.1.7_Contract再精査修正版.md`

を基底として、R5.1.7で残っていたImport Source周辺のHOLDを、2026-10-07のユーザー明示採用に基づいて閉じる後発差分資料である。

R5.1.7以前は削除・上書きせず履歴として保持する。

最重要原則は変更しない。

> **見た目はv5.9。中身は既存の確定済み設計。**

以下は変更しない。

```text
Formal Entity = 9
MVP UC = 14
v5.9 Visual = 維持
Endpoint集合 = 維持
DB Formal Schema = 維持
FILE binary replacement = new Source add + old Source delete
Source mutation成功時Analysis invalidation = Backend Atomic
REGISTERED Candidate後Source add/edit/delete = 禁止
```

R5.1.8でPublic Wireへ固定する新規最小項目は、ユーザーが採用したSource Mutation成功Responseの`sessionVersion`と作成Source識別子のみである。

---

# 1. R5.1.7から継承する確定事項

R5.1.7で訂正済みの以下をそのまま維持する。

## 1.1 Source Mutation Error Priority

Application Mutation Protocolへ到達した後：

```text
Authentication / CSRF等の既存Security Gate
↓
Current User所有ImportSession取得
↓
Session Row Lock
↓
expectedSessionVersion検証
↓
Source Membership検証（PATCH / DELETE）
↓
REGISTERED Candidate後のSource Mutation Lock
↓
Mutation固有Validation
↓
Import Business Limit Validation
↓
Source mutation
↓
未登録Analysis State invalidation
↓
Commit / session.version更新
```

したがって：

```text
stale expectedSessionVersion + REGISTEREDあり
→ 409 OPTIMISTIC_LOCK_CONFLICT
```

Transport / Pre-binding 413はこのApplication-level優先順位へ含めない。

## 1.2 Business Limit Error集合

Business Validationへ到達した同一Requestでは、同一proposal snapshotから成立する適用可能Business Limit違反を全件返す。

Business Limit内部でfail-fastしない。

R5.1.7で固定したproposal計算、fieldErrors path / code / message / 順序を変更しない。

## 1.3 Failure / Success Atomicity

400 / 404 / 409等でMutation不成立の場合：

- Sourceを変更しない。
- Analysis Stateを変更しない。
- `session.version`を変更しない。

成功したSource mutationは：

```text
Source mutation
+
未登録Analysis State invalidation
+
session.version increment
```

を同一TransactionでCommitする。

---

# 2. R5.1.8採用A：Source Mutation成功Response Contract

R5.1.7 HOLD-BE-01を次で閉じる。

Import Source Mutationは、親ImportSessionが存続し次Mutationへ最新`sessionVersion`を渡す必要があるため、一般的なDelete=204 Ruleより本Endpoint固有Contractを優先する。

## 2.1 File Source add

```text
POST /api/import/session/{id}/sources/files
```

成功：

```text
HTTP 201
Content-Type: application/json
```

Response：

```json
{
  "sessionVersion": 4,
  "sourceIds": [101, 102]
}
```

Rules：

- `sessionVersion`は当該Mutation Commit後の最新ImportSession version。
- `sourceIds`は今回のRequestで作成されたFILE Source IDだけを含む。
- `sourceIds`順はmultipart `files` partのRequest順と一致する。
- 1..N Fileを同一Requestで追加する場合、成功Responseは1回だけ返す。
- Request全体が失敗した場合、部分的Source追加を成功扱いしない。

## 2.2 Pasted Text Source add

```text
POST /api/import/session/{id}/sources/text
```

成功：

```text
HTTP 201
Content-Type: application/json
```

Response：

```json
{
  "sessionVersion": 5,
  "sourceId": 103
}
```

Rules：

- `sessionVersion`はCommit後の最新version。
- `sourceId`は今回作成したPASTED_TEXT Source ID。

## 2.3 Pasted Text Source PATCH

```text
PATCH /api/import/session/{id}/sources/{sourceId}
```

成功：

```text
HTTP 200
Content-Type: application/json
```

Response：

```json
{
  "sessionVersion": 6
}
```

Source IDはURLですでに確定しているため、成功Responseへ重複返却しない。

## 2.4 Source DELETE

```text
DELETE /api/import/session/{id}/sources/{sourceId}?expectedSessionVersion={version}
```

成功：

```text
HTTP 200
Content-Type: application/json
```

Response：

```json
{
  "sessionVersion": 7
}
```

Import Source DELETEは、削除後も親ImportSessionが存続し、次Mutationへ最新versionを渡す必要があるため、Common Successの完全Delete=204に対するEndpoint固有例外とする。

## 2.5 禁止事項

Frontend / BackendはSource Mutation成功時に：

- `sessionVersion + 1`を推測しない。
- Version専用Response Headerを新設しない。
- Mutation成功後のVersion取得だけを目的とした追加GETを要求しない。
- 上記以外のWrapper ShapeをCodex裁量で作らない。

---

# 3. R5.1.8採用B：ImportSource orderをBackend-ownedへ固定

R5.1.7 HOLD-BE-02を次で閉じる。

## 3.1 Public PATCHから`order`を削除

MVP Public ContractではSource並べ替えを提供しない。

したがって：

```text
PATCH /api/import/session/{id}/sources/{sourceId}
```

はPASTED_TEXT Sourceの`text`更新専用とする。

Request：

```json
{
  "expectedSessionVersion": 4,
  "text": "..."
}
```

`order`はPublic Request Fieldではない。

FILE binary差替えは既存どおりPATCHせず：

```text
new FILE Source add
+
old Source delete
```

で扱う。

## 3.2 `source_order`はSupport Dataとして維持

DB / Support Modelの`source_order`は削除しない。

ただしFrontendから変更しないBackend-owned値とする。

新規Source追加時、Session Row Lock取得後の同一Transaction内で：

```text
new source_order
=
COALESCE(MAX(existing source_order), 0) + 1
```

とする。

複数FILE Sourceを1 Requestで作成する場合：

- multipart `files` partのRequest順で連続採番する。
- 既存最大値の次から開始する。

PASTED_TEXT Source追加も既存最大値の次へ追加する。

## 3.3 DELETE後のorder

Source DELETE後：

- 残存Sourceの`source_order`を詰め直さない。
- 欠番を許容する。
- `session.version`以外をorder再採番だけのために変更しない。

## 3.4 Parser入力順

Sourceを解析へ渡す順序は：

```text
source_order ASC
```

とする。

Backend-owned採番により同一Session内の新規作成Sourceへ同一`source_order`を付与しない。

## 3.5 同値Text PATCH

Public PATCHは`text`更新Mutationとして扱う。

Requestが現在の`raw_text`と同一文字列であっても、RequestがVersion / Membership / Lock / Validationを通過して成功した場合は通常のSource Mutationとして扱い：

- Analysis State invalidation
- `session.version` increment
- HTTP 200 + latest `sessionVersion`

を行う。

Frontendは変更がない場合の不要PATCHを送らないが、Backend Contractは同値PATCHでも一意にする。

---

# 4. R5.1.8訂正C：ImportSession DELETEはVersion返却一般則の例外

R5.1.7 HOLD-BE-03 / 全体HOLD-04は、新しいBusiness Rule追加ではなく既存Contractの適用範囲訂正として閉じる。

```text
DELETE /api/import/session/{id}?expectedSessionVersion={version}
```

は既存どおり：

- Current User所有Session取得
- Session Row Lock
- `expectedSessionVersion`検証
- Delete

を行う。

成功：

```text
HTTP 204
Bodyなし
```

Session DELETE成功後は対象ImportSession自体が存在しないため、同Sessionの「次Mutation」も存在しない。

したがって：

> Import Session Version Ruleの「Mutation成功Responseで最新`sessionVersion`を返し次Mutationへ引き継ぐ」は、ImportSession自身の完全DELETEには適用しない。

Frontendは成功後に削除済みSessionのversionを保持・推測しない。

新しくインポートする場合は、既存確定FlowどおりDELETE成功後に新しいImportSessionをPOSTで作成する。

---

# 5. Frontend事前Validationとの責務境界

R5.1.7 HOLD-01に対する採用内容をBackend側から固定する。

- Import Business Limitの最終正本はBackend。
- Frontendが信頼できるLocal byte数を計算しても、Business Limit理由でSubmit Request自体をBlockingしない。
- BackendはFrontend事前計算を信頼せず、自身で既存Business Limit Validationを実行する。
- Backend `fieldErrors[]`が正式なError Summary / Focus判断の根拠となる。
- Transport 413は従来どおりApplication Business 400と分離する。

---

# 6. Automated / Integration Test確定

R5.1.7までのTestに加え、以下を固定する。

## 6.1 Source Mutation Success Response

- File 1件 add → 201 / `sessionVersion` / `sourceIds` 1件。
- File複数 add → 201 / `sourceIds`がRequest順。
- Text add → 201 / `sessionVersion` / `sourceId`。
- Text PATCH → 200 / `sessionVersion`。
- Source DELETE → 200 / `sessionVersion`。
- Source DELETE成功後の次Mutationは返却versionを使用できる。

## 6.2 Source order

- 最初のSourceは`source_order=1`。
- 追加ごとに既存MAX+1。
- 複数FileはRequest順で連続採番。
- DELETE後に欠番を詰めない。
- 次の追加は残存MAX+1。
- Parserは`source_order ASC`。
- Public PATCHへ`order`を送る正常系Testを削除する。
- R5.1.5の`order-only PATCH`正常系はR5.1.8 Contractにより廃止する。

## 6.3 Same-value PATCH

- 同値text PATCHでも成功Mutationとしてversionを1進める。
- 未登録Analysis Stateをinvalidatesする。
- Responseは200 + latest `sessionVersion`。

## 6.4 Session DELETE

- stale version → 409 OPTIMISTIC_LOCK_CONFLICT。
- current version → 204 Bodyなし。
- 204 Responseに`sessionVersion`を要求しない。

---

# 7. R5.1.8 Backend回帰監査

## R5.1.3との整合

- Session Row Lock → expectedSessionVersion → Membershipの既存Mutation Protocolを維持。
- Source add / text / patch / delete Endpointを維持。
- REGISTERED Candidate後Source mutation禁止を維持。
- Atomic Analysis invalidationを維持。
- FILE binary replacement方式を維持。
- Source DELETEのみ、親Session version返却要件を満たすため後発Endpoint固有Ruleとして200 JSONへ明示Override。
- Session DELETEはCommon Success 204を維持。

判定：**整合。**

## R5.1.4 / R5.1.5との整合

- Byte定義 / Large Text Guard / Session Aggregate計算を変更しない。
- R5.1.5 `order-only PATCH`だけは、後発のユーザー採用により廃止。
- Business Limit計算式へ影響なし。

判定：**整合。**

## R5.1.6との整合

- Business Limit Error Shape / 400 / 413境界を維持。
- R5.1.6のError Priority問題はR5.1.7訂正を維持。

判定：**整合。**

## R5.1.7との整合

- deterministic multiple fieldErrorsを維持。
- HOLD-BE-01 → 本書§2で解消。
- HOLD-BE-02 → 本書§3で解消。
- HOLD-BE-03 → 本書§4で解消。

判定：**残Backend HOLD 0。**

---

# 8. R5.1.8 Backend最終判定

```text
Formal 9 Entity = 維持
MVP 14 UC = 維持
v5.9 Visual = 維持
DB Formal Schema追加 = なし
Endpoint追加 = なし

Error Priority = 決定済み
Business Limit fieldErrors集合 = 決定済み
Source Mutation Success Wire = 決定済み
Source order = Backend-ownedで決定済み
Session DELETE/version = 決定済み
Atomicity / Concurrency = 維持

Backend仕様不明HOLD = 0
Backend独立Freeze Blocker = 0
Backend Codex実装 = GO
```

本書の採用事項は2026-10-07のユーザー明示採用に基づく。
