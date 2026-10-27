# 次チャット引継ぎ：体験版Home Galleryの見た目のバランス

更新日：2026-10-27（Asia/Tokyo）。対象ワークスペースは `C:\Src\takukairo`。

## 再開後の更新

ユーザーの「再開してください」を受け、上揃えによる上下余白の偏り、離れたFavorite操作、一覧までの余白を調整した。詳細は [Visual調整記録](reviews/home-gallery-visual-balance-2026-10-27.md)。No.29の方式・画像比率・順序は維持。通常版と体験版のビルド、ブラウザ検査18件が成功した。公開へのpushは行っておらず、見た目はユーザー未確認。

以下は再開前の引継ぎ記録。「今回メモのみ」「このチャットでは始めない」等は作成時点の過去の指示として読み、現在の作業停止指示には扱わない。

## 最初に把握すること

**No.29の中央配置は実装済みで、公開ページへの反映もユーザーが確認済み。ただし、ユーザーは「すごいバランスが悪い」と評価している。Visualの完成・承認には至っていない。**

最新のユーザー発言：

> できていましたが。すごいバランスが悪いですね。
> 別チャットに移行しますので、引き続きメモをお願いします。

この発言を受け、今回は引継ぎの記録だけを行う。UIの追加変更・再デプロイ・再テストは行っていない。次の作業は、未反映や古いCI失敗を調べ直すことではなく、**現在の公開版の見た目を確認し、設計に沿ってバランスを改善すること**である。

ユーザーが特に求めている作業姿勢：

- 体験版でも仕様書とUI資料に従う。
- v5だけ、または実装正本の一部だけで判断せず、関連する資料全体を確認する。
- 現在の実装を根拠に「設計どおり」と正当化しない。
- 自動テスト成功と、見た目のバランスがよいこと・ユーザー承認を混同しない。
- 不要な確認や提案だけで止めず、根拠を示して実際に確認・修正する。

## 正本と資料の優先関係

1. **追加されたNo.29追補を必ず読む**：
   [卓回廊_HomeGallery_左右余白と並び替え時の空白対策_No29確定事項まとめ.md](../卓回廊_HomeGallery_左右余白と並び替え時の空白対策_No29確定事項まとめ.md)
2. [実装正本R5.1.12](../卓回廊_Codex実装正本_2026-10-08_R5.1.12_最終Contract補強版.md) の読解優先順位とPart C §8〜13、§17（Home）。同TopicはPart Aの上書きを優先する。
3. `仕様書（テスト用）参照資料/` の資料全17件。特に最新統合正本のN-02A〜C、VisualResponsiveAccessibilityのHome Gallery・Motion・Focus関連、Scenario関連のVisual資料。
4. `TRPG_GAME_LIBRARY_FrontendPrototype_v5/` はHomeの参考実装。`TRPG_PCFocus_v5_9/TRPG_PCFocus_v5_9/` は卓詳細の参考実装であり、Homeの基準と混同しない。

追加No.29は新しい思いつきの提案ではなく、**2026-09-29に採用済みだった内容の補完資料**としてユーザーが追加したもの。前の資料でNo.29が未確定と書かれていても、追加資料の具体的な採用記録を無視しない。元の資料本文は変更しない。

### No.29で確定していること

- Landscapeは2単位、Portrait / Squareは1単位。Desktopは1行約4単位が基本イメージ。
- **CSS Flexbox：`display:flex; flex-wrap:wrap; justify-content:center`。**
- **各行を中央配置し、埋まらない横方向の空間を左右へ均等に配分する。**
- 並び替え・検索・絞り込みでも片側にだけ大きな空白を偏らせない。
- DOM順・視覚順・Sort順・Keyboard / Focus順を維持する。
- 画像の縦横比と構図を維持し、不必要にCropしない。
- **JavaScript Row Packing、`grid-auto-flow:dense`、穴埋めのための強制的な順序変更はMVP不採用。**
- 「Curated Grid」というコンセプト名はCSS Gridを使えという指示ではない。
- 空白をすべてゼロにする仕様ではない。
- Desktop Padding40px、Gallery Gap24pxは一旦採用の調整基準で、同じ数値にする指定ではない。
- 比率の分類しきい値、Breakpointごとの幅、最終px値には調整の余地がある。

## 現在の実装

主要ファイル：

