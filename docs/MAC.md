# Macで卓回廊を使う

Apple Silicon（Mシリーズ）とIntel Mac用の起動・停止ファイルを用意しています。通常版はWindowsと同じReact画面・Javaバックエンド・PostgreSQLを使用します。

## 初回だけ行う準備

1. 卓回廊のフォルダー一式をMacへ置きます。ソースコード・`backend/mvnw`・`frontend/package-lock.json`・画像素材を含めてください。
2. [Node.js 24](https://nodejs.org/en/download)と[Temurin JDK 25](https://adoptium.net/temurin/releases/?version=25)をインストールします。固定の開発基準はNode 24.21.0 / npm 11.19.0 / JDK 25.0.4.1+1です。Apple Siliconはarm64 / aarch64、Intelはx64を選んでください。
3. [Docker Desktop for Mac](https://docs.docker.com/desktop/setup/install/mac-install/)をMacのチップに合わせてインストールし、起動を完了させます。PostgreSQL 18.6は起動ファイルが用意するため、通常は別途インストール不要です。
4. 「ターミナル」で卓回廊のフォルダーへ移動し、次を実行します。`cd `に続けてFinderからフォルダーをドラッグすると、実際の場所を入力できます。

```sh
cd "/卓回廊を置いた場所/takukairo"
chmod +x 起動.command 停止.command 体験版.command
node --version
/usr/libexec/java_home -v 25
docker compose version
```

Windowsの`.tools/`内のJava・Node・PostgreSQL実行ファイルはMacでは使用しません。Mavenは同梱Wrapperから固定Versionを取得します。初回の依存パッケージ・DBイメージ取得にはインターネット接続が必要です。`.command`とWrapperの改行は`.gitattributes`でLFに固定しています。

## 通常版の起動と停止

`起動.command`をダブルクリックします。初回は準備に数分かかります。準備が完了すると <http://localhost:5173/> が開きます。「固定テストユーザーでログイン」を選び、必要ならTRPG名を設定してください。

起動済みの同じアプリは再利用します。起動に必要な8080 / 5173番ポートや、別DBが5432番ポートを使用している場合は停止せずに案内します。`.env`がなければ`.env.example`から作成し、既存ファイルは上書きしません。環境変数をターミナルで明示している場合は、その値を`.env`より優先します。

終了時は`停止.command`をダブルクリックします。ブラウザや起動時のターミナルを閉じても、通常版は停止しません。停止ではPID・起動時刻・このフォルダーの実行パスを確認します。アプリと、今回の起動で開始したComposeのDBだけを停止し、保存データは削除しません。もともと動いていたComposeのDB、外部DB、別のアプリは停止しません。

ターミナルからも実行できます。実行権限が付けられない場合は`sh`で開けます。

```sh
sh 起動.command
sh 停止.command
# ブラウザを自動で開かない
sh 起動.command --no-browser
# 検査Profile（Rate Limit・定期Cleanupを停止）
sh 起動.command --qa --no-browser
```

最初のシナリオ・PCのテスト画像を投入する場合は、通常版の起動後に次を一度実行します。既存の画像がある項目はスキップします。

```sh
cd frontend
node qa/seed-images.mjs
```

## ブラウザ体験版だけを使う場合

Node.js 24だけを用意し、`体験版.command`をダブルクリックします。Java・Docker・DBは不要です。ビルド後に <http://localhost:4175/> を開きます。終了はそのターミナルでEnterまたはCtrl+Cを押します。

体験データは使用中のブラウザへ保存します。同じMacでもSafariとChromeの体験データは別です。通常版のDBとは連動しません。詳しい機能範囲は [体験版の説明](BROWSER_DEMO.md) を参照してください。

```sh
sh 体験版.command
# 配信開始・HTML取得・終了を自動確認する
sh 体験版.command --check-only
```

## 保存場所とWindowsからの移行

| 内容 | 保存先 |
| --- | --- |
| 通常版の記録・Import途中保存 | Docker Composeの`postgres-data` Volume内 |
| アップロード画像 | `.runtime/storage/`（標準設定） |
| 接続設定 | `.env` |
| 起動情報 | `.runtime/mac-processes.json` |
| ログ | `.runtime/backend.log` / `backend-error.log` / `frontend.log` / `frontend-error.log` |

**Macで初めて起動すると新しいテストDBが作られます。Windowsの記録は自動移行しません。** `.runtime/pgdata`やWindowsのPostgreSQL実行ファイルをMacへコピーして起動することはできません。既存の記録を引き継ぐ場合は、元DBを`pg_dump`で論理バックアップし、Mac側DBに`pg_restore`で復元します。画像の`.runtime/storage/`も同じバックアップ時点のものをコピーし、`.env`の`STORAGE_PATH`をMacで有効な場所へ合わせます。MacではDBがDocker Volumeにあるため、フォルダーをコピーするだけではDBのバックアップになりません。

DB・画像はセットで保持してください。起動・停止ファイルはDBのリセットや`docker compose down -v`を行いません。

## インストール済みPostgreSQLを使う場合

自分で起動・管理しているPostgreSQL 18のDBも使用できます。`.env`の`DB_URL` / `DB_USER` / `DB_PASSWORD`をそのDBに合わせ、次で起動してください。DBの作成・起動・停止は自分で行います。

```sh
sh 起動.command --external-db
```

通常起動ではローカルComposeの標準DB設定を使用します。独自のDB設定を検出した場合、`--external-db`の指定を案内します。

## 起動できないとき

| 表示・症状 | 確認すること |
| --- | --- |
| 実行権限がない | 初回の`chmod`、または`sh 起動.command`で実行 |
| Node.jsが見つからない | Node 24のmacOSインストーラーを使用。Homebrewの標準パスにも対応。nvm等で管理している場合はNode 24を選択したターミナルで`node scripts/mac-local.mjs start`を実行 |
| Javaが見つからない | JDK 25の導入と`/usr/libexec/java_home -v 25`。独自配置は`JAVA_HOME`を設定 |
| Dockerへ接続できない | Docker Desktopを起動し、起動完了を待つ |
| ポートが使用中 | 既存アプリを確認。同じ卓回廊の前回起動なら`停止.command`を実行 |
| 起動が途中で失敗 | `.runtime/`のログを確認。検証できた今回のアプリは自動停止し、データは保持 |
| フォルダーが異なるという表示 | アプリを停止してから移動する。古い場所のプロセスが停止済みか確認し、移動前の`mac-processes.json`を退避して再起動 |
| 起動・停止処理が進行中のままになる | 強制終了で残ったロックの復旧は下記を参照 |

macOSのセキュリティ機能がダウンロードしたファイルの実行を止めた場合は、システム設定の「プライバシーとセキュリティ」で対象を確認してください。

ロック復旧: `.runtime/mac-launcher.lock`（体験版は`mac-demo.lock`）の`pid`を確認し、`ps -p そのPID -o command=`で起動処理が終了済みであることを確認してから、対象のロックファイルだけを削除します。通常の終了・Ctrl+Cでは自動で解除します。自動的な古いロックの削除は、同時起動との競合を避けるため行いません。

## 検査

画像ライブラリをApple Silicon・Intel用のネイティブコードを含む`com.github.usefulness:webp-imageio:0.11.0`へ変更しています。[配布元の対応説明](https://github.com/usefulness/webp-imageio)も参照してください。

```sh
node --test scripts/mac-local.test.mjs
sh backend/mvnw -B -ntp -f backend/pom.xml -Dtest=LimitsParserImageTest test
cd frontend
npm ci
npm run typecheck
npm run build:demo
npx playwright install chromium webkit
npx playwright test --config playwright.mac.config.ts
```

`.github/workflows/mac.yml`はApple Silicon / IntelのMac runnerで画像処理、通常版の起動・二重起動防止・停止、Chromium / WebKit、体験版ランチャーを確認する構成です。CIのDBはHomebrewのPostgreSQL 18を専用領域・55432番ポートで起動するため、Mac runnerにDockerは不要です。これは検査構成の追加であり、CIを実行済みという意味ではありません。WebKitでの検査と実機Safariでの確認も区別します。
