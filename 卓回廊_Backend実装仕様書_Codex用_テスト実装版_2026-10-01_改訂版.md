# 卓回廊 Backend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-10-01 改訂版

---

# 0. この文書の目的

本書は、TRPG活動履歴管理Webサービス **「卓回廊」** のBackendをCodexが実装するための実装仕様書である。

今回の実装フェーズは **実ユーザーを受け入れないローカル / テスト実装** を対象とする。

ただし、テスト用だからといってFormal Specを簡略化・改変してはならない。

最重要原則：

> **見た目は確定済みVisual Design。中身は既存の確定済み設計。**

Backendでは特に、以下を守る。

- Formal Entityは9件のまま増やさない。
- MVP UCは14件のまま増やさない。
- Mock Dataを正式Data Modelへ逆輸入しない。
- SAN / HP / MPをPC本体へ現在値として追加しない。
- `SKP` Roleや`isMainKP`を追加しない。
- `Participation.person` と `PC.person` をDB制約で一致強制しない。
- Child Entityへ冗長 `user_id` を追加しない。
- Scenario名、Scenario URL、Person表示名、PC名をUnique化しない。
- 本書にないData属性・業務RuleをCodexが推測追加しない。

不足がある場合は勝手に補完せず `TODO_SPEC_CONFIRMATION` として残す。

---

# 1. 情報源の優先順位

実装時に解釈が衝突した場合、以下の順で扱う。

1. 本書（2026-10-01改訂版）
2. `最終横断チェック後 引継ぎメモ兼プロンプト 2026-10-01`
3. `卓回廊_S1-S6_BackendDataSecurityPhysicalDesign_確定事項まとめ_2026-09-30.md`
4. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md`
5. `TRPG活動履歴管理Webサービス_VisualResponsiveAccessibility仕上げフェーズ_確定事項まとめ_2026-09-30.md`
6. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-29_No20-No27確定版.md`
7. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-29_修正版.md`
8. `TRPG活動履歴管理Webサービス_最新統合正本_2026-09-28_確定版_フロント制作補強修正版(3).md`
9. 各Visual / CRUD / 詳細設計資料

2026-10-01までのユーザー明示採用内容を9/30版の古い記述より優先する。

`TRPG_PCFocus_v5_9` やFrontend PrototypeのMock値はData仕様の根拠にしない。

状態区分：

- 【確定】
- 【一旦採用】
- 【未確定 / `TODO_SPEC_CONFIRMATION`】

---

# 2. 今回の実装範囲

## 2.1 実装する

- PostgreSQL Physical Schema
- Flyway Migration
- Formal 9 Entity
- JPA Mapping
- User所有権境界
- Scenario CRUD
- ScenarioFavorite
- Person Create / Read / Update
- PC CRUD
- PC現在Person変更Flow
- Table / TableDate / Participation / EndPcState Aggregate CRUD
- Game System Profile
- Spotlight永続化
- Recent候補算出
- UC17集計
- Import一時Data構造
- Import解析Pipeline
- Import途中保存 / 再開 / 一部登録
- Upload画像Validation / Local Storage Adapter
- BOOTH取得Interface + Mock / Optional Live Adapter
- Cookie Session
- CSRF
- Optimistic Lock
- Rate Limit
- Structured Error Response
- Dev/Test用固定User Login
- Test Seed
- Testcontainers PostgreSQL Test

## 2.2 今回はProduction完成条件にしない

以下は設計上のInterfaceは作るが、実運用完成を要求しない。

- 実ユーザー向けGoogle OAuth運用
- Cloudflare R2実Credential接続
- Render Production Deploy
- Production PITR運用
- Account削除Tombstoneの最終方式
- R2とPITRの時間整合最終方式
- 利用規約 / Privacy Policy正式本文
- External Scenario ImageのProduction法務判断
- Production Log Retention最終値
- Activity Export
- Offline Read Snapshot

上記は `DEFERRED_PRODUCTION` とする。

---

# 3. Technology Stack

## 3.1 Backend

- Java: Eclipse Temurin 25 LTS
- Spring Boot: 4.1.x
- Spring Data JPA
- Spring Security
- Spring Session JDBC
- Spring Web / REST API
- Maven 3.9.x + Maven Wrapper
- PostgreSQL 18.x
- Flyway
- Test: Spring Boot Test + Testcontainers PostgreSQL

Exact Patch Versionは実装開始時点のStableへLockする。

## 3.2 Parser / External

- CSV: Apache Commons CSV
- HTML: jsoup
- Markdown: commonmark-java
- TXT / Pasted Text: Java標準処理
- BOOTH HTTP: Spring RestClient
- Production R2 Adapter候補: AWS SDK for Java 2.x S3 Client
- Image Processing: `ImageProcessor` Interfaceを設ける
- `vips-ffm + libvips` は `PROVISIONAL`。Adapterとして隔離する。

---

# 4. Project Structure

Codexは責務を分離する。

```text
backend/
├─ pom.xml
├─ mvnw
├─ mvnw.cmd
├─ src/main/java/.../
│  ├─ TakukairoApplication.java
│  ├─ config/
│  ├─ security/
│  ├─ common/
│  │  ├─ api/
│  │  ├─ error/
│  │  └─ validation/
│  ├─ user/
│  ├─ scenario/
│  ├─ person/
│  ├─ pc/
│  ├─ table/
│  ├─ importsession/
│  ├─ gamesystem/
│  ├─ image/
│  ├─ booth/
│  └─ activity/
├─ src/main/resources/
│  ├─ application.yml
│  ├─ application-dev.yml
│  ├─ application-test.yml
│  ├─ db/migration/
│  └─ db/dev/
└─ src/test/
```

Controllerへ業務Logicを直接書かない。

基本分離：

```text
Controller
→ Application Service
→ Domain / Validation
→ Repository / External Adapter
```

---

# 5. Dev / Test Runtime

## 5.1 Local runtime

```text
Frontend : http://localhost:5173
Backend  : http://localhost:8080
Postgres : localhost:5432
```

Development CORSは `http://localhost:5173` のみ許可する。

ProductionはSame Origin前提。

## 5.2 Dev User

実ユーザーを使用しないため、`dev` Profileでのみ固定Test Userを作れる。

実装例：

```text
POST /api/dev/session
```

- `dev` / `test` Profile以外ではBean自体を登録しない。
- Productionでは404になること。
- Dev LoginでもSpring Sessionを発行する。
- Cookie / CSRF / AuthorizationのBackend経路はProductionと同じものを通す。
- User IDをFrontendから自由指定させない。

Seed User例：

```text
auth_provider = GOOGLE
provider_subject = dev-test-sub
```

Dev endpointだけがGoogle Token検証を迂回してこのSeed UserへSessionを発行する。

**User Schema / auth_providerのFormal Ruleは変更しない。**

推奨：Dev SeedをProduction Flywayと分離する。

```text
src/main/resources/db/migration  # 正式Schema
src/main/resources/db/dev        # devのみSeed
```

---

# 6. Formal Entity

Formal Entityは以下9件のみ。

1. User
2. Scenario
3. Person
4. PC
5. Table
6. TableDate
7. Participation
8. EndPcState
9. ScenarioFavorite

ImportSession等はSupport / Temporary DataでありFormal 9 Entityへ数えない。

---

# 7. Physical Schema

## 7.1 Common

正式Entityの主キーは原則：

```sql
BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
```

日時：

```text
TIMESTAMPTZ(3)
Java: Instant
```

実施日：

