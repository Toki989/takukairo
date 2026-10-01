# Web制作総合演習1
# 卓回廊（TRPG活動履歴管理Webサービス）
## 追加確定事項まとめ
### 2026-09-30 Backend / Data / Security / Platform 追加確定・再確認事項まとめ

---

# 0. 本資料の位置づけ

本資料は、2026-09-30までに追加で検討・確定した

- Service名称
- Game System Profile
- Participation表示順
- Spotlight
- Upload画像
- Object Storage
- Backend / Frontend技術Stack
- Hosting / Deploy
- Session / CSRF / CORS / CSP等Security Architecture
- PWA / Offline
- Scenario外部画像候補
- Child EntityのUserID
- Import / Libraryの物理実装方針

を、既存の確定資料へ追加するために整理したものである。

既存のEntity / CRUD / UC / Visual Designを勝手に変更するものではない。

また、本資料には次の4種類を明示的に区別して記載する。

- Userが明示的に採用 / 確定した事項
- Userの「エンジニアを中心に決めてよい」という明示的委任のもとで採用した技術判断
- 既存資料ですでに確定しており、今回再監査で再確認した事項
- 一旦採用 / 未確定として今後の再評価余地を残す事項

最重要原則：

> **見た目はv5.9。中身は既存の確定済み設計。**

PCフォーカスビュー v5.9 はVisual Design / UI実装ベースであり、
Entity / Data属性 / CRUD / 業務Ruleの正本ではない。

---

# 1. 正式Service名【確定】

正式Service名は、

> **卓回廊（たくかいろう）**

とする。

- 正式名に英語名は使用しない。
- 「TRPG GAME LIBRARY」は正式Service名ではなく、Visual / Design Conceptとして扱う。
- 世界観は、遊んだScenario / Table / PCを、自分の回廊・美術館・Galleryのように収集し、展示し、振り返る方向とする。

---

# 2. Activity Export【将来機能として採用 / 現MVP外】

Activity Exportは、

> **将来実装する機能として採用する。**

ただし、現MVPには含めない。

現時点では以下を確定しない。

- CSV / JSON等の具体Format
- Export対象Field
- Imageを含めるか
- Backup / Restore用途
- 他ServiceへのImport用途

したがって、

> 採否は確定したが、具体仕様と実装PriorityはMVP外

として扱う。

---

# 3. 配布方式 / PWA【一旦採用】

卓回廊は、

> **Responsive Web App + PWA**

を基本とする。

- Browserから通常利用できることを前提とする。
- PWA Installは任意。
- Installを利用必須条件にしない。
- Manifest / App Icon / Standalone表示等を利用する。
- Native Appは現MVP対象外。
- Push Notification / Background Sync等も現MVPでは採用しない。

---

# 4. MVP Offline / Local Storage方針【採用】

MVPでは、

> **Install可能PWA + Online前提**

とする。

## 4.1 User業務Data

以下のUser Dataを、Offline閲覧目的でBrowserへ永続保存しない。

- Scenario
- PC
- Table
- TableDate
- Participation
- EndPcState
- HO
- Quote
- Person
- Import内容
- User情報
- その他認証後に取得する活動履歴Data

MVPでは、

- localStorage
- IndexedDB
- Service Worker Cache

等へ、これらのUser業務Dataを永続保存しない。

## 4.2 認証情報

以下をlocalStorage / IndexedDBへ保存しない。

- Session ID
- Google ID Token
- Google Access Token
- Google Refresh Token
- その他認証Secret

SessionはHttpOnly Cookieを利用する。

## 4.3 Offline時

Offline中はCreate / Update / Deleteを行わない。

既存UI Copy：

```text
オフラインです
現在は変更内容を保存できません。
インターネットに接続してから、もう一度保存してください。
```

接続復帰時は短い通知を行ってよい。

## 4.4 localStorageへ保存してよいもの

漏洩してもUser業務Data・認証DataのSecurityへ影響しないUI Preferenceのみ。

例：

- Mobile Grid Density 2 / 3 / 4列

Account / Device間Syncは行わない。

## 4.5 将来のRead-only Offline

将来候補として、

> **明示Opt-inのRead-only Offline**

を残す。

実装する場合も、

- Offline編集なし
- Session Token保存なし
- User切替時Data混在防止
- Logout / Account削除時にOffline Snapshot削除
- Master画像をOffline保存しない
- External BOOTH画像を意図的な永続Offline Cache対象にしない

等を前提とする。

