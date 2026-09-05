# 検証記録

## 自動テスト

- 広告ホスト判定がYahoo!メール本体の`mail.yahoo.co.jp`を誤遮断しない
- 広告／販促リソースを幅0、高さ0、余白0、`gone`へ変換する
- メールの「プロモーション」分類リソースを維持する
- 広告権限を除去し、FirebaseのAnalytics／Crashlytics／Messaging Registrarを維持する
- 販促専用メソッドだけを停止し、通常通知を扱うdispatcherを維持する
- 広告コンテナのDEXを書き出して読み直し、選択時に行の寸法を復帰されても測定寸法0を維持し、通常ビューへ影響しない
- WebViewのsuper呼び出し（通常／range、ヘッダーあり／なし）はURL引数だけを変換し、通常の仮想呼び出しへ置換しない

## 2026-09-05 Pixel 10a 回帰調査

- インストール済み6.2.5で全選択・解除を録画し、解除時にメール行が一時的に上下移動することを確認した。
- 元APKの広告モデルは選択モードを内容比較に含み、再bindで広告行の高さを通常メール相当に復帰する。アプリが測定した後に`OnGlobalLayout`で0へ戻す競合を防ぐため、広告コンテナの`onMeasure`で0×0を維持する。
- 6.1.1／6.2.5／6.2.18の元DEXに対象広告コンテナが存在し、RelativeLayout継承、既存`onMeasure`なしを確認した。全3世代への修正版RVP適用は`--force`なしで成功した。
- Google Ads WebViewの`super.loadUrl`を拡張ラッパーへ置換すると、overrideがRunnableを再投入し続ける経路を確認した。super命令を残し、URL引数だけを遮断用に変換する修正を追加した。
- 未読メールを開く間欠クラッシュはユーザー報告。調査開始時のPixel 10aのcrashバッファにはYahoo!メールの例外が残っておらず、取得できた終了履歴はタスク削除だった。上記WebView不具合と報告されたクラッシュの因果関係は未確定であり、クラッシュ解決済みとは扱わない。
- `test lint :patches:buildAndroid`、RVPのDEX検証、6.2.5のPixel 10a ABI照合は成功。認証情報・ネットワークを使わず、既存Gradleキャッシュと`--offline -PgithubPackagesUsername=offline -PgithubPackagesPassword=offline`で検証した。
- 自動テストはパッチ15件、拡張6件が全件成功。CLI生成3世代のAPK署名を検証し、広告コンテナの0×0測定とGoogle Adsのsuper呼び出し維持を出力DEXでも確認した。
- Pixel 10aのManager 2.6.0で検証版`0.1.4-dev.1`を6.2.5のarm64元APKへ適用し、保存・署名まで成功した。保存APKのDEXで0×0測定を1件、Google Adsの保護済みsuper呼び出しを2件確認した。署名はインストール済み版と一致し、ABI照合と16KB ZIP alignmentも成功した。
- 検証APK: `ymail-6.2.5-regression-manager.apk`、SHA-256 `2990b0700a1f9e2802a4cc50a10b2d1868b8d6b4290e124195f52d6421776203`。上書きインストール承認待ちのため、修正後の全選択・単独選択、本文開閉、間欠クラッシュは実機未検証。

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

## 2026-09-01 Pixel 10a ABI互換性

Pixel 10a（Android 17、`arm64-v8a`のみ、16KBページ）で、`config.armeabi_v7a.apk`だけを含む6.2.18 XAPKから生成したv0.1.3適用APKは、Package Installerが`INSTALL_FAILED_NO_MATCHING_ABIS`（native library抽出失敗、`res=-113`）として拒否した。APKEditorやAnti Split Mによる単一APK化はCPU ABIを変換しないため、この入力はPixel 10aに対応しない。

`config.arm64_v8a.apk`を含む6.2.5 XAPKを単一化し、同じv0.1.3 RVPを`--force`なしで適用したAPKについて、次を確認した。

- `native-code: 'arm64-v8a'`
- minSdk 26、targetSdk 35
- APK Signature Scheme v2/v3検証成功
- `zipalign -c -P 16 -v 4`成功
- 全native libraryのELF `LOAD` alignmentが`0x4000`（16KB）
- Pixel 10aのReVanced Managerでも同じarm64入力への適用・保存が完走し、Managerの既存BKS署名鍵、APK Signature Scheme v2/v3、16KB ZIP alignmentを維持
- Manager署名版のインストール成功、インストール後の`primaryCpuAbi=arm64-v8a`
- Yahoo!ログイン画面まで起動し、`FATAL EXCEPTION`、`UnsatisfiedLinkError`、`dlopen failed`なし

