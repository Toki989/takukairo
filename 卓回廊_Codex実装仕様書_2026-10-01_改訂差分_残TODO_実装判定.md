# 卓回廊 Codex実装仕様書 2026-10-01 改訂差分・残TODO・実装判定

## 1. 結論

**実装工程へ進める状態。**

2026-09-30版Frontend / Backend Codex実装仕様書について、2026-10-01までに明示採用された差分を反映し、Frontend ↔ REST API ↔ Backend ↔ PostgreSQLの横断整合を再確認した。

ただし、以下の未確定箇所は`TODO_SPEC_CONFIRMATION`を維持し、該当機能だけ最終固定しない。

- Previous EndPcState候補取得APIの最終Endpoint / Request Shape
- CCFOLIA Mapping / Preview APIの最終Shape
- Tekey / Udonarium共通Adapter
- CCFOLIA `iconUrl`のPC画像利用可否
- Frontendのみ：Recording URLなし時のActionをHidden / Disabledのどちらにするか

上記はFormal 9 Entity、14 UC、主要CRUD、認可、Table Aggregate、PC System Settings、Import主要Contract、画像更新、安全性、v5.9 Visual実装開始を妨げない。

---

## 2. 改訂ファイル

### Frontend

- `卓回廊_Frontend実装仕様書_Codex用_テスト実装版_2026-10-01_改訂版.md`
- 行数：2561 → 3090
- 差分概算：+684 / -155 lines
- SHA-256：`7fb3ff86cff3ce414167ccfc967dc45ccaeb00ca05b344da4eb26d5a0ea23f4f`

### Backend

- `卓回廊_Backend実装仕様書_Codex用_テスト実装版_2026-10-01_改訂版.md`
- 行数：1994 → 2577
- 差分概算：+731 / -148 lines
- SHA-256：`bedc06b551decc152576426e118a0029190c634451ef07ce3a09a1704b12e5cf`

---

## 3. Frontend 主な変更差分

1. **PC単一Game System前提を撤去**
   - PC本体はCommon / Default Character Sheet URLを維持。
   - 0..N Game System SettingsをTab UIで編集。
   - System専用Character Sheet URLを保持し、専用URL優先 → 共通URLFallback。
2. **Game System Profile / OptionSource / Canonical Resolve接続を追加**
   - ProfileごとのReact Componentハードコードを禁止。
3. **PC Focusを複数System対応へ更新**
4. **Previous EndPcStateをRead Only別欄からReference Prefillへ変更**
   - StatusのみPrefill。
   - growth / outcome / aftereffectsは引継がない。
   - Touched Fieldを再計算で上書きしない。
5. **PL ChangeからPC Detachを削除**
   - PC.current Person + 選択Participation.personのみ変更。
6. **Cross-Table Optimistic Lock UXを追加**
   - 409時は自動再試行せず最新Contextを再取得して再確認。
7. **Import APIを10/1詳細Contractへ全面更新**
   - Session / Source / Analysis Reset / Candidate / Bulk Preview / Split Preview / Merge Preview / Register / Complete。
8. **Common Errorを`fieldErrors[]` + JSON Pointerへ更新**
9. **SelfPerson Setup Gateを追加**
10. **Image expectedVersion Sequenceを追加**
11. **画像高速化方針を追加**
    - 安全性・Ownership・CSRF・Optimistic Lockは削らない。
12. **Tablet QAを追加**
    - 767/768、1199/1200を重点確認。
13. **Accessible Text Token方針を一旦採用として明示**
14. **Quote 24文字をExtended Grapheme Clusterへ更新**
    - `Intl.Segmenter`。
15. **Definition of Doneを最新仕様へ更新**

---

## 4. Backend 主な変更差分

1. **旧`pc.game_system`を廃止**
2. **`pc_system_setting` Support Structureを追加**
   - Formal Entityは9件のまま。
3. **PC共通Character Sheet URL + System専用URLを明確化**
4. **PC Aggregate APIへ`systemSettings[]`を統合**
   - 省略=変更なし / `[]`=全削除 / 指定=完全一覧。
5. **Game System Profile API追加**
6. **OptionSource仕様追加**
7. **Canonical Resolve API追加**
8. **EndPcState Profile Version Ruleを更新**
   - TableのScenario.gameSystem基準。
   - 既存EndPcStateは保存済みprofileKey維持。
9. **Previous EndPcStateをReference Prefill用Responseへ更新**
10. **`POST /api/self-person`追加**
11. **SelfPerson Setup Gate追加**
12. **Login成功時Session ID変更を確定**
13. **Import APIを10/1詳細Contractへ更新**
14. **ImportResolution追加**
15. **Candidate Version / Optimistic Lockを明確化**
16. **PL `change-person`から`detachPcFromParticipationIds`を削除**
17. **Cross-Table Optimistic Lock追加**
18. **Image Update Version Sequence追加**
19. **Image Cleanup / Retry Sequence追加**
20. **Image高速化方針追加**
21. **Common ErrorをArray + JSON Pointerへ変更**
22. **Error Code追加**
23. **`display_quote VARCHAR(24)` → `TEXT`**
24. **Quote Extended Grapheme Cluster Validation追加**
25. **Automated Test Minimum / Definition of Done更新**
26. **PC System Setting ownership / cascadeを追加**

---

## 5. 横断監査結果

### Frontend ↔ Backend API

- FrontendでMethod付き明示Endpoint：35件
- Backendに対応定義が見つからないEndpoint：0件
- **0件。明示Endpointは整合。**


### Markdown構造

- Frontend code fence数：208（偶数=True）
- Backend code fence数：182（偶数=True）

### 旧仕様逆戻りチェック

以下は、禁止事項・移行説明としての言及を除き、実装Contractから除去済み。

- PC本体の単一Game System
- PL変更内PC Detach
- Previous EndPcState Read Only別欄
- Common ErrorのObject型`fieldErrors`
- `display_quote VARCHAR(24)`
- Session ID RenewalのDeferred扱い

---

## 6. 残る `TODO_SPEC_CONFIRMATION`

### 共通

1. Previous EndPcState候補取得APIの最終Endpoint / Request Shape
2. CCFOLIA Mapping Configの正式Shape
3. CCFOLIA Preview API Requestの最終Shape / profileKeyの渡し方
4. Tekey / Udonarium共通Adapter設計
5. CCFOLIA `iconUrl`のPC画像利用可否

### Frontendのみ

6. Recording URLなし時のActionをHidden / Disabledのどちらにするか

### TODOではなく【一旦採用】

- Breakpoint：Mobile 0–767 / Tablet 768–1199 / Desktop 1200+
- Accessible Text Token追加方針
- Quote最大24 Extended Grapheme Cluster

これらは現テスト実装では採用方向で実装し、実ブラウザ / PrototypeでVisual Detailを再調整可能とする。

---

## 7. 実装開始判定

### 判定：**GO**

次の実装を開始してよい。

```text
Backend Skeleton / Flyway
→ Formal 9 Entity
→ Support Structure (pc_system_setting / Import系)
→ Dev Session / CSRF / SelfPerson Gate
→ Scenario / Person / PC Aggregate
→ Game System Config
→ Table Aggregate
→ Image
→ Import
→ Frontend API Client / App Shell
→ Home / Scenario / PC / Table / Import
→ v5.9 Desktop卓詳細
→ Responsive / Accessibility QA
```

ただし、Previous EndPcStateのAPI RouteとCCFOLIA未確定部分は、実装時に推測で固定せず`TODO_SPEC_CONFIRMATION`境界を維持する。
