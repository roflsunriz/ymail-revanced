# 検証記録

## 自動テスト

- 広告ホスト判定がYahoo!メール本体の`mail.yahoo.co.jp`を誤遮断しない
- 広告／販促リソースを幅0、高さ0、余白0、`gone`へ変換する
- メールの「プロモーション」分類リソースを維持する
- 広告権限と広告／計測Registrarだけを除去し、Firebase Messagingを維持する
- 販促専用メソッドだけを停止し、通常通知を扱うdispatcherを維持する

Gradle 9.7.1の`--warning-mode all`では、最新のReVanced patches plugin `v1.0.0-dev.11`が旧Project依存表記を使うというGradle 10向け警告が1件出ます。公式タグに新しい修正版がないことを確認済みで、Gradle 9.7.1のビルド・テスト・lint・RVP生成は成功します。Gradle 10へは上流修正後に更新します。

## 2026-08-31 実測

| 項目 | 結果 |
| --- | --- |
| `test :patches:buildAndroid` | 成功 |
| RVP内`classes.dex` | あり |
| RVP内`extensions/ymail.rve` | あり |
| 6.1.1適用 | `--force`なしで成功 |
| 6.2.5適用 | `--force`なしで成功 |
| 6.2.18適用 | `--force`なしで成功 |
| 3世代のAPK署名検証 | 成功 |
| 広告関連権限残存 | 各0件 |
| 有効な広告／Adjust／Billingコンポーネント | 各0件 |
| 広告・販促レイアウト | 全対象で`0dp, 0dp, gone` |

## Android ReVanced Manager

SH-R80P（Android 16、1260×2730、480dpi）で次を確認しました。

- Android用RVPをストレージから読み込み、「Yahoo!メール ReVanced Patches 0.1.0 / 1個のパッチ」として表示
- Yahoo!メール6.2.18単一APKを選択
- 「Yahoo!メール 広告除去」1件を選択
- Manifest／リソースデコード、パッチ、8 DEXコンパイル、リソースコンパイル、整列、署名がエラーなく完走
- Manager生成APKをストレージへ保存し、PCへ取得
- システムの更新確認画面まで到達

公式版とManager生成APKの署名が異なるため、ADB上書きは`INSTALL_FAILED_UPDATE_INCOMPATIBLE`で拒否されました。端末は非rootでmount方式も使えないため、既存データを保護してアンインストールは実施していません。このため、パッチ版を起動した実画面の目視確認は、同一署名の旧パッチ版またはデータ移行を許可した検証環境で継続します。

## 手動確認項目

- メール一覧の広告行が消え、前後のメールが空白なく詰まる
- メール本文下部の広告枠と影が消える
- ドロワー／旧サイドバーのバナーが消える
- 設定のLYP Premium誘導と販促ダイアログが消える
- 通常の新着メール通知とメール「プロモーション」分類が動作する
- 広告／Adjust／広告計測ホストへ通信しない
