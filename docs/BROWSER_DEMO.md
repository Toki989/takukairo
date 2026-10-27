# 卓回廊 ブラウザ体験版

ログインやアカウント作成なしで、TRPGのシナリオ・PC・卓の管理を試せるポートフォリオ用の静的サイトです。通常版のReact画面を使用し、体験版ビルドだけデータ処理をブラウザ内へ切り替えます。Java、PostgreSQL、外部API、Cookie認証は使いません。

## ローカルで開く

Windowsではルートの `体験版.cmd` をダブルクリックします。ビルド後にブラウザが開きます。終了するには起動した画面でEnterを押します。既存のportable Nodeがあれば自動で使い、なければインストール済みNodeを使います。初回のみ依存パッケージ取得のためネット接続が必要です。

コマンドで起動する場合は、Node.js 24.21.0を用意して次を実行します。

```sh
cd frontend
npm ci
npm run dev:demo
```

`http://localhost:5175/` を開きます。開発サーバーはCtrl+Cで終了できます。通常版の `npm run dev` は従来どおり5173番とバックエンド接続を使用します。

公開用のビルドを確認する場合:

```sh
cd frontend
npm run build:demo
npm run preview:demo
```

`http://localhost:4175/` を開きます。生成物は `frontend/dist-demo/`。HTMLをfile://から直接開くのではなく、HTTPで配信してください。

## GitHub Pagesへの公開

公開設定は `.github/workflows/demo-pages.yml` に用意しています。この実装作業ではリモートへのpushやPages設定の変更は行っていません。

1. 作成したファイルを含めてGitHubリポジトリの `main` へコミット・pushします。`frontend/public/demo-assets/` の画像と `frontend/src/demo/profiles.json` も必要です。
2. リポジトリの **Settings → Pages → Build and deployment → Source** で **GitHub Actions** を選択します。
3. **Actions → Browser demo on GitHub Pages → Run workflow** で `main` を選んで実行します。以後は `main` へのpushでも実行されます。
4. ビルドとブラウザ検査が成功すると、静的な `dist-demo/` だけをPagesへ配信します。URLはActionsの `github-pages` 環境に表示されます。

現在のorigin (`Toki989/takukairo`) の場合、標準設定での公開先は **https://toki989.github.io/takukairo/** です。これは公開後の想定URLであり、この作業での公開確認済みURLではありません。

体験版は相対アセットパスとHash Routerを使用します。`/takukairo/#/tables/1` のような詳細URLを直接開いたり再読み込みしたりしても、サーバーのSPA書き換え設定を必要としません。リポジトリ名が変わってもビルド設定の変更は不要です。

