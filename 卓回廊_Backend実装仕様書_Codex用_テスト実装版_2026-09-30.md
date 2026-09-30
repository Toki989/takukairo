# 卓回廊 Backend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-09-30

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

1. 本書
2. `卓回廊_S1-S6_BackendDataSecurityPhysicalDesign_確定事項まとめ_2026-09-30.md`
3. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-30_BackendDataSecurityPlatform_精査修正版.md`
4. `TRPG活動履歴管理Webサービス_VisualResponsiveAccessibility仕上げフェーズ_確定事項まとめ_2026-09-30.md`
5. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-29_No20-No27確定版.md`
6. `TRPG活動履歴管理Webサービス_追加確定事項まとめ_2026-09-29_修正版.md`
7. `TRPG活動履歴管理Webサービス_最新統合正本_2026-09-28_確定版_フロント制作補強修正版(3).md`
8. 各Visual / CRUD / 詳細設計資料

`TRPG_PCFocus_v5_9` やFrontend PrototypeのMock値はData仕様の根拠にしない。

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

## 7.5 pc

```text
id                   BIGINT PK
user_id              BIGINT NOT NULL
person_id            BIGINT NOT NULL
name                 VARCHAR(255) NOT NULL
game_system          VARCHAR(255) NULL
character_sheet_url  VARCHAR(2048) NULL
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

`PC.person` は現在のPerson。

過去 `Participation.person` はPC.person変更で自動更新しない。

---

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
display_quote  VARCHAR(24) NULL
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

Quoteは最大24文字。

HOには新しい文字数上限を追加しない。

---

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

Child EntityはParentを辿って所有者を確認する。

冗長 `user_id` を追加しないChild：

- TableDate
- Participation
- EndPcState

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

# 10. Game System Profile

Formal Entityへ追加しない。

MVPではVersioned Application Config。

概念：

```text
GameSystemProfile
- profileKey
- canonicalSystemKey
- aliases
- statuses[]
  - key
  - label
  - displayOrder
  - inputType
- showOutcome
- growthLabel
- aftereffectsLabel
```

EndPcStateで利用するProfileは、

> TableのScenario.gameSystem

を基準にする。

PC.gameSystemを基準にしない。

保存時点のVersioned `profile_key` をEndPcStateへ保存する。

Scenario.gameSystemが後から変わっても過去EndPcState.profile_keyを変更しない。

未知Game Systemは勝手に既知Profileへ推測Mappingしない。

未知の場合はStatus入力を無理に生成しない。

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
- PL→KP変更でPC / HO / Quote / EndPcStateが失われる場合はFrontendが確認し、Backendは最終Requestをそのまま整合Validationする。
- Edit時はTable.versionでOptimistic Lock。
- 子更新時も親Table.versionを更新する。

---

# 14. Previous EndPcState Candidate

継続PCの前回値は入力補助であり自動確定しない。

候補順：

1. TableDateがある過去Table → 最新TableDate
2. TableDateなし → Table.created_atを低精度参考順位として使用可

Table.created_atを「実際に遊んだ日」と表示しない。

APIは参考精度を返す。

例：

```json
{
  "sourceTableId": 100,
  "basis": "TABLE_DATE",
  "basisDate": "2026-09-01",
  "values": {...}
}
```

または：

```json
{
  "basis": "CREATED_AT_FALLBACK",
  "lowConfidence": true
}
```

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

# 22. Import Temporary Model

Formal 9 Entityへ追加しないSupport Data。

最低限：

```text
ImportSession
ImportSource
ImportCandidate
ImportCandidateSourceTrace
```

Concept：

### ImportSession

- user
- currentStep
- savedAt
- expiresAt
- completionState

1 Userにつき進行中最大1件。

### ImportSource

- session
- sourceType
- originalFileName nullable
- encoding
- rawText / rawStorageRef
- order

### ImportCandidate

- session
- status: REGISTERABLE | NEEDS_REVIEW | NEEDS_FIX
- checked: boolean
- selectedForRegistration: boolean
- parsed candidate structure
- registration state

### Trace

Candidate fieldとSource位置の対応を保持する。

元SourceをDETAILで比較できること。

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
- raw_text nullable      # pasted text等。File binaryをDBへ詰め込まない
- source_order

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
```