現MVPでは実装しない。

---

# 5. External Image初回同意Timing【確定】

BOOTH等のExternal Image Referenceについて、

> **Userが外部画像候補をScenario画像として実際に採用しようとした最初の1回**

に説明・同意を行う。

以下では同意を要求しない。

- BOOTH情報取得
- 外部画像候補取得
- Preview表示

Userが、

```text
この画像を使用
```

等の操作で正式採用する時点で、初回のみ確認する。

一度同意済みのUserに対して、Scenarioごとに同じ確認を繰り返さない。

重要：

- この同意はRights Holderからの利用許諾を意味しない。
- 技術的に取得可能であることと、自由利用可能であることを混同しない。
- TermsにもExternal Image Referenceの仕組み・権利条件を記載する。

---

# 6. Scenario外部画像候補のDraft保持【確定】

BOOTH等から取得したExternal Image Candidateについて、

> **Userが正式採用していない候補は、Form内の一時Stateとしてのみ保持する。**

## 6.1 保持範囲

登録 / 編集画面を開いている間：

- 外部画像候補を保持してよい。
- Userは候補を再選択可能。

以下では破棄する。

- Page離脱
- Reload
- Cancel

## 6.2 保存しないもの

未採用候補を以下へ保存しない。

- Scenario正式Data
- DB Draft Entity
- localStorage
- IndexedDB

必要になれば再度BOOTH等から取得する。

## 6.3 正式保存

Userが、

```text
この画像を使用
```

を選択しScenarioを保存した時だけ、

> 正式なScenario External Image URL

として保存する。

External Image Fileそのものは卓回廊のObject Storageへ複製しない。

---

# 7. Game System Profile【確定】

SAN / HP / MP等を、全Game System共通の固定Statusとして扱わない。

既知Game Systemについては、

> **Canonical System Key + Game System Profile**

を利用する。

## 7.1 Profile選択基準

EndPcStateで利用するProfileは、

> **TableのScenario.gameSystem**

を基準とする。

PC.gameSystemを基準にしない。

## 7.2 Historical Meaning

EndPcState保存時に、

> **保存時点で利用したGame System Profile Key**

を保持する。

概念例：

```text
coc_7e_v1
```

Scenario.gameSystem / PC.gameSystemが後から変更されても、
過去EndPcStateのProfile Keyを自動で書き換えない。

## 7.3 Profile変更

既存Profileの意味を破壊的に変更しない。

意味互換性がない変更を行う場合は、

```text
coc_7e_v2
```

等の新しいVersion Keyを作る。

## 7.4 Profile配置

MVPでは、

> **Versioned Application Config**

として管理する。

- 正式9 Entityへ追加しない。
- User所有Entityにしない。
- MVPではDB Masterにしない。
- Java / JSON / YAML等のApplication Configとして保持可能。
- 将来DB Masterへ移行できる余地は残す。

---

# 8. Participation表示順【確定】

Participationに表示順を持たせる。

```text
display_order
```

## 8.1 Physical Rule

- Type：INTEGER
- NOT NULL
- 1-based
- Table × Role単位で管理

制約：

```text
UNIQUE(table_id, role, display_order)
```

## 8.2 新規作成

新しいParticipationは、

> 同一Table・同一Role内の末尾

へ追加する。

## 8.3 再採番

以下の時はBackend Transaction内で1..Nへ再採番する。

- Reorder
- Delete
- Role変更

## 8.4 KP / SKP

RoleとしてSKPを新設しない。

`isMainKP`等も追加しない。

```text
Role = KP && display_order = 1
→ UI上「KP」

Role = KP && display_order >= 2
→ UI上「SKP」
```

PL Participation Selectorにも同じdisplay_orderを利用する。

---

# 9. Spotlight永続化 / Physical DB【永続化＝既存確定 / Physical DB＝今回確定】

Spotlight表示選択はParticipation単位で永続保持する。

概念値：

```text
HO
QUOTE
NULL
```

挙動：

- HOのみ → HO
- Quoteのみ → QUOTE
- HO / Quote両方 → User選択を保存・復元
- 両方なし → NULL

HO本文とQuote本文は引き続き別Dataとして保持する。

## 9.1 Physical Column

Participationに、

```text
spotlight_type VARCHAR(16) NULL
```

を持たせる。

PostgreSQL native ENUMは使用しない。

DB CHECK制約：

```text
spotlight_type IS NULL
OR spotlight_type IN ('HO', 'QUOTE')
```