- [ScenarioGallery.tsx](../frontend/src/components/ScenarioGallery.tsx)：Home用の作品一覧・自然寸法からの比率分類・補助操作。
- [ScenarioGallery.module.css](../frontend/src/components/ScenarioGallery.module.css)：Flexbox、単位幅、Hover / Focus、レスポンシブ。
- [Library.tsx](../frontend/src/features/Library.tsx)：Homeの検索・Filter・Sort・SummaryとGallery呼び出し。
- [Images.tsx](../frontend/src/components/Images.tsx)：画像取得・自然寸法のonLoad通知。
- [globals.css](../frontend/src/styles/globals.css)：ページ幅・Padding・共通UI。Gallery以外にも影響するため変更範囲に注意。
- [DemoChrome.tsx](../frontend/src/demo/DemoChrome.tsx)、[demo.css](../frontend/src/demo/demo.css)：体験版バナー、説明、リセット、Shell。
- [demo.spec.ts](../frontend/qa/demo.spec.ts)：体験版のブラウザ検査。

配置の現状：

- Flexboxで折り返し・各行中央寄せ。JavaScriptの高さ測定と行span計算は削除済み。
- `--units` はDesktop4 / Tablet3 / Mobile2。これは具体的な実装調整値で、No.29による全Breakpointの確定値ではない。
- `--work-gap:24px`。
- 1単位幅は `(100% - (units - 1) * gap) / units`、横長は2単位＋間のgap。
- 比率分類は幅/高さが1.2以上で横長、0.8以下で縦長、間は正方形付近。しきい値も調整値。
- 画像は幅100% / 高さauto / max-heightなし。9:16の縦長素材は大きく長くなる。
- `.page` はmax-width1440px / Desktop左右Padding40px。v5はmax1500px / Padding32pxだが、後発資料のPadding基準を優先する。最大幅1440px自体は実装判断。
- 通常表示は画像・作品名・作者。卓数と未Favorite操作はHover / Focusで260msフェード。Favorite済みの星は常時表示。Touchでは補助操作を表示。
- 補助操作に44pxの行を確保している。画像の高さ、作品名・作者、操作行、行間、バナー・Summary等を含めた**全体の見た目のバランスは未解決**。

上記を「今後もすべて固定すべき値」と扱わない。特に画像の大きさ・高さ差と、各行の構成・情報の位置が、ユーザーの不満に関わる可能性がある。ただし、不満の原因を推測だけで断定せず、現在の全体画面を実画像で確認すること。

## 公開・Gitの状況

- 公開URL：<https://toki989.github.io/takukairo/>
- origin：`git@github.com:Toki989/takukairo.git`
- 引継ぎ作成直前のHEAD：`50fb291`（修正）。その時点の作業ツリーはclean。
- 関連コミット：`474792a`＝No.29のHome修正、`50fb291`＝ブラウザテストの同期修正。
- ユーザーがコミット・pushしてきた。エージェントがpushしたものではない。
- 最新の反映は**ユーザーが確認済み**。この引継ぎ作成時にはGitHub APIや公開CSSを再取得していないので、最新Workflow番号・完了時刻は記録しない。

古いスクリーンショットに対して「最新版が配信されていないのでは」と何度か切り分けた履歴があるが、**最新の問題は反映ではなく見た目のバランス**である。

## CI失敗と修正済みの経緯

GitHub Actions #4（`474792a`）では、ビルド成功後に18件中1件の検査が失敗し、deployがスキップされた。

- 失敗テスト：`No29 centers every row after sorting, filtering and viewport changes`
- エラー：お気に入りCheckboxの `locator.check: Clicking the checkbox did not change its state`。
- URLクエリに制御されたCheckboxが更新を確定する前に、Playwrightがクリック直後の状態を確認していた。
- CPUスロットリング倍率6の独立ブラウザで同じ失敗を再現。失敗のすぐ後にはURLが`favorite=true`、Checkboxがcheckedになっていた。

修正済み：

- Sort後にURL、Select値、全作品の並び順、読み込み完了を待つ。
- Checkboxは通常Click後、再試行可能な`toBeChecked()`等で状態反映を待ち、URLと表示件数3件 / 19件も確認する。
- No.29の複合テスト自体をCPUスロットリング倍率6で実行、当該テストのみtimeout60秒。
- `force`、固定sleep、失敗を隠すretryは追加していない。

直近のローカル検証：

- TypeScript検査・体験版ビルド成功。
- 修正テストはCPUスロットリング倍率6で3回連続成功。
- その後の全18件も成功（49.3秒）。
- No.29対応時には通常版ビルドも成功。

これは機能・配置条件の検証結果であり、Visualの仕上がりがよいという判定ではない。ユーザーは現在の見た目を承認していない。

## 過去の判断ミスを繰り返さない

