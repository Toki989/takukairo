# Frontend / Backend Contract照合

正本R5.1.12 Part Aを優先。下記は実装済みEndpointの照合記録で、新しいPublic DTOやEndpointの提案ではない。Method / PathはControllerとReactのAPI呼び出しを照合し、DB操作を含むHTTP検査とBrowser Flowで確認する。

## 業務API

Pathの先頭はすべて `/api`。

| 対象 | Method / Path | Request・Response・Version |
|---|---|---|
| Session | GET `/csrf`, GET `/session`, POST `/dev/session`, POST `/logout` | login後Fresh CSRF、固定subjectのみ、HttpOnly Cookie。Logout 204。Dev Beanはdev/test限定 |
| Self | POST `/self-person` | displayName。未設定のみ、Ownership一致 |
| Scenario | GET/POST `/scenarios`, GET/PATCH/DELETE `/scenarios/{id}` | 作成201、PATCHはversion、削除は未参照のみ。画像Keyは非公開 |
| Favorite | POST/DELETE `/scenarios/{id}/favorite` | 204、User+Scenario一意、idempotent |
| Duplicate | POST `/scenarios/duplicate-candidates` | name / authorName / sourceUrl。候補はValidation ErrorでなくUser判断 |
| Person | GET/POST `/persons`, PATCH/DELETE `/persons/{id}`, GET `/persons/{id}/impact` | PATCH version。search / recent、Recent最大5、Impactはdistinct Table |
| PC | GET/POST `/pcs`, GET/PATCH/DELETE `/pcs/{id}` | PATCH version。systemSettings省略=維持、[]=全削除、list=全置換。現在PLはchange-person経路 |
| PL change | GET `/pcs/{id}/change-person-context`, POST `/pcs/{id}/change-person` | pcVersion、newPersonId、選択Participation/Tableの各Version。全Rollback、PC Detach混在なし |
| Profile | GET `/game-system-profiles`, GET `/game-system-profiles/{profileKey}` | Catalog SummaryとDetailを区別。DetailにOptionSource bundle |
| Resolver | POST `/game-systems/resolve` | gameSystem → EXACT/SUGGESTED/UNKNOWN。初期3 Exact、Alias空、NFKC+trimのみ |
| Character | POST `/ccfolia/character-preview` | rawText、optional scenarioId/manualProfileKey。Previewは明示Apply、Raw恒久保存なし |
| BOOTH | POST `/booth/preview` | url。Mock Adapterのみ |
| Table | GET/POST `/scenarios/{id}/tables`, GET `/tables/{id}`, GET `/tables/{id}/detail`, PUT/DELETE `/tables/{id}` | Aggregate、親version。Child idのParent所属検証、PC変更時旧EndPcState削除＋保存をAtomicに実行 |
| History | GET `/pcs/{id}/appearances`, POST `/pcs/{id}/previous-end-state-candidate` | PreviousはStatusのみ、exact profileKey、Current Table除外。候補なし200 |
| Activity | GET `/activity/summary`, GET `/activity/by-month?year=...`, GET `/activity/by-year` | SelfPersonのPL/KP、distinct Table、開催日優先、日付なしcreatedAt |
| Image | PUT/DELETE `/pcs/{id}/image`, PUT/DELETE `/scenarios/{id}/image`, PATCH `/pcs/{id}/image-transform` | multipart/query/JSONのexpectedVersion。成功200で最新親version / imageを返す。FileとPC Transformは同じPUT |
| Image read | GET `/images/pcs/{id}/derivative-url`, GET `/images/scenarios/{id}/derivative-url` | Ownership後の短期URL。Local Adapterの `/dev-images/{token}` は認証・token所有者を検証。Storage Key非公開 |

## Import API

Baseは `/api/import/session`。Source Mutation、登録後Gate、Session Version、Candidate / Resolutionの所属とVersionをBackendが検証する。

