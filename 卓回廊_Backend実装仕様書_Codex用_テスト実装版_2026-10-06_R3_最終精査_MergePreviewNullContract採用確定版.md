# 卓回廊 Backend実装仕様書
## Codex用・ユーザーなしテスト実装版
### 2026-10-06 横断精査修正版 R3 / 最終精査追補 / Merge Preview Null Contract採用確定

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

1. 本書（2026-10-06横断精査修正版 R3 追加ImportContract確定版）
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

- **【確定】**：ユーザーが明示採用済み、または既存確定資料から直接確認できる事項。
- **【確定・復元】**：過去資料で確定済みだったが、新仕様書への転記から落ちていたため戻した事項。
- **【一旦採用】**：実装Contractを閉じるため新たに具体化したが、ユーザーの正式採用前である事項。
- **【TODO_SPEC_CONFIRMATION】**：資料から確定できず、ユーザー判断が必要な事項。
- **【資料照合残件】**：機能仕様自体は確定済みで、歴史的資料・旧訂正履歴等の完全照合だけが残る事項。

`資料照合残件`だけを理由に機能実装をHOLDしない。

## 1.1 今回の改訂基準【2026-10-06】

本版は **2026-10-01再改訂版を直接の比較元** とする。9/30版へ巻き戻して再作成しない。

比較元SHA-256：

```text
1a28ed9e66200d924049b0eca183989d78bb94fbcc559a7e18ed208f8704f01b
```

改訂記録時刻：`2026-10-06T10:34:09+09:00`。

区分：

- **【確定・復元】**：既存資料 / ユーザー監査で確定済みと確認できるもの。
- **【一旦採用・2026-10-06提案】**：Frontend / Backendを対で実装できるよう今回具体化するHTTP / DTO / Lock Contract。明示承認前は既存確定仕様と呼ばない。
- **【未確定】**：資料から決められず、実装都合で推測しないもの。

旧記述 / 訂正後 / 根拠 / 採用区分 / 修正日時は末尾Appendixと同日作成の横断精査ファイルへ残す。

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

## 9.2 Resource Membership / Parent-Child Boundary【一旦採用・2026-10-06 Contract補完】

**Current User所有確認だけでは不十分。** 同じUserが所有する別Aggregate / 別SessionのIDを混在させないよう、Path Parentと送信Childの所属関係を必ず検証する。

最低限：

- Table Aggregate Updateで既存`TableDate.id`を送る場合、その`table_date.table_id`が対象`tableId`と一致すること。
- 既存`Participation.id`は対象`tableId`に属すること。
- 既存`EndPcState.id`はRequest内で対応するParticipationに属すること。
- PC PATCH内の既存`PC System Setting.id`は対象`pcId`に属すること。別PCのSetting IDを付け替えない。
- `change-person`で指定するParticipationは、対象`pcId`のChange Contextとして返した関連Participationであり、各`tableId` / `tableVersion`もそのParticipationの実Parentと一致すること。
- Import Source / Candidate / ResolutionはURLの`sessionId`に属すること。
- Bulk / Split / Merge / Registerで渡すCandidate群は全件同一URL Sessionに属すること。
- ImportResolutionの`existingEntityId`はCurrent User所有の対応Entity Typeであること。

同一User所有であっても所属が異なる既存Child IDを「移動」と解釈しない。新規Childは`id=null` / ID省略で作る。

Path ParentとChild IDが不一致、または他User Resourceの場合は、存在情報を漏らさないため原則`404 NOT_FOUND`へ正規化してよい。Version一致はMembership検証の代替にならない。

---

# 10. Game System Profile / OptionSource【確定概念 + 2026-10-06 Contract具体化】

Formal Entityへ追加しない。MVPではVersioned Application Config。

## 10.1 Game System Profile Concept【確定】

- `profileKey`はVersionを含む安定Key。例：`coc_7e_v1`。
- `canonicalSystemKey`は既知Systemの内部Key。
- Canonical SystemごとにActive Profile最大1件。
- 旧Profileは過去Dataが参照している限り削除しない。
- FrontendへSystem別React Logicをハードコードさせない。
- PC System Settingの`profileValues`とEndPcStateのStatusはProfile定義でValidationする。

## 10.2 Profile JSON Schema【一旦採用・2026-10-06提案】

Generic Renderer / ValidatorをFrontend / Backendで同じ意味にするため、full definitionを次のShapeへ固定する。

```json
{
  "profileKey": "coc_7e_v1",
  "canonicalSystemKey": "coc_7e",
  "displayName": "クトゥルフ神話TRPG 7版",
  "active": true,
  "configRevision": 1,
  "pcFields": [],
  "endStateStatuses": [
    {"key": "san", "label": "SAN", "displayOrder": 10, "required": false},
    {"key": "hp",  "label": "HP",  "displayOrder": 20, "required": false},
    {"key": "mp",  "label": "MP",  "displayOrder": 30, "required": false}
  ],
  "showOutcome": true,
  "growthLabel": "成長",
  "aftereffectsLabel": "後遺症",
  "optionSources": []
}
```

### PC Field Definition

```json
{
  "key": "field_key",
  "label": "表示名",
  "inputType": "TEXT",
  "required": false,
  "displayOrder": 10,
  "validation": {"maxLength": 255},
  "optionSourceRef": null,
  "itemFields": null
}
```

`inputType`：

```text
TEXT
INTEGER
SELECT
BOOLEAN
REPEATER
```

`MULTI_SELECT`は将来候補でMVP Schemaへ入れない。

Field `key`はProfile内で一意かつ保存Keyとして安定させる。MVP実装ではASCII lower snake case（`^[a-z][a-z0-9_]{0,63}$`）を使用する。既存Keyの意味を破壊的変更・再利用しない。

`validation`で扱う値：

- TEXT：`maxLength` optional
- INTEGER：`min` / `max` optional。未指定時に卓回廊共通上限/下限を勝手に追加しない。
- SELECT：`optionSourceRef` required
- BOOLEAN：追加Validationなし
- REPEATER：`minItems` / `maxItems` optional + `itemFields` required

MVPのREPEATERは1段だけ。`itemFields`内でさらにREPEATERを入れない。Frontend上のReact key用一時IDは保存JSONへ含めない。

### OptionSource Definition

SELECTはLabelではなく安定Option Keyを保存する。

```json
{
  "sourceKey": "example_options",
  "revision": 1,
  "options": [
    {
      "key": "option_a",
      "label": "Option A",
      "group": null,
      "displayOrder": 10,
      "selectable": true
    }
  ]
}
```

Profile Fieldの参照：

```json
{
  "optionSourceRef": {"sourceKey": "example_options", "revision": 1}
}
```

使用済みOption Keyを削除・再利用しない。廃止Optionは`selectable=false`で過去Dataを表示可能にする。新規選択では`selectable=true`のみを候補にする。既存保存値が廃止Keyの場合は表示・保持できる。

### profileValues Validation

- Profileに存在しないKeyは拒否。
- `required=true`の欠落はValidation Error。
- TEXT / INTEGER / BOOLEAN / SELECT / REPEATERのJSON型を厳密検証。
- SELECTはOption Keyを検証。
- REPEATER各Itemは`itemFields`にないKeyを拒否。
- `profileValues`をLabel文字列で保存しない。

### EndState Status Definition

MVPの`endStateStatuses[]`はNullable Integer Status。負数可、小数不可。サービス共通のmin/maxは設けない。Profileが明示した場合だけSystem固有min/maxを検証可能とする。

`growthLabel` / `aftereffectsLabel`は表示Label。Growth / Aftereffects本体は既存仕様どおり任意Text最大500。`showOutcome`がtrueの場合だけ`SURVIVED / LOST / NULL`を表示・保存対象にする。

### 初期Profile集合の扱い【未確定を明示】

資料から正式に確定できる「MVP初期Active Profile一覧」は存在しない。`coc_7e_v1`は既存資料で使われている具体例であるため **Test Fixtureとして最低1件用意してよい** が、これを「MVPで対応するSystemはCoC7だけ」または「初期対応一覧の全て」と解釈しない。

したがって：

- Generic Profile Engine / API / Validator → 実装可。
- Test Fixture `coc_7e_v1` → 実装可。
- MVP初期Active Profile集合と各System固有`pcFields` / OptionSource実データ → `TODO_SPEC_CONFIRMATION`。

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

