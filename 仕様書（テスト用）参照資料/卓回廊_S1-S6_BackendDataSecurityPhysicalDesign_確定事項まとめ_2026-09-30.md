# Web制作総合演習1
# 卓回廊（TRPG活動履歴管理Webサービス）
## Backend / Data / Security / Physical Design
### S1〜S6 確定事項まとめ
### 2026-09-30

---

# 0. 本資料の位置づけ

本資料は、2026-09-30時点で「卓回廊」の実装前Security Hardening / Physical Designとして検討した **S1〜S6** の決定事項を統合した追補資料である。

対象：

- S1：Upload画像 Security / Decompression Bomb対策
- S2：Session Security
- S3：Rate Limit / Abuse Protection
- S4：PostgreSQL Physical Schema
- S5：R2 / Image Access Security
- S6：Logging / Backup / Security Operations

本資料は、既存のFormal MVP・9 Entity・14 UC・CRUD・画面仕様・Visual Designを新しく作り直すものではない。

最重要原則：

> **見た目はv5.9。中身は既存の確定済み設計。**

特にPC Focus v5.9はVisual Design / UI実装ベースであり、Mock上の項目をFormal Data Modelへ逆輸入しない。

---

# 1. ステータスの読み方

本資料では以下を区別する。

## 【既存確定】
過去の確定資料ですでに確定済みのもの。

## 【今回採用】
2026-09-30のBackend / Security検討で、ユーザーからの

> エンジニア中心に、Security面に注意して慎重に決めてよい

という委任方針のもと採用したTechnical Decision。

## 【一旦採用】
採用方向ではあるが、実装検証や最終横断監査で再評価余地を残すもの。

## 【最終監査対象】
次チャットの最終横断監査で、法務・Security・既存仕様・外部Service規約等まで含めて再確認するもの。

---

# 2. 前提として維持する既存確定事項

Formal Entityは以下9件。

1. User
2. Scenario
3. Person
4. PC
5. Table
6. TableDate
7. Participation
8. EndPcState
9. ScenarioFavorite

Formal MVP UCは14件。

- UC01
- UC02
- UC03
- UC05
- UC06
- UC07
- UC08
- UC09
- UC10
- UC11
- UC12
- UC13
- UC14
- UC17

S1〜S6の決定によって、Formal Entityを勝手に増やさない。

ImportSession等、Spring Session、Storage Delete Task等はInfrastructure / Support Dataであり、Formal 9 Entityへ数えない。

---

# 3. S1：Upload画像 Security【今回採用】

## 3.1 既存Upload対象

User Uploadの正式対象：

1. PC画像 / 立ち絵
2. User UploadのScenario画像

BOOTH等のExternal Scenario Imageは卓回廊のObject Storageへ複製しない。

---

## 3.2 対応Format

対応：

- JPEG
- PNG
- WebP

MVP非対応：

- GIF
- SVG
- HEIC
- HEIF
- AVIF

Animated WebPは非対応とし、`n-pages > 1` はRejectする。

---

## 3.3 File / Decode上限

採用値：

- 1画像最大File Size：**10MB**
- Max Width：**8192px**
- Max Height：**8192px**
- Max Decoded Pixel Count：**32,000,000 pixels**
- Estimated Decoded Raster Size：**192 MiB以下**

Internal Safety Formula：

```text
width × height × bands × bytesPerSample <= 192 MiB
```

File SizeだけではDecompression Bomb対策として不十分なため、Decode前にHeader情報からDimension / Pixel / Estimated Raster Sizeを確認する。

---

## 3.4 libvips Security / Resource Limit

採用：

- Application Instanceごとの画像処理同時実行：**1 job**
- libvips worker concurrency：**2**
- libvips operation cache memory：**64 MiB**
- `unlimited`：**false / 使用しない**
- Loader `fail_on`：**ERROR**
- Pixel Access：**sequential**

---

## 3.5 Decode Pipeline

