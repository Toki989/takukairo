# GitHub Pages体験版のBOOTH情報取得

**2026-10-28の結論：このWorker構成での公開体験版への対応は中止。** Workerの公開と稼働は正常だが、BOOTHはJSON/公開HTMLの両方にHTTP 403と「ブロックされました」「あなたの環境からはBOOTHにアクセスできません。」を返す。公開Workerのversionは`2026-10-28-error-page-summary`で確認済み。ユーザーのDeploy漏れではない。どの規則（IP評価・Bot判定・Workers判定など）に当たったかは応答からは特定できない。ユーザーの「もうむりそうならやめるのでいい」に従い、この構成での追加修正・再Deploy依頼はここで止める。ローカル版の実取得機能は保持する。公開体験版の自動取得を完成扱い・有効化しない。認証情報・Cookie・CAPTCHA回避・外部の無関係なProxyは使わない。

訪問者は体験版のシナリオ追加・編集画面で商品URLを入力し、「情報を取得」から候補を確認する。Cloudflareへの登録や設定は運営者のみが行う。訪問者のログイン・拡張機能・ファイル操作は不要。

## 403の原因調査（2026-10-28）

- 同じ生成済みWorkerコードをこのPCのNode.jsで実行すると、商品3403696を200で取得できる。同じUser-Agentの直接GETも200、BOOTHの応答Serverはcloudflare。
- 公開WorkerではOriginなし・`https://toki989.github.io`ありの両方で、JSONとHTMLがそれぞれ403。OriginありではWorker自身のAccess-Control-Allow-Originも正しい。したがって今回の失敗箇所はWorkerからBOOTHへの取得であり、Pages側のCORS設定や商品解析より前にある。
- この結果だけでは、IP評価・Bot判定・Workers経由の制限・その他の規則のどれかを断定できない。比較結果はローカルの `.runtime/booth-import/denial-investigation.json` に記録した。

診断版`2026-10-28-denial-diagnostics`は公開反映済み。JSON/HTMLの両方でServer=cloudflare、Content-Type=text/html、Cloudflareのエラーページのマーカーを確認。cf-mitigatedはなく、先頭16KiB内にはエラー番号や拒否メッセージが見つからなかった。ただし両方truncated=trueなので、未読の後半を確認せずに正確な原因は断定できない。結果は `.runtime/booth-import/deployed-denial-diagnostics.json`。

修正版`2026-10-28-denial-diagnostics-v2`もユーザーがDeployし、2026-10-28 15:05 JSTに公開反映と実応答を確認済み。JSON/HTML両方の応答は373,091 bytesでtruncated=false、全体を検査できた。ただし英語の既知定型句・エラー番号に一致せず、classificationはunclassified-denialのまま。Cloudflareエラーマーカーがあるだけでは具体的な拒否規則を証明できない。結果は `.runtime/booth-import/deployed-denial-diagnostics-v2.json`。JSONのRay IDは`a517c075dbd9e383-NRT`、HTMLは`a517c0781dcee383-NRT`。

定型句だけの診断では表示文言を確認できなかったため、`2026-10-28-error-page-summary`を作成し、ユーザーがDeploy。2026-10-28 15:10 JSTに反映と実応答を確認した。JSON/HTML双方のタイトルはBOOTH、見出しは「ブロックされました」、本文は「あなたの環境からはBOOTHにアクセスできません。」。英語の既知定型句しか見ていないclassificationはunclassified-denialのままだが、pageSummaryの日本語表示文言により接続環境への拒否を確認できた。証拠は `.runtime/booth-import/deployed-error-page-summary.json`。JSONのRay IDは`a517c6e51e71b498-NRT`、HTMLは`a517c6e64eb9b498-NRT`。応答は両方373,091 bytes、truncated=false。具体的な拒否規則までは不明で、必要ならBOOTH運営側にこの日時・Ray IDを基に確認してもらう範囲。問い合わせの送信は行っていない。

失敗時の`upstreamDiagnostics`は、JSON/HTMLそれぞれのstatus、content-type・server・cf-mitigated・cf-ray・retry-afterだけのヘッダー、本文2MiB以内の既知マーカー・エラー番号に加え、`pageSummary`（タイトル200文字、見出し6個まで各240文字、表示本文先頭1800文字）を返す。HTML/Script/CSS/SVGを除去し、数値文字参照を復元、IP・メールアドレス・URL・長いトークンを伏せる。生HTML・Cookie・訪問者のヘッダーは返さず、保存もしない。読み取り失敗時も元の403を残す。12件のUnit検査と共有TSのstrict型検査が成功。