# 13. Table Aggregate Save【2026-10-01再監査で整合補強】

Table登録 / 編集は画面全体で最後に1回保存する。

BackendでもAggregate Transactionとして扱う。

Request概念：

```json
{
  "version": 3,
  "scenarioId": 10,
  "tableName": null,
  "tableDates": [
    {"id": 501, "playedOn": "2026-09-20"},
    {"id": null, "playedOn": "2026-09-21"}
  ],
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
      "id": 700,
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

TableDateはObject配列で統一する。

- Create時：`id`省略 / `null`
- Edit時：既存TableDateは`id`を保持
- `playedOn`必須
- Validation JSON Pointer：`/tableDates/{index}/playedOn`

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

### Participation.pc変更時のEndPcState【既存確定仕様の再転記】

既存Participationの保存済みPC参照と今回Requestの`pcId`を比較する。

#### PC-A → PC-B

`oldPcId != null && newPcId != null && oldPcId != newPcId`の場合：

- PC-Aに属していた既存EndPcStateをPC-Bへ付け替えない。
- 既存EndPcStateは同一Table Aggregate Transaction内で削除する。
- RequestにPC-B用`endPcState`が存在する場合は、PC-Bの今回状態として新規Validation / 保存する。
- Requestに存在しなければPC-BのEndPcStateは作らない。

#### PC解除

`oldPcId != null && newPcId == null`の場合：

- 既存EndPcStateを同一Transaction内で削除する。
- EndPcStateを残したままPCだけNULLにする状態を許可しない。

Frontendはデータ消失前確認を行うが、Backendも最終状態の整合性を必ず保証する。

PL変更APIではこのPC変更 / 解除処理を行わない。

### EndPcState Profile Validation【確定】

新規EndPcStateでは、BackendがTableのScenario.gameSystemからProfileを解決して保存する。

Frontendが`profileKey`を送る場合も任意のProfile選択権として信用せず、Scenario基準と一致するか検証する。

既存EndPcState編集では保存済み`profile_key`を使用する。

Scenario.gameSystem変更を理由に既存EndPcStateのProfileを自動Migrationしない。

Profile解決不能時にActive Profileへ勝手にFallbackしない。

# 14. Previous EndPcState Reference Prefill【2026-10-01再監査で条件補強】

PC本体へ現在SAN / HP / MP等を持たせない。

過去EndPcStateは新規Table入力の補助値。

### Candidate対象

- 同一User所有Data
- 同一PC
- `sourceTableId != targetTableId`。編集中Table自身を必ず除外する。
- TargetのEndPcState用`profileKey`と**完全一致する保存済みprofileKey**のみ自動Prefill候補にする。
- 別Game System / 別Profile VersionのStatusを自動Mappingしない。

### Target TableDateがある場合

Targetの最も早いTableDateを`targetCutoffDate`とする。

Source Tableに複数TableDateがある場合は、そのSourceの最も新しいTableDateを`sourceBasisDate`とする。

```text
sourceBasisDate < targetCutoffDate
```

を満たす履歴だけを「対象卓より前」とみなす。

その中で`sourceBasisDate`が最も新しいEndPcStateを候補にする。

### Target TableDateがない場合

時系列上「対象卓より前」を確定できないため、同一PC / 同一profileKeyの他履歴を参考順位で提示してよいが、必ず`lowConfidence=true`とする。

順位は：

1. TableDateがある他Table → 最新TableDate
2. TableDateがない他Table → Table.created_at

Table.created_atを「実際に遊んだ日」と表示しない。

### Response意味

Responseは参考精度と、`endStateStatuses[]`に対応するStatus値だけを返す。

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

Frontendは別Read Only欄として今回値と競合させず、今回EndPcStateが未保存 / 未入力のStatus FieldへのReference Prefillにだけ使う。

引き継ぐのは`endStateStatuses[]`のみ。

以下は引き継がない：

- growth
- outcome
- aftereffects

既存Table編集で今回EndPcStateが保存済みなら、その保存値を優先しPrevious値で上書きしない。

`TODO_SPEC_CONFIRMATION`：候補取得APIの最終Endpoint / Request Shapeは未確定。現行の`GET /api/tables/{tableId}/previous-end-state?pcId=...`を確定Endpointとして実装しない。

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

# 16. UC17 Activity Summary【2026-10-01再監査で集計意味固定】

SelfPerson基準。

ユーザー向けの「PL参加回数」「KP参加回数」は**Participation行数ではなく、Roleごとのdistinct Table数**として数える。

理由：同一Personが同一Tableで複数PCを担当して複数Participationを持っても、その卓へのPL参加は1回として見せるため。

```text
PL参加回数 = COUNT(DISTINCT table_id)
  WHERE person_id = selfPersonId AND role = PL

KP参加回数 = COUNT(DISTINCT table_id)
  WHERE person_id = selfPersonId AND role = KP
```

同じTableでSelfPersonがPLとKPの両Roleを持つ場合：

- PL参加回数 +1
- KP参加回数 +1
- 参加Table数は +1

Scenario数もSelfPersonが参加したTableの`scenario_id`をdistinctで数える。

月別 / 年別も同じdistinct意味を維持し、既存のAggregation Date Ruleを利用する。

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

`registration_status = REGISTERED`のCandidateはImport側から再編集しない。Candidate PATCH / Bulk / Split / Merge対象外とし、修正は正式Data側の編集機能で行う。

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

# 25. Import Session Lifecycle【2026-10-01再監査で安全境界補強】

- STEP2 / STEP3 Auto Save
- 最終保存から7日保持
- 保存成功ごとにexpiresAt延長
- 新規Import開始時、既存ACTIVE Sessionがあれば明示破棄確認
- 正式登録可能Candidateのみ一部登録可
- 未処理Candidateが残ればSession継続
- 正式登録済みCandidateはImport側から再編集しない
- 全Candidate登録 or 残りを明示EXCLUDEDでComplete
- Complete後Raw Import Dataは48時間保持後削除
- Account削除時は48時間を待たない

### Source Mutation / Analysis Reset Lock

Session内に`REGISTERED` Candidateが**1件でも存在した後**は：

- Source追加禁止
- Source編集禁止
- Source削除禁止
- Analysis Reset禁止

とする。

理由：Partial Register後にCandidate / Trace / Resolutionを再生成し、すでに正式登録したDataとの対応関係・Idempotency情報を失わないため。

該当Requestは：

```text
409 IMPORT_SOURCE_LOCKED_AFTER_REGISTRATION
```

を返す。

Source追加 / 編集 / 削除が許可される段階では、そのSource mutationと未登録解析状態の無効化を**1つのBackend論理操作**として扱う。

DB上のSource更新・Candidate / Trace / 未登録Resolution削除は同一Transactionで確定する。

File Object Storageを伴う場合は、Staging + DB Commit後採用 / 失敗時補償削除により、Frontendから見た原子性を維持する。

Frontendに「Source mutation成功 → 別Requestでreset」という中間不整合状態を作らせない。

`POST /analysis/reset`は、REGISTERED Candidateが0件のSessionでユーザーが明示的に解析結果だけを捨てる場合に使用する。

この条件により、Resetが正式登録結果を消すことはない。

ImportResolutionの`createdEntityId`、Candidateの`registrationResult`等、Partial Registerの二重登録防止情報はSession Cleanupまで維持する。

Cleanup Job：

- 原則1時間ごと
- Idempotent
- Retry可能
- 正式9 EntityはCleanup対象外

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
IMPORT_SOURCE_LOCKED_AFTER_REGISTRATION
IMPORT_ANALYSIS_LOCKED_AFTER_REGISTRATION
IMPORT_CANDIDATE_VERSION_CONFLICT
IMPORT_CANDIDATE_REGISTERED
IMPORT_PREVIEW_STALE
SELF_PERSON_SETUP_REQUIRED
RATE_LIMITED
NETWORK_EXTERNAL_ERROR
INTERNAL_ERROR
```

`traceId`を維持。

`context`は必要時のみ。

他User Resource情報を`context`へ漏らさない。