```text
TableDate.played_on = DATE
```

Mutable Entityには：

```text
version BIGINT NOT NULL DEFAULT 0
```

JPA `@Version` を使用する。

対象：

- User
- Scenario
- Person
- PC
- Table
- TableDate
- Participation
- EndPcState

ScenarioFavoriteはOptimistic Lock対象外。

---

## 7.2 app_user

```text
id                      BIGINT PK
auth_provider           VARCHAR(32) NOT NULL
provider_subject        VARCHAR(255) NOT NULL
self_person_id          BIGINT NULL
external_image_consent_at TIMESTAMPTZ(3) NULL
created_at              TIMESTAMPTZ(3) NOT NULL
updated_at              TIMESTAMPTZ(3) NOT NULL
version                 BIGINT NOT NULL
```

Constraint：

```text
UNIQUE(auth_provider, provider_subject)
UNIQUE(self_person_id)
CHECK(auth_provider IN ('GOOGLE'))
```

Dev Seedも `auth_provider=GOOGLE` を使用し、Schema制約を緩めない。

Google由来でProduction DBへ恒久保存する認証情報は基本 `sub` のみ。

保存しない：

- email
- email_verified
- name
- given_name
- family_name
- picture
- locale
- hd
- Google ID Token
- Access Token
- Refresh Token

---

## 7.3 scenario

```text
id                    BIGINT PK
user_id               BIGINT NOT NULL
name                   VARCHAR(255) NOT NULL
author_name            VARCHAR(255) NULL
source_url             VARCHAR(2048) NULL
game_system            VARCHAR(255) NULL
external_image_url     VARCHAR(2048) NULL
image_master_key       VARCHAR(512) NULL
image_derivative_key   VARCHAR(512) NULL
created_at
updated_at
version
```

禁止：

```text
UNIQUE(name)
UNIQUE(source_url)
```

を作らない。

---

## 7.4 person

```text
id            BIGINT PK
user_id       BIGINT NOT NULL
display_name  VARCHAR(255) NOT NULL
created_at
updated_at
version
```

同名Personを許可する。

---

## 7.5 pc【2026-10-01更新】

```text
id                   BIGINT PK
user_id              BIGINT NOT NULL
person_id            BIGINT NOT NULL
name                 VARCHAR(255) NOT NULL
character_sheet_url  VARCHAR(2048) NULL   # Common / Default URL
image_master_key     VARCHAR(512) NULL
image_derivative_key VARCHAR(512) NULL
image_position_x     NUMERIC(9,4) NOT NULL DEFAULT 0
image_position_y     NUMERIC(9,4) NOT NULL DEFAULT 0
image_zoom           NUMERIC(9,4) NOT NULL DEFAULT 1
created_at
updated_at
version
```

```text
CHECK(image_zoom > 0)
```

旧`pc.game_system`はMigration後に削除し、二重管理しない。

`pc.character_sheet_url`は単一Game Systemと組にしたFieldではなく、PC共通 / Default Character Sheet URLとして維持する。

`PC.person` は現在のPerson。

過去 `Participation.person` はPC.person変更で自動更新しない。

### PC System Setting Support Structure【確定】

Formal 9 Entityへ追加しない。PC Aggregate配下のSupport Structure。

物理Table：

```text
pc_system_setting
- id                    BIGINT PK
- pc_id                 BIGINT NOT NULL
- game_system_name      VARCHAR(255) NOT NULL
- canonical_system_key  VARCHAR(128) NULL
- profile_key           VARCHAR(128) NULL
- profile_values        JSONB NOT NULL DEFAULT '{}'
- character_sheet_url   VARCHAR(2048) NULL   # System専用
- display_order         INTEGER NOT NULL
- created_at
- updated_at
```

Rules：

- 1 PCに0..N件。
- PCをSystemごとに複製しない。
- 同じ非NULL `canonical_system_key` は同一PC内で重複不可。
- `canonical_system_key IS NULL` のUNKNOWN Systemは文字列だけで自動Deduplicateしない。
- `profile_values`はProfile定義に従いBackend Validationする。
- SELECTはOption LabelではなくOption Keyを保存する。
- System専用Character Sheet URLが存在すれば共通URLより優先する。
- 親`PC.version`でOptimistic Lockする。
- Setting専用CRUD APIは作らない。

## 7.6 trpg_table

```text
id             BIGINT PK
user_id        BIGINT NOT NULL
scenario_id    BIGINT NOT NULL
table_name     VARCHAR(255) NULL
recording_url  VARCHAR(2048) NULL
created_at     TIMESTAMPTZ(3) NOT NULL
updated_at     TIMESTAMPTZ(3) NOT NULL
version        BIGINT NOT NULL
```

`created_at` はサービス登録日時であり、実際に遊んだ日ではない。

---

## 7.7 table_date

```text
id
 table_id
 played_on DATE NOT NULL
 created_at
 updated_at
 version
```

同一Table同一日の重複をDB Uniqueにしない。

TableDate 0件を許可する。

---

## 7.8 participation

```text
id             BIGINT PK
table_id       BIGINT NOT NULL
person_id      BIGINT NOT NULL
pc_id          BIGINT NULL
role           VARCHAR(16) NOT NULL
display_order  INTEGER NOT NULL
ho             TEXT NULL
display_quote  TEXT NULL
spotlight_type VARCHAR(16) NULL
created_at
updated_at
version
```

Constraint:

```text
CHECK(role IN ('KP','PL'))
CHECK(display_order >= 1)
CHECK(spotlight_type IS NULL OR spotlight_type IN ('HO','QUOTE'))
UNIQUE(table_id, role, display_order) DEFERRABLE INITIALLY DEFERRED
```

Role `SKP` は作らない。

表示上：

```text
role=KP && display_order=1  -> KP
role=KP && display_order>=2 -> SKP
```

Quote最大24文字はDB `VARCHAR(24)`で制約しない。

Application ValidationでUnicode Extended Grapheme Cluster 24以下を検証する。

HOには新しい文字数上限を追加しない。

## 7.9 end_pc_state

```text
id                BIGINT PK
participation_id  BIGINT NOT NULL
profile_key       VARCHAR(128) NULL
status_values     JSONB NOT NULL DEFAULT '{}'
growth            TEXT NULL
outcome           VARCHAR(16) NULL
aftereffects      TEXT NULL
created_at
updated_at
version
```

DB Constraint:

```text
jsonb_typeof(status_values) = 'object'
outcome IS NULL OR outcome IN ('SURVIVED','LOST')
length(growth) <= 500
length(aftereffects) <= 500
UNIQUE(participation_id)
```

全項目空ならレコードを作らない。

数値Status：

- Nullable Integer
- 負数可
- 小数不可
- 共通Min / Maxなし

Profileが許可するKeyだけBackendで受け付ける。

---

## 7.10 scenario_favorite

```text
id
user_id
scenario_id
created_at
```

```text
UNIQUE(user_id, scenario_id)
```

Favoriteは存在 / 非存在で意味を持つ。

---

# 8. Relation / Delete Rule

## 8.1 CASCADE

- TableDate → Table
- Participation → Table
- EndPcState → Participation
- ScenarioFavorite → Scenario
- ScenarioFavorite → User
- PC System Setting → PC

`pc_system_setting`はPC Aggregate配下のSupport Structureであり、PC本体が削除可能な場合は同時削除する。

## 8.2 RESTRICT / Application Control

- PC → Person
- Table → Scenario
- Participation → Person
- Participation → PC
- User.SelfPerson → Person
- User所有Top-level Entity → User