| Method / Path | Command・Response |
|---|---|
| GET `/current`, POST Base, GET/PATCH/DELETE `/{id}` | currentなしはJSON null、作成201、PATCH expectedSessionVersion/currentStep、DELETE Query expectedSessionVersion |
| POST `/{id}/sources/text`, PATCH `/{id}/sources/{sourceId}` | Commandはtext。Source ResponseはrawText。両者を混同しない |
| POST `/{id}/sources/files` | multipart expectedSessionVersion + files。20FILE / 20MiB per FILE / 50MiB aggregate |
| PUT `/{id}/sources/{sourceId}/file` | multipart expectedSessionVersion + **file**正確に1件。ResponseはsourceId/sessionVersion。sourceId/order維持、最終容量current−old+new |
| DELETE `/{id}/sources/{sourceId}` | Query expectedSessionVersion。登録前のみ、候補・判断の無効化 |
| POST `/{id}/analyze`, POST `/{id}/analysis/reset` | expectedSessionVersion。REGISTEREDが1件でも禁止 |
| GET `/{id}/review` | sessionId/sessionVersion/summary/items。status/page/size |
| GET/PATCH `/{id}/candidates/{candidateId}` | Detail bundle、PATCH expectedSessionVersion/expectedVersion、candidateData/confirmationStatus/registrationTarget/resolutionChanges |
| GET `/{id}/resolutions` | **sessionId / sessionVersion / items envelope**。optional entityType |
| POST `/{id}/bulk-apply/preview`, POST `/{id}/bulk-apply` | targets各Version、operation APPLY_RESOLUTION/CLEAR_RESOLUTION/EXCLUDE/INCLUDE、ApplyはpreviewRevision |
| POST `/{id}/candidates/{candidateId}/split-preview`, POST `/{id}/candidates/{candidateId}/split` | explicit parts/bindings、未MappingはcanApply=false、ApplyはpreviewRevision |
| POST `/{id}/candidates/merge-preview`, POST `/{id}/candidates/merge` | sources各Version、mergedCandidateData、bindings/discardResolutionIds。対象不一致はnull＋Conflict、明示boolean必須 |
| POST `/{id}/register` | expectedSessionVersion、candidates各Version。resultsでPartial Register、同じresolutionIdだけcreatedEntityId共有 |
| POST `/{id}/complete` | expectedSessionVersion。全候補REGISTEREDまたはEXCLUDEDを確認 |

## Error・競合・復旧

Common Error: code / message / fieldErrors[] / traceId、必要なcontext。fieldErrors.pathはJSON Pointer。複数Field ErrorはSummary→Field Focus、単独はinline Error。HTTP 413はRequest全体、fieldErrors=[]としてField Business Limitと区別する。

所有者外や親子不一致は404に正規化。Mutationの古いVersionは409。Import Preview Requestの古いSessionは通常Conflict、Preview後のSnapshot変化は **IMPORT_PREVIEW_STALE優先**。Frontendは入力を保持し、既存GETで再同期して再Previewする。

FILE PUTでtimeout / network / 非解釈可能Response / 5xxなど結果不明なら、File Draftを保持する。自動PUT再送をせず、既存GET Session / Reviewで現在のVersionとDataを再取得する。ファイル名だけで成功判定しない。再送は再同期後のUser明示操作。

画像Metadata保存成功→画像処理失敗では、FileとDraftを保持し、Metadataを二重保存せず画像だけ再試行する。削除後もResponseの最新親Versionを使用する。

## MVP 14 UCの対応

| UC | 実装経路 |
|---|---|
| UC01 取り込み | Import INPUT、TEXT/FILE parser、正式登録 |
| UC02 確認 | REVIEW、Source Trace、Resolution表示 |
| UC03 欠損修正 | DETAIL、unassigned assignment、Autosave、Bulk/Split/Merge |
| UC05 新規卓 | TableForm、Scenario/Person/PCの手入力・再利用 |
| UC06 Scenario再利用 | ScenarioView → TableForm |
| UC07 PC再利用 | Participant ComboBox、Appearances |
| UC08 最近使用 | Person Recent、PC Recent、Previous Status Prefill |
| UC09 Scenario CRUD | Home/ScenarioView/ScenarioForm、Favorite |
| UC10 PC CRUD | PcCollection/PcForm、0..N Settings、PL変更 |
| UC11 Scenarioから履歴 | Archive Row、全開催日・参加者 |
| UC12 履歴検索 | Home search AND、role OR、Favorite Filter、Sort |
| UC13 卓詳細 | v5.9 Desktop3レーン、Mobile/Tablet再配置、Selector/Keyboard |
| UC14 履歴修正削除 | Table Aggregate PUT/DELETE、Version、Cascade |
| UC17 活動 | Activity summary/by-month/by-year、Home表示 |