ImportのCandidate別Business Validation FailureはHTTP Request全体Errorではなく正常Response内Resultとして扱う。ただしSession / Candidate Version競合は処理開始前のHTTP 409であり、Partial Register Failureへ含めない。

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
    "selfPerson": {"id": 10, "displayName": "テストPL"},
    "externalImageConsentGiven": false
  }
}
```

SelfPersonなしの場合は`selfPerson: null`。`externalImageConsentGiven`は`app_user.external_image_consent_at != null`の派生booleanであり、Timestamp自体をFrontendへ返す必要はない。

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

## 28.1 CSRF Endpoint Contract / Lifecycle【2026-10-01再監査で確定】

`GET /api/csrf`は未認証でも許可する。

Responseを明示DTOで固定する。

```json
{
  "token": "...",
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf"
}
```

`headerName`はSpring Securityの`CsrfToken.headerName`を返し、Frontendはその値を使用する。

State Changing Request：

```text
POST / PUT / PATCH / DELETE
```

は現在TokenをResponseで指定されたHeader名に設定する。

Lifecycle：

```text
未認証App初期化
→ GET /api/csrf
→ POST /api/dev/session または POST /api/auth/google
→ 認証成功 + Session ID変更
→ 旧CSRF Token破棄
→ GET /api/csrf でFresh Token取得
→ POST /api/self-person 等
```

Logout：

```text
Current Tokenで POST /api/logout
→ Logout成功
→ 旧Token無効
→ Login画面を継続するなら GET /api/csrf を再取得
```

Session Expired後の再認証でもFresh Tokenを取得する。

旧Token付きUnsafe RequestをBackend / Frontendのどちらも自動再送しない。

Spring Securityの認証成功 / Logout成功で以前のCSRF Tokenがclearされる前提でTestする。

# 28.2 Game System Config API【確定】

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

## 29.0 External Image Consent連携【確定復元 + HTTP Contract一旦採用】

既存確定事項：

- 外部画像の説明・同意は、UserがExternal Image CandidateをScenario画像として正式採用する最初の1回だけ。
- 同意済みUserへScenarioごとに繰り返さない。
- 未採用CandidateはForm内一時Stateだけで、Scenario / DB Draft / localStorage / IndexedDBへ保存しない。
- `app_user.external_image_consent_at`でUser単位の同意済み状態を保持する。

HTTP連携は2026-10-06一旦採用として次で固定する。Scenario Create / Patchで、未同意UserがExternal Image URLを正式採用して保存する場合だけcommand-only fieldを送る。

```json
{
  "externalImageUrl": "https://example.invalid/image.jpg",
  "externalImageConsentAccepted": true
}
```

Backend Rule：

1. Current Userの`external_image_consent_at`を確認。
2. 既同意なら`externalImageConsentAccepted`は不要。
3. 未同意かつExternal Imageを正式採用する保存では`externalImageConsentAccepted == true`を要求。
4. Scenario保存と`external_image_consent_at`初回設定を同一TransactionでCommitする。
5. このcommand fieldはScenario属性へ保存しない。
6. User Upload画像やExternal Imageを採用しない保存で同意を要求しない。

`GET /api/session`の`user.externalImageConsentGiven`でFrontendへ同意済み状態を返す。

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
participation=PL&participation=KP
favorite=true|false
sort=RECENT_PLAYED|NAME|CREATED_DESC|CREATED_ASC
```

`participation`はrepeatable query parameterとして`List<Role>`で受ける。

Role条件内はOR。

- `participation=PL` → SelfPersonがPL参加したScenario
- `participation=KP` → SelfPersonがKP参加したScenario
- `participation=PL&participation=KP` → PL **または** KPで参加したScenario
- parameter省略 → Role Filterなし

`favorite` / `search`等の別カテゴリ条件とはANDで組み合わせる。

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

Response：

```json
{
  "relatedPcCount": 3,
"relatedTableCount": 12
}
```

`relatedTableCount`は`COUNT(DISTINCT participation.table_id)`。

UIの「過去の卓N件」とraw Participation件数を混同しない。削除可否判定自体はParticipationの存在有無で行う。

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

# 33. Activity API【2026-10-01再監査で集計Contract補強】

```text
GET /api/activity/summary
GET /api/activity/by-month?year=2026
GET /api/activity/by-year
```

SelfPerson基準。

Summary概念：

```json
{
  "scenarioCount": 12,
  "tableCount": 20,
  "plParticipationCount": 15,
  "kpParticipationCount": 6
}
```

名称は互換上`plParticipationCount` / `kpParticipationCount`を使用してもよいが、**意味はRole別distinct Table count**とする。raw Participation row countではない。

同一Tableで同一SelfPersonが同RoleのParticipationを複数持っても1回。

月別 / 年別も同じ意味を維持する。

# 34. Import API【2026-10-06横断精査・Contract補完版】

## 34.0 ImportSession Mutation Protocol【一旦採用・2026-10-06提案】

ImportはCandidate Versionだけでなく、**Session配下の解析Graph全体**を同時編集するため、全Mutationで共通Protocolを使う。

### 原則

- ImportSession配下を変更するRequestは`expectedSessionVersion`を必須とする。
- BackendはTransaction開始時にCurrent User所有のImportSessionを取得し、DB Row Lock（`SELECT ... FOR UPDATE`相当）を取得する。
- Lock取得後に`session.version == expectedSessionVersion`を検証する。
- Source / Candidate / Resolution等のMembershipを同Transaction内で検証する。
- Mutation成功時は`session.version`を1進め、Responseで最新`sessionVersion`を返す。
- FrontendはそのVersionを次Mutationへ引き継ぐ。
- Session Version不一致は原則`409 OPTIMISTIC_LOCK_CONFLICT`。自動再試行しない。
- **例外：Bulk / Split / MergeのApply Endpointは有効な`previewRevision`に紐づくSnapshotを再検証するため、Preview後にSession / Candidate / Resolutionが変化していた場合は`409 IMPORT_PREVIEW_STALE`を優先する。** Preview作成Request自体の開始時点で`expectedSessionVersion`が古い場合は通常どおり`OPTIMISTIC_LOCK_CONFLICT`。

この共通Lockにより、Source/Reset/Analyze/Register等の別HTTP Requestが同時に条件判定してすり抜けることを防ぐ。

### RegisterのPartial Failure

RegisterはSession Row LockをRequestの論理Mutation中保持する。各Candidateは **Candidate単位でFormal Dataを中途半端に残さない** 必要があり、Business Validation / Candidate単位失敗をSavepoint / Nested Transaction等で隔離してよい。

ここでいう「Candidate単位Transaction」はCandidateごとのAtomicityを要求する意味であり、実装方式として別Physical Commitを強制しない。Request全体の致命的DB障害では全体Rollbackを許容する。通常のCandidate別Validation Failureは他Candidateの成功を妨げない。

### Register / Reset競合の期待結果

- Reset/Analyzeが先にLockして成功 → Session Versionが進む → 古いVersionのRegisterは409で開始しない。
- Registerが先にLockしてCandidateをREGISTERED化 → 後続Reset/AnalyzeはREGISTERED検出で拒否。

単一Transaction内の原子性と、別Request間の競合防止をこのProtocolで分離して扱う。

## 34.1 Session

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

Session PATCHでFrontendが変更できるのは`currentStep`のみ。

```json
{
  "expectedSessionVersion": 7,
  "currentStep": "REVIEW"
}
```

`status` / `savedAt` / `expiresAt` / completion stateはBackend-owned。

Session DELETEも競合を避けるため：

```text
DELETE /api/import/session/{id}?expectedSessionVersion={version}
```

とし、34.0のSession Lock / Version検証を行う。

## 34.2 Source / Reset

```text
POST   /api/import/session/{id}/sources/files
POST   /api/import/session/{id}/sources/text
PATCH  /api/import/session/{id}/sources/{sourceId}
DELETE /api/import/session/{id}/sources/{sourceId}
POST   /api/import/session/{id}/analysis/reset
```

Source mutationには`expectedSessionVersion`を要求し、34.0のSession Row Lock / Version検証を行う。

Source Request Shape【一旦採用】：

```text
POST /api/import/session/{id}/sources/files
Content-Type: multipart/form-data
parts: files (1..N), expectedSessionVersion
```

```text
POST /api/import/session/{id}/sources/text
{
  "expectedSessionVersion": 3,
  "text": "..."
}
```

`PATCH /sources/{sourceId}`はPASTED_TEXTの`text`更新と`order`更新に使用できる。FILE Sourceのbinary差替えはPATCHせず、新Source追加 + 旧Source削除として扱う。