正確な `candidate_data` 内部JSON ShapeはParser / Candidate DTOと同じ型定義から管理し、自由なMapを画面ごとに増殖させない。

ImportSessionの進行中UniqueはUser単位でApplication + DB制約のどちらか安全な方法で保証する。

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

## 27.1 Common Error

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力内容を確認してください。",
  "fieldErrors": {
    "name": "シナリオ名を入力してください。"
  },
  "traceId": "..."
}
```

主なcode：

```text
VALIDATION_ERROR
NOT_FOUND
FORBIDDEN
CONFLICT
OPTIMISTIC_LOCK_CONFLICT
RATE_LIMITED
SESSION_EXPIRED
NETWORK_EXTERNAL_ERROR
UPLOAD_INVALID
IMPORT_CONFLICT
```

403で他User Resourceの存在有無を不用意に漏らさない。

---

# 28. Auth / Session API

```text
GET  /api/session
GET  /api/csrf
POST /api/logout
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

Production Google endpointはInterfaceだけ用意可能：

```text
POST /api/auth/google
```

実Production運用は今回完成条件外。

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

# 31. PC API

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

`change-person` Request概念：

```json
{
  "version": 4,
  "newPersonId": 50,
  "updateParticipationIds": [100,101],
  "detachPcFromParticipationIds": [102]
}
```

Rules：

- PC.person更新
- 選択された過去Participation.personだけ更新
- 非選択Participationは旧Personのまま
- detach対象はParticipation.pc=NULL
- detach時EndPcStateがあれば削除
- 失われるDataはFrontendで事前確認
- すべてTransaction

---

# 32. Table API