```text
Upload Request
↓
10MB File Size上限
↓
Magic Number / Actual Format確認
↓
JPEG / PNG / WebP以外Reject
↓
libvipsでHeader Read
↓
Width / Height確認
↓
Decoded Pixel Count確認
↓
Estimated Decoded Raster Size確認
↓
Static Image確認
↓
実Decode
↓
EXIF Orientation正規化
↓
Dimension再確認
↓
Derivative生成
↓
Storage保存
```

---

## 3.6 画像処理既存方針

維持：

- Aspect Ratio維持
- Auto Cropしない
- 小画像を強制Upscaleしない
- Alpha透明維持
- 透明Canvas Marginを勝手にTrimしない
- Lossless Master保持
- PC Position / Zoomは非破壊Metadata
- 新画像保存成功前に旧画像を破壊しない

Derivative：

- Format：WebP【採用】
- Quality：85【採用】
- 最大長辺：2048px【一旦採用】

Image Encoder：

- vips-ffm + libvips【一旦採用】
- DockerでRuntime Environmentを固定
- 明確な互換性問題が出た場合のみ再選定

---

## 3.7 User向けError

Dimension / Pixel超過：

> 画像サイズが大きすぎます。8192×8192px以内、かつ3200万画素以内の画像を使用してください。

Animated WebP：

> アニメーションWebPには対応していません。静止画のJPEG・PNG・WebPを使用してください。

「Decompression Bomb」という内部Security用語をUser向け文面には出さない。

---

# 4. S2：Session Security【今回採用】

## 4.1 Session基本構成

既存方針を維持。

- Spring Session JDBC + PostgreSQL
- Cookie Session
- JWT / Session TokenをlocalStorage / IndexedDBへ保存しない

Cookie：

```text
__Host-TAKUKAIRO_SESSION
Secure
HttpOnly
SameSite=Lax
Path=/
Domainなし
```

ProductionはSame Originを基本とする。

---

## 4.2 再ログイン頻度

当初案の「30分Idle / 8時間Absolute」は採用しない。

卓回廊ではUXを重視し、以下へ変更。

- Idle Timeout：**7日**
- Absolute Session Lifetime：**30日**
- Browser終了：Logoutしない
- Persistent Cookie
- Timeout判定はServer-side

利用例：

```text
毎日利用
→ 基本的には30日まで再Login不要

7日以上まったく利用しない
→ 次回Login必要

毎日使い続けても
→ 30日で再認証
```

---

## 4.3 Remember Me

別のRemember-Me Token機構はMVPでは作らない。

Spring Session自体をPersistent Sessionとして管理する。

---

## 4.4 Session Fixation

Login成功時：

- Session IDを変更
- Spring Security標準のSession Fixation Protectionを利用

Session IDをJavaScriptから読ませない。

---

## 4.5 Concurrent Session

採用：

- 1 User最大 **5 Session**
- 6件目のLogin時は、新Loginを成功させる
- 古い既存Sessionを失効対象とする

PC / Smartphone / PWA / Browser違い等を考慮し、3件よりUX側へ余裕を持たせる。

---

## 4.6 Logout

正式Logout：

```text
POST /logout
+ CSRF Token
```

Logout時：

- Session invalidate
- SecurityContext clear
- CSRF Token破棄
- `__Host-TAKUKAIRO_SESSION`失効
- FrontendはLogin画面へ
- Google Account全体からLogoutさせない
- 通常LogoutでGoogle Token revokeは行わない

---

## 4.7 CSRF Lifecycle

Cookie Session方式なのでCSRF Protectionは維持。

State-changing Request：

- POST
- PUT
- PATCH
- DELETE

には正しいCSRF Tokenを要求する。

Login / Logout等で旧Tokenが無効になった場合、Frontendは新しいCSRF Tokenを取得する。

Multipart画像UploadでもCSRF Headerを送る。

SameSiteのみをCSRF対策としない。

---

## 4.8 Reauthentication

日常操作では頻繁なGoogle再Loginを要求しない。

重大操作：

- Account削除
- その他将来追加される特に高RiskなAccount操作

ではGoogle再認証を要求する。

---

# 5. S3：Rate Limit / Abuse Protection【今回採用】

基本方針：

> 正常Userには意識させず、異常な連打・Bot・Resource Abuseを止める。

User単位 / IP単位 / Endpoint単位を組み合わせる。