```json
{
  "expectedSessionVersion": 4,
  "text": "...",
  "order": 2
}
```

DELETEはQueryでVersionを渡す。

```text
DELETE /api/import/session/{id}/sources/{sourceId}?expectedSessionVersion=5
```

REGISTERED Candidateが1件でもある場合、Source mutation / Resetは：

```text
409 IMPORT_SOURCE_LOCKED_AFTER_REGISTRATION
```

Source mutationが成功する場合、未登録Candidate / Trace / 未登録Resolutionの解析状態無効化までBackendがAtomicに実行する。

Frontendへ「Sourceだけ変更済み、旧Candidateがまだ残る」状態を返さない。

`analysis/reset`はREGISTERED Candidate 0件のSessionだけ許可する。Request：

```json
{
  "expectedSessionVersion": 6
}
```

判定とResetは同じSession Lock Transaction内で行う。

## 34.3 Analysis

```text
POST /api/import/session/{id}/analyze
```

Candidate StatusはBackendが判定。

Frontendから`REGISTERABLE`等を設定させない。

Analyze Request：

```json
{
  "expectedSessionVersion": 7
}
```

Session Versionを検証する。さらにREGISTERED Candidateが1件でも存在する場合、再Analyzeは行わず：

```text
409 IMPORT_ANALYSIS_LOCKED_AFTER_REGISTRATION
```

とする。一部登録後は既存の未登録CandidateをReview / Detail / Registerするが、解析Graph自体は再生成しない。

## 34.4 Review

```text
GET /api/import/session/{id}/review
```

Filter / Pagination対応。

Response概念：

```json
{
  "sessionId": 1,
  "sessionVersion": 8,
  "summary": {
    "total": 20,
    "registerable": 12,
    "needsReview": 5,
    "needsFix": 3,
    "checked": 14
  },
  "items": [
    {
      "candidateId": 11,
      "version": 5,
      "status": "REGISTERABLE",
      "confirmationStatus": "CHECKED",
      "registrationTarget": true,
      "registrationStatus": "PENDING",
      "summary": {}
    }
  ]
}
```

## 34.5 Candidate PATCH / EXCLUDED Transition【一旦採用・2026-10-06 DTO補完】

```text
GET   /api/import/session/{id}/candidates/{candidateId}
PATCH /api/import/session/{id}/candidates/{candidateId}
GET   /api/import/session/{id}/resolutions
```

CandidateはURL Sessionに属することを検証する。

### candidateData共通Shape

MVP Importで推定対象として確定している項目を次のDraftに統一する。

```json
{
  "scenarioName": "狂気山脈",
  "gameSystemName": "クトゥルフ神話TRPG 7版",
  "tableName": null,
  "tableDates": [{"playedOn": "2026-06-20"}],
  "recordingUrl": null,
  "participations": [
    {
      "candidateParticipationKey": "p-1",
      "personName": "土岐",
      "role": "PL",
      "pcName": "五色 探",
      "handout": "HO1"
    }
  ],
  "unassignedValues": [
    {
      "itemKey": "u-1",
      "rawValue": "A",
      "assignment": {"type": "UNASSIGNED", "candidateParticipationKey": null}
    }
  ]
}
```

`candidateParticipationKey` / `itemKey`はCandidate内だけのstable temporary keyでFormal Entity IDではない。

`assignment.type`：

```text
UNASSIGNED
PARTICIPATION_PERSON
PARTICIPATION_PC
PARTICIPATION_HANDOUT
IGNORE
```

正式登録可能判定では、未割当値をシステム判断で捨てない。`UNASSIGNED`が残る場合は必要に応じNEEDS_REVIEW / NEEDS_FIXとする。

### ResolutionはcandidateDataへ混ぜない

既存Scenario / Person / PCを再利用する判断はImportResolutionで保持する。CandidateからはSlotで参照する。

Slot：

```text
{"type":"SCENARIO"}
{"type":"PERSON","candidateParticipationKey":"p-1"}
{"type":"PC","candidateParticipationKey":"p-1"}
```

PATCH Request：

```json
{
  "expectedSessionVersion": 8,
  "expectedVersion": 5,
  "candidateData": {},
  "confirmationStatus": "CHECKED",
  "registrationTarget": true,
  "resolutionChanges": [
    {
      "slot": {"type":"SCENARIO"},
      "decision": "REUSE_EXISTING",
      "existingEntityId": 100
    },
    {
      "slot": {"type":"PERSON", "candidateParticipationKey":"p-1"},
      "decision": "CREATE_NEW",
      "draftData": {"displayName":"土岐"}
    }
  ]
}
```

`REUSE_EXISTING`では`existingEntityId`を要求し、`CREATE_NEW`ではentity別`draftData`を要求する。`useResolutionId`形式とは排他的。

省略Fieldは変更しない。`resolutionChanges`は省略可。

Frontendが編集可能：

- `candidateData`の上記Draft部分
- `confirmationStatus`
- `registrationTarget`
- 明示した`resolutionChanges`

Backend-owned：

- classification `status`
- `registrationStatus`
- `registrationResult`
- Source Trace
- Backend Warning / inference metadata
- `resolutionRefs`のID採番

`registrationTarget=false`は明示除外として`registrationStatus=EXCLUDED`へ。未完了・未登録ならtrueへ戻して`PENDING`へ戻せる。

REGISTERED CandidateへのPATCH：`409 IMPORT_CANDIDATE_REGISTERED`。

前回登録試行で`registrationStatus=FAILED`のCandidateをユーザーが編集・Resolution変更した場合は、旧`registrationResult`を履歴/監査用Support情報として必要範囲保持しつつ、現在状態を`PENDING`へ戻して再確認可能にする。編集なしでの明示Retryも許可する。

Candidate Version不一致 / Session Version不一致は409。Backendはユーザー未確認の最新版へPatchを当てない。

## 34.6 Candidate Detail / Source Trace

Candidate GETは原文比較に必要なTraceを返す。

```json
{
  "candidate": {
    "id": 11,
    "version": 5,
    "candidateData": {},
    "status": "NEEDS_REVIEW",
"confirmationStatus": "UNCHECKED",
    "registrationTarget": true,
    "registrationStatus": "PENDING",
    "resolutionRefs": {
      "scenarioResolutionId": 71,
      "participations": [
        {
          "candidateParticipationKey": "p-1",
          "personResolutionId": 72,
          "pcResolutionId": 73
        }
      ]
    }
  },
  "sourceContexts": [
    {
      "sourceId": 3,
      "sourceType": "FILE",
      "originalFileName": "history.md",
      "traces": [
        {
          "fieldPath": "/table/scenarioName",
          "lineStart": 12,
          "lineEnd": 14,
          "excerpt": "..."
        }
      ]
    }
  ]
}
```

### Resolution再開復元【確定・2026-10-06採用】

Candidate Detail Responseは`resolutionRefs`のIDだけで終わらせず、そのCandidateが参照するResolution DTOを`resolutions[]`へ同梱する。Reload / 再開後はDB上のImportResolutionを正とする。

```json
{
  "id": 71,
  "version": 2,
  "entityType": "SCENARIO",
  "decision": "CREATE_NEW",
  "existingEntityId": null,
  "createdEntityId": null,
  "draftData": {"name": "狂気山脈", "gameSystem": "クトゥルフ神話TRPG 7版"}
}
```

Entity別`draftData`：

- SCENARIO：`name`必須、`gameSystem`任意
- PERSON：`displayName`必須
- PC：`name`必須。Person関係はCandidateの対応PERSON Slotで解決し、`personId`を埋め込まない
- `REUSE_EXISTING`では`existingEntityId`必須、`draftData = null`
- `CREATE_NEW`では`existingEntityId = null`。Formal Entity作成後は`createdEntityId`を保持する

`resolutionRefs`の非NULL IDには同Response内の`resolutions[]`が必ず対応する。

部分登録後、同じ`resolutionId`を参照する後続Candidateは`createdEntityId`を使い、Formal Entityを重複作成しない。**文字列一致は共有条件ではなく、同一`resolutionId`だけを共有Identityとする。**

Session内Resolutionを再利用選択できるよう、次を追加する。

```text
GET /api/import/session/{id}/resolutions?entityType=SCENARIO|PERSON|PC
```

