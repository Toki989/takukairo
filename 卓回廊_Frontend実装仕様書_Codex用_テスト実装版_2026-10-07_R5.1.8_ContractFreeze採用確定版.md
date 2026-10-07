# 卓回廊 Frontend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-10-07 R5.1.8 / Contract Freeze採用確定版

---

# 0. この文書の位置づけ

本書は、

`卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-07_R5.1.7_Contract再精査修正版.md`

を基底として、R5.1.7で残っていたFrontend / Cross Contract HOLDを、2026-10-07のユーザー明示採用に基づいて閉じる後発差分資料である。

R5.1.7以前は削除・上書きせず履歴として保持する。

最重要原則は変更しない。

> **見た目はv5.9。中身は既存の確定済み設計。**

以下は変更しない。

```text
Formal Entity = 9
MVP UC = 14
v5.9 Visual = 維持
Endpoint集合 = 維持
Draft保持 = 維持
Keyboard / Focus / Reduced Motion = 維持
```

---

# 1. R5.1.7から継承するError UX

Backend R5.1.7以降が返す`fieldErrors[]`をFrontend側で縮約しない。

```text
1件
→ Inline / Section Error中心
→ 適切な可視TargetへFocus

2件以上
→ Import Form上部Error SummaryへFocus
```

同じ`path=/sources`でもCodeが異なるErrorは別Entryとして保持する。

Backend順序を維持する。

400 / 409 / 413 / Transport ErrorでDraftを破棄しない。

---

# 2. R5.1.8採用A：Frontend事前ValidationはNon-blocking補助表示

R5.1.7 HOLD-FE-01を次で閉じる。

## 2.1 基本Rule

FrontendがBrowser上で信頼できるByte数を把握できる場合、Backendと同じBusiness Limitを用いて事前計算してよい。

ただしLocal Business Limit判定は：

> **送信を止めない補助表示のみ**

とする。

Business Limitの最終正本はBackendである。

## 2.2 Submit

UserがImport Source追加 / Text更新をSubmitした場合、LocalでBusiness Limit超過を検出していても、Business Limitを理由にHTTP RequestをBlockingしない。

BackendへRequestを送り、Backend Responseを正式結果として扱う。

## 2.3 Local Hint

信頼できるLocal byte数でBusiness Limit超過を検出した場合は、送信前に補助的な案内を表示する。

ただしLocal Hintは：

- Backend `fieldErrors[]`ではない。
- Error Summary件数へ加算しない。
- 1件 / 2件以上Focus Ruleへ使用しない。
- Backend Error Codeを推測生成しない。
- Session totalを正確に把握できない場合、Aggregate超過を推測表示しない。

## 2.4 Backend Response後

送信後はBackend結果を正本とする。

- 400 JSON → `fieldErrors[]`を正式Error表示へMapping。
- 409 → Backend Codeに従う。自動再送しない。
- non-JSON 413 → Request-level Error。Fieldへ推測Mappingしない。
- Transport Error → Backend Business Errorを推測しない。

これにより同じSubmit操作について「Localで止める実装」と「Backendへ送る実装」の分岐を禁止する。

---

# 3. R5.1.8採用B：Source Mutation成功Responseの受領

FrontendはBackend R5.1.8のWire Shapeだけを使用する。

## 3.1 File Source add

```text
POST /api/import/session/{id}/sources/files
→ 201
```

Response：

```json
{
  "sessionVersion": 4,
  "sourceIds": [101, 102]
}
```

Frontendは：

- `sessionVersion`を次Mutationへ保持する。
- `sourceIds`をRequest内File順に対応付ける。

## 3.2 Text Source add

```text
POST /api/import/session/{id}/sources/text
→ 201
```

Response：

```json
{
  "sessionVersion": 5,
  "sourceId": 103
}
```

Frontendは返却`sourceId`を作成Source identityとして使用する。

## 3.3 Text Source PATCH

```text
PATCH /api/import/session/{id}/sources/{sourceId}
→ 200
```

Response：

```json
{
  "sessionVersion": 6
}
```

## 3.4 Source DELETE

```text
DELETE /api/import/session/{id}/sources/{sourceId}?expectedSessionVersion=...
→ 200
```

Response：

```json
{
  "sessionVersion": 7
}
```

## 3.5 禁止事項

Frontendは：

- `sessionVersion + 1`を推測しない。
- Headerから未定義Versionを取得しない。
- Version取得だけを目的としたGETを追加しない。
- Source Mutation成功後の自動再送をしない。

---

# 4. R5.1.8採用C：Source order InteractionをMVPから除外