- 初回は固定高270px / Mobile180pxの画像枠を使い、作品を同じ箱へ収めるような見た目になっていた。
- 次に原画像比率を維持したGridへ変更したが、片側に大きな空きが残った。
- その後、JSによる高さ計算を追加した。これは追加されたNo.29の不採用方式に反するため、すでに削除している。復活させない。
- 当初は一部資料しか確認せず、「設計どおり」「禁止ルールが確認できない」と説明していた。後にNo.29追補が提供され、判断を更新した。
- ページ外側の余白と、行内に片側だけ残る空きを取り違えて説明した。これらを混同しない。
- v5を丸ごと正本とみなすことも、v5の具体実装を無視することも避ける。資料の役割・採用日・後発追補を照合する。
- 仕様に許容される余白と、見た目として悪いバランスは別に評価する。「仕様に禁止されていない」だけで完成扱いしない。

## 次のチャットで進める作業

1. 本メモとNo.29原文を読み、最新コード・Git状況を確認する。
2. 現在の公開版または同じビルドのローカル版を、Gallery全体と部分の両方で確認する。画面幅・ズームも記録し、添付画像の切り抜きだけで判断しない。
3. 実装正本、関連Visual資料、v5参考実装、19枚の実画像を照らし、画像サイズ・行のリズム・余白・Metadata・操作の位置のどこが不均衡かを具体的に整理する。
4. No.29の採用済み方式を維持したまま、根拠のあるVisual調整を行う。未確定の調整値と正式な仕様変更を区別する。
5. 最近遊んだ順 / 名前順 / 登録順、混在画像、少数件、検索・Favorite絞り込み、Desktop / Tablet / Mobileで実画面を確認する。
6. 関連する既存検査を通し、機能検査に加えて画面を目視する。テストPASSだけで完了宣言しない。

ユーザーは今回、別チャットへ移るためのメモだけを依頼している。このチャットでは上の作業を始めず、引継ぎで終了する。

## 起動・検査と証拠

- Windows：ルートの`体験版.cmd`。ビルドしてpreviewを開く。Enterで終了。
- `frontend`で`npm run dev:demo`：<http://localhost:5175/>。
- `npm run build:demo` → `npm run preview:demo`：<http://localhost:4175/>。
- `npm run test:demo`：専用の静的サーバーを4175番・`/takukairo/`で起動する。previewと同時に同じポートを使わない。
- この作業環境では`.tools/node/node-v24.21.0-win-x64/`のportable Nodeを利用できる。
- 通常版は別の起動方法・DBを使う。今回、その稼働状況は確認していない。通常DBのリセットや再seedは不要。
- 体験版の追加データはブラウザ内保存。ユーザーの保存内容を消さず、検査は独立ブラウザContextで行う。

関連資料：

- [体験版説明・公開手順](BROWSER_DEMO.md)
- [No.29実装・検証記録](reviews/home-gallery-no29-2026-10-27.md)
- [旧調査記録](reviews/gallery-layout-2026-10-27/README.md)：冒頭の更新を読む。No.29受領前の結論を現行仕様として扱わない。

ローカル画面・証拠（`.runtime/`はGit対象外。次チャットでファイルの存在を確認する）：

- `.runtime/demo-home-desktop.png` / `.runtime/demo-home-mobile.png`：最後の検査時の全体画面。
- `.runtime/no29-two-works-desktop.png`：横長＋縦長の2件Fixture。中央配置の確認用であり、19件全体の見た目の承認画像ではない。
- `.runtime/demo-filter-race.json`：テストの同期問題を再現した記録。
- `.runtime/demo-report/index.html`：最後のブラウザ検査結果。
- `.runtime/gallery-spacing-after.png`等は**No.29適用前のGrid方式の画面**であり、現在の期待値にしない。

公開ファイルとActions状態の読取は、過去の作業では通常Webツールから取得できず、権限付きシェルの公開HTTPS GETで確認できた。詳細ジョブログは認証が必要で、ユーザーにエラー部分を共有してもらった。現在は修正反映をユーザーが確認しているため、必要なくこの調査を繰り返さない。

## 次チャットへ貼る短い依頼文

> C:\Src\takukairo の docs/HANDOFF_HOME_GALLERY_2026-10-27.md を最初に読んでください。体験版のNo.29中央配置は公開に反映済みですが、Home Galleryの見た目のバランスが悪いままです。追加したNo.29資料、実装正本、関連UI資料、v5参考実装を確認し、実画像と実画面を比較しながら改善してください。自動テスト成功だけでVisual完成と判断しないでください。