- Current User所有 + URL Session Membershipを検証
- Response：`sessionId`, `sessionVersion`, Resolution DTO `items[]`
- `createdEntityId`を持つResolutionもSession終了まで返却対象にできる

Candidate PATCHの`resolutionChanges[]`は以下3形式を排他的に扱う。

1. Slotへ新規判断を設定：`decision` + (`existingEntityId` or `draftData`)
2. 既存Session ResolutionへBind：`useResolutionId`
3. Slotを未設定へ戻す：`clearResolution: true`

CLEAR例：

```json
{
  "slot": {"type": "PERSON", "candidateParticipationKey": "p-1"},
  "clearResolution": true
}
```

`decision` / `useResolutionId` / `clearResolution`は同一Change内で排他。

`clearResolution`では、対象Candidate SlotのResolution FK / RefだけをNULLへ戻す。ImportResolution本体は削除せず、同じResolutionを参照する他Candidateへ影響させない。`createdEntityId`を持つResolutionや既作成Formal Entityも削除しない。参照0件になったResolutionはSession cleanupまで保持してよい。**このWire Shapeは、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】とする。**

`useResolutionId`は同一Session・同一`entityType`だけ許可。共有Resolutionの内容を1 Candidateから直接変更して他Candidateへ暗黙波及させない。変更要求では対象Slotを新しいResolutionへRebindする。

HTML Sourceを返す場合もBrowserで実行させずTextとして扱う。

## 34.7 Bulk Preview / Apply

```text
POST /api/import/session/{id}/bulk-apply/preview
POST /api/import/session/{id}/bulk-apply
```

Preview Request例：

```json
{
  "expectedSessionVersion": 9,
  "operation": {
    "type": "APPLY_RESOLUTION",
    "slot": {"type": "SCENARIO"},
    "decision": "REUSE_EXISTING",
    "existingEntityId": 100
  },
  "targets": [
    {"candidateId": 11, "expectedVersion": 5},
    {"candidateId": 12, "expectedVersion": 3}
  ]
}
```

PERSON / PC Resolutionを一括適用する場合は、各Targetに対象`candidateParticipationKey`を含め、同じ意味のSlotへだけ適用する。

Response：

```json
{
  "sessionVersion": 9,
  "previewRevision": "opaque-value",
  "affectedCount": 2,
  "targets": [
    {"candidateId": 11, "version": 5, "willChange": true},
    {"candidateId": 12, "version": 3, "willChange": true}
  ]
}
```

Apply RequestはPreviewと同じ **`expectedSessionVersion` / Operation / Targets** + `previewRevision`。

Apply時にPreview作成時のSession / Candidate / Resolution SnapshotとPreview Revisionを再検証する。

1件でも変化：

```text
409 IMPORT_PREVIEW_STALE
```

**Apply Endpointでは`IMPORT_PREVIEW_STALE`を共通`OPTIMISTIC_LOCK_CONFLICT`より優先する。** Preview Request自体が古い`expectedSessionVersion`で開始された場合だけ`OPTIMISTIC_LOCK_CONFLICT`。

> **確定・2026-10-06採用**：Preview作成前のVersion不一致と、成功済みPreviewのApply時Snapshot失効を区別し、後者では`IMPORT_PREVIEW_STALE`を優先する。

Bulk Applyはこの一時Data変更単位ではAll-or-Nothing。

`operation.type`：

```text
APPLY_RESOLUTION
CLEAR_RESOLUTION
EXCLUDE
INCLUDE
```

`CLEAR_RESOLUTION`は`slot`を必須とし、各Target CandidateのそのSlot参照だけを未設定へ戻す。Previewは対象ごとに現在のResolution有無とClear後が未設定になることを確認可能にする。Applyは他のBulk操作と同じくTemporary Data変更単位でAll-or-Nothing。

共有ImportResolution本体、他Candidateの参照、`createdEntityId`で作成済みのFormal Entityは削除しない。これにより既存確定要件「正式登録前なら一括適用結果を取り消し可能」を実現する。REGISTERED Candidateは対象外。

## 34.8 Split Preview / Apply【Resolution継承：確定・2026-10-06採用】

```text
POST /api/import/session/{id}/candidates/{candidateId}/split-preview
POST /api/import/session/{id}/candidates/{candidateId}/split
```

Preview Request：

```json
{
  "expectedSessionVersion": 10,
  "expectedVersion": 5,
  "parts": [
    {
      "clientPartKey": "part-1",
      "candidateData": {},
      "resolutionBindings": [
        {"slot": {"type": "SCENARIO"}, "resolutionId": 71},
        {"slot": {"type": "PERSON", "candidateParticipationKey": "p-1"}, "resolutionId": 72}
      ]
    },
    {
      "clientPartKey": "part-2",
      "candidateData": {},
      "resolutionBindings": []
    }
  ]
}
```

Resolution継承は**明示Binding**。同名やTemporary Key一致だけで自動コピーしない。各BindingはTarget Part内に実在するSlotを指し、Resolutionは同一Session・対応`entityType`でなければならない。

Preview Response：

```json
{
  "sourceCandidateId": 11,
  "sourceVersion": 5,
  "sessionVersion": 10,
  "previewRevision": "opaque-value",
  "resultingCandidates": [
    {
      "clientPartKey": "part-1",
      "candidateData": {},
      "resolutionRefs": {},
      "resolutions": [],
      "unmappedSourceResolutionRefs": [],
      "canApply": true
    }
  ]
}
```

Source Candidateが参照していた非NULL ResolutionがどのResult PartにもBindされず失われる場合は`unmappedSourceResolutionRefs`へ列挙し、**`canApply=false`**。Frontendに再Mappingを要求し、暗黙破棄しない。

ApplyはPreviewと同じPayload + `previewRevision`。Preview Snapshotが変わっていれば`409 IMPORT_PREVIEW_STALE`。

ApplyはSource CandidateをN件の新Temporary CandidateへAtomicに置換し、TraceとResolution Refを再関連付けする。

Result Candidate State：

- `confirmationStatus = UNCHECKED`
- `registrationTarget`はSourceから継承
- target=true → `registrationStatus=PENDING`
- target=false → `registrationStatus=EXCLUDED`
- 旧`registrationResult`はコピーしない
- 明示Bindingした共有Resolutionの`createdEntityId`は維持

REGISTERED CandidateはSplit不可。

## 34.9 Merge Preview / Apply【Resolution競合処理：確定・2026-10-06採用】

```text
POST /api/import/session/{id}/candidates/merge-preview
POST /api/import/session/{id}/candidates/merge
```

Preview Request：

```json
{
  "expectedSessionVersion": 11,
  "sources": [
    {"candidateId": 11, "expectedVersion": 5},
    {"candidateId": 12, "expectedVersion": 3}
  ],
  "mergedCandidateData": {},
  "resolutionBindings": [
    {"slot": {"type": "SCENARIO"}, "resolutionId": 71}
  ],
  "discardResolutionIds": [],
  "resultRegistrationTarget": true
}
```

`mergedCandidateData.participations[].candidateParticipationKey`はTarget Candidate内で一意必須。Sourceごとに同じ`p-1`があってもTargetで衝突を許可しない。

PreviewはSource Candidate群が参照していたResolutionを集約し、Target SlotへのBindingを検証する。

- 同一Target Slotへ異なるResolution候補があるのに選択がない → `resolutionConflicts`
- Source ResolutionをTargetで使わず、`discardResolutionIds`にもない → `unmappedSourceResolutionRefs`
- Sourceの`registrationTarget`が不一致で`resultRegistrationTarget`未指定 → `registrationTargetConflict`

`resultRegistrationTarget`省略時は、Source Candidate全件の`registrationTarget`が一致していればその共通値を**resolved result**として継承する。全件一致しない場合だけ上記Conflictとし、明示値を必須とする。明示値がある場合はそれを使用する。**この省略Ruleは、ユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】とする。**

いずれかが残る場合`canApply=false`。Backendは「同じ名前だから」等でResolutionを勝手に選ばない。

Preview Response概念：

```json
{
  "sessionVersion": 11,
  "previewRevision": "opaque-value",
  "sourceVersions": [],
  "mergedCandidate": {
    "candidateData": {},
    "resolutionRefs": {},
    "resolutions": [],
    "confirmationStatus": "UNCHECKED",
    "registrationTarget": true
  },
  "resolutionConflicts": [],
  "unmappedSourceResolutionRefs": [],
  "registrationTargetConflict": false,
  "canApply": true
}
```