```text
POST   /api/scenarios/{scenarioId}/tables
GET    /api/tables/{tableId}
PUT    /api/tables/{tableId}
DELETE /api/tables/{tableId}
GET    /api/tables/{tableId}/detail
GET    /api/tables/{tableId}/previous-end-state?pcId={pcId}
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

---

# 33. Activity API

```text
GET /api/activity/summary
GET /api/activity/by-month?year=2026
GET /api/activity/by-year
```

SelfPerson基準。

---

# 34. Import API

```text
GET    /api/import/session/current
POST   /api/import/session
DELETE /api/import/session/{sessionId}
POST   /api/import/session/{sessionId}/analyze
GET    /api/import/session/{sessionId}/review
PATCH  /api/import/session/{sessionId}/candidates/{candidateId}
POST   /api/import/session/{sessionId}/candidates/{candidateId}/split
POST   /api/import/session/{sessionId}/candidates/merge
POST   /api/import/session/{sessionId}/bulk-apply
POST   /api/import/session/{sessionId}/register
POST   /api/import/session/{sessionId}/complete
```

Auto SaveはCandidate / Session PatchへDebounceで送る。

保存失敗時もFrontend Draftを失わせない。

---

# 35. Image API

PC / ScenarioのCreate / Update時、FrontendはFileをBrowser側Draftとして保持し、**Metadata保存後に専用Image Endpointへ送る2段階方式**で統一する。

今回のテスト実装ではmultipart Aggregate endpointを併用しない。

```text
1. Scenario / PC MetadataをJSONで保存
2. Entity IDを受け取る
3. Pending image changeがある場合だけ専用Image Endpointへ送信
4. Image成功後に画面全体のSave完了とする
```

画像だけ失敗した場合：

- Metadata保存成功をRollbackしたふりをしない
- 「基本情報は保存されましたが、画像の保存に失敗しました」と返せる状態にする
- FrontendがFile Draftを保持し、画像だけ再試行できる
- 既存画像差し替えでは旧画像を維持する

Endpoint：

```text
PUT    /api/pcs/{pcId}/image
DELETE /api/pcs/{pcId}/image
PUT    /api/scenarios/{scenarioId}/image
DELETE /api/scenarios/{scenarioId}/image
PATCH  /api/pcs/{pcId}/image-transform
```

`image-transform`：

```json
{
  "positionX": 0.12,
  "positionY": -0.08,
  "zoom": 1.15,
  "version": 4
}
```

Scenario画像にはPC用Position/Zoomを勝手に追加しない。

---

# 36. CCFOLIA Input Assistance

Table EndPcState入力補助としてClipboard JSONを解析する。

Endpoint例：

```text
POST /api/ccfolia/end-state-preview
```

CoC系ではStatusから主に：

- SAN
- HP
- MP

の `value` を候補抽出。

保存しない：

- CCFOLIA JSONそのもの
- Skills
- Ability
- Memo
- Chat Palette
- Image
- その他CCFOLIA固有Data

解析失敗でTable保存自体を失敗させない。

---

# 37. Session / CSRF / Cookie

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

Dev SessionでもCSRFを無効化しない。

Session timeout Production target：

- Idle 7日
- Absolute 30日
- Browser終了Logoutしない
- 1 User最大5 Session

`DEFERRED_PRODUCTION`：Session ID Renewal追加有無。

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

# 40. Optimistic Lock

Update Requestは対象Entity / Aggregateの `version` を受ける。

JPA `OptimisticLockException` / Spring例外を：

```text
409 CONFLICT
code = OPTIMISTIC_LOCK_CONFLICT
```

へ変換する。

Frontendに最新Data再読込を促す。

Table Formは子だけの更新でも親Table.versionを増加させる。

---

# 41. Validation Policy

Validation Errorは400。

業務上実行不可：409を使用可能。

例：

- ScenarioにTableがあり削除不可
- PCにParticipationがあり削除不可
- Personに参照があり削除不可

存在しない / 他User所有Resourceは、情報漏洩を避け必要に応じ404へ統一してよい。

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

## Service

- Scenario delete condition
- PC delete condition
- Person delete condition
- Table aggregate create / update / delete
- PL→KP整合
- PC detach + EndPcState delete
- PC.current Person変更で過去Participation自動変更なし
- selected ParticipationのみPerson更新
- Recent算出
- UC17集計
- Spotlight normalize
- Game System Profile validation

## Security

- Other user resource denied
- CSRF required
- Dev session profile only
- Session cookie HttpOnly contract

## Import

- Parser per format
- One candidate atomic registration
- Partial registration
- Idempotency
- Expired session cleanup

## Upload

- Extension spoof rejected via actual format
- too large
- too many pixels
- animated WebP rejected
- safe replacement failure keeps old image

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
- EndPcState最大値
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
- Dev固定UserでSession Loginできる。
- Formal 9 Entity CRUD / Ruleが動く。
- Table Aggregate全体保存が動く。
- v5.9表示に必要なTable Detail APIが返る。
- PC Focus Appearances APIが返る。
- Game System Profileに応じたEndPcStateを保存 / 読込できる。
- Import INPUT→REVIEW→DETAIL→REGISTERのAPI Contractが成立する。
- 画像Upload / Replace / DeleteがLocal Adapterで動く。
- 他User DataへアクセスできないTestが通る。
- Optimistic Lockが409になる。
- CSRFが機能する。
- Test SeedですべてのFrontend重要状態を再現できる。
- Production Deferred項目を実装済みと偽らない。

---

# 48. Codexへの最終指示

この文書を実装仕様として扱うこと。

仕様に明記されていないData属性や業務Ruleは追加しない。

実装上必要な内部Class / DTO / Support Tableは追加可能だが、Formal 9 Entityと混同しない。

新しいSupport Dataを作る場合は、Class / Table名に目的が分かる名前を付け、READMEへ「Formal Entityではない」と明記する。

疑義がある箇所はコードで推測解決せず、

```text
TODO_SPEC_CONFIRMATION
```

を残す。
