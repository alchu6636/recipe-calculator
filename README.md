# 料理材料計算機 (Recipe Calculator)

料理の材料と比率を入力すると、実際に必要な重量を自動計算するAndroidアプリです。

## 機能

- 材料名と比率を入力
- 合計量を指定
- 各材料の必要量を自動計算
- パーセンテージ表示
- 複数の材料を追加可能

## 使い方

1. 「合計量 (g)」に作りたい料理の総重量を入力
2. 各材料の名前と比率を入力
   - 例: 小麦粉 = 3, 砂糖 = 1, バター = 2
3. 「材料を追加」ボタンで材料を追加
4. 「計算」ボタンで各材料の必要量を計算
5. 結果が表示されます

## 例

合計量: 600g
- 小麦粉: 3
- 砂糖: 1
- バター: 2

計算結果:
- 小麦粉: 300g (50%)
- 砂糖: 100g (16.7%)
- バター: 200g (33.3%)

## セットアップ

### 必要な環境

- Android Studio (最新版推奨)
- JDK 8以上
- Android SDK (API Level 24以上)

### ビルド手順

1. Android Studioを起動
2. "Open an Existing Project"を選択
3. このプロジェクトフォルダを選択
4. Gradleの同期を待つ
5. エミュレーターまたは実機で実行

### コマンドラインでのビルド

```bash
# Windowsの場合
gradlew.bat assembleDebug

# Mac/Linuxの場合
./gradlew assembleDebug
```

APKファイルは `app/build/outputs/apk/debug/` に生成されます。

## 技術スタック

- Kotlin
- Android SDK
- Material Design Components
- ViewBinding

## ライセンス

MIT License