`classification=cloudflare-challenge`なら認証チャレンジの証拠。Cloudflare公式は[`cf-mitigated: challenge`での識別](https://developers.cloudflare.com/cloudflare-challenges/challenge-types/challenge-pages/detect-response/)を説明している。エラー番号があれば番号に対応する規則を調べる。server=cloudflareだけでWAF/Bot制限と断定せず、pageSummaryの実際の文言と区別する。[Workersからのリクエストを区別して拒否する設定](https://developers.cloudflare.com/waf/custom-rules/use-cases/block-worker-subrequests/)も公式に存在するが、BOOTHがその設定を使っている証拠はまだない。[Ray IDによるサイト管理者側の調査](https://developers.cloudflare.com/fundamentals/reference/cloudflare-ray-id/)が必要なら、上記の日時・IDを提示できる。

Workerは公開商品情報を取得して返すだけで、DB・R2・KV・画像ファイル保存は使わない。シナリオ・PC・卓の記録、アップロード画像、ブラウザ内の保存データはWorkerへ送らない。送信するのは正規化したBOOTHの商品URLのみ。画像はBOOTHの配信先URLを返し、採用後もブラウザが配信元を参照する。

## 公開手順

1. CloudflareのWorkers Freeで `takukairo-booth-preview` を作成する。
2. 「Edit code」で初期コードを `worker.mjs` の全文へ置き換えてDeployする。JavaScriptの単一Moduleなので、追加ライブラリーやBindingは不要。
3. WorkerのURLを開き、`{"service":"takukairo-booth-preview","ready":true}` が返ることを確認する。
4. `/booth/preview?url=https%3A%2F%2Fbooth.pm%2Fja%2Fitems%2F3403696` で実商品情報を取得できることを確認する。
5. このリポジトリーの変更をmainへ反映する。既存のGitHub Pages Workflowが体験版をビルド・公開する。

このアカウントのWorkerは `https://takukairo-booth-preview.tokibs.workers.dev/booth/preview` だが、上記403があるため公開体験版へ自動接続しない。実取得が動く接続先を確認できた時点でのみ、GitHub Repository Variablesの `BOOTH_PREVIEW_URL` へHTTPS URLを設定してPagesを再ビルドする。未設定では公開体験版の自動取得は表示しない。APIキーや秘密情報はFrontendへ入れない。

Worker側の許可Originは `https://toki989.github.io`。公開サイトのOriginを変更する場合は、Workerの環境変数 `ALLOWED_ORIGINS` を更新する。複数Originはカンマで区切る。

CLIで管理する場合の構成は `wrangler.jsonc`。Dashboardに貼り付ける方法でも動作し、CLIの導入は必須ではない。

## コードの更新

正本は `frontend/booth-preview.ts`。Node.js 24で、リポジトリー直下から次を実行して `worker.mjs` を再生成する。

```powershell
./.tools/node/node-v24.21.0-win-x64/node.exe scripts/build-booth-worker.mjs
```

## 確認範囲と制限

- 入力URLから数字の商品IDを取り出し、BOOTHの商品JSON URLへ再構築する。JSONが403/404/406/415の場合だけ同じ商品の公開HTMLを1回確認する。任意ホストの中継には使えない。
- Cookieや認証情報は転送しない。Redirectは追跡しない。
- 取得時間18秒、応答2MiBまで。取得失敗時は入力を残し、手入力でも続けられる。
- 作者の明示クレジットがなければショップ名を候補にする。ゲームシステムの版が明示されていなければ空欄にする。
- Workers Freeの公開条件は[公式料金](https://developers.cloudflare.com/workers/platform/pricing/)と[公式制限](https://developers.cloudflare.com/workers/platform/limits/)を参照。2026-10-28確認では1日100,000リクエストまでで、Free上限超過時はError 1027になる。有料プランへ切り替えない。
- 非公開・ログイン必須の商品や、配信元の拒否・仕様変更では取得できない場合がある。

Unit検査は `frontend/qa/booth-preview.test.mjs`。体験版の画面検査では中継応答を明示的にStubし、候補の採用・画像URLだけの保存・再読み込み・失敗時の入力保持を確認する。公開Workerからの実取得確認は別途行う。
