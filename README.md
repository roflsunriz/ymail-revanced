# Yahoo!メール ReVanced

Yahoo!メール（`jp.co.yahoo.android.ymail`）から広告通信、広告表示枠、アプリ内の販促表示、販促通知を除去するReVancedパッチです。メール受信、プッシュ通知、メールの「プロモーション」分類など、本来のメール機能は維持します。

## ダウンロード

- [ymail-patches.rvp](https://github.com/roflsunriz/ymail-revanced/releases/latest/download/ymail-patches.rvp)
- `patches.json`登録用URL（長押しまたは選択してコピー）

```text
https://raw.githubusercontent.com/roflsunriz/ymail-revanced/main/patches.json
```

## 対応状況

- 対象パッケージ: `jp.co.yahoo.android.ymail`
- バージョン指定: なし
- 実適用確認済み: `6.1.1`、`6.2.5`、`6.2.18`
- Android用DEX入りRVP: 対応

## ReVanced Managerで使う

1. 上記の`patches.json` URLをコピーします。
2. ReVanced Managerの「パッチ」画面からパッチ追加を開き、「URLを入力」を選びます。
3. URLを貼り付けて「Yahoo!メール ReVanced Patches」を追加します。
4. 「アプリ」→「ストレージから選択」で、単一APK化したYahoo!メールAPKを選びます。
5. 「Yahoo!メール 広告除去」を選んでパッチを実行します。
6. 完了後、生成APKを保存してインストールします。

APKPureなどのXAPKは分割APKです。PCでは[APKEditor](https://github.com/REAndroid/APKEditor)の`merge`、AndroidではAnti Split Mなどを使い、先に単一APKへ変換してください。

### 既存アプリを更新できない場合

公式Yahoo!メールとReVanced Manager生成APKでは署名が異なるため、通常のAndroidでは`INSTALL_FAILED_UPDATE_INCOMPATIBLE`となり、公式版へ上書きできません。既存データを消さずに無理に回避しないでください。

- 同じReVanced署名鍵で作った旧パッチ版からは更新できます。
- 公式版から移行する場合は、Yahoo!メール側で同期状態と再ログイン手段を確認し、必要なデータを保護してから公式版をアンインストールします。
- root環境ではReVancedのmount方式を利用できる場合があります。

## 除去するもの

- Google Mobile AdsとYahoo!広告SDKの通信・初期化コンポーネント
- Adjust、広告計測用Firebase Analytics / Crashlytics / Sessionsの通信
- メール一覧の広告行、読み込み中・空・ミュート済み広告行
- メール本文下部広告、ドロワー／旧サイドバーバナー
- LYP Premium誘導、キャンペーン画像／Webダイアログ、ターゲット販促枠
- Yahoo!メールの販促通知スケジュールと表示処理
- 広告ID、Privacy Sandbox広告、Install Referrer、課金関連権限

広告枠は静的リソースと実行時の両方で幅・高さ・余白を0にし、`gone`へ変更します。広告SDKはManifestで無効化し、DEXのDNS、URL接続、WebView、URLビルダー境界でも遮断します。

## 開発

必要なもの:

- JDK 21以上
- Android SDK
- GitHub Packagesを読めるGitHubトークン

```powershell
$env:ANDROID_HOME = 'C:\path\to\Android\Sdk'
$env:ORG_GRADLE_PROJECT_githubPackagesUsername = 'your-name'
$env:ORG_GRADLE_PROJECT_githubPackagesPassword = 'your-token'
.\gradlew.bat clean test lint :patches:buildAndroid
.\scripts\verify-android-rvp.ps1 -Path .\patches\build\libs\patches-0.1.1.rvp
```

詳しい更新・検証方法は[how-to-update.md](how-to-update.md)と[verification.md](verification.md)を参照してください。

## ライセンス

[MIT License](LICENSE)
