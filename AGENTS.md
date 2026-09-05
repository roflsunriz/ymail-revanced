# AGENTS.md

## 作業開始前の必須手順（最優先・例外なし）

1. エージェントは、調査、計画、コマンド実行、スキル利用、ファイル編集、コミット、プッシュを始める前に、必ずリポジトリ直下の `.\COMMON-AGENTS.md` を開き、先頭から末尾まで全文を読む。
2. `COMMON-AGENTS.md` はGit管理外のシンボリックリンクである。`git`や既定のignore設定が有効な`rg --files`の検索結果だけで、ファイルが存在しないと判断してはならない。PowerShellでは最初に次を実行する。

```powershell
Get-Content -Raw -LiteralPath .\COMMON-AGENTS.md
```

3. 読み取りに失敗した場合、出力が省略された場合、または末尾まで読めたことを確認できない場合は、一切の作業を開始せず、パスとシンボリックリンク先を確認して全文を再取得する。必要なら分割して末尾まで読む。
4. 全文を読了するまで、ローカル `AGENTS.md` だけを根拠に作業を続けてはならない。読了後は `COMMON-AGENTS.md` を最優先の指針とし、読了直後の最初の進捗報告で全文を読了したことを明示する。
   このファイルでは `y!mail-revanced` 固有の補足だけを記載する。

## 目的
- Yahoo! Mail(ID:jp.co.yahoo.android.ymail)アプリ用のReVanced patchを作る
- 広告を除去する

## 広告除去の回帰調査

- `MailListAdViewContainer`は選択モード変更時の再bindで広告行の高さと可視性が復帰する。`OnGlobalLayout`での再collapseだけでは一覧の移動アニメーションを防げないため、`AdViewLayoutPatch.kt`で測定寸法を0に保つ。6.1.1、6.2.5、6.2.18の元DEXで同じクラスとRelativeLayout継承を確認した。通常メールのアニメーションは変更しない。
- ネットワーク境界の`WebView.loadUrl`書き換えでは`invoke-super`を維持する。仮想呼び出しを行う拡張ラッパーへ置換すると広告WebViewのoverrideへ再入し、Google AdsではRunnableを再投入し続け得る。URL引数だけを変換し、通常呼び出しとsuper/rangeを別途テストする。
- 実機のクラッシュ履歴が空でも、ユーザーが報告した間欠クラッシュを否定しない。修正候補と原因確定を区別し、詳細は`verification.md`へ記録する。
- Managerへ同名・同バージョンのローカルRVPを追加すると公開版と別ソースとして並び、両方が選択され得る。検証版は1ソースだけを選び、保存APKのDEXに修正が含まれることを確認してからインストールする。表示順だけで修正版と判断しない。