Java側：

```text
SpotlightType {
  HO,
  QUOTE
}
```

nullable Enumとして扱う。

理由：

- Flyway変更が容易
- JPA Mappingが単純
- PostgreSQL固有型依存を減らせる
- DB側でも不正値を拒否できる

---

# 10. Child Entityの冗長UserID【既存確定・今回再確認】

親Relationから所有Userを一意に辿れるChild Entityには、

> **user_idを重複保持しない。**

対象：

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
```

以下は作らない。

```text
TableDate.user_id
Participation.user_id
EndPcState.user_id
```

理由：

- ParentとChildのUser所有情報不整合を作らない
- 所有権Source of Truthを1つにする
- Composite FK / Trigger等の不要なComplexityを増やさない

ただしSecurityは弱めない。

Backendでは親Relationを含めてCurrent User所有Dataか確認する。

例：

```text
EndPcState
→ Participation
→ Table
→ Table.user_id == CurrentUser.id
```

FrontendからIDを直接指定されても、
Current User所有でないDataは取得・更新・削除不可。

ScenarioFavoriteは、

> User × Scenario

自体がEntityの意味なので `user_id` を持つ。

---

# 11. Upload画像の正式対象【確認・維持】

User UploadとしてObject Storageへ保存する画像は、現行Formal Specでは主に以下2種類。

1. PC画像 / 立ち絵
2. User UploadのScenario画像

以下は現時点の正式User Upload対象ではない。

- Table画像
- Participation画像
- Person画像
- Google Profile画像の恒久保存

BOOTH等のScenario External ImageはObject Storageへ複製しない。

---

# 12. Upload画像形式 / Validation【確定】

対応Format：

- JPEG
- PNG
- WebP

MVP非対応：

- GIF
- SVG
- HEIC
- HEIF
- AVIF

1画像最大：

> **10MB**

Validation：

- Extensionだけを信用しない。
- Backendで実画像としてDecode可能か確認する。
- MIME / Magic Number / Decode結果を確認する方向。
- EXIF Orientationを正規化。
- Aspect Ratio維持。
- 自動Cropしない。
- 小さい画像を無理にUpscaleしない。
- Alpha透明を維持。
- 透明Canvas Marginを自動Trimしない。

---

# 13. Upload画像 Master / Derivative【確定・一旦採用を区別】

## 13.1 Master【確定】

Uploadされた原本は、

> **Lossless Master**

として保持する。

PC画像のPosition / Zoomは表示Metadataであり、
Masterへ破壊的Cropを行わない。

## 13.2 Derivative Format【採用】

Web表示用Derivative：

> **WebP**

## 13.3 WebP Quality【採用】

> **Quality 85**

## 13.4 Derivative最大長辺【一旦採用】

> **2048px**

- 2048px以下の画像はUpscaleしない。
- Aspect Ratio維持。
- Alpha維持。
- Auto Cropなし。

実Browser / High-DPIで明確な画質問題が出た場合は再評価可能。

---

# 14. Upload画像差し替え / 削除【確定】

画像差し替え：

1. 新Master保存
2. 新Derivative生成・保存
3. DB更新
4. 上記すべて成功
5. 旧画像削除

途中失敗時は旧画像を維持する。

Entity削除 / Account削除時：

- User所有Upload画像を物理削除対象とする。

External Image URLは卓回廊が物理削除する対象ではない。

---

# 15. Object Storage【一旦採用】

Object Storage：

> **Cloudflare R2**

Bucket：

> **Private**

保存対象：

- PC User Upload画像
- Scenario User Upload画像

External Scenario Imageは保存しない。

基本構成：

```text
masters/
derivatives/
```

DBでは、

> Binary / 固定公開URLではなくStorage Key

を基本保持する。

Storage Provider依存をApplicationのStorage Service層へ隔離し、
将来Provider変更可能な構造にする。

---

# 16. Image Encoder【一旦採用】

Spring Boot Backendにおける画像処理は、

> **vips-ffm + libvips**

を本命とする。

目的：

- WebP
- Resize
- Alpha
- Quality設定
- Metadata制御
- Memory Efficiency

Native libvipsが必要なため、
DockerでRuntime Environmentを固定する。

Hosting上で明確な互換性問題が出た場合のみ再選定する。

---

# 17. Backend技術Stack【Engineer委任判断により採用】

Backend：

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- REST API

具体技術基盤：

- JDK：Eclipse Temurin 25 LTS
- Spring Boot：4.1.x系
- Build Tool：Maven 3.9.x + Maven Wrapper
- Database：PostgreSQL 18.x
- DB Migration：Flyway
- Session：Spring Session JDBC
- Test：Spring Boot Test + Testcontainers PostgreSQL

Version番号のExact Patchは、
実装開始時点でOfficial Stableを再確認してLockする。

---

# 18. DB Schema管理【Engineer委任判断により採用】

DB Schemaの正本は、

> **Flyway Migration**

とする。

本番でHibernateによる自動Schema生成を行わない。

基本：

```text
ddl-auto = validate
```

相当。

目的：

- JPA EntityとPhysical Schemaの不一致を起動時検知
- Migration履歴を明示
- Production Schema変更を自動推測に任せない

---

# 19. Google Login Backend Library【Engineer委任判断により採用】

Frontend：

> Google Identity Services（Sign in with Google Button）

既存仕様どおり、

- One Tapなし
- Auto Selectなし

Backend：

> Google公式Java Client / GoogleIdTokenVerifier

Backendで少なくとも以下を検証する。

- Signature
- issuer
- audience
- expiration
- token validity
- subject (`sub`)

Frontendから送信された`sub`文字列を直接信用しない。

Google ID / Access / Refresh TokenはアプリDBへ恒久保存しない。

認証完了後はApp独自Sessionへ切り替える。

---

# 20. Import Parser Library【Engineer委任判断により採用】

Backend Stack確定に伴い、Parser Libraryを以下とする。

CSV：

> Apache Commons CSV

HTML：

> jsoup

Markdown：

> commonmark-java

TXT / Pasted Text：

> Java標準処理

CSV Encoding既存仕様：

- UTF-8
- UTF-8 BOM
- CP932

Encoding判定は、
曖昧な自動推測へ依存しすぎず、既存仕様に沿って扱う。

---

# 21. BOOTH取得Library【Engineer委任判断により採用】

BOOTH取得：

- HTTP：Spring RestClient
- HTML Parse：jsoup

基本Flow：

```text
User入力BOOTH URL
↓
Product ID抽出
↓
Backend側で許可されたBOOTH URLを再構築
↓
RestClient
↓
HTML
↓
jsoup
↓
Candidate
```

重要：

> User入力URLを、そのまま任意URL Fetchへ渡さない。

SSRF対策を維持する。

BOOTH全体をCrawlerする機能ではなく、
Userが指定した1商品だけを対象とする。

---

# 22. R2接続Library【Engineer委任判断により採用】

Cloudflare R2とのBackend接続：

> **AWS SDK for Java 2.x S3 Client**

R2 Provider固有処理をStorage Service層へ隔離する。

Development / Production Credentialを分離する。

---

# 23. Frontend技術Stack【Engineer委任判断により採用】

Frontend：

- React 19系
- TypeScript
- Vite
- React Router
- Node.js LTS
- npm + package-lock.json
- CSS Modules
- Global Design Tokens
- SPA

使用しない：

- React Server Components
- SSR

FrontendとBackendはREST APIとして論理分離する。

v5.9 Visual Designを守るため、
UI Framework / Tailwindへ無理に寄せない。

---

# 24. Hosting / Deploy【Engineer委任判断により採用】

Hosting：

> **Render**

構成：

```text
Browser / PWA
      │ HTTPS
      ▼