どれかのLimitを超えた場合：

```text
429 Too Many Requests
```

User向け：

> リクエストが短時間に集中しています。少し待ってから、もう一度お試しください。

内部Bucket種別や残り回数等をClientへ詳細表示しない。

Rate Limit発動だけでAccount Lockしない。

---

## 5.1 Google Login / Auth

IP単位：

- **20回 / 分**
- **60回 / 時**

一時Throttleのみ。

---

## 5.2 BOOTH取得

User：

- **10回 / 10分**
- **30回 / 時**

IP：

- **30回 / 10分**
- **100回 / 時**

追加：

- BOOTHへの実HTTP通信：Server全体同時 **1件**
- 実通信間隔：最低 **2秒**
- 同一Product ID取得結果：Server Memoryで **10分Cache**
- Cacheは永続化しない
- BOOTH画像をR2へ複製しない

既存SSRF対策：

```text
User入力URL
↓
Product ID抽出
↓
Backend側で許可BOOTH URLを再構築
↓
Fetch
```

User入力URLを任意HTTP Clientへそのまま渡さない。

---

## 5.3 画像Upload

User：

- **20回 / 10分**
- **60回 / 時**

IP：

- **60回 / 10分**
- **200回 / 時**

S1の画像処理Concurrency Limitも併用する。

---

## 5.4 Import File Upload

User：

- **30回 / 10分**
- **60回 / 時**

IP：

- **60回 / 10分**
- **200回 / 時**

既存Resource Limit：

- 1 File：20MB
- 最大20 File / ImportSession
- Session総量50MB
- Paste Text 2MB

も併用。

---

## 5.5 Import Heavy処理

User：

- **10回 / 10分**
- **30回 / 時**

IP：

- **30回 / 10分**
- **100回 / 時**

Concurrency：

- 1 User：同時1件
- Server全体：同時1件

---

## 5.6 Search

User：

- **60回 / 分**

IP：

- **180回 / 分**

Frontend：

- 約300ms Debounce

---

## 5.7 その他認証済みAPI

User：

- **120回 / 分**

IP：

- **300回 / 分**

Endpoint固有Limitがある場合は両方を判定する。

---

## 5.8 Rate Limiter実装

採用：

- Bucket4j
- Token Bucket方式
- Endpoint Group単位
- MVPではBounded In-memory State
- Application再起動でBucket Resetを許容

正式Business DataではないためPostgreSQLへRate Limit Entityを作らない。

将来Multi-instance化した場合はDistributed Rate Limiterへ移行する。

---

## 5.9 Client IP

Application Codeで生の`X-Forwarded-For`先頭値を独自Parseして信用しない。

Production：

```text
server.forward-headers-strategy=native
```

を基本とし、RenderのTrusted Reverse Proxyを前提とした正規化済みClient Addressを利用する。

---

# 6. S4：PostgreSQL Physical Schema【今回採用】

## 6.1 Physical Table Name

```text
User             → app_user
Scenario         → scenario
Person           → person
PC               → pc
Table            → trpg_table
TableDate        → table_date
Participation    → participation
EndPcState       → end_pc_state
ScenarioFavorite → scenario_favorite
```

PostgreSQL予約語やQuoted Identifier依存を避ける。

---

## 6.2 Primary Key

Formal 9 Entityの主キー：

```sql
BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
```

を基本とする。

IDの推測困難性を認可対策に使わない。

認可はBackendで必ずCurrent User所有権を確認する。

---

## 6.3 app_user

主なPhysical Field：

```text
id
auth_provider              VARCHAR(32)  NOT NULL
provider_subject           VARCHAR(255) NOT NULL
self_person_id             BIGINT NULL
external_image_consent_at  TIMESTAMPTZ(3) NULL
created_at                 TIMESTAMPTZ(3) NOT NULL
updated_at                 TIMESTAMPTZ(3) NOT NULL
version                    BIGINT NOT NULL
```

制約：

```text
UNIQUE(auth_provider, provider_subject)
UNIQUE(self_person_id)
CHECK(auth_provider IN ('GOOGLE'))
```

