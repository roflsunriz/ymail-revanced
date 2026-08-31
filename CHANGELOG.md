# 変更履歴

このプロジェクトの重要な変更はこのファイルに記録します。

書式は[Keep a Changelog](https://keepachangelog.com/ja/1.1.0/)に従い、バージョン番号は[Semantic Versioning](https://semver.org/lang/ja/)に準拠します。

## [Unreleased]

### Fixed

- Pixel 10aなど64-bit専用端末で32-bit XAPK由来のパッチAPKが「非対応」になることを事前に検出できるように、APKと実機のCPU ABI照合スクリプトを追加し、`arm64-v8a`バリアントの選択手順を明記した。

## [0.1.3] - 2026-08-31

### Fixed

- ログイン後のメール一覧で広告行データバインディングがnullテーマを参照しないように、void APIの無効化をGoogle Adsの初期化／読込とAdjustの明示送信APIだけへ限定し、Yahoo!メール独自広告Viewのsetterを維持した。
- ドロワーの毎日くじ案内`incentive_cognition`とGmail追加案内`guide_imap_login`がData Bindingで再表示されないように、専用レイアウトとinclude先を幅・高さ0へ変換し、実行時には拘束条件を保つ0サイズのプレースホルダーへ置換した。

## [0.1.2] - 2026-08-31

### Fixed

- Firebase SessionsのDaggerコンポーネントを壊さず起動できるように、void APIの全面無効化対象をGoogle／Yahoo!広告SDKとAdjustだけへ限定し、Firebaseはcollection無効化とネットワーク境界遮断で通信を止める方式へ修正した。

## [0.1.1] - 2026-08-31

### Changed

- CIと依存更新PRを安定させるため、JUnit JupiterとPlatform Launcherを整合する6.1.3へ同時更新し、Gradle wrapperを9.7.1、actions/labelerを7へ更新した。

### Fixed

- Yahoo!メール本体が起動時にCrashlyticsを取得してもクラッシュしないように、Firebase Registrarは維持し、collection無効化とDEXネットワーク境界遮断で通信を止める方式へ修正した。

## [0.1.0] - 2026-08-31

### Added

- Yahoo!メールのメール機能を保ったまま広告を除去できるように、`jp.co.yahoo.android.ymail`へバージョン指定なしで適用するReVancedパッチを追加した。
- 広告SDKの通信を全経路で遮断できるように、Google Mobile Ads、Yahoo!広告SDK、Adjust、広告計測用FirebaseのManifest停止とDEXネットワーク境界書き換えを追加した。
- 広告欄の空白を残さないため、メール一覧広告、本文下部広告、ドロワー／サイドバーバナーを静的・実行時の両方で幅0、高さ0、余白0、`gone`へ変換する処理を追加した。
- アプリ内セルフプロモーションを抑制するため、LYP Premium誘導、販促ダイアログ、ターゲット販促枠、販促通知専用処理を除去した。
- Android ReVanced Managerで配布物を利用できるように、Android用DEXと拡張RVEを含むRVP、ReVanced API形式`patches.json`、ビルド／リリースワークフローを追加した。
- バージョン差による退行を防ぐため、6.1.1、6.2.5、6.2.18の単一APK化・実適用・署名・リソース変換を検証する手順を追加した。

[Unreleased]: https://github.com/roflsunriz/ymail-revanced/compare/v0.1.3...HEAD
[0.1.3]: https://github.com/roflsunriz/ymail-revanced/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/roflsunriz/ymail-revanced/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/roflsunriz/ymail-revanced/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/roflsunriz/ymail-revanced/releases/tag/v0.1.0
