# 変更履歴

このプロジェクトの重要な変更はこのファイルに記録します。

書式は[Keep a Changelog](https://keepachangelog.com/ja/1.1.0/)に従い、バージョン番号は[Semantic Versioning](https://semver.org/lang/ja/)に準拠します。

## [Unreleased]

## [0.1.0] - 2026-08-31

### Added

- Yahoo!メールのメール機能を保ったまま広告を除去できるように、`jp.co.yahoo.android.ymail`へバージョン指定なしで適用するReVancedパッチを追加した。
- 広告SDKの通信を全経路で遮断できるように、Google Mobile Ads、Yahoo!広告SDK、Adjust、広告計測用FirebaseのManifest停止とDEXネットワーク境界書き換えを追加した。
- 広告欄の空白を残さないため、メール一覧広告、本文下部広告、ドロワー／サイドバーバナーを静的・実行時の両方で幅0、高さ0、余白0、`gone`へ変換する処理を追加した。
- アプリ内セルフプロモーションを抑制するため、LYP Premium誘導、販促ダイアログ、ターゲット販促枠、販促通知専用処理を除去した。
- Android ReVanced Managerで配布物を利用できるように、Android用DEXと拡張RVEを含むRVP、ReVanced API形式`patches.json`、ビルド／リリースワークフローを追加した。
- バージョン差による退行を防ぐため、6.1.1、6.2.5、6.2.18の単一APK化・実適用・署名・リソース変換を検証する手順を追加した。

[Unreleased]: https://github.com/roflsunriz/ymail-revanced/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/roflsunriz/ymail-revanced/releases/tag/v0.1.0