Account削除は専用Application Flowで順序を管理する。

## 8.3 Delete Business Rule

Scenario削除：

- 関連Table 0件のみ可能
- 関連TableをCascade削除しない
- ScenarioFavoriteは削除

PC削除：

- 関連Participation 0件のみ可能

Person削除：

以下すべて満たす場合のみ。

- SelfPersonではない
- 関連PC 0件
- 関連Participation 0件

Table削除：

- Table
- TableDate
- Participation
- EndPcState

を削除する。

Scenario / Person / PC本体は残す。

---

# 9. Ownership

すべての認証後Read / WriteでCurrent User所有権をBackendで確認する。

Frontendから送信された `userId` を認可根拠にしない。

Child / Support StructureはParentを辿って所有者を確認する。

冗長 `user_id` を追加しないChild / Support：

- TableDate → Table → User
- Participation → Table → User
- EndPcState → Participation → Table → User
- PC System Setting → PC → User
- Import Candidate / Source / Resolution → ImportSession → User

ScenarioFavoriteはUser × Scenario自体がRelationの意味を持つため例外。

## 9.1 Composite FK補強

採用済み方針：

```text
PC(user_id, person_id) -> Person(user_id, id)
Table(user_id, scenario_id) -> Scenario(user_id, id)
ScenarioFavorite(user_id, scenario_id) -> Scenario(user_id, id)
User(id, self_person_id) -> Person(user_id, id)
```

実装上PostgreSQLのFK参照先を成立させるため、以下の補助UNIQUEをFlywayへ追加する。

```text
Person   UNIQUE(user_id, id)
Scenario UNIQUE(user_id, id)
```

これは `display_name` や `Scenario.name` をUnique化するものではない。

**監査補完であり業務仕様変更ではない。**

---

# 10. Game System Profile / OptionSource【確定】

Formal Entityへ追加しない。

MVPではVersioned Application Config。

## 10.1 Game System Profile

概念：

```text
GameSystemProfile
- profileKey
- canonicalSystemKey
- displayName
- pcFields[]
- endStateStatuses[]
- showOutcome
- growthLabel
- aftereffectsLabel
```

Field Input Type：

```text
TEXT
INTEGER
SELECT
BOOLEAN
REPEATER
```

将来候補：`MULTI_SELECT`。

FrontendへSystem別React Logicをハードコードさせない。

Canonical SystemごとにActive Profile最大1件。

旧Profileは過去Dataが参照している限り削除しない。

## 10.2 OptionSource

Versioned Application Config。

Option：

```text
key
label
group optional
displayOrder
selectable
```

DB保存はKey。

使用済みKeyを削除・再利用しない。

廃止Optionは`selectable=false`として過去表示可能にする。

## 10.3 EndPcState Profile Version Rule

EndPcStateで利用するProfileは：

> **TableのScenario.gameSystem**

を基準にする。

PC System Settingを基準にしない。

保存時点のVersioned `profile_key` をEndPcStateへ保存する。

既存EndPcState編集は保存済み`profile_key`を使う。

Scenario.gameSystemやPC System Settingが後から変わっても過去EndPcState.profile_keyを変更しない。

新Profileへ自動Migrationしない。

保存済みProfileを解決できない場合、現在Active Profileへ勝手にFallbackしない。

## 10.4 Canonical Resolve

```text
POST /api/game-systems/resolve
```

状態：

```text
EXACT
SUGGESTED
UNKNOWN
```

- EXACTのみ自動Canonical化可能。
- SUGGESTEDは候補提示のみ。User選択後に保存。
- UNKNOWNは自由入力を維持し、Canonical KeyはNULL可。
- `CoC`等から6版 / 7版を勝手に決めない。

---

# 11. Participation Rule

1 Participation =

```text
1 Person
1 Role
0..1 PC
```

Roleは `KP | PL`。

同一Personが複数PCを担当する場合はParticipationを複数作成する。

`Participation.person` と `PC.person` の一致をDBで強制しない。

PCなしPL Participationを許可する。

PCなしParticipationにはEndPcStateを作成しない。

Participation表示順は `Table × Role` 単位で1-based。

Add / Delete / Role change / Reorder後はTransaction内で1..Nへ再採番する。

---

# 12. Spotlight Rule

HOとQuoteは別Dataとして両方保存可能。

```text
HO only       -> spotlight_type = HO
Quote only    -> spotlight_type = QUOTE
Both          -> User selection
Neither       -> NULL
```

Backend保存時に矛盾をNormalizeする。

例：

- HO空・Quoteあり・spotlight=HO → QUOTEへ補正
- HOあり・Quote空・spotlight=QUOTE → HOへ補正
- 両方空 → NULL

---

# 13. Table Aggregate Save

Table登録 / 編集は画面全体で最後に1回保存する。

BackendでもAggregate Transactionとして扱う。

Request概念：

```json
{
  "version": 3,
  "scenarioId": 10,
  "tableName": null,
  "tableDates": ["2026-09-20", "2026-09-21"],
  "recordingUrl": "https://...",
  "participations": [
    {
      "id": null,
      "personId": 20,
      "pcId": null,
      "role": "KP",
      "displayOrder": 1,
      "ho": null,
      "displayQuote": null,
      "spotlightType": null,
      "endPcState": null
    },
    {
      "personId": 30,
      "pcId": 40,
      "role": "PL",
      "displayOrder": 1,
      "ho": "HO1",
      "displayQuote": "帰ろう。",
      "spotlightType": "QUOTE",
      "endPcState": {
        "profileKey": "coc_7e_v1",
        "statusValues": {"san": 51, "hp": 10, "mp": 12},
        "growth": "目星 +3",
        "outcome": "SURVIVED",
        "aftereffects": null
      }
    }
  ]
}
```

Rules：

- New時ScenarioはRoute Contextですでに選択済み。
- EditではScenario変更可。
- TableName空を許可。
- TableDate 0件を許可。
- Participation 0件を許可。
- PCなしParticipationを許可。
- EndPcStateはPCありParticipationのみ。
- 全空EndPcStateは保存しない。
- PL→KP変更でPC / HO / Quote / EndPcStateが失われる場合はFrontendが確認し、Backendは最終Requestを整合Validationする。
- Edit時はTable.versionでOptimistic Lock。
- 子更新時も親Table.versionを更新する。

### EndPcState Profile Validation【確定】

新規EndPcStateでは、BackendがTableのScenario.gameSystemからProfileを解決して保存する。

Frontendが`profileKey`を送る場合も任意のProfile選択権として信用せず、Scenario基準と一致するか検証する。

既存EndPcState編集では保存済み`profile_key`を使用する。

Scenario.gameSystem変更を理由に既存EndPcStateのProfileを自動Migrationしない。

Profile解決不能時にActive Profileへ勝手にFallbackしない。

---

# 14. Previous EndPcState Reference Prefill【確定】

PC本体へ現在SAN / HP / MP等を持たせない。

過去EndPcStateは新規Table入力の補助値。

候補順：

1. TableDateがある過去Table → 最新TableDate
2. TableDateなし → Table.created_atを低精度参考順位として使用可

Table.created_atを「実際に遊んだ日」と表示しない。

Responseは参考精度と、**`endStateStatuses[]`に対応するStatus値**を返せる構造にする。

例：