MVPでGoogleから恒久保存する認証由来情報は基本的に`sub`のみ。

保存しない：

- email
- email_verified
- name
- given_name
- family_name
- picture
- locale
- hd
- その他不要Profile Data

---

## 6.4 scenario

主なField：

```text
id
user_id                  BIGINT NOT NULL
name                     VARCHAR(255) NOT NULL
author_name              VARCHAR(255) NULL
source_url               VARCHAR(2048) NULL
game_system              VARCHAR(255) NULL
external_image_url       VARCHAR(2048) NULL
image_master_key         VARCHAR(512) NULL
image_derivative_key     VARCHAR(512) NULL
created_at
updated_at
version
```

Scenario名 / URLはUniqueにしない。

---

## 6.5 person

```text
id
user_id       BIGINT NOT NULL
display_name  VARCHAR(255) NOT NULL
created_at
updated_at
version
```

同名Person許可。

`UNIQUE(user_id, display_name)`は作らない。

---

## 6.6 pc

```text
id
user_id               BIGINT NOT NULL
person_id             BIGINT NOT NULL
name                  VARCHAR(255) NOT NULL
game_system           VARCHAR(255) NULL
character_sheet_url   VARCHAR(2048) NULL
image_master_key      VARCHAR(512) NULL
image_derivative_key  VARCHAR(512) NULL
image_position_x      NUMERIC(9,4) NOT NULL DEFAULT 0
image_position_y      NUMERIC(9,4) NOT NULL DEFAULT 0
image_zoom            NUMERIC(9,4) NOT NULL DEFAULT 1
created_at
updated_at
version
```

CHECK：

```text
image_zoom > 0
```

PC名はUniqueにしない。

PC.personは現在のPerson。

過去Participation.personは自動更新しない。

---

## 6.7 trpg_table

```text
id
user_id        BIGINT NOT NULL
scenario_id    BIGINT NOT NULL
table_name     VARCHAR(255) NULL
recording_url  VARCHAR(2048) NULL
created_at     TIMESTAMPTZ(3) NOT NULL
updated_at     TIMESTAMPTZ(3) NOT NULL
version        BIGINT NOT NULL
```

`created_at`は卓の「サービス登録日時」であり、実施日ではない。

---

## 6.8 table_date

```text
id
table_id
played_on DATE NOT NULL
created_at
updated_at
version
```

同一Table同一日をDB Uniqueにはしない。

TableDateが存在しないことで日付不明を表す。

---

## 6.9 participation

```text
id
table_id        BIGINT NOT NULL
person_id       BIGINT NOT NULL
pc_id           BIGINT NULL
role            VARCHAR(16) NOT NULL
display_order   INTEGER NOT NULL
ho              TEXT NULL
display_quote   VARCHAR(24) NULL
spotlight_type  VARCHAR(16) NULL
created_at
updated_at
version
```

制約：

```text
CHECK(role IN ('KP', 'PL'))
CHECK(display_order >= 1)
CHECK(spotlight_type IS NULL OR spotlight_type IN ('HO','QUOTE'))
UNIQUE(table_id, role, display_order)
```

Participation表示順Uniqueは並べ替えTransactionを成立させるため、

```text
DEFERRABLE INITIALLY DEFERRED
```

を利用する。

HOは新たな文字数上限を追加しない。

QuoteはFormal Specどおり最大24文字。

---

## 6.10 EndPcState

重要：

> SAN / HP / MPを固定Physical Columnへ戻さない。

Game System Profileに応じてStatusが可変であるため、採用構造：

