# Mac対応の実装・検証記録（2026-10-28）

## 変更

- Mac用の`起動.command / 停止.command / 体験版.command`、FinderからのPATH設定、Node製ランチャーを追加。
- 通常版はMaven Wrapper・JDK 25・Node 24・Docker PostgreSQLを使用。外部管理DBは`--external-db`で使用可能。
- 起動処理の排他、ポート競合の検出、起動済みアプリの再利用、PID・起動時刻・絶対パスの照合、失敗時の後片付け、DBと画像の保持を実装。依存パッケージのキャッシュはOS・CPU・manifest・lockfileで区別する。
- `org.sejda.imageio:webp-imageio:0.1.6`を`com.github.usefulness:webp-imageio:0.11.0`へ変更。実際の配布jarで`native/Mac/aarch64/libwebp-imageio.dylib`と`native/Mac/x86_64/libwebp-imageio.dylib`を確認。Windows用のネイティブコードも含まれる。
- Macの初回準備・保存場所・移行上の違いを`docs/MAC.md`に記載。README、file://入口、体験版ガイドにMacの導線を追加。
- MacのApple Silicon / Intel runnerで起動から停止まで検査するCIを追加。まだリモート実行していない。

## WebKitで見つけた編集時の不具合

最初のブラウザ検査はChromium 20件成功、WebKit 16件成功・4件失敗。1件はテスト内でChromium専用CDPを呼んでいたため、CPU throttlingをChromiumでのみ行うように修正した。配置・検索・並び替えの検査自体はWebKitでも実行する。

残り3件はシナリオ・PC・卓の編集。保存済みデータの初期読み込みより先に入力欄が表示され、入力後に初期値が到着すると編集内容が上書きされていた。WebKitで、シナリオ編集直後の入力が空欄、入力イベント後には元の名前へ戻り、そのまま元の値を保存する挙動を再現した。

3画面とも保存済みデータをフォームへ反映するまで読み込み表示にし、失敗時の再取得ボタンを追加した。初期取得後の編集・保存・再読み込み、画像追加、削除を既存の操作テストで確認した。通常版と体験版は同じ編集コンポーネントを使用する。

## 実行済み

実行環境はWindows。Mac実機上の結果へ読み替えない。

| 検査 | 結果 |
| --- | --- |
| `node --test scripts/mac-local.test.mjs` | 9件成功。設定値の安全な読み込み、キャッシュ更新、起動競合、ロック保持、ポート競合、PID再利用時の停止拒否、データ保全、繰り返し停止 |
| `sh -n`（3つの`.command`と`mac-env.sh`を個別に検査） | 成功。各ファイルと`backend/mvnw`はLF・BOMなしを確認 |
| `LimitsParserImageTest` | 9件成功。WebP変換・透過・縮小・EXIF補正・サイズ制限・アニメーション拒否等 |
| `ContractIntegrationTest#*Image*+*image*` | 独立したPostgreSQL 18.6で3件成功。画像保存・差し替え・削除、Version再確認、失敗時の補償を確認 |
| Frontend typecheck | 成功 |
| 通常版ビルド | 成功 |
| 体験版ビルド | 成功 |
| `playwright.mac.config.ts` | Chromium 20件・WebKit 20件、計40件成功。失敗した3編集ケースも成功 |

ブラウザ結果は`.runtime/mac-browser-report/index.html`。通常DB・既存の途中保存は変更していない。テストは各ブラウザの独立した体験データを使用した。

バックエンド全体の初回実行は272件中129件成功、Dockerエンジン停止によりDB連携143件が環境エラーとなった。Docker起動後の全件再試行は、Windows PowerShellのログリダイレクトがMockitoの警告をエラーとして扱い中断した。終了コードを判定する実行方法に修正し、今回の変更に関係する画像API3件を再実行して成功した。画像APIのログは`.runtime/mac-image-integration.log`。バックエンド全件の完走・PASSは報告しない。

## 未検証

Mac実機でのFinder起動、Docker Desktop for Macを使った起動、Apple Silicon / Intelでのネイティブライブラリ実行、実Safari、追加したMac CIのリモート実行は未検証。Mac用CIでは独立したPostgreSQL 18を55432番ポートで起動して通常版の起動・二重起動防止・接続・停止を検査する。

起動ランチャーの単体検査をMac本体での起動成功と同一視しない。CI・commit・push・外部公開はこの作業から実行していない。