### Merge Preview `registrationTarget` Response Contract【確定・2026-10-06 12:11採用】

Merge Preview DTOの`mergedCandidate.registrationTarget`だけは **nullable Boolean** とし、意味を次で固定する。

- 明示`resultRegistrationTarget=true/false` → 同じbooleanを返す。
- Field省略 + Source全件一致 → 共通booleanを返す。
- Field省略 + Source混在 → `null`を返し、`registrationTargetConflict=true`、`canApply=false`。

`false`は解決済みの「登録対象外」であり、`null`は未解決Conflictなので同一視しない。Request DTOの`resultRegistrationTarget`はoptional Booleanだが、未選択は**Field省略**で表し、JSON `null`を未選択表現として受理する契約にはしない。

Unresolved Target Conflict Response概念：

```json
{
  "sessionVersion": 11,
  "previewRevision": "opaque-value",
  "sourceVersions": [],
  "mergedCandidate": {
    "candidateData": {},
    "resolutionRefs": {},
    "resolutions": [],
    "confirmationStatus": "UNCHECKED",
    "registrationTarget": null
  },
  "resolutionConflicts": [],
  "unmappedSourceResolutionRefs": [],
  "registrationTargetConflict": true,
  "canApply": false
}
```

このnullable表現は**Response Preview DTOだけ**に閉じる。`import_candidate.registration_target BOOLEAN`やApply後のMerge Resultをnullable化しない。Merge ApplyはTarget Conflict解消後のみ可能で、実際に作成するTemporary Candidateの`registrationTarget`は必ずbooleanへ解決する。

> **採用区分：確定。** 最終精査で初めて具体化したResponse Contractを、ユーザーが`2026-10-06T12:11:27+09:00`に明示採用したためFormal【確定】Contractとする。

`discardResolutionIds`はユーザーがPreview上で明示的に不要と判断したResolutionだけを指定する。Support Data自体はSession cleanupまで残してよいが、新Candidateから参照しない。

ApplyはPreviewと同じPayload + `previewRevision`。`resultRegistrationTarget`はPreviewで明示した場合のみApplyでも送る。Previewで省略し、Source全件一致により共通値を継承した場合はApplyでも省略を維持してよい。Preview Revisionは解決済みTarget値も含むSnapshotへ結びつける。Preview後にSession / Candidate / Resolution Snapshotが変わっていれば`409 IMPORT_PREVIEW_STALE`。

Merge Result：

- `confirmationStatus = UNCHECKED`
- `registrationTarget = resolvedResultRegistrationTarget`（明示値、または全Source一致時の共通継承値）
- target=true → `PENDING`、false → `EXCLUDED`
- Sourceの旧`registrationResult`はコピーしない

Source Candidate群から新Temporary Candidate 1件への置換、Trace統合、Resolution Ref確定を1 Transactionで行う。REGISTERED CandidateをMerge対象に含めない。

## 34.10 Register【一旦採用・2026-10-06 Version Contract】

```text
POST /api/import/session/{id}/register
```

旧`candidateIds`だけのRequestは使用しない。

```json
{
  "expectedSessionVersion": 12,
  "candidates": [
    {"candidateId": 11, "expectedVersion": 5},
    {"candidateId": 12, "expectedVersion": 3},
    {"candidateId": 14, "expectedVersion": 8}
  ]
}
```

Register開始時：

1. 34.0のSession Row Lock取得。
2. Session Version検証。
3. Candidate全件がURL Sessionに属することを検証。
4. Candidate Versionをそれぞれ検証。
5. `registrationTarget=true`、REGISTEREDでないこと、必要な確認 / Resolution / Candidate Dataが正式登録可能であることを検証。
6. CandidateごとにFormal Data登録をAtomicに処理。

**確認後に別TabでCandidateが更新された場合、Backendは最新版を勝手に登録しない。** Version不一致として止める。

Frontend側Autosave完了はUX上の前提だが、Backendはそれを信用せずRequest Versionを必ず検証する。

Candidate単位Partial Failure可。Double Submit / Idempotency対策必須。ImportResolutionの`createdEntityId`を利用し同一CREATE_NEW判断から重複Entityを生成しない。

Response：

```json
{
  "sessionId": 1,
  "sessionVersion": 13,
  "results": [
    {
      "candidateId": 11,
      "requestedVersion": 5,
      "outcome": "REGISTERED",
      "candidateVersion": 6,
      "registrationStatus": "REGISTERED",
      "registeredTableId": 901,
      "error": null
    },
    {
      "candidateId": 12,
      "requestedVersion": 3,
      "outcome": "FAILED",
      "candidateVersion": 4,
      "registrationStatus": "FAILED",
      "registeredTableId": null,
      "error": {
        "code": "VALIDATION_ERROR",
        "message": "登録内容を確認してください。",
        "fieldErrors": []
      }
    }
  ]
}
```

Request開始時にVersion競合があれば**1件も処理しない**。Session Version不一致は`409 OPTIMISTIC_LOCK_CONFLICT`、Candidate Version不一致は`409 IMPORT_CANDIDATE_VERSION_CONFLICT`としてRequest全体を停止する。通常のCandidate別Business Validation Failureだけを正常Response内`FAILED` Resultで返す。

成功Candidateは`REGISTERED`となりImport側からimmutable。

一部登録後も未登録CandidateのReview / Detail / Registerは継続できるが、Source mutation / Analysis Reset / Re-Analyzeは禁止。

## 34.11 Complete

```text
POST /api/import/session/{id}/complete
```

Request：

```json
{
  "expectedSessionVersion": 13
}
```

34.0のSession Lock / Version検証後、全Candidateが`REGISTERED`または`EXCLUDED`であることをBackendが確認。

Frontendから強制Complete不可。

Complete後、生Import Dataは48h後削除。Formal Dataは削除しない。

# 35. Image API【一旦採用・2026-10-06 HTTP Contract補完 + 既存Safe Sequence】

PC / ScenarioのCreate / Update時、FileはBrowser側Draftとして保持し、Metadata + Imageの2段階保存を基本とする。

Endpoint：

```text
PUT    /api/pcs/{pcId}/image
DELETE /api/pcs/{pcId}/image
PUT    /api/scenarios/{scenarioId}/image
DELETE /api/scenarios/{scenarioId}/image
PATCH  /api/pcs/{pcId}/image-transform
```

## 35.1 PC Image PUT

```text
Content-Type: multipart/form-data
```

Parts：

- `file` binary required
- `expectedVersion` decimal text required
- `positionX` decimal text required
- `positionY` decimal text required
- `zoom` decimal text required / `> 0`

MultipartでもCSRF Header必須。Position / Zoomは表示Metadataで、Master画像を破壊的Cropしない。FrontendのClamp済み値を受けるが、Backendも数値型 / finite / DB桁 / zoom>0をValidationする。

## 35.2 Scenario Image PUT

Multipart parts：`file` / `expectedVersion`。ScenarioにPC Transform Fieldを追加しない。

## 35.3 Transform only

```text
PATCH /api/pcs/{pcId}/image-transform
{
  "expectedVersion": 9,
  "positionX": 0.125,
  "positionY": -0.050,
  "zoom": 1.20
}
```

3値を1 Mutationとして保存する。

## 35.4 DELETE

Version送信場所をQuery Parameterへ固定する。

```text
DELETE /api/pcs/{pcId}/image?expectedVersion=10
DELETE /api/scenarios/{scenarioId}/image?expectedVersion=4
```

PC画像削除時は画像KeyをNULL化し、Transform MetadataはDefault `0 / 0 / 1`へ戻す【一旦採用】。

## 35.5 Response

Image PUT / Transform / DELETEは200 + 親の最新Versionを返す。Storage Keyは返さない。

PC例：

```json
{
  "id": 10,
  "version": 11,
  "image": {
    "present": true,
    "positionX": 0.125,
    "positionY": -0.050,
    "zoom": 1.20
  }
}
```

Delete後は`"image": null`。Scenario ResponseはTransformを含めない。画像表示URLは既存`GET /api/images/pcs/{pcId}/derivative-url` / `GET /api/images/scenarios/{scenarioId}/derivative-url`で取得し、Mutation ResponseへStorage Keyを混ぜない。