最新版6.2.18にも`arm64-v8a`バリアントが存在するため、Pixel 10aではそのバリアントを取得して単一APK化する。端末とAPKのABIは`verify-apk-abi.ps1`でパッチ前後に照合する。

### v0.1.1 起動修正

v0.1.0を新規インストールした実機では、Yahoo!メール本体が起動時に`FirebaseCrashlytics.getInstance()`を呼ぶ一方、パッチがCrashlytics Registrarを削除していたため`FirebaseCrashlytics component is not present`でクラッシュした。v0.1.1ではFirebase Registrarを維持し、collectionフラグとDEXネットワーク境界で広告計測通信を遮断する。Manifest変換テストへAnalytics、Crashlytics、Messaging Registrarの維持を追加した。

### v0.1.2 起動修正

v0.1.1ではFirebase Registrarを復元した一方、広告計測SDKとして分類したFirebase Sessions内部のvoidメソッドまで`nop`化し、`DaggerFirebaseSessionsComponent`のProviderが未初期化になった。v0.1.2では全面`nop`化を広告SDKとAdjustだけへ限定し、Firebase Sessions／Crashlyticsのvoid APIを維持する回帰テストを追加した。

### v0.1.3 メール一覧修正

v0.1.2でログイン後にメール一覧を開くと、Yahoo!メール独自`AdView.setAdTheme()`まで広告SDKのvoid APIとして`nop`化され、広告行データバインディングがnullテーマを参照してクラッシュした。v0.1.3では直接無効化するAPIをGoogle Adsの`initialize/loadAd`系とAdjustの明示送信APIだけへ限定し、独自広告ViewとYahoo!広告SDKのsetterを維持する回帰テストを追加した。

メール一覧の広告行は消えて空白なく詰まったが、ドロワーの毎日くじ案内`incentive_cognition`とGmail追加案内`guide_imap_login`はData Bindingが可視性と寸法を繰り返し戻して再表示した。v0.1.3では各専用レイアウトとinclude先を幅・高さ0へ変換し、実行時には元ビューを拘束条件付きの0サイズプレースホルダーへ置換してData Bindingによる再表示を遮断する。`banner`、`target_text_position`、`guide_switch_gmail_account`と旧世代の`side_bar_list_target_text_position_item`も同じ方式で除去し、通常機能の`calendar_banner_body`とメール一覧ガイドは除外する。

SH-R80Pのログイン済み環境で、受信箱の広告行が空白なく消えること、本文下部の広告IDがUI階層に存在しないこと、ドロワー先頭から毎日くじとGmail案内が消えて「アカウント」へ詰まること、設定のLYP Premium誘導が消えることを目視確認した。メール本文の開閉、設定の全体スクロール、受信箱の更新後にも広告枠は再表示されず、`FATAL EXCEPTION`は発生しなかった。広告SDKの対象ホストは`UnknownHostException`で名前解決前に遮断され、外部接続は成立しなかった。

## Android ReVanced Manager

SH-R80P（Android 16、1260×2730、480dpi）で次を確認しました。

- Android用RVPをストレージから読み込み、「Yahoo!メール ReVanced Patches 0.1.0 / 1個のパッチ」として表示
- Yahoo!メール6.2.18単一APKを選択
- 「Yahoo!メール 広告除去」1件を選択
- Manifest／リソースデコード、パッチ、8 DEXコンパイル、リソースコンパイル、整列、署名がエラーなく完走
- Manager生成APKをストレージへ保存し、PCへ取得
- システムの更新確認画面まで到達

初回は公式版とManager生成APKの署名差により`INSTALL_FAILED_UPDATE_INCOMPATIBLE`になった。その後、ユーザー許可の下で公式版をアンインストールし、Manager生成の6.2.18パッチ版を新規インストールしてログインした。以降はManagerのBKS署名キーを維持し、最終v0.1.3も同じ署名で上書きしたため、ログインデータを保ったまま受信箱、本文、ドロワー、設定、更新操作を実画面で確認できた。

## 手動確認項目

- 全選択・解除、単独選択・解除を繰り返して広告行相当の上下バウンスがない
- 未読メールの初回表示、本文から一覧への復帰、次の未読表示を繰り返し、クラッシュ時は時刻と`AndroidRuntime`ログを取得する
- メール一覧の広告行が消え、前後のメールが空白なく詰まる
- メール本文下部の広告枠と影が消える
- ドロワー／旧サイドバーのバナーが消える
- 設定のLYP Premium誘導と販促ダイアログが消える
- 通常の新着メール通知とメール「プロモーション」分類が動作する
- 広告／Adjust／広告計測ホストへ通信しない