手順の参照元: [GitHub Pagesのカスタムワークフロー](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages)、[Viteの静的サイト公開](https://vite.dev/guide/static-deploy)。

## 体験できる操作

| 機能 | 体験版の動作 |
| --- | --- |
| 回廊 | テスト画像19枚を使ったシナリオ一覧、検索、並び替え、お気に入り、自分のPL/KP参加による絞り込み |
| シナリオ | 詳細、追加・編集・参照されていないシナリオの削除、画像の追加・削除 |
| PL / PC | テスト画像10枚、人物とPCの追加・編集、システム設定、PL変更、過去の登場卓 |
| 卓の記録 | 追加・編集・削除、開催日、KP/PL、HO、飾り台詞、終了時データ、PC切り替え |
| IMPORT | サンプルまたはUTF-8 CSVのプレビュー、項目修正、除外、登録、途中入力の保存 |
| 駒JSONの入力補助 | PC名、シートURL、SAN/HP/MPの簡易読み取り。形式は必要に応じて手動指定 |
| 初期化 | 体験版の保存データとIMPORT途中入力だけを削除し、最初のサンプルへ戻す |

Homeの一覧は画像の元の縦横比を保つ展示配置です。[No.29確定事項まとめ](../卓回廊_HomeGallery_左右余白と並び替え時の空白対策_No29確定事項まとめ.md)に従い、Flexboxで各行を中央配置し、残りの空間を左右へ均等に配分します。横長作品は2単位、縦長・正方形付近は1単位を使い、Sort順とDOM・Keyboard順を維持します。通常は作品名・作者を中心に表示し、卓数などの補助情報はHover / Focusで確認できます。Hoverのないタッチ端末では補助操作を表示します。最新の対応は[No.29の実装・検証記録](reviews/home-gallery-no29-2026-10-27.md)に記載しています。

IMPORTでは見出し `シナリオ名,ゲームシステム,卓名,開催日,KP,PL,PC` の7列を指定します。列順は変更できます。1行は1卓で、KPとPLを各1人指定可能です。引用符内のカンマ・改行、二重引用符のエスケープ、BOMとCRLFに対応します。最大100件・256KBです。登録前にシナリオ名、開催日、PCとPLの対応を確認します。登録は一括で保存し、途中行が不正なら回廊へ一部だけ保存しません。

同名・同ゲームシステムのシナリオ、同名人物、同PLの同名PCを再利用します。同じCSVを再度登録すると別の卓が追加されます。重複卓の推論や自動統合はしません。

本体の自由文解析・推論・高度な照合、候補の分割・結合、外部サービスの自動取得、実ユーザー認証・データ共有は体験版では実行しません。画面にもこの範囲を表示します。

## 保存と配信

- 作品名、作者名、人物、卓は架空の体験用データです。
- アプリへの入力と追加画像をサーバーへ送信しません。ページと同梱画像を静的に配信します。外部リンクを閲覧者が開く場合は、そのリンク先への通信が発生します。
- データはlocalStorageの `takukairo-demo-v1`、IMPORT途中入力は `takukairo-demo-import-v1` に保存します。PC表示密度は `takukairo-demo-pc-density` に保存し、通常版の設定と区別します。
- localStorageを利用できない場合は、ページを閉じるまでのメモリ保存に切り替え、画面に案内します。保存容量を超えた変更を、保存成功として表示しません。
- 画像追加はPNG/JPEG/WebP、1.5MB以下です。ブラウザの保存容量に依存するため、大量の画像を保管する用途には向きません。
- ブラウザや端末を変えると体験データは引き継がれません。サイトデータの削除でも初期状態になります。
- 同じブラウザで複数のタブを使う場合は、画面を再読み込みして最新状態を表示します。本体の競合制御や同期を再現するものではありません。

## 画像の由来

| 公開用素材 | 元の素材 |
| --- | --- |
| `frontend/public/demo-assets/scenario-01.jpg` 〜 `scenario-19.jpg` | `シナリオトレーラー画像テスト用asset/` のPNG全19枚 |
| `frontend/public/demo-assets/pc-01.png` 〜 `pc-10.png` | `テスト用PCasset/` のPNG全10枚 |
| 卓詳細の装飾 | 既存の `frontend/public/assets/decoration.png` |

元ファイルは変更していません。シナリオ画像は縦横比を維持し、長辺1200px以内・JPEG品質82の配信用コピーを作っています。PC画像は透過を維持してコピーしています。配信用29枚の合計は約7.9MBです。個別の対応は `frontend/public/demo-assets/sources.json` に記録しています。既存素材のライセンスや権利条件を変更するものではありません。

素材を再生成する場合は、Windows PowerShellで次を実行します。生成物をコミットするため、GitHub Actions上での変換ツールは不要です。

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/prepare-demo-assets.ps1
```

ゲームシステム設定は既存の `backend/src/main/resources/config/profiles.json` を体験版に同梱しています。このスクリプトで同時に更新できます。

## 検証

```sh
cd frontend
npm run typecheck
npm run build:demo
npx playwright install chromium
npm run test:demo
npm run build
```

専用ブラウザ検査は、ビルド済みファイルを `/takukairo/` 配下から配信する静的HTTPサーバーを使います。APIやSPA fallbackを提供せず、通常DBへアクセスしません。テスト用ブラウザの保存領域で、ログイン不要の起動、画像表示、保存と再読み込み、CRUD、CSV、初期化、保存制限、画面幅を検査します。結果は `.runtime/demo-report/`、画面は `.runtime/demo-*.png` です。

2026-10-26の実行結果: TypeScript検査、通常版ビルド、体験版ビルド、体験版のブラウザ検査13件がすべて成功しました。Windows起動スクリプトも `-CheckOnly` で配信開始・HTML取得・終了を確認しています。ブラウザ検査には375px / 768px表示、編集途中のリセット、保存領域のアクセス拒否と容量不足、閲覧者ごとの保存領域の独立性を含みます。GitHub Actions上での実行・Pagesへの配信は未実施です。