## 35.6 Safe Replacement Sequence【確定】

Metadata→Imageの順なら、Metadata Responseで返した最新VersionをImage Requestへ使用する。画像しか変更していない場合はMetadata APIを無駄に要求しない。

```text
Ownership + Membership確認
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

旧画像をStorageから先に削除しない。DB Commit失敗 / Version競合で新画像が不採用なら、新Master / Derivativeを補償削除し、削除失敗は`storage_delete_task`へ送る。旧画像Delete TaskはDB切替と同一Transactionで作成する。

FrontendへStorage Keyを公開しない。
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

# 37. Session / CSRF / Cookie【2026-10-01再監査で矛盾解消】

## Production Cookie【確定】

```text
Cookie name = __Host-TAKUKAIRO_SESSION
Secure = true
HttpOnly = true
SameSite = Lax
Path = /
Domain = なし
```

`__Host-` Prefixを使うProduction Cookieで`Secure=false`を許可しない。

## Local Dev / Test HTTP Cookie【今回採用】

Local runtimeはHTTPを維持するため、Dev / Testでは別Cookie名を使う。

```text
Cookie name = TAKUKAIRO_SESSION_DEV
Secure = false
HttpOnly = true
SameSite = Lax
Path = /
Domain = なし
```

Dev / Test HTTPで`__Host-TAKUKAIRO_SESSION`を使用しない。

将来Local HTTPSへ切り替える場合はProduction相当Prefixを利用可能だが、Cookie Prefix要件とSecure設定を必ず一致させる。

JWT / Session TokenをlocalStorage / IndexedDBへ保存しない。

## CSRF【確定】

State Changing：

- POST
- PUT
- PATCH
- DELETE

はCSRF Token必須。

`GET /api/csrf`は未認証でも許可し、以下DTOを返す。

```json
{
  "token": "...",
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf"
}
```

Dev SessionでもCSRFを無効化しない。

Login / Dev Login前にToken取得可能とする。

認証成功時：

1. Session Fixation対策としてSession ID変更
2. 以前のCSRF Tokenを使用済みとみなす
3. Frontendが`GET /api/csrf`でFresh Token再取得
4. その後にSelfPerson Setup等のUnsafe Requestを許可

Logout成功後もFresh Token再取得が必要。

Session Expired → 再Login後も同じ。

## Session Lifetime Production【確定】

- Idle 7日
- Absolute 30日
- Browser終了Logoutしない
- Persistent Cookie
- 1 User最大5 Session
- Timeout判定はServer-side
- Login成功時Session ID変更

Login成功時Session ID変更は`DEFERRED_PRODUCTION`ではない。

将来、これに加えてPeriodic Session Rotationを導入するかは別のProduction運用判断とする。

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
- Candidate GETで`resolutionRefs`と`resolutions[]`が整合し、Reload復元できる
- `GET /resolutions`は同一Session Membership / entityTypeを守る
- `useResolutionId`で同一Session・同一entityTypeだけBind可能
- Splitで未Mapping Resolutionがある場合`canApply=false`、明示Binding後にTrace/Resolution RefをAtomic再関連付け
- Mergeで異なるResolution判断を暗黙採用せず`resolutionConflicts`、明示Bindingまたはdiscard後のみApply可
- Merge Target内の`candidateParticipationKey`重複拒否
- Preview Requestの古いSession Versionは`OPTIMISTIC_LOCK_CONFLICT`、成功Preview後ApplyのSession/Candidate/Resolution Snapshot変化は`IMPORT_PREVIEW_STALE`
- Candidate PATCH `clearResolution: true`で対象Slotだけ未設定へ戻り、同一Resolutionを参照する他CandidateとResolution本体は維持される
- Bulk `CLEAR_RESOLUTION`がPreview→ApplyでAll-or-Nothingとなり、正式登録前の一括適用を取消可能
- MergeでSourceの`registrationTarget`が全件一致する場合、`resultRegistrationTarget`省略時に共通値を継承し、Preview Responseにもそのbooleanを返す
- MergeでSourceの`registrationTarget`が不一致の場合、`resultRegistrationTarget`省略では`mergedCandidate.registrationTarget=null`、`registrationTargetConflict=true`、`canApply=false`
- `registrationTarget:null`はPreview DTOだけで許容し、実Merge Result / `import_candidate.registration_target`はbooleanのまま維持する
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

# 47. Implementation Readiness / Backend Definition of Done【2026-10-01再監査で分離】

## 47.1 基盤実装開始条件

以下を基盤として実装開始してよい。

- PostgreSQL / Flyway / JPA骨格
- Formal 9 EntityとSupport Structureの分離
- User Ownership / Parent経由Ownership
- Dev Session
- Production / Dev Cookie名の分離
- CSRF Endpoint / Token Lifecycle
- Common Error `fieldErrors[]` + JSON Pointer
- TableDate Object Shape
- Optimistic Lock基盤
- Storage Adapter / Safe Image Replace基盤

判定：

> **Backend基盤実装開始可。**

## 47.2 実装可能範囲【2026-10-06再整理】

### A. 既存確定事項として実装可

- Scenario / Person / PC / Table CRUD
- PC + 0..N System Settings Aggregateの既存概念
- PL Change Cross-Table Optimistic Lock
- Table AggregateでのPC変更 / 解除とEndPcState削除
- UC17 distinct Table集計
- Scenario PL/KP repeatable OR Filter
- Person Impact distinct Table count
- Quote Grapheme Validation
- Production / Dev Cookie分離とCSRF Lifecycle

### B. 2026-10-06一旦採用Contractとして実装可能

- ImportSession共通Mutation Lock / expectedSessionVersion
- Register candidate expectedVersion / Partial Result DTO
- CandidateData / ImportResolution Slot Contract
- Resource Membership Validation
- Generic Game System Profile JSON Schema / Validator / Renderer Contract
- Image Multipart / Transform / DELETE Version Contract

Bはユーザー明示承認前に「既存確定仕様」と呼ばない。

## 47.3 未確定機能の実装保留

- Game System ProfileのMVP初期Active Profile集合 / 各System固有Config実データ
- Previous EndPcState候補取得Endpoint / Request Shape
- CCFOLIA Mapping Config / Preview Requestの未確定部分
- Tekey / Udonarium共通Adapter
- CCFOLIA `iconUrl`利用

Generic Profile Engineは実装可。`coc_7e_v1`はTest Fixtureとして利用可だが、本番MVP対応System一覧の確定とはみなさない。

## 47.4 全体完成条件

Backend完成とみなすには：

- Flyway 0→latest成功 / Hibernate validate成功
- Cookie Prefix / HTTP Dev Cookieを実Browser Test
- Login前CSRF → Login → Fresh CSRF → SelfPerson POSTが成功
- Logout後Fresh CSRF Test
- PC-A→B / PC解除で旧EndPcStateがAtomicに削除され、移送されないTest
- Import Partial Register後Source mutation / Resetが409になるTest
- REGISTERED Candidate immutable Test
- Preview Requestの古いSession Versionは`OPTIMISTIC_LOCK_CONFLICT`、Preview成功後のApply Snapshot変化は`IMPORT_PREVIEW_STALE`となる優先順位Test
- ImportResolution / registrationResultによる二重登録防止Test
- 他User Data access拒否Test
- Optimistic Lock 409 Test
- 47.3のTODOがユーザー判断で解消
- Production Deferredを実装済みと偽らない

現時点判定：

> **全体完成判定は保留。**

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


# 49. 残TODO_SPEC_CONFIRMATION【2026-10-06再整理】

1. **Game System ProfileのMVP初期Active Profile集合 / 各Profile実データ**
   - Generic Schema / Validation Contractは本版で一旦採用として具体化。
   - `coc_7e_v1`はTest Fixtureのみ確定可能。
2. **Previous EndPcState候補取得APIの最終Endpoint / Request Shape**
   - Cutoff / Current Table除外 / exact profileKey / Low Confidence Ruleは確定。
3. **CCFOLIA Mapping Configの最終Field Mapping**
4. **CCFOLIA Preview APIでProfile / Canonical System情報をどう渡すか**
5. **Tekey / Udonarium共通Adapterの具体Contract**
6. **CCFOLIA `iconUrl`のPC画像利用**

Import Register Version、Re-Analyze禁止、Session mutation Lock、Resource Membership、CandidateData/Resolution DTO、Image HTTP Contractは「記述不足TODO」から外し、本版の **一旦採用・2026-10-06提案** としてFrontendと対にした。

ただし、R2で追加した **Resolution再開復元 / Split時Resolution引継ぎ / Merge時Resolution衝突処理 / Preview競合Error優先順位** の4点は、`2026-10-06T11:02:00+09:00`にユーザーが明示採用したため **【確定】**。その他の10/06一旦採用Contractは従来どおり一旦採用のまま。

---

# Appendix A. 2026-10-06訂正履歴（Backend）

改訂日時：`2026-10-06T10:34:09+09:00`。

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| BE-R2-01 | RegisterがcandidateIdsのみ | expectedSessionVersion + candidate expectedVersion | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-02 | REGISTERED後のAnalyze未定義 | Re-Analyze禁止、409、未登録Review継続 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-03 | Register/Reset別Request競合未定義 | Import全Mutation共通Session Row Lock + version | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-04 | Ownershipのみ | Parent-Child / Same Session Membership検証を追加 | Security横断監査 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-05 | Profile概念のみ | Generic JSON Schema / OptionSource / REPEATER / Validation具体化 | Profile既存概念を実装Contract化 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-06 | 初期Profile集合が暗黙 | 正式集合はTODO、coc_7e_v1はTest Fixture限定 | 資料から確定不能 / 【未確定明示】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-07 | candidateData / Resolution関係不明 | CandidateData Shape + Resolution Slot/Refs | Import既存概念をDTO化 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-08 | Image Field / DELETE Version場所不明 | Multipart Field / JSON Transform / Query expectedVersion固定 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-09 | External Image同意のDB FieldはあるがHTTP連携不明 | Session boolean + Scenario Save commandでUser初回同意を同一Transaction化 | BackendDataSecurityPlatform §5-6 + Physical Design / 【確定復元 + HTTP連携一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-10 | Preview→ApplyのSession Versionが暗黙 | Bulk/Split/Merge ApplyへexpectedSessionVersionを明示 | Import競合監査 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |
| BE-R2-11 | Candidate Version競合時のPartial処理可否が曖昧 | 全Candidate Versionを処理前検証し、1件でも不一致なら全体409・登録0件 | 三視点横断判断 / 【一旦採用】 | `2026-10-06T10:34:09+09:00` |

本Appendixは過去のR1訂正履歴を削除するものではなく、後発修正として積み上げる。


# Appendix B. 2026-10-06 R2追補訂正履歴

追補日時：`2026-10-06T10:49:25+09:00`。

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| BE-R3-01 | Resolution ID参照だけでReload復元不能 | Candidate `resolutions[]`、Session Resolution一覧GET、entity別Draft、`useResolutionId` | 再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| BE-R3-02 | Split/Merge後のResolution継承・衝突未定義 | 明示Binding / Unmapped / Conflict / registrationTarget規則 | 再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |
| BE-R3-03 | Preview Applyと共通OPTIMISTIC errorが競合 | Preview作成前staleはOPTIMISTIC、Apply snapshot staleはIMPORT_PREVIEW_STALE優先 | 再監査 / 【一旦採用】 | `2026-10-06T10:49:25+09:00` |


# Appendix C. Import Contract採用確定履歴

採用日時：`2026-10-06T11:02:00+09:00`。

ユーザーの明示採用により、R2で一旦採用だった次の4点を正式仕様へ昇格する。

1. ImportResolutionのReload / 再開復元 Contract
2. Split時のResolution明示Binding / 引継ぎ Contract
3. Merge時のResolution Conflict / discard / Target State Contract
4. Preview競合Error優先順位 Contract

上記4点のみが今回の採用対象。Register Version、Session mutation Lock、Profile Schema、Image HTTP Contract等の既存一旦採用事項をこの採用だけで自動確定しない。


# Appendix D. 2026-10-06 R3追補訂正履歴

R3追補記録時刻：`2026-10-06T11:32:57+09:00`（Asia/Tokyo）

| ID | 旧記述 / 不足 | 訂正後 | 根拠 / 採用区分 | 修正日時 |
|---|---|---|---|---|
| BE-R4-01 | Resolutionを未設定へ戻すRequest/一括取消契約なし | `clearResolution: true` / `CLEAR_RESOLUTION`を追加。Slot Refのみ解除し共有Resolutionは維持 | Import一括適用「正式登録前なら取り消し可能」【確定・復元】 + Wire具体化【当時一旦採用 → 2026-10-06T11:41:14+09:00 確定】 | `2026-10-06T11:32:57+09:00` |
| BE-R4-02 | Mergeの`resultRegistrationTarget`省略時が未定義 | Source全件一致なら共通値継承、不一致ならConflictで明示必須 | Merge Contract補完【当時一旦採用 → 2026-10-06T11:41:14+09:00 確定】 | `2026-10-06T11:32:57+09:00` |

R3の追加Wire Contract 2点はユーザーが`2026-10-06T11:41:14+09:00`に明示採用したため【確定】。既存の6系統TODOやその他の10/06一旦採用Contractを自動的に解消・確定したものではない。


# Appendix F. 2026-10-06 R3追加Import Contract 採用確定

採用日時：`2026-10-06T11:41:14+09:00`（Asia/Tokyo）

正式仕様へ昇格：

- Candidate PATCH `clearResolution: true` / Bulk `CLEAR_RESOLUTION`によるSlot参照解除Contract
- Merge `resultRegistrationTarget`省略時の共通値継承／不一致時明示必須Rule

上記以外の10/06一旦採用Contractは今回の採用対象外。


# Appendix G. 2026-10-06 最終精査・訂正履歴

| 修正日時 | 対象 | 旧記述 | 訂正後 | 根拠 | 採用区分 |
|---|---|---|---|---|---|
| 2026-10-06 11:49 JST | 情報源優先順位 | 最優先の「本書」が旧R2表記 | `本書（2026-10-06横断精査修正版 R3 追加ImportContract確定版）` | 現在の直接正本名との一致 | 【確定・訂正】 |
| 2026-10-06 11:49 JST | Merge Preview DTO | Source不一致Conflict時の`mergedCandidate.registrationTarget`表現未定義 | Response Preview DTOだけ`Boolean|null`。未解決時`null`、Conflict解消後/Apply後はboolean | Frontend / Backend DTO整合、DB persisted CandidateとPreview DTOの分離 | 【当時一旦採用 → 2026-10-06 12:11確定】 |
| 2026-10-06 11:49 JST | 状態区分 | 3区分のみ記載 | 【確定】【確定・復元】【一旦採用】【TODO_SPEC_CONFIRMATION】【資料照合残件】の5区分を明記 | 今回の引継ぎルール | 【確定・文書整理】 |
| 2026-10-06 11:49 JST | Example JSON | 一部`json` fenceが断片 / Method行 / 複数objectを含み構文上JSONでなかった | 完全JSONへ補正、または`text` fenceへ変更 | 機械検査で本文ContractとExample形式を一致 | 【確定・文書整形】 |

> **実装判定注記：** Appendix Fまでの「R3追加Import Contract 2点＝確定・GO」に加え、最終精査で具体化した`registrationTarget:null` Preview Response表現もユーザーが`2026-10-06T12:11:27+09:00`に明示採用したため【確定・GO】とする。


# Appendix H. Merge Preview Null Contract 正式採用

採用日時：`2026-10-06T12:11:27+09:00`（Asia/Tokyo）

ユーザーの明示採用により、最終精査で【一旦採用】としていた次のContractを正式仕様へ昇格する。

- Sourceの`registrationTarget`が混在し、`resultRegistrationTarget`が未指定のMerge Previewでは、`mergedCandidate.registrationTarget = null`を返す。
- 同時に`registrationTargetConflict = true`、`canApply = false`とする。
- `false`は「登録対象外へ解決済み」、`null`は「未解決」を意味し、Frontendは両者を区別する。
- `null`はMerge Preview Response DTOだけに許可し、正式Candidate / Merge Result / DB `registration_target`はbooleanのままとする。
- Request側の未選択は`resultRegistrationTarget`のField省略で表し、JSON `null`送信を未選択Contractとして追加しない。

採用区分：**【確定】**。実装判定：**GO**。
