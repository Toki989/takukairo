# Home Gallery：No.29適用後のVisual調整

2026-10-27。公開への反映をユーザーが確認した後、「すごいバランスが悪い」と指摘された状態から再開した。未反映や古いCI失敗の再調査ではなく、実画像を使った見た目の調整を行った。

## 資料の照合

- 追加No.29の全文を再確認。Flexbox / wrap / 各行中央配置、横長2単位・縦長と正方形1単位、順序と原画像比率の維持を優先した。JS Row PackingやGrid denseは導入していない。
- 実装正本R5.1.12のPart C §8〜13、§17を確認。画像が主役、通常情報は作品名と作者、補助情報はHover / Focus、Spacingと44px以上の操作領域を確認した。
- `仕様書（テスト用）参照資料/` 全17件を横断してHome配置・共通Visual・操作に関わる記載を調査。最新統合正本N-02A〜C / N-05 / N-06Aと、VisualResponsiveAccessibility §4.4 / §5.5を具体的な照合先とした。
- v5のHome描画とCSSも確認。画像からMetadataまで12px、名前・作者・補助情報の階層、見出しとSummaryのまとめ方を参照した。v5の固定比率とCropや旧Grid方式は、新しいNo.29より優先してコピーしない。
- PC Focus v5.9は卓詳細の参考実装。Home Galleryの配置基準には使用しない。

## 確認できた偏りと変更

| 観点 | 変更前 | 今回の調整 |
| --- | --- | --- |
| 行内の上下余白 | すべて上揃え。短い横長・正方形の下側へ余白が集中 | `align-items:center`で各作品とMetadataを含む展示単位を上下中央に配置 |
| Favoriteと卓数 | Metadataの後に44pxの独立した操作行。星が作品情報から離れて見える | 作者の右側と、その直下の補助情報領域に44pxの操作領域を重ね、独立した空の操作行を除去 |
| 一覧までの間隔 | ページ見出し・Summary・操作帯の余白が積み重なる | Homeに限って見出し余白とSummary・操作帯の間隔を縮めた。1440px幅のGallery上端は約566pxから約474pxへ移動 |

上下中央揃えとHome固有の間隔は、確定済みの新仕様と称するものではなく、今回のVisual Tuningで選んだ調整値。横方向のGap24px、Desktop Page Padding40px、2 / 1単位は維持した。別画面のページ間隔は変えていない。

原画像の幅と高さの比率は変更せず、固定高の画像箱・Crop・並べ替え・穴埋めを使っていない。したがって高さの違いから生じる余白自体は残る。No.29が求める左右分散に加え、上下の余白の偏りを調整した。

Favoriteは画像に重ねず、作者の右側に52pxの余地を確保した。作品名は全幅で表示する。作者なしの場合も操作領域を確保する。Hover / Focusでの260ms Fade、Touch時の常時操作、Reduced Motion対応を維持した。

## 検証

- 通常版ビルド・体験版ビルド（いずれもTypeScript検査を含む）が成功。
- `npm run test:demo` は18件すべて成功（約1.1分）。Sort / Filter / Search、全19作品、原画像比率、左右中央配置、キーボード、Touch、Hover、保存・Import等を検査した。
- 上下中央揃えに伴い、既存の行判定を上端から行の中心線に変更。行内は左→右、次の行は下方向というSort / DOM順の検査を維持した。作品名とFavorite操作領域が重ならないことも6画面幅で検査した。
- 別の独立ブラウザで作者なし・長い作品名と作者・画像なしを375 / 768 / 1440px幅で確認。作品名・作者とFavoriteが重ならず、44px以上の操作領域と横スクロールなしを確認した。
- DesktopのGallery全体と拡大画面、Mobile全体をスクリーンショットで目視確認した。

ローカル証拠（Git対象外）：

- `.runtime/gallery-balance-before.png`：変更前、1440px幅。
- `.runtime/gallery-balance-after.png`：変更後、1440px幅の全体。
- `.runtime/gallery-balance-after-detail.png`：変更後、Gallery先頭部分の実寸画面。
- `.runtime/gallery-balance-after-mobile.png`：変更後、375px幅の全体。
- `.runtime/gallery-balance-metadata-375.png`：長い情報・作者なし・画像なしの検査用Fixture。
- `.runtime/demo-report/index.html`：18件の自動検査結果。

公開へのpush・deployは行っていない。自動検査成功をVisual承認として扱わず、ユーザーによる見た目の確認はこれからとなる。