```json
{
  "sourceTableId": 100,
  "basis": "TABLE_DATE",
  "basisDate": "2026-09-01",
  "profileKey": "coc_7e_v1",
  "statusValues": {"san": 51, "hp": 10, "mp": 12},
  "lowConfidence": false
}
```

TableDateなしFallback例：

```json
{
  "basis": "CREATED_AT_FALLBACK",
  "profileKey": "coc_7e_v1",
  "statusValues": {"san": 51},
  "lowConfidence": true
}
```

Frontendはこれを別Read Only欄に固定表示する方式ではなく、今回終了時Status InputへReference Prefillする。

引き継ぐのは`endStateStatuses[]`のみ。

以下は引き継がない：

- growth
- outcome
- aftereffects

`TODO_SPEC_CONFIRMATION`：候補取得APIの最終Endpoint / Request Shapeは未確定。現行の`GET /api/tables/{tableId}/previous-end-state?pcId=...`を確定Endpointとして実装しない。

---

# 15. Recent Rule

新しい `lastUsedAt` Columnは作らない。

TableDateがある場合：

> 最新TableDate

TableDateがない場合：

> Table.created_atを低精度補助順位として使用

Person Recent：

- Participationとして参加したTableの最新使用日時
- KP / PL別Rankingは作らない
- 最大5件

PC Recent：

- Person選択後、そのPersonに現在紐づくPCを通常候補
- その中でRecentを優先
- 最大5件
- SearchではCurrent User所有の全PCを検索可

自動選択しない。

---

# 16. UC17 Activity Summary

SelfPersonを基準に集計する。

Home Summary：

- 遊んだScenario数
- 参加したTable数
- PL参加回数
- KP参加回数

月別 / 年別集計基準日：

- TableDate 1件 → その日
- TableDate複数 → 最新TableDate
- TableDate 0件 → Table.created_atを集計所属年月決定だけに使用

1 Tableは1月・1年にだけ所属する。

`aggregationDate` 永続Columnを追加しない。

---

# 17. URL Validation

Scenario URL / Character Sheet URL / Recording URL等で共通Utilityを用意してよい。

Scenario URL正式Rule：

- 任意
- `http://` / `https://` のAbsolute URLのみ
- hostname必須
- trim
- 空白のみは未設定
- UserInfo (`user:pass@`) を拒否
- Query / Fragment可
- `javascript:` / `data:` / `file:` / `ftp:` / `mailto:` 等を拒否
- DB Application Limit: 2048 characters

表示時は生URLをUIへ露出する必要はない。

---

# 18. Scenario Duplicate Candidate

自動統合しない。

比較範囲はCurrent User所有Scenarioのみ。

強いCandidate：

- 正規化URL一致
- Scenario名 + 作者名一致

確認CandidateはWarningであり登録禁止ではない。

Candidate提示後、Userが既存Scenario使用 / 新規継続を決める。

---

# 19. Image Upload

## 19.1 Accepted

- JPEG
- PNG
- WebP static only

Rejected：

- GIF
- SVG
- HEIC / HEIF
- AVIF
- Animated WebP

Limits：

- File <= 10MB
- Width <= 8192
- Height <= 8192
- Pixel <= 32,000,000
- Estimated decoded raster <= 192 MiB

Validation order：

```text
File Size
→ Magic Number / Actual Format
→ Header read
→ Dimension
→ Pixel Count
→ Estimated Raster
→ Static Image
→ Decode
→ EXIF Orientation normalize
→ Dimension re-check
→ Derivative
```

## 19.2 Processing

- Aspect Ratio維持
- Auto Cropしない
- 小画像を強制Upscaleしない
- Alpha保持
- Transparent Canvas Marginを勝手にTrimしない
- Lossless Master保持
- PC Position / ZoomはMetadata

Derivative：

- WebP
- Quality 85
- Max long edge 2048px = `PROVISIONAL_BUT_IMPLEMENT_FOR_TEST`

## 19.3 Storage abstraction

```java
interface ImageStorage {
  StoredImage saveMaster(...);
  StoredImage saveDerivative(...);
  Optional<ImageObject> read(...);
  void delete(...);
}
```

Dev/Testは `LocalImageStorage` を実装する。

Production R2は別Adapter。

FrontendへMasterの恒久公開URLを返さない。

DevでもDerivative URL取得APIを通す。

## 19.4 Safe Replace

画像差し替え：

1. 新画像Validation
2. 新Master / Derivative保存
3. DB更新
4. DB Commit成功
5. 旧画像削除をSchedule

途中失敗時は旧画像を維持する。

`DEFERRED_PRODUCTION`：PITR Windowとの旧画像削除整合。

Test実装では即削除でも可だが、`ImageDeleteScheduler` Interfaceを挟みProduction差替え可能にする。

---

# 20. Image URL API

FrontendはStorage Keyを直接扱わない。

API例：

```text
GET /api/images/pcs/{pcId}/derivative-url
GET /api/images/scenarios/{scenarioId}/derivative-url
```

Response：

```json
{
  "url": "/api/dev-images/....",
  "expiresAt": "2026-09-30T12:05:00Z"
}
```

Dev Adapterでも短期URL契約を模倣する。

Frontendは403 / expired時に1回だけ再取得可能。

Production設計：5分Signed URL / private / no-store。

---

# 21. BOOTH Input Assistance

BOOTHはScenario登録そのものではなく入力補助。

基本Flow：

```text
User URL
→ Product ID抽出
→ Backendで許可BOOTH URL再構築
→ Fetch
→ Parse
→ Candidate DTO
```

User入力URLを任意URL Fetchへ直接渡さない。

取得対象：

- Scenario名候補
- 作者候補
- Game System候補
- 商品画像候補
- Scenario URL

取得結果を正式保存へ自動確定しない。

Live BOOTH accessはDevでFeature Flag：

```text
app.booth.live-fetch=false
```

DefaultはFixture / Mock Adapter。

External Image FileをLocal Storage / R2へ複製しない。

---

# 22. Import Temporary Model【2026-10-01更新】

Formal 9 Entityへ追加しないSupport Data。

最低限：

```text
ImportSession
ImportSource
ImportCandidate
ImportCandidateSourceTrace
ImportResolution
```

### ImportSession

- user
- currentStep
- savedAt
- expiresAt
- completionState
- version

1 UserにつきACTIVE最大1件。

COMPLETED / 48h削除待ちは新規開始を阻害しない。

### ImportSource

- session
- sourceType
- originalFileName nullable
- encoding
- rawText / rawStorageRef
- order

Source変更時は古い解析・確認状態をResetし、古いCandidateを残さない。

### ImportCandidate

- session
- status: REGISTERABLE | NEEDS_REVIEW | NEEDS_FIX
- confirmation status
- registration target
- parsed candidate structure
- registration status
- version

Candidate Status等のBackend-owned FieldをFrontendからPatchさせない。

### Trace

Candidate fieldとSource位置の対応を保持する。

元SourceをDETAILで比較できること。

### ImportResolution【確定】

Formal EntityではないSupport Structure。

概念：

```text
session
entityType: SCENARIO | PERSON | PC
decision: REUSE_EXISTING | CREATE_NEW
existingEntityId nullable
draftData
createdEntityId nullable
version
```

Partial Registerで最初のCandidateがCREATE_NEWによりEntityを作成した場合、`createdEntityId`を保持し、同じResolutionを参照する後続Candidateは同一Entityを再利用する。

同じ「新規作成判断」から重複Entityを生成しない。

---

# 23. Import Input Limits

- 1 File <= 20MB
- Max 20 files / session
- Session total <= 50MB
- Pasted Text <= 2MB