Render Web Service
  ├─ React SPA
  └─ Spring Boot /api/**
      │ Private Network
      ▼
Render PostgreSQL

Spring Boot
      │ HTTPS / S3 API
      ▼
Cloudflare R2 Private Bucket
```

Render Region：

> Singaporeを基本とする。

Frontend / BackendはProductionで、

> **Same Origin**

とする。

REST APIとしての論理分離は維持する。

---

# 25. Production CORS【Engineer委任判断により採用】

Frontend / BackendをSame Origin配信するため、

> **Productionでは原則CORS不要**

とする。

Developmentのみ、

```text
http://localhost:5173
```

等の明示Originを許可する。

認証APIで、

```text
Access-Control-Allow-Origin: *
```

を使用しない。

---

# 26. App Session / Cookie【Engineer委任判断により採用】

App Sessionは、

> **Spring Session JDBC + PostgreSQL**

で管理する。

Redisは現MVPでは導入しない。

Cookie基本形：

```text
__Host-TAKUKAIRO_SESSION
Secure
HttpOnly
SameSite=Lax
Path=/
Domainなし
```

Session IDをJavaScriptから読ませない。

JWT / Session TokenをlocalStorageへ保存しない。

---

# 27. CSRF【Engineer委任判断により採用】

Cookie Session方式のため、

> **CSRF Protectionを無効化しない。**

POST / PUT / PATCH / DELETE等のState-changing Requestでは、
正しいCSRF Tokenを要求する。

SameSite属性だけを唯一のCSRF対策にしない。

---

# 28. CSP【Engineer委任判断により採用】

Content Security Policyを導入する。

導入順：

1. Report-Only
2. Compatibility確認
3. Enforce

基本方針例：

```text
default-src 'self'
object-src 'none'
base-uri 'self'
frame-ancestors 'none'
```

Google Identity Servicesに必要なOriginだけ追加する。

External Imageについても、
無制限に全HTTPS Hostを許可するのではなく、
必要HostをAllowlistする方向とする。

---

# 29. Security Headers【Engineer委任判断により採用】

使用するSecurity Header：

- Strict-Transport-Security
- X-Content-Type-Options: nosniff
- Frame防止
- Referrer-Policy: no-referrer
- Content-Security-Policy
- Permissions-Policy

外部Serviceへ卓回廊内の閲覧URL等を不要に送らないため、
Referrer Policyを強くする。

---

# 30. PostgreSQL Network Security【Engineer委任判断により採用】

Production PostgreSQLは、

> **Public Internetへ公開しない。**

Render Private Networkからのみ接続する。

PasswordだけでInternet公開する構成にしない。

---

# 31. Secret管理【Engineer委任判断により採用】

以下をGit RepositoryへCommitしない。

- DB Password
- R2 Secret Access Key
- Google関連Secret
- その他Credential

Render Runtime Environment / Secret管理を利用する。

Frontendの、

```text
VITE_*
```

はBrowser Bundleへ入るPublic設定として扱う。

Secretを `VITE_*` へ入れない。

Google Client ID等、
元々公開前提の値のみFrontendへ渡してよい。

---

# 32. Docker【Engineer委任判断により採用】

DeploymentはMulti-stage Docker Buildとする。

概念：

```text
Frontend Build Stage
→ React / Vite

Backend Build Stage
→ Java / Maven

Runtime Stage
→ JRE
→ libvips
→ Spring Boot
→ React build output
```

最終Runtime Imageへ不要なBuild Toolを残さない。

例：

- Node build tooling
- npm build environment
- Maven
- JDK Compiler

Applicationは、

> **non-root User**

で起動する。

---

# 33. R2 Image Access Security【Engineer委任判断により採用方針】

R2 BucketはPrivateのまま利用する。

通常表示：

```text
Browser
↓
卓回廊Backend
↓ User所有権確認
OK
↓
短時間有効なSigned URL
↓
Derivative表示
```

Master：

- 通常Browser表示へ使用しない。
- 恒久公開URLを渡さない。

Object Key：

- UserがUploadした元File名をそのままKeyにしない。
- Server生成Identifierを利用する方向。

Credential：

- Backendのみ保持。
- Development / Productionを分離。
- 必要最小権限。

Signed URL TTL等のExact値は未確定。

---

# 34. Import関連の既存確定事項【再確認】

Import一時Data：

- ImportSession
- ImportSource
- ImportCandidate
- ImportCandidateSourceTrace

正式9 Entityとは分離。

Import上限：

- 1 File 最大20MB
- 最大20 File / Session
- Session総量50MB
- Paste Text 2MB

Import Session：

- 1 Userにつき進行中最大1件
- STEP2 / STEP3 Auto Save
- 最終保存から7日保持
- 新規Import開始時は既存Session破棄確認

Import正式登録：

- Candidate単位でTransaction
- Candidate内の正式Entity登録はAtomic
- Partial Registration可

完了後：

- Raw Import Dataを48時間保持後削除
- Backend Cleanup Jobを原則1時間ごとに実行
- Account削除時は48時間待たず削除対象
- 正式9 EntityはCleanup対象へ含めない
- CleanupはIdempotent / Retry可能

---

# 35. Person削除条件【既存確定・再確認】

Personは以下をすべて満たす場合のみ削除可能。

- UserのSelfPersonではない
- 関連PC件数 = 0
- 関連Participation件数 = 0

削除時：

- PCをCascade変更しない
- Participationを書き換えない
- 別Personへ自動再割当しない

SelfPerson削除は通常Person削除では扱わず、
Account削除Flowで扱う。

---

# 36. EndPcState Physical Meaning【既存確定・再確認】

Game System Profileで数値Statusとして定義される値：

- Nullable Integer
- 負数可
- 小数不可
- Service共通Min / Maxなし

Growth：

- 任意Multiline Text
- 最大500文字
- 改行保持
- Skill / Before / After等へ構造化しない

Outcome：

```text
SURVIVED
LOST
NULL
```

- 任意
- 自由入力Outcomeにはしない

Aftereffects：

- 任意Multiline Text
- 最大500文字
- 改行保持
- 症状 / 期間 / 効果等へ構造化しない

---

# 37. 今回確定していないもの【重要】

以下を本資料で勝手に確定した扱いにしない。

## 37.1 Upload Security exact値

未確定：

- 最大Decoded Pixel数
- 最大Width
- 最大Height
- libvips処理時Memory Limit
- 同時画像処理Concurrency

10MB File SizeだけではDecompression Bomb対策として不十分なため、
次工程で決定する。

## 37.2 Session Security exact値

未確定：

- Idle Timeout
- Absolute Lifetime
- Concurrent Session
- Remember Login有無
- CSRF Token Lifecycle exact
- Login時Session Rotation exact
- Logout後Cookie処理exact

## 37.3 Rate Limit

未確定：

- Auth
- BOOTH Fetch
- Upload
- Import
- Search
- API

のUser / IP / Endpoint単位Rate Limit値。

## 37.4 PostgreSQL Physical Schema exact

未確定：

- 全FK exact
- 全NOT NULL
- 全UNIQUE
- 全CHECK
- Index
- ON DELETE
- VARCHAR Length
- Timestamp Type
- Optimistic Lock
- Initial Flyway Migration

論理Data Modelは変更しない。

## 37.5 R2 exact

未確定：

- Signed URL TTL
- Master取得Endpoint
- Derivative Cache-Control
- Content-Type / Content-Disposition exact
- Object Key Naming exact
- 画像削除失敗Retry Job / Queue

## 37.6 Logging / Backup / Security Operations

未確定：

- Log Masking detail
- Backup / Restore procedure
- R2 cleanup retry detail
- Secret Rotation
- Dependency Vulnerability Check
- Security Update運用

## 37.7 Terms / Privacy正式本文

画面構成は確定しているが、
正式な利用規約 / Privacy Policy本文は別途作成・最終確認する。

## 37.8 Activity Export detail

将来機能として採用済みだが、
Format等の具体仕様は未確定。

## 37.9 Read-only Offline detail

将来候補。
MVPには含めない。

---

# 38. 次工程【確定】

実装前Security / Physical Designとして、原則以下の順で進める。

1. Upload画像 Decompression Bomb対策
   - 最大Decoded Pixel数
   - 最大Width / Height
   - Memory / Concurrency

2. Session Security exact値

3. Rate Limit / Abuse Protection

4. PostgreSQL Physical Schema最終化

5. R2 Image Access Security exact値

6. Logging / Backup / Security Operations

これらは、
既存Formal Specを変更する工程ではなく、

> **既存の確定仕様を安全に実装可能なPhysical Designへ落とし込む工程**

として扱う。

---

# 39. 実装時のSecurity最重要原則

以下を維持する。

- Frontend表示制御だけを認可としない
- Backendで必ずCurrent User所有権を確認
- User入力ID / UserIDを信用しない
- Google `sub`文字列をFrontendから直接信用しない
- TokenをlocalStorageへ保存しない
- Production DBをPublic Internetへ露出しない
- CSRF Protectionを無効化しない
- Auth APIでCORS `*` を使わない
- Upload File extensionだけを信用しない
- External URLをそのまま任意Fetchしない
- R2 BucketをPublic化しない
- SecretをGit / Browser Bundleへ含めない
- Stack Trace / Internal Information / 他User情報をError Responseへ露出しない
- Formal SpecにないMock DataをDBへ追加しない

---

# 40. 本資料追加後の解釈Rule

今後、古い資料に

> 「未確定」

と書かれている事項でも、
本資料または2026-09-29〜09-30の後続確定資料で解決済みなら、

> **後続の明示的な確定を優先する。**

逆に、
本資料で「未確定」と明記したExact値・運用詳細は、
推測でFormal Specへ補完しない。

---

以上を、
2026-09-30時点の

> **Backend / Data / Security / Platform 技術設計 追加確定事項**

として扱う。