```text
id
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

`status_values`例：

```json
{
  "san": 51,
  "hp": 10,
  "mp": 12
}
```

Backendで：

```text
profile_key
↓
Game System Profile
↓
許可Status Key
↓
Integer型確認
```

を行う。

Formal既存ルール：

- 数値Status：Nullable Integer
- 負数可
- 小数不可
- Service共通Min / Maxなし
- Growth：任意Multiline / 最大500文字
- Outcome：SURVIVED / LOST / NULL
- Aftereffects：任意Multiline / 最大500文字

DB CHECK：

```text
jsonb_typeof(status_values) = 'object'
growth <= 500文字
aftereffects <= 500文字
outcome IS NULL OR outcome IN ('SURVIVED','LOST')
```

全項目空ならEndPcStateを作らない。

`UNIQUE(participation_id)`で1 Participationにつき0..1件を保証。

---

## 6.11 ScenarioFavorite

```text
id
user_id
scenario_id
created_at
```

```text
UNIQUE(user_id, scenario_id)
```

Favoriteは存在 / 非存在で意味を表す。

---

## 6.12 ON DELETE

採用方向：

### CASCADE

- TableDate → Table
- Participation → Table
- EndPcState → Participation
- ScenarioFavorite → Scenario
- ScenarioFavorite → User

親の構成要素として意味を持つData。

### RESTRICT

- PC → Person
- Table → Scenario
- Participation → Person
- Participation → PC
- User.SelfPerson → Person
- User所有Top-level Entity → User

独立再利用Entityを参照の都合で勝手に消さない。

Account削除は専用Application Flowで順序を管理する。

---

## 6.13 User所有関係のDB補強

同User内RelationであることをDBでも可能な範囲で補強。

例：

```text
PC.user_id + PC.person_id
→ Person(user_id, id)

Table.user_id + scenario_id
→ Scenario(user_id, id)

ScenarioFavorite.user_id + scenario_id
→ Scenario(user_id, id)

User.id + self_person_id
→ Person(user_id, id)
```

ただし以下Childには既存方針どおり冗長`user_id`を追加しない。

- TableDate
- Participation
- EndPcState

BackendでParent Relationを辿って所有権確認する。

---

## 6.14 Timestamp

日時：

```text
TIMESTAMPTZ(3)
```

Java：

```text
Instant
```

Browser表示時にLocal Timeへ変換。

TableDate.played_onのみ：

```text
DATE
```

---

## 6.15 Optimistic Lock

Mutable Entity：

- User
- Scenario
- Person
- PC
- Table
- TableDate
- Participation
- EndPcState

に：

```text
version BIGINT NOT NULL DEFAULT 0
```

JPA：

```text
@Version
```

を利用。

ScenarioFavoriteは対象外。

卓編集では子Entity変更時も親Table.versionを更新し、Table Form全体の競合検知を行う。

---

## 6.16 Index

最低限：

```text
scenario(user_id, created_at DESC, id DESC)

person(user_id, id)

pc(user_id, created_at DESC, id DESC)
pc(user_id, person_id)

trpg_table(user_id, created_at DESC, id DESC)
trpg_table(user_id, scenario_id, created_at DESC)

table_date(table_id, played_on DESC, id DESC)

participation(table_id, role, display_order)
participation(person_id)
participation(pc_id) WHERE pc_id IS NOT NULL

end_pc_state(participation_id)