対象：

- CSV
- Markdown
- HTML
- TXT / other text
- Pasted Text

PDFはMVP外。

CSV Encoding：

- UTF-8
- UTF-8 BOM
- CP932

---

# 24. Import Pipeline

```text
INPUT
→ format-specific parsing
→ common intermediate structure
→ common inference
→ Table Candidates
→ REVIEW
→ optional DETAIL
→ REGISTER
```

推定積極性：

```text
項目分類          : 比較的積極的
値境界            : 慎重
Data関係付け      : より慎重
Table分割/統合    : 非常に慎重
```

同名 = 同一として自動統合しない。

未推定Dataを勝手に破棄しない。

AI解析はMVP必須にしない。

---

# 25. Import Session Lifecycle

- STEP2 / STEP3 Auto Save
- 最終保存から7日保持
- 保存成功ごとにexpiresAt延長
- 新規Import開始時、既存Sessionがあれば明示破棄確認
- 正式登録可能Candidateのみ一部登録可
- 未処理Candidateが残ればSession継続
- 全Candidate登録 or 残りを明示除外でComplete
- Complete後Raw Import Dataは48時間保持後削除
- Account削除時は48時間を待たない

Cleanup Job：

- 原則1時間ごと
- Idempotent
- Retry可能
- 正式9 EntityはCleanup対象外

---

# 26. Import Register Transaction

Candidate単位でTransaction。

1 Candidate内の正式Entity作成はAtomic。

複数Candidate一括登録は、1 Candidate失敗で全件Rollbackさせない。

Candidate A成功 / B失敗を許容する。

Idempotencyを持たせ、同一Candidateの二重登録を防ぐ。

Implementation例：

- Candidateに `registration_key` / `registered_table_id` 等のSupport状態
- REGISTER RequestにIdempotency Key

具体Column名はSupport Model内で決めてよいがFormal Entityへ追加しない。

---

# 26.1 Import Support Physical Schema

Support DataはFormal 9 Entityではない。物理Namingは以下を基準とする。

```text
import_session
- id
- user_id
- status                 # ACTIVE / COMPLETED / DELETE_PENDING
- current_step           # INPUT / REVIEW / DETAIL / REGISTER
- saved_at
- expires_at
- completed_at nullable
- delete_after_at nullable
- version

import_source
- id
- import_session_id
- source_type            # FILE / PASTED_TEXT
- original_file_name nullable
- encoding nullable
- temporary_storage_key nullable
- raw_text nullable
- source_order
- version

import_candidate
- id
- import_session_id
- candidate_data JSONB
- status                 # REGISTERABLE / NEEDS_REVIEW / NEEDS_FIX
- confirmation_status    # UNCHECKED / CHECKED
- registration_target BOOLEAN
- registration_status    # PENDING / REGISTERED / FAILED / EXCLUDED
- registration_result JSONB nullable
- version

import_candidate_source_trace
- id
- import_candidate_id
- import_source_id
- source_location JSONB

import_resolution
- id
- import_session_id
- entity_type            # SCENARIO / PERSON / PC
- decision               # REUSE_EXISTING / CREATE_NEW
- existing_entity_id nullable
- draft_data JSONB
- created_entity_id nullable
- version
```

正確な`candidate_data` / `draft_data` Shapeは対応DTOと同じ型定義から管理し、自由なMapを画面ごとに増殖させない。

ACTIVE ImportSessionのUser単位UniqueはDB + Applicationの安全な方法で保証する。

ImportResolutionはPartial Register間で同一の新規作成判断を再利用する。

---

# 26.2 Infrastructure Support Table

Formal Entityではない。

```text
storage_delete_task
- id
- object_key
- attempt_count
- next_attempt_at
- created_at
- last_error_code nullable
- status                 # PENDING / DEAD
```

Worker：

- 原則1分ごと
- Exponential Backoff
- 1m → 2m → 4m → 8m ...
- 最大24h間隔
- 30日失敗でDEAD
- Object absentは成功扱い

Dev Local Storageでも同じService Interfaceを通す。

---

# 27. REST API Contract

API Prefix：

```text
/api
```

JSONはcamelCase。

日時はISO-8601 UTC。

IDはnumberで扱う。

## 27.1 Common Success Response【確定】

成功Responseを一律Envelope化しない。

- Create → `201` + 作成DTO
- Update → `200` + 更新後DTO
- Version付きEntity → 最新`version`
- 完全Delete → `204`
- 親Versionを更新するImage Delete等 → 最新Versionを返す

