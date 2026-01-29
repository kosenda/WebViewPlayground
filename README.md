## 概要

WebView周りで色々試したり知見をまとめたりするためのリポジトリ
知見などは一旦 [メモ.md](docs/メモ.md) にまとめる

## 資料

- [WebView でウェブアプリを作成する - AndroidDevelopers](https://developer.android.com/develop/ui/views/layout/webapps/webview?hl=ja)
- [2024年以降でも Android で WebView ベースのアプリを作るあなたへ - Zenn](https://zenn.dev/aldagram_tech/articles/bbb12025b9747d)
- [WebView wrapper for Jetpack Compose - Accompanist](https://google.github.io/accompanist/web/)

## 今後試したりする可能性があること

- WebViewの基本機能・動作関連
    - Cookie管理: CookieManagerを使った永続化やサードパーティCookie制御
    - キャッシュ制御: キャッシュモード（LOAD_DEFAULT/LOAD_CACHE_ELSE_NETWORK等）の挙動確認
    - スクロール位置の保存と復元: 画面回転時などの状態保持
- ファイル・メディア関連
    - ファイルアップロード: <input type="file">からのファイル選択（WebChromeClientのonShowFileChooser）
    - ダウンロード処理: DownloadListenerを使ったファイルダウンロード
    - 画像の長押し保存: HitTestResultを使った画像の取得・保存
    - メディア再生: 動画/音声の自動再生制御やフルスクリーン対応
    - カメラ・位置情報: PermissionRequestを使った権限ハンドリング
- レンダリング・表示関連
    - ダークモード対応: forceDark設定とprefers-color-schemeの連携
    - ズーム機能: ピンチズームの有効化/初期スケール設定
    - フォント設定: テキストサイズや最小フォントサイズの変更
    - Safe Browsing: セーフブラウジング機能の動作確認
    - ビューポート設定: レスポンシブデザインとの連携
- パフォーマンス・最適化
    - プリレンダリング: WebViewの事前初期化によるロード時間短縮
    - メモリ管理: WebViewの破棄タイミングとメモリリーク対策
    - オフライン対応: Service WorkerやApp Cacheの動作
    - レンダリングモード: ハードウェアアクセラレーションの有効/無効
- セキュリティ・認証関連
    - SSL証明書エラー処理: onReceivedSslErrorでの適切なハンドリング
    - Basic認証/Digest認証: onReceivedHttpAuthRequestでの認証処理
    - Mixed Content: HTTPS内でのHTTPリソース読み込み制御
    - WebView隔離: 複数WebViewインスタンス間のデータ分離
- UI/UX関連
    - プルトゥリフレッシュ: SwipeRefreshLayoutとの組み合わせ
    - エラーページカスタマイズ: オフライン時や404時の独自UI表示
    - コンテキストメニュー: 長押し時の独自メニュー実装
    - 通知: WebNotificationの取り扱い