R5.1.7 HOLD-FE-03を次で閉じる。

MVP FrontendではSource並べ替えInteractionを提供しない。

実装しない：

- Drag reorder
- Up / Down reorder Button
- `order`入力
- `order` PATCH
- Client側独自再採番

Frontendが送るPASTED_TEXT PATCHは：

```json
{
  "expectedSessionVersion": 4,
  "text": "..."
}
```

のみ。

FILE binary replacementは既存どおり：

```text
新FILE Source add
↓
旧Source delete
```

で行う。

表示・解析上のSource順はBackendから返るBackend-owned `source_order`順を前提とし、Frontendが別順序を正式化しない。

---

# 5. Session DELETEのVersion Rule訂正

ImportSession自身のDELETE：

```text
DELETE /api/import/session/{id}?expectedSessionVersion=...
→ 204 Bodyなし
```

成功後は対象Session自体が消滅するため、最新`sessionVersion`を次Mutationへ引き継ぐ対象から除外する。

Frontendは：

- DELETE成功時に削除済みSessionのversionを推測しない。
- Local ImportSession stateを破棄する。
- 「新しくインポート」のFlowでは、その後に新ImportSession POSTを実行する。

Source DELETEとは異なり、Session DELETEは204を維持する。

---

# 6. Accessibility / Focus維持

R5.1.8によって既存Accessibilityを変更しない。

- Error Summary Focus Ruleを維持。
- Summary itemからTargetへ移動可能。
- `/sources`はKeyboard Focus可能なSection Anchor / Headingへ移動。
- `/text`は`aria-invalid` / Error説明関連付けを維持。
- File row Error Targetを識別可能にする。
- Local HintはBackend正式Errorと混同しない。
- Reduced Motionを維持。
- v5.9 Visualを変更しない。

---

# 7. Frontend QA確定

## 7.1 Local Validation

- 信頼できるLocal超過を検出してもSubmit Requestを送る。
- Local HintだけではError SummaryへFocusしない。
- Backend 400受領後に正式Error Summary / Inline Errorへ切り替える。
- 不正確なSession Aggregateを推測しない。

## 7.2 Source Mutation Response

- File add 201の`sessionVersion`を次Mutationへ使用。
- File add `sourceIds`をRequest順に対応付ける。
- Text add 201の`sourceId`を使用。
- PATCH / DELETE 200の`sessionVersion`を使用。
- VersionをFrontendで加算しない。

## 7.3 Source order

- Reorder UIが存在しない。
- `order`をPATCH Bodyへ送らない。
- Source削除後の表示順をFrontendで詰め直して正式順として保存しない。

## 7.4 Session DELETE

- stale version → Backend 409に従う。
- current version → 204でLocal Session stateを破棄。
- 204 BodyからVersionを読もうとしない。

---

# 8. R5.1.8 Frontend回帰監査

## R5.1.3との整合

- ImportSession配下Mutationへ`expectedSessionVersion`を渡すRuleを維持。
- Source add / edit / delete成功後にBackend返却versionを使用するRuleを維持。
- Session DELETEだけはResource消滅のため後発訂正でVersion引継ぎ対象外。
- Source API Endpoint集合を維持。

判定：**整合。**

## R5.1.4 / R5.1.5との整合

- Import Visual / INPUT構造を変更しない。
- Large Text Guard / Business Limitを変更しない。
- `order-only PATCH`はBackend R5.1.8と同様に廃止。

判定：**整合。**

## R5.1.6との整合

- Error Mapping / Draft保持 / 413 fallbackを維持。
- R5.1.6のoptional multiple errorはR5.1.7の決定的全件返却を維持。

判定：**整合。**

## R5.1.7との整合

- HOLD-FE-01 → 本書§2で解消。
- HOLD-FE-02 → 本書§3で解消。
- HOLD-FE-03 → 本書§4で解消。

判定：**残Frontend HOLD 0。**

---

# 9. R5.1.8 Frontend最終判定

```text
Formal 9 Entity = 維持
MVP 14 UC = 維持
v5.9 Visual = 維持
Endpoint追加 = なし

Local prevalidation = Non-blocking補助表示へ決定
Backend Business Validation = 最終正本
Source Mutation sessionVersion受領方法 = 決定済み
Source reorder UI = MVPでは実装しない
Session DELETE/version = 決定済み
Error Summary / Focus / Draft / Accessibility = 維持

Frontend仕様不明HOLD = 0
Frontend独立Freeze Blocker = 0
Frontend Codex実装 = GO
```

本書の採用事項は2026-10-07のユーザー明示採用に基づく。