## 27.2 Common Error【確定】

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力内容を確認してください。",
  "fieldErrors": [
    {
      "path": "/systemSettings/1/gameSystemName",
      "code": "REQUIRED",
      "message": "ゲームシステム名を入力してください。"
    }
  ],
  "traceId": "...",
  "context": {}
}
```

`fieldErrors`はArray。

各要素：

- `path`：JSON Pointer
- `code`
- `message`

Section Errorでは`/participations/2`等も許可。

主なcode：

```text
VALIDATION_ERROR
MALFORMED_REQUEST
SESSION_EXPIRED
FORBIDDEN
NOT_FOUND
CONFLICT
OPTIMISTIC_LOCK_CONFLICT
IMPORT_CONFLICT
SELF_PERSON_SETUP_REQUIRED
RATE_LIMITED
NETWORK_EXTERNAL_ERROR
INTERNAL_ERROR
```

`traceId`を維持。

`context`は必要時のみ。

他User Resource情報を`context`へ漏らさない。

Import Partial Register FailureはHTTP Request全体Errorではなく、正常Response内のCandidate別Resultとして扱う。

---

# 28. Auth / Session / SelfPerson API【2026-10-01更新】

```text
GET  /api/session
GET  /api/csrf
POST /api/logout
POST /api/self-person
POST /api/dev/session           # dev/test only
```

`GET /api/session` Response概念：

```json
{
  "authenticated": true,
  "user": {
    "id": 1,
    "selfPerson": {"id": 10, "displayName": "テストPL"}
  }
}
```

SelfPersonなしの場合は`selfPerson: null`。

`POST /api/self-person`：

```json
{
  "displayName": "..."
}
```

Frontendから`userId` / `isSelf`を受け取らない。

1 Transaction：

1. Current User取得
2. SelfPerson未設定確認
3. User所有Person作成
4. `User.self_person_id`設定

既存SelfPersonがある場合、2件目を作らない。

SelfPerson名称変更は通常Person Update API。

### Setup Gate【確定】

authenticated UserでもSelfPerson未設定なら通常Business APIを利用不可。

許可：

```text
GET  /api/session
GET  /api/csrf
POST /api/self-person
POST /api/logout
```

その他：

```text
SELF_PERSON_SETUP_REQUIRED
```

で判別可能にする。

Production Google endpointはInterfaceだけ用意可能：

```text
POST /api/auth/google
```

認証成功時はSession Fixation対策として**Session IDを変更する**。

実Production運用は今回完成条件外。

---

# 28.1 Game System Config API【確定】

```text
GET  /api/game-system-profiles
GET  /api/game-system-profiles/{profileKey}
POST /api/game-systems/resolve
```

一覧：Active Profileのみ、lightweight summary、`configRevision`。

個別：Exact Versionを取得でき、Active / Inactive / Old Versionのfull definitionを返せる。必要なOptionSourceをbundleする。

Canonical SystemごとにActive Profile最大1件。

旧Profileは過去Dataが参照している限り削除しない。

`POST /api/game-systems/resolve`は`EXACT | SUGGESTED | UNKNOWN`を返す。

---

# 29. Scenario API

```text
GET    /api/scenarios
POST   /api/scenarios
GET    /api/scenarios/{scenarioId}
PATCH  /api/scenarios/{scenarioId}
DELETE /api/scenarios/{scenarioId}
POST   /api/scenarios/{scenarioId}/favorite
DELETE /api/scenarios/{scenarioId}/favorite
GET    /api/scenarios/{scenarioId}/tables
POST   /api/scenarios/duplicate-candidates
POST   /api/booth/preview
```

List Query例：

```text
search
participation=PL|KP
favorite=true|false
sort=RECENT_PLAYED|NAME|CREATED_DESC|CREATED_ASC
```

Search対象：

- Scenario名
- 作者
- PL名
- PC名

Scenario detail ResponseにはFavorite状態とSession Archive用Summaryを含めてよい。

---

# 30. Person API

```text
GET   /api/persons?search=&recent=true
POST  /api/persons
PATCH /api/persons/{personId}
DELETE /api/persons/{personId}
```

Deleteは削除条件を満たす時だけ。

Person Update前にFrontend表示用Impact APIを提供してよい：

```text
GET /api/persons/{personId}/impact
```

Response：関連PC件数 / Participation件数。

---

# 31. PC API【2026-10-01更新】

```text
GET    /api/pcs
POST   /api/pcs
GET    /api/pcs/{pcId}
PATCH  /api/pcs/{pcId}
DELETE /api/pcs/{pcId}
GET    /api/pcs/{pcId}/appearances
GET    /api/pcs/{pcId}/change-person-context
POST   /api/pcs/{pcId}/change-person
```

PC List：

```text
search=PC名/PL名
personId
sort=RECENT|NAME|CREATED_DESC|CREATED_ASC
```

PC Create / Detail / PatchはPC本体 + `systemSettings[]`をAggregateとして扱う。

概念DTO：

```json
{
  "id": 40,
  "version": 5,
  "name": "五色 探",
  "personId": 20,
  "characterSheetUrl": "https://common.example/pc",
  "systemSettings": [
    {
      "id": 301,
      "gameSystemName": "クトゥルフ神話TRPG 7版",
      "canonicalSystemKey": "coc_7e",
      "profileKey": "coc_7e_v1",
      "profileValues": {},
      "characterSheetUrl": "https://system.example/pc",
      "displayOrder": 1
    }
  ]
}
```

PC本体`characterSheetUrl`はCommon / Default URL。

Setting側`characterSheetUrl`はSystem専用URL。

System専用URLがある場合はそれを優先し、なければ共通URLを参照可能。

PATCH semantics：

- `systemSettings`省略 → Settings変更なし
- `systemSettings: []` → 全削除
- 指定あり → 保存後の完全一覧

PC本体 + Settingsを1 Transactionで保存し、親`PC.version`でOptimistic Lock。

BackendはCanonical / Profile / ProfileValuesを再Validationする。

Setting専用CRUD Endpointは作らない。

### change-person【確定】

Request概念：

```json
{
  "pcVersion": 4,
  "newPersonId": 50,
  "participationUpdates": [
    {
      "participationId": 100,
      "participationVersion": 7,
      "tableId": 10,
      "tableVersion": 12
    }
  ]
}
```

正確なField NamingはこのContractに合わせFrontend / Backendで統一する。

Rules：

- PC.current Person更新
- 選択された過去Participation.personだけ更新
- 非選択Participationは旧Personのまま
- **PC detachは行わない**
- PL変更だけではEndPcStateを変更・削除しない
- PC.version / Participation.version / Table.versionをすべて確認
- 変更対象が複数Tableにまたがっても1 Transaction
- TableはtableId等の一定順序でLock / Update
- Participation.person変更時は親Table.versionも進める
- 1件でも競合 → 全Rollback + `409 OPTIMISTIC_LOCK_CONFLICT`

---

# 32. Table API

```text
POST   /api/scenarios/{scenarioId}/tables
GET    /api/tables/{tableId}
PUT    /api/tables/{tableId}
DELETE /api/tables/{tableId}
GET    /api/tables/{tableId}/detail
```

`GET /api/tables/{id}/detail` はDesktop / Mobile Table detailに必要な情報を返す。

必須Context：

- Scenario
- TableName
- TableDate all
- KP Participations
- PL Participations ordered
- Person
- PC optional
- PC image ref optional
- HO
- Quote
- Spotlight
- EndPcState
- Recording URL

SelectorはPL Participationだけを返す / FrontendでPLだけにFilterしてもよいが、Response上roleを明示する。

`TODO_SPEC_CONFIRMATION`：Previous EndPcState候補取得APIの最終Endpoint / Request Shape。

9/30版の：

```text
GET /api/tables/{tableId}/previous-end-state?pcId={pcId}
```

を確定Contractとして固定しない。

---

# 33. Activity API

```text
GET /api/activity/summary
GET /api/activity/by-month?year=2026
GET /api/activity/by-year
```

SelfPerson基準。

---

# 34. Import API【確定】

## Session

```text
GET    /api/import/session/current
POST   /api/import/session
GET    /api/import/session/{id}
PATCH  /api/import/session/{id}
DELETE /api/import/session/{id}
```

ACTIVE Sessionが存在する場合、POSTで勝手に上書きしない。

Frontendが明示的に既存SessionをDELETE後、新規作成する。

COMPLETED / 48h削除待ちは新規開始を阻害しない。

## Source

```text
POST   /api/import/session/{id}/sources/files
POST   /api/import/session/{id}/sources/text
PATCH  /api/import/session/{id}/sources/{sourceId}
DELETE /api/import/session/{id}/sources/{sourceId}
POST   /api/import/session/{id}/analysis/reset
```

Source変更後に古い解析結果・確認内容を残さない。

## Analysis

```text
POST /api/import/session/{id}/analyze
```

Candidate StatusはBackendが判定。

Frontendから`REGISTERABLE`等を設定させない。

## Review

```text
GET /api/import/session/{id}/review
```

Filter / Pagination対応。

Summary：

- total
- registerable
- needsReview
- needsFix
- checked

## Candidate

```text
GET   /api/import/session/{id}/candidates/{candidateId}
PATCH /api/import/session/{id}/candidates/{candidateId}
```

Candidateは`version`を持ち、Autosave競合をOptimistic Lock。

Backend-owned FieldはPatch不可。

## Bulk

```text
POST /api/import/session/{id}/bulk-apply/preview
POST /api/import/session/{id}/bulk-apply
```

Previewは非破壊。

## Split / Merge

```text
POST /api/import/session/{id}/candidates/{candidateId}/split-preview
POST /api/import/session/{id}/candidates/{candidateId}/split
POST /api/import/session/{id}/candidates/merge-preview
POST /api/import/session/{id}/candidates/merge
```

Preview後に明示操作。

## Register

```text
POST /api/import/session/{id}/register
```

Request：

```json
{
  "candidateIds": [11, 12, 14]
}
```

Candidate単位Transaction。

Bulk全体All-or-Nothingにはしない。

Partial Failure可。

Double Submit / Idempotency対策必須。

ImportResolutionを利用し、同じCREATE_NEW判断から重複Entityを生成しない。

## Complete

```text
POST /api/import/session/{id}/complete
```

全Candidateが`REGISTERED`または`EXCLUDED`であることをBackendが確認。

Frontendから強制Complete不可。

Complete後、生Import Dataは48h後削除。Formal Dataは削除しない。

---

# 35. Image API【Version Sequence更新】

PC / ScenarioのCreate / Update時、FileはBrowser側Draftとして保持し、Metadata + Imageの2段階保存を基本とする。

Endpoint：

```text
PUT    /api/pcs/{pcId}/image
DELETE /api/pcs/{pcId}/image
PUT    /api/scenarios/{scenarioId}/image
DELETE /api/scenarios/{scenarioId}/image
PATCH  /api/pcs/{pcId}/image-transform
```

Image APIには親Entityの`expectedVersion`を送る。

Metadata→Imageの順なら、Metadata Responseで返した最新VersionをImage Requestへ使用する。

画像しか変更していない場合はMetadata APIを無駄に要求しない。

PC画像PUT時にPosition / Zoomを同時保存可能としてよい。

安全なSequence：

```text
Ownership確認
→ Version早期確認
→ Image Validation
→ Master / Derivative生成（Decode重複禁止）
→ Storage保存（可能な範囲でMaster / Derivative並列）
→ DB Transaction
→ Version再確認
→ 新Storage Keyへ切替
→ PCならTransform保存
→ version increment
→ 旧画像storage_delete_task INSERT
→ Commit
```

旧画像をStorageから先に削除しない。

DB Commit失敗 / Version競合で新画像が不採用：

- 新Master / Derivativeを補償削除
- 削除失敗 → `storage_delete_task`

旧画像Delete TaskはDB切替と同一Transactionで作成する。

旧画像削除はUser Response待ち時間から外す。

FrontendへStorage Keyを公開しない。

Scenario画像にはPC用Position / Zoomを追加しない。

---

# 36. CCFOLIA Input Assistance【一部確定 / 一部TODO】

Table EndPcState入力補助としてClipboard JSONを解析する。

確定：

- Clipboard JSON
- Preview
- User明示操作後に今回EndPcStateへ反映
- 反映後は編集可能
- Parsing失敗でもManual入力継続
- Raw JSON恒久保存なし
- Skills / Ability / Memo / Chat Palette等を正式保存しない
- CCFOLIAを正式Data Sourceとしない
- CCFOLIA JSONからCoC6 / 7を勝手に判定しない

Profile導入後の方向：

```text
CCFOLIA statuses
→ Mapping
→ Table / ScenarioのprofileKey
→ endStateStatuses[]
→ Preview
```

`TODO_SPEC_CONFIRMATION`：

- Mapping Configの正式Shape
- Preview API Requestの最終Shape
- profileKeyの具体的渡し方
- Tekey / Udonariumとの共通Adapter設計
- `iconUrl`をPC画像として利用する正式仕様

旧9/30版の`POST /api/ccfolia/end-state-preview`は「Endpoint例」に過ぎないため、最終Contractとして固定しない。

---

# 37. Session / CSRF / Cookie【2026-10-01更新】

Production Contract：

```text
Cookie name = __Host-TAKUKAIRO_SESSION
Secure
HttpOnly
SameSite=Lax
Path=/
Domainなし
```

Test ProfileではHTTPSでないためSecure Cookieをfalseにできるが、Production Configはtrue固定。

JWT / Session TokenをlocalStorage / IndexedDBへ保存しない。

State Changing：

- POST
- PUT
- PATCH
- DELETE

はCSRF Token必須。

```text
GET /api/csrf
```

Dev SessionでもCSRFを無効化しない。

Session timeout Production：

- Idle 7日
- Absolute 30日
- Browser終了Logoutしない
- Persistent Cookie
- 1 User最大5 Session
- Timeout判定はServer-side

**Login成功時はSession IDを変更する。**

Session Fixation対策を`DEFERRED_PRODUCTION`へ戻さない。

---

# 38. Rate Limit

実装：

- Bucket4j
- Token Bucket
- Endpoint Group
- MVP single instanceではbounded in-memory

値：

Auth IP：

- 20/min
- 60/hour

BOOTH User：

- 10/10min
- 30/hour

BOOTH IP：

- 30/10min
- 100/hour

Upload User：

- 20/10min
- 60/hour

Upload IP：

- 60/10min
- 200/hour

Import Upload User：

- 30/10min
- 60/hour

Import Heavy User：

- 10/10min
- 30/hour

Search User：

- 60/min

Search IP：

- 180/min

Other authenticated API：

- User 120/min
- IP 300/min

429 Responseに内部Bucket残数等を出さない。

Dev/TestでIntegration Test時だけRate Limit無効化Profileを作ってよい。

---

# 39. Logging

Structured Logging。

Logへ平文で出さない：

- Cookie
- Session ID
- CSRF Token
- Authorization Header
- Google Token
- R2 Credentials
- DB Password / Connection String
- Signed URL全文
- Request / Response Body
- Import原文
- Upload Binary

Security Event：

- Auth success / failure
- Rate Limit
- Ownership 403
- Upload reject
- Import重大失敗
- Account delete start / complete
- Image delete retry failure

Internal user_idは通常Access Logへ常時出さない。

---

# 40. Optimistic Lock【2026-10-01更新】

Update Requestは対象Entity / Aggregateの`version`を受ける。

JPA `OptimisticLockException` / Spring例外を：

```text
409 CONFLICT
code = OPTIMISTIC_LOCK_CONFLICT
```

へ変換する。

Frontendに最新Data再読込を促す。

Table Formは子だけの更新でも親Table.versionを増加させる。

## PC Change PL Cross-Table Lock【確定】

`change-person-context`で：

- PC.version
- Participation.version
- Table.version

を返し、保存時すべて確認する。

複数Tableを1 Transactionで処理。

TableはtableId等の一定順序で処理する。

Participation.person変更時は親Table.versionも進める。

1件でも競合：

```text
全Rollback
409 OPTIMISTIC_LOCK_CONFLICT
```

部分成功させない。

---

# 41. Validation Policy

Validation Errorは400。

業務上実行不可：409を使用可能。

例：

- ScenarioにTableがあり削除不可
- PCにParticipationがあり削除不可
- Personに参照があり削除不可

存在しない / 他User所有Resourceは、情報漏洩を避け必要に応じ404へ統一してよい。

## Quote Grapheme Validation【一旦採用】

Quote最大24文字の1文字はUnicode Extended Grapheme Clusterとして扱う。

DBは`TEXT`。

24 Grapheme制限はApplication Validation。

Java側でもExtended Grapheme Clusterとして検証し、UTF-16 code unit数や単純`String.length()`だけで判定しない。

超過時に勝手に切り捨てない。

入力文字列を勝手にUnicode正規化して別文字列へ変換しない。

---

# 42. Test Seed

Dev Seedには必ず以下の状態を含める。

## Scenario

- 画像あり
- 画像なし
- External image URLあり
- Table 0件
- Table複数
- Favorite / non-favorite

## Table

- TableDate 0 / 1 / 2 / 3+
- Participation 0
- KP 1
- KP + SKP
- PL 1 / 2 / 4 / 7+
- Recording URLあり / なし

## PC

- 画像あり
- PCあり画像なし
- Participation自体PCなし
- HO only
- Quote only
- HO + Quote
- EndPcStateあり / なし
- SURVIVED / LOST / NULL
- Growth / Aftereffects

## Person

- SelfPerson
- 同名Person
- 複数PC所有Person

## Import

- Sessionなし
- REVIEW途中
- REGISTERABLE
- NEEDS_REVIEW
- NEEDS_FIX
- 完了直前

---

# 43. Automated Test Minimum

## Repository / Constraint

- 他User Relationを拒否
- Scenario名重複可
- Person名重複可
- PC名重複可
- TableDate重複可
- Participation display order deferred reorder
- PCなしParticipation可
- PCなしEndPcState不可
- `pc.game_system`が存在しない
- 同一PC内で同じ非NULL canonicalSystemKeyのSystem Setting重複を拒否
- UNKNOWN / canonical NULLを文字列だけで自動Deduplicateしない
- `display_quote`がTEXTで、DB文字数制約へ依存しない

## Service

- Scenario delete condition
- PC delete condition
- Person delete condition
- PC Aggregate create / patch + 0..N System Settings
- systemSettings省略 / [] / full list semantics
- Canonical Resolve EXACT / SUGGESTED / UNKNOWN
- Profile / OptionSource validation
- Table aggregate create / update / delete
- PL→KP整合
- PC.current Person変更で過去Participation自動変更なし
- selected ParticipationのみPerson更新
- PL ChangeにPC Detachが混在しない
- Cross-Table PL Changeで1件競合時All Rollback
- Recent算出
- UC17集計
- Spotlight normalize
- EndPcState保存済みprofileKey維持
- Previous EndPcState Status-only Prefill用Data生成
- Quote 24 Grapheme validation

## Security

- Other user resource denied
- CSRF required
- Dev session profile only
- Session cookie HttpOnly contract
- Login成功時Session ID変更
- SelfPerson Setup Gate

## Import

- Parser per format
- Source変更後Analysis Reset
- Candidate version optimistic lock
- Bulk preview is non-destructive
- Split / Merge preview必須
- One candidate atomic registration
- Partial registration
- ImportResolutionによる重複Entity生成防止
- Idempotency
- Expired session cleanup
- Complete条件Backend検証

## Upload

- Extension spoof rejected via actual format
- too large
- too many pixels
- animated WebP rejected
- expectedVersion早期 / DB更新前再確認
- safe replacement failure keeps old image
- 新画像不採用時Cleanup / storage_delete_task fallback

---

# 44. Production Deferred

Codexは以下を勝手にProduction完成扱いしない。

```text
DEFERRED_PRODUCTION:
- Google consent / production OAuth verification
- Render production deploy
- R2 real credentials
- Account deletion tombstone
- PITR x R2 old-object retention
- Production log retention
- Terms / Privacy final text
- External image production legal decision
- vips-ffm final adoption smoke test
```

Test UI上でこれらを「正式対応済み」と表示しない。

---

# 45. DO NOT IMPLEMENT

- UC15 一括編集
- UC16 卓メモ・感想
- UC18 卓共有
- Activity Export
- Offline編集
- Read-only Offline Snapshot
- Native App
- Push Notification
- Background Sync
- PDF Import
- Google One Tap
- Auto Select
- Person専用管理画面
- SKP Role
- isMainKP
- PC.current SAN / HP / MP
- PC本体の単一`game_system`
- SystemごとのPC Entity複製
- EndPcState最大値
- PL変更Flow内のPC Detach
- Profile解決不能時のCurrent Profile自動Fallback
- CCFOLIA JSONからCoC6/7の自動判定
- Undo / Trash
- AI Import必須化

---

# 46. Implementation Order

Codexは以下の順を推奨。

1. Spring Boot Skeleton / Postgres / Flyway
2. Formal 9 Entity + Repository
3. Dev Session + Security + CSRF
4. Scenario / Person / PC
5. Table Aggregate
6. Game System Profile / EndPcState
7. Activity / Recent
8. Image Local Adapter
9. Import Temporary Model + Parser
10. Import Register
11. BOOTH Mock Adapter
12. Rate Limit / Logging
13. Integration Test
14. Frontend接続調整

---

# 47. Backend Definition of Done

テスト実装としてBackend完成とみなす条件：

- PostgreSQL起動でFlywayが0から成功する。
- Hibernate validateが通る。
- Dev固定UserでSession Loginでき、Login成功時Session IDが変更される。
- SelfPerson Setup API / Gateが動く。
- Formal 9 Entity CRUD / Ruleが動く。
- `pc.game_system`旧Columnが残らず、PC + 0..N `pc_system_setting` Aggregateが動く。
- PC共通Character Sheet URL + System専用URLの優先関係を扱える。
- Game System Profile / OptionSource / Canonical Resolve APIが動く。
- Table Aggregate全体保存が動く。
- v5.9表示に必要なTable Detail APIが返る。
- PC Focus Appearances APIが返る。
- Game System Profileに応じたEndPcStateを保存 / 読込でき、保存済みProfile Versionを維持する。
- Previous EndPcStateのStatus-only Reference Prefill用Responseを生成できる（Endpoint最終ShapeはTODO境界を維持）。
- PC Change PLがCross-Table Optimistic LockでAll-or-Nothing動作し、PC Detachを行わない。
- Import Source→Analyze→Review→Detail→Register→Completeの最新API Contractが成立する。
- ImportResolution / Candidate Version / Preview系が動く。
- Common Errorが`fieldErrors[]` + JSON Pointerで返る。
- Quote 24 Grapheme Validationが動く。
- 画像Upload / Replace / DeleteがexpectedVersion + Safe SequenceでLocal Adapter上動く。
- 他User DataへアクセスできないTestが通る。
- Optimistic Lockが409になる。
- CSRFが機能する。
- Test SeedですべてのFrontend重要状態を再現できる。
- CCFOLIAの未確定部分を勝手に固定していない。
- Production Deferred項目を実装済みと偽らない。
- `TODO_SPEC_CONFIRMATION`は未確定事項だけに限定される。

---

# 48. Codexへの最終指示

この文書を実装仕様として扱うこと。

仕様に明記されていないData属性や業務Ruleは追加しない。

実装上必要な内部Class / DTO / Support Tableは追加可能だが、Formal 9 Entityと混同しない。

2026-10-01改訂で特に禁止する逆戻り：

- `pc.game_system`を復活させる
- PL `change-person`へ`detachPcFromParticipationIds`を戻す
- Import APIを9/30簡略版へ戻す
- `fieldErrors`をObjectへ戻す
- `display_quote VARCHAR(24)`へ戻す
- Login成功時Session ID変更をDeferredへ戻す

新しいSupport Dataを作る場合は、Class / Table名に目的が分かる名前を付け、READMEへ「Formal Entityではない」と明記する。

疑義がある箇所はコードで推測解決せず、

```text
TODO_SPEC_CONFIRMATION
```

を残す。

---


# 49. 残TODO_SPEC_CONFIRMATION

2026-10-01改訂後、仕様未確定として残すもの：

1. **Previous EndPcState候補取得APIの最終Endpoint / Request Shape**
   - 候補算出Rule / Reference Prefill用Response意味は確定。
   - 9/30版Endpointをそのまま確定扱いしない。
2. **CCFOLIA Mapping Configの正式Shape**
3. **CCFOLIA Preview API Requestの最終Shape / profileKeyの渡し方**
4. **Tekey / Udonariumとの共通Adapter設計**
5. **CCFOLIA `iconUrl`をPC画像として利用するか**

【Production Deferredであり仕様TODOとは分離】

- Google production OAuth verification
- R2 real credentials / Production Signed URL
- Render production operation
- PITR等のProduction運用
- Terms / Privacy最終本文
- External Scenario ImageのProduction法務判断

上記Deferredはローカル / テスト実装開始を妨げない。
