# PC Focus v5.9 Validation

## Webクリエイター視点

v5.8ではPC Selectorが参考画像に対してまだ少し太く、人物の見える量も多かった。
v5.9では幅のみを一段縮小し、細長い斜めスリットとして見える方向へ調整した。
activeとinactiveの幅差も縮め、選択中だけ別種の大型カードに見えないようにした。
高さ、斜め角度、段差、数px単位の密集感はv5.8の状態を維持している。

## エンジニア視点

DesktopのSelector幅のみを追加overrideし、skew方式・counter-skew・PC切替ロジックは変更していない。

Wide Desktop (1451px以上):
- inactive: max 94px
- active: max 108px
- Add: max 66px

1366-1450px帯:
- inactive: max 82px
- active: max 94px
- Add: max 66px

## 実ブラウザ検証

- 1672x941: PC1 / PC2
- 1680x800: PC1 / PC2
- 1440x900: PC1 / PC2
- 1366x768: PC1 / PC2
- 390x844: PC1 / PC2

確認結果:
- EndingとView Navの縦方向重なり: Desktop全条件 0px
- 横スクロール: 全条件なし
- PC1 -> PC2切替: 正常
- ArrowRightによるactive/focus追従: 正常
- Mobile: Desktop向け幅調整による退行なし

PC1選択時の中央情報右端からactive Selector左端までの実測gap:
- 1672x941: 0.17px
- 1680x800: 8.96px
- 1440x900: 11.92px
- 1366x768: 1.17px

## 扱い

v5.9は正式採用ではなく検証版。
