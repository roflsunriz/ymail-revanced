# プロジェクト固有の知見

共通指針と [AGENTS.md](AGENTS.md) を読んだ後、作業前にこの文書を全文読む。元の条件・確認日・未検証事項を保ったまま、固有の実現手段と再利用する知見を集約する。

## 共通指針の具体化と適用範囲

- 対象: 共通指針の「設計と実装方針」「検証」「更新手順」「ドキュメント」と、各節に明記した共通ルール。
- 理由・範囲: Kotlin/GradleでReVancedパッチとAndroid用DEXを生成するため、一般的なBunコマンドではなくGradleとRVP検査を使う。共通の検証義務、実機へのインストール承認、通常のメール機能を保護する条件は維持する。
- 代替手段・検証: 以下の実効コマンド・根拠を用い、[検証記録](verification.md) で成功・未検証を区別する。共通方針の完了条件やユーザーの明示指示は弱めない。

## AGENTS.mdから移した知見

## 広告除去の回帰調査

- `MailListAdViewContainer`は選択モード変更時の再bindで広告行の高さと可視性が復帰する。`OnGlobalLayout`での再collapseだけでは一覧の移動アニメーションを防げないため、`AdViewLayoutPatch.kt`で測定寸法を0に保つ。6.1.1、6.2.5、6.2.18の元DEXで同じクラスとRelativeLayout継承を確認した。通常メールのアニメーションは変更しない。
- ネットワーク境界の`WebView.loadUrl`書き換えでは`invoke-super`を維持する。仮想呼び出しを行う拡張ラッパーへ置換すると広告WebViewのoverrideへ再入し、Google AdsではRunnableを再投入し続け得る。URL引数だけを変換し、通常呼び出しとsuper/rangeを別途テストする。
- 実機のクラッシュ履歴が空でも、ユーザーが報告した間欠クラッシュを否定しない。修正候補と原因確定を区別し、詳細は`verification.md`へ記録する。
- Managerへ同名・同バージョンのローカルRVPを追加すると公開版と別ソースとして並び、両方が選択され得る。検証版は1ソースだけを選び、保存APKのDEXに修正が含まれることを確認してからインストールする。表示順だけで修正版と判断しない。

## Dependabotラベルの運用

- 明示指定のラベルはDependabotが自動初期化するとは限らない。既定ブランチの設定を正本とする専用workflowで不足分だけ維持する。検証と復旧は `how-to-update.md` のDependabotラベル節、回帰は `.github/tests/test-dependabot-labels.py`。既存の自動マージ・承認は別のworkflowであり、ラベルの変更で回避しない。

## READMEなどから移した開発・運用情報

## 開発

開発には次のものが必要です。

- JDK 21以上
- Android SDK
- GitHub Packagesを読めるGitHubトークン

```powershell
$env:ANDROID_HOME = 'C:\path\to\Android\Sdk'
$env:ORG_GRADLE_PROJECT_githubPackagesUsername = 'your-name'
$env:ORG_GRADLE_PROJECT_githubPackagesPassword = 'your-token'
.\gradlew.bat clean test lint :patches:buildAndroid
.\scripts\verify-android-rvp.ps1 -Path .\patches\build\libs\patches-0.1.4.rvp
```

詳しい更新・検証方法は[how-to-update.md](how-to-update.md)と[verification.md](verification.md)を参照してください。

## 依存更新の自動処理

Dependabot は対象の依存関係を毎週確認します。patch／minor 更新は PR のチェック（Build）が成功した後に自動で squash merge されます。CI の失敗ジョブは 1 回だけ再実行します。再失敗した PR は残して手動で修正します。major 更新は手動で確認します。

## 文書の役割

- [README.md](README.md): 利用開始と操作、利用者が知る必要のある制限。
- この文書: 固有の配置・ツール・実行手段・例外・再利用する調査知見。
- [verification.md](verification.md): 検証手順、実測結果、証拠、未検証事項。
- [how-to-update.md](how-to-update.md): 更新と復旧の手順。