scenario_favorite(user_id, scenario_id)
```

Search用特殊Indexは実Query / EXPLAIN ANALYZEを見てから追加。

---

## 6.17 VARCHAR等のApplication Limit

採用：

- 通常名称 / Game System：255
- URL：2048
- Storage Key：512
- provider_subject：255
- Profile Key：128
- Code系：16〜32
- Quote：24
- Growth：500
- Aftereffects：500
- HO：TEXT / 新上限なし

URL 2048はHTTP仕様上の絶対最大値ではなく、卓回廊Application Limit。

---

## 6.18 Flyway

Schema Source of Truth：

> Flyway Migration

ProductionでHibernate自動DDL生成は行わない。

Hibernate：

> validate相当

Initial Migration構成：

```text
V1__create_core_schema.sql
V2__create_import_schema.sql
V3__create_spring_session_schema.sql
```

Import Schema / Spring Session SchemaはFormal 9 Entityへ追加しない。

---

# 7. S5：R2 / Image Access Security【今回採用】

注意：

Cloudflare R2というStorage製品そのものは、既存資料では【一旦採用】のラベルを持つ。

本S5では、

> **R2を採用する場合のSecurity / Access Exact Design**

を今回採用したものとして整理する。

最終横断監査で、R2選定自体を正式確定へ昇格するか再確認する。

---

## 7.1 Bucket

- Private Bucket
- Public `r2.dev` URLを使わない
- Public Custom Domainを使わない
- Masterの恒久公開URLを作らない

---

## 7.2 Signed URL

通常画像表示：

```text
Browser
↓
卓回廊Backend
↓
Current User所有権確認
↓
短時間Signed GET URL
↓
Derivative表示
```

Signed URL：

- Method：GET
- TTL：**5分**

Signed URLはFrontend Component State等で一時保持し、期限切れ時にBackendから再取得。

localStorage / IndexedDBへ永続保存しない。

---

## 7.3 Master Image

MVP：

> **Browser向けMaster取得Endpointを作らない。**

通常表示・編集UIではDerivativeを利用する。

新規Upload直後のPreviewはBrowser Local Fileを利用可能。

MasterはStorage内に保持するが通常Browserへ渡さない。

---

## 7.4 Derivative Response Metadata

```text
Content-Type: image/webp
Content-Disposition: inline
Cache-Control: private, no-store, max-age=0
```

MVPの

> User業務DataをBrowserへ永続Offline Cacheしない

方針と合わせる。

---

## 7.5 Object Key

Server生成。

```text
masters/{userId}/{imageUuid}.{ext}
derivatives/{userId}/{imageUuid}.webp
```

Keyへ含めない：

- Original File Name
- PC Name
- Scenario Name
- その他User入力名称

`imageUuid`はServer生成UUID。

---

## 7.6 R2 Credential

Production Backend：

- Production Bucket限定
- Object Read & Write相当の必要最小権限
- Account全体Admin Tokenを使わない
- Development / Production Credentialを分離

SecretはBackendのみ保持。

Frontendの`VITE_*`へ入れない。

---

## 7.7 Encryption

R2側標準のStorage暗号化を利用する。

MVPでは独自SSE-C Key管理は追加しない。

理由：

- Key Rotation Complexity
- Key紛失Risk
- MVP規模での追加価値とのBalance

---

## 7.8 画像削除Retry

画像差し替え既存仕様：

```text
新Master保存
↓
新Derivative生成
↓
DB更新
↓
成功
↓
旧画像削除
```

DB更新後にR2削除が失敗する場合へ対応。

Formal 9 Entityとは別のInfrastructure Table：

```text
storage_delete_task
```

Concept：

```text
id
object_key
attempt_count
next_attempt_at
created_at
last_error_code
status
```

Worker：

- 原則1分ごとに確認
- Exponential Backoff
- 1分 → 2分 → 4分 → 8分 ……
- Retry間隔最大24時間
- 30日成功しなければ`DEAD`
- Objectが既に存在しない場合は成功扱い

Entity削除 / Account削除 / 画像差し替えで利用する。

`storage_delete_task`はFormal Entityへ数えない。

---

# 8. S6：Logging / Backup / Security Operations【今回採用】

## 8.1 Production Logging

基本：

```text
Structured JSON
→ stdout
→ Render Logs
```

記録候補：

- timestamp
- level
- event_code
- request_id
- HTTP method
- route template
- HTTP status
- duration
- safe error code

Raw URLのID等をそのまま大量にLogせず、

```text
/api/pcs/{id}
```

等のRoute Templateを基本とする。

---

## 8.2 Logへ平文で出さない情報

以下はLogへ出さない。

- Cookie
- Session ID
- CSRF Token
- Authorization Header
- Google ID Token
- Google Access Token
- Google Refresh Token
- R2 Access Key
- R2 Secret Key
- DB Password
- DB Connection String
- Signed URL全文
- Request Body
- Response Body
- Import原文
- Upload File本文
- Image Binary
- その他Secret / Credential

Error Stack Trace内にも上記が混入しないようSanitizeする。

---

## 8.3 Security Event Log

記録対象：

- Authentication成功 / 失敗
- Rate Limit発動
- 所有権違反による403
- Upload Validation拒否
- Import重大処理失敗
- Account削除開始 / 完了
- R2削除Retry失敗
- Security上意味のある異常

ただしCredentialそのものは残さない。

Internal user_idは通常Access Logへ常時出さず、Security Incident追跡等で必要なEventに限定する。

---

## 8.4 Log Retention

MVPでは独自長期Log Platformを追加しない。

Render Logsを基本とする。

Retentionは利用Plan依存のため、Production Plan決定時に公式最新仕様を再確認する。

【最終監査対象】

- 法務 / Privacy上必要なLog Retention
- Incident Response上必要なRetention
- Backup復旧時Account削除再適用に必要なRetention

---

# 9. PostgreSQL Backup / Restore【今回採用】

## 9.1 Production条件

実User DataをProductionで保存する段階：

> **PITRを利用できるPaid Render PostgresをProduction基準とする。**

Development / 授業DemoではFree環境を利用してもよい。

ただし、Free環境をReal User Data ProductionのBackup基準にしない。

---

## 9.2 Backup

Primary：

> Render Point-in-Time Recovery

Secondary：

> 大きなFlyway Migration前にLogical Backup

MVPでは独自Daily DB DumpをR2へ長期蓄積する仕組みを追加しない。

理由：

- Personal Data複製箇所の増加
- Retention管理の増加
- 削除整合性のComplexity増加

---

## 9.3 Restore Runbook

復旧時：

```text
Application書き込み停止
↓
PITRで別Databaseを作成
↓
Schema / Flyway履歴 / 主要件数確認
↓
Restore時刻以降の削除対象を確認
↓
Account削除等を再適用
↓
DATABASE_URL切替
↓
Smoke Test
↓
Service再開
```

元Production DBを直接上書きしない。

---

## 9.4 Account削除とBackup

既存Formal Rule：

> Account削除 = User所有Application Data全体削除

対象：

- User
- Scenario
- Person
- PC
- Table
- TableDate
- Participation
- EndPcState
- ScenarioFavorite
- Import関連Data
- User Upload Scenario画像
- User Upload PC画像
- Auth側User

Google Account自体は削除しない。

古いBackup / PITR PointへRestoreした場合、削除済みAccountを復活させないよう、

> Restore Window中に発生したAccount削除EventをService再開前に再適用する

ことをRunbookへ含める。

【最終監査対象】

Privacy PolicyにBackup Retentionとの関係をどう明記するか。

---

# 10. Secret管理 / Rotation【今回採用】

既存：

- GitへCommitしない
- Render Runtime Environment / Secretで管理
- `VITE_*`へSecretを入れない
- Public値だけFrontendへ渡す
- Development / Productionを分離

Rotation：

- 定期：**180日**
- Incident発生時：**即時**

Incident例：

- 誤Commit
- 漏洩の疑い
- Credentialを保持した端末の紛失
- 権限保持者の離脱
- Security Incident

R2 Rotation Flow：

```text
新Token発行
↓
Render Secret更新
↓
Redeploy
↓
動作確認
↓
旧Token revoke
```

Google Client IDはPublic値としてSecret Rotation対象外。

---

# 11. Dependency / Container Security【今回採用】

CIで確認。

## Java

OWASP Dependency-Check Maven。

Release Gate：

- CVSS **9.0以上** → Build Fail

---

## Frontend

```text
npm audit --audit-level=high
```

High / CriticalをRelease Gate対象とする。

---

## Docker / Runtime

Trivy。

対象：

- OS Package
- Runtime Package
- libvipsを含むContainer Dependency

Critical：

- Build Fail

High：

- Review必須

---

## Scan Timing

- PR / Merge
- Production Release
- 週1回定期

自動`fix`を無検証でProductionへ直接適用しない。

---

# 12. Security Update SLA【今回採用】

目安：

| Severity | 対応 |
|---|---|
| Critical | Release停止。原則72時間以内にPatch / Mitigation |
| High | 原則7日以内 |
| Medium | 原則30日以内 |
| Low | 通常Maintenance |

Severityだけでなく、

- 卓回廊からReachableか
- 実際に利用している機能か
- Exploit条件
- Exposure

も確認する。

Fixが存在しない場合：

- 該当機能無効化
- 到達経路遮断
- Version固定
- その他Compensating Control

を検討。

理由なしにSuppressionだけ行わない。

---

# 13. S1〜S6完了状況

2026-09-30時点：

- S1 Upload Security：**完了**
- S2 Session Security：**完了**
- S3 Rate Limit / Abuse Protection：**完了**
- S4 PostgreSQL Physical Schema：**完了**
- S5 R2 / Image Access Security exact：**完了**
- S6 Logging / Backup / Security Operations：**完了**

ここでいう「完了」は、

> 現時点のTechnical Designとして実装へ進める程度に決定した

という意味。

次チャットで最終横断監査を行い、既存Formal Spec・法務・External Service・Security・UX間に矛盾がないか最終確認する。

---

# 14. 一旦採用のまま残す項目

以下は勝手に正式採用へ昇格させない。

## PWA配布方式

- Responsive Web App + PWA【一旦採用】
- Browser利用維持
- Install任意
- Native AppはMVP外

MVP Offline / Local Storage Policy自体は採用済み。

---

## Cloudflare R2

Object Storage製品として【一旦採用】。

S5のSecurity Exact DesignはR2を利用する場合の採用Design。

最終横断監査で、

- Security
- Privacy
- 法務
- Cost
- Operation
- Vendor依存

まで含め正式化可否を確認する。

---

## Derivative最大長辺

2048px【一旦採用】

実Browser / High-DPIで明確な画質問題が出た場合に再評価可能。

---

## vips-ffm + libvips

Image Encoderとして【一旦採用】。

Docker上でCompatibility問題が出た場合のみ再選定。

---

# 15. MVP外 / 将来課題

現MVP完成を阻害しない。

- Activity Export詳細Format
- Read-only Offline
- IndexedDB / Service Workerによる業務Data Offline
- Native App
- 高度なImport解析 / AI補助
- 共有機能
- Game System Profileの将来的なDB Master化
- Multi-instance時のDistributed Rate Limiter

---

# 16. 最終横断監査で確認すること

次チャットでは少なくとも以下を横断監査する。

## Formal Spec整合

- 9 Entity / 14 UC
- CRUD
- Entity Relation
- 削除Rule
- Table / TableDate
- Participation
- EndPcState
- Game System Profile
- Spotlight
- PC / Scenario画像
- Import

## Frontend / UX

- v5.9 Visualを壊していないか
- Login / Session UX
- Upload Error
- Offline
- Rate Limit
- Account削除
- Restore / Error時のUser体験
- Responsive / Keyboard / Focus / Reduced Motion

## Backend / Security

- Authentication
- Authorization
- CSRF
- Session
- CORS
- CSP
- Security Header
- Upload Security
- Rate Limit
- PostgreSQL Constraint
- R2
- Logging
- Secret
- Backup / Restore
- Dependency Security

## 法務 / Compliance

- Google認証
- Cookie / Session
- Personal Data / Third-party Person表示名
- User Upload画像
- External Image Reference
- BOOTH
- Import Data
- Retention
- Backup
- Account削除
- Log
- Render / Cloudflare R2等Subprocessor / External Service
- 利用規約
- プライバシーポリシー
- 著作権 / 画像利用

外部情報確認時は、一次資料・公式情報を最優先する。

---

# 17. 最重要禁止事項

今後も以下を勝手に行わない。

- v5.9 Mock DataをFormal Specへ追加
- SAN / HP / MP最大値をv5.9から逆輸入
- PC本体へ現在SAN / HP / MPを追加
- SKP Role / isMainKPを追加
- Child Entityへ冗長user_idを追加
- External BOOTH画像をR2へ複製
- Session TokenをlocalStorageへ保存
- Production DBをPublic Internetへ公開
- 認証APIへCORS `*`
- CSRF Protection無効化
- 既存Non-Unique項目を勝手にUnique化
- User判断なくFormal Entityを追加
- 一旦採用項目を勝手に正式採用へ昇格

---

# 18. 次工程

次工程：

> **最終横断監査**

別チャットで実施する。

監査では、

1. Webクリエイター
2. エンジニア
3. 法務・コンプライアンス

を最低限の独立視点として用いる。

必要に応じてマーケティング / User Value観点も補足する。

監査完了後、問題がなければ実装フェーズへ移行する。
