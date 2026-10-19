# 調査知見

- `MailListAdViewContainer` は選択モード変更時の再bindで高さと可視性が戻る。`OnGlobalLayout` の再collapseだけでは一覧アニメーションを防げないため、`AdViewLayoutPatch.kt` で測定寸法を0に保つ。6.1.1、6.2.5、6.2.18で同じ構造を確認済み。
- `WebView.loadUrl` の書換えでは `invoke-super` を維持する。仮想呼出しのラッパーへ置換すると広告WebViewのoverrideへ再入し、Runnable再投入ループを起こし得る。URL引数だけを変換する。
- ReVanced Managerでは同名・同版のローカルRVPと公開版が別ソースとして並ぶ。検証時は1ソースだけを選び、保存APKのDEXに修正が入ったことを確認してからインストールする。表示順で判定しない。
- 実機クラッシュ履歴が空でも間欠クラッシュを否定せず、修正候補と原因確定を区別する。詳細な証拠は [verification.md](verification.md) に置く。
- Dependabotの指定ラベルは自動作成されるとは限らない。既定ブランチを正本に専用workflowで不足分だけ維持し、自動マージworkflowとは分離する。
