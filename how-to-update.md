# 更新手順

## 前提

- JDK 21以上
- Android SDK
- GitHub Packagesを読めるトークン
- 検証対象の複数世代XAPK/APK
- 公式配布物のSHA-256を確認したAPKEditor、JADX、ReVanced CLI

## 1. 作業前確認

```powershell
git status --short --branch
git remote -v
git tag --sort=-version:refname
```

既存差分は戻さず、Yahoo!メールの最新XAPKと少なくとも2つの旧世代を`y!mail-apks/`へ保存します。

## 2. XAPKを単一APK化

```powershell
java -jar APKEditor.jar merge -i '.\y!mail-apks\Yahoo!+Mail_VERSION.xapk' -o '.\work\ymail-VERSION-universal.apk' -f -validate-modules
```

APKEditorの公式リリースに掲載されたSHA-256とローカルファイルを照合します。

## 3. SDK・リソース差分を確認

- Manifestの広告ID、Privacy Sandbox広告、Billing、Install Referrer権限
- Google Mobile Ads、Yahoo!広告SDK、Adjust、Firebase Analytics / Crashlytics / Sessions
- `mail_list_ad`、`message_list_ad_*`、`detail_footer_ad`
- `drawer_banner_item`、旧`ymail_sidebar_*banner`
- `lyp_premium_*`、`target_promotion_position`、`ymail_promotion_*dialog`
- 販促通知文字列指紋

メールの「プロモーション」分類用IDや画面は削除対象に含めません。

## 4. ビルドとテスト

```powershell
$env:ANDROID_HOME = 'C:\path\to\Android\Sdk'
$env:ORG_GRADLE_PROJECT_githubPackagesUsername = 'your-name'
$env:ORG_GRADLE_PROJECT_githubPackagesPassword = 'your-token'
.\gradlew.bat clean test lint :patches:buildAndroid
.\scripts\verify-android-rvp.ps1 -Path .\patches\build\libs\patches-VERSION.rvp
```

## 5. 複数世代へ実適用

各単一APKへ公式ReVanced CLIでRVPを適用し、`--force`なしで全世代が成功することを確認します。適用後は次を確認します。

- APK署名検証が成功する
- 広告関連権限が0件
- 広告／Adjust／Billingコンポーネントの有効残存が0件
- `BootstrapProvider`が1件
- 対象レイアウトと埋め込み要素が`0dp, 0dp, gone`
- 通常のメール「プロモーション」分類要素が残る

## 6. Android ReVanced Managerと実機

1. RVPまたは公開`patches.json`をManagerへ追加する。
2. 最新の単一APKを選択し、パッチがエラーなく完走することを確認する。
3. 生成APKを保存し、SHA-256と署名を確認する。
4. 同じ署名の旧パッチ版へ上書きし、起動、メール一覧、本文、ドロワー、設定画面を確認する。
5. 広告枠が消え、上下の内容が詰まっていることをスクリーンショットとUI階層で確認する。
6. 広告／Adjust／広告計測ホストへの通信がないことを確認する。

公式版とは署名が異なるため、データ保護を確認せずアンインストールしません。

## 7. リリース

1. `CHANGELOG.md`へ日付付きバージョンを追加する。
2. `gradle.properties`と`patches.json`のバージョンを一致させる。
3. 依存関係の脆弱性監査を実行し、警告を修正する。
4. 日本語Conventional Commitsでコミットし、`main`へプッシュする。
5. `vVERSION`タグをプッシュする。
6. Release ActionsがRVP、`patches.json`、CHANGELOG抜粋を公開することを確認する。
7. 全リリースに`patches.json`があることを確認する。

## ロールバック

- コードは直前の正常タグから再ビルドする。
- 端末は同一署名の直前パッチAPKへ上書きする。
- 公式版へ戻す場合は同期・バックアップと再ログイン手段を確認し、パッチ版をアンインストールして公式ストアから再導入する。
