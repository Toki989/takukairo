# Home Scenario Gallery：No.29対応記録

## 適用した資料

ユーザーが2026-10-27に追加した[卓回廊_HomeGallery_左右余白と並び替え時の空白対策_No29確定事項まとめ.md](../../卓回廊_HomeGallery_左右余白と並び替え時の空白対策_No29確定事項まとめ.md)を適用した。採用日は2026-09-29。従来の資料に具体的に転記されていなかったHomeの配置方式・余白処理を補う資料であり、単なる新しい実装案として扱わない。

## 仕様と実装の対応

| 資料 | 採用済み事項 | 今回の対応 |
| --- | --- | --- |
| §2 | 横長2単位、縦長・正方形1単位。Desktopは1行約4単位 | 自然寸法からFrontendで分類。Flexのbasisを2 / 1単位に設定 |
| §3 | 各行を中央配置し、残余空間を左右へ均等に分配 | 行単位の `justify-content: center` を適用 |
| §4 | `display:flex`、`flex-wrap:wrap`、`justify-content:center` | GridをFlexboxへ置き換え |
| §4 | DOM・視覚・Sort・Keyboard順を一致させ、比率と構図を維持 | データ配列の順序をそのまま描画。画像はwidth:100% / height:auto |
| §5 | JS Row PackingとGrid denseはMVP不採用 | ResizeObserver、行span計算、grid-auto-rowsを削除。独自の詰め込みや並べ替えを行わない |
| §6 | 外側40px / Gallery Gap24pxは一旦採用。Breakpoint別の具体幅は未固定 | Desktop padding40pxを維持、Gap24px。既存のDesktop4 / Tablet3 / Mobile2単位構成を継続 |
| §7 | Sort・Search・Filter変更後も左右の残余空間を均等に分配 | 4種類のSort、Search、Favorite Filter、6画面幅で行ごとに実測 |

画像の高さの違いによる上下の展示余白は残る。No.29の目的はすべての穴をなくすことではなく、各行の横方向の残余空間を左右へ分けることにある。非表示のダミー作品や、行幅を満たすための並び替えは追加していない。

## 確認結果

- TypeScript検査、通常版ビルド、体験版ビルドが成功。
- 体験版ブラウザ検査18件が成功。
- 1440 / 1200 / 1199 / 768 / 767 / 375pxで行の左右の残余空間の差が1px未満であることを確認。
- ユーザー提示の横長「失われた遊園地」＋縦長「硝子温室の残響」の組合せを独立したブラウザFixtureで再現。1440pxでは従来右に346px残っていた空間が、左右各173pxに分配された。
- 検索で1件になった場合、お気に入りFilterで3件になった場合も中央配置を確認。
- 既存の画像比率、Sort順、Hover / Focus、Touch、Reduced Motion、保存・編集・CSV等の検査も成功。

画面: `.runtime/no29-two-works-desktop.png`。ブラウザの実データと追加資料の原文は変更していない。GitHub Pagesへのpush・公開操作は実施していない。

## GitHub ActionsでのFilter操作失敗への対応

公開処理#4（コミット`474792a`）はビルド成功後、No.29の複合検査でPlaywrightの `check()` が失敗し、deployがスキップされた。ユーザー提供のログでは、18件中17件が成功し、失敗は「クリック直後にCheckboxが切り替わらない」という判定だった。

ChromiumのCPUスロットリング倍率を6にした独立ブラウザで同じ失敗を再現した。失敗直後の状態を確認すると、URLは `favorite=true`、Checkboxはcheckedになっていた。URLクエリから制御する表示状態の反映より、Playwrightの即時確認が先に走っていた。

テストを次のように修正した。アプリの配置・Filter処理・No.29仕様は変更していない。

- 各Sort操作の後、URL、Selectの値、表示された全作品の順序、読み込み完了を待ってから中央配置を検査する。
- Checkboxは通常のClick後、再試行可能な `toBeChecked()` / `not.toBeChecked()` で実際の反映を待つ。URLのFilter条件と3件 / 19件という結果件数も検査する。
- 同じNo.29テスト自体をCPU6倍の条件で実行し、遅い描画環境でも結果を確認する。固定時間のsleepやforce操作、失敗を通すためのretry追加は使用しない。

修正後のNo.29テストは、CPUスロットリング倍率6の条件で3回連続成功した。その後、同条件を含む全18件も成功（49.3秒）。TypeScript検査・体験版ビルドも成功。GitHub上での再実行結果は、修正をpushした後に別途確認する。
