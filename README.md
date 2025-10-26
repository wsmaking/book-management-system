# 書籍管理システム

Spring Boot + Kotlin + jOOQ + PostgreSQL による書籍管理システムのバックエンドAPI

## 🚀 クイックスタート

### 前提条件

- **Docker Desktop**（起動済み）
- **Java 21**

### ワンコマンド起動
```bash
./run.sh
```

以下のメッセージが表示されれば成功です：
```
🎉 All smoke tests passed!
📍 Application is running at http://localhost:8080
```

自動的に以下の処理が実行されます：
1. PostgreSQL コンテナの起動
2. データベースマイグレーション（Flyway）
3. アプリケーションのビルド
4. アプリケーションの起動
5. スモークテストの実行

---

## 技術スタック

- **言語**: Kotlin 1.9.25
- **フレームワーク**: Spring Boot 3.4.10
- **データアクセス**: jOOQ 3.20.0
- **データベース**: PostgreSQL 16
- **マイグレーション**: Flyway
- **ビルドツール**: Gradle 8.14
- **Java**: 21
- **コンテナ**: Docker Compose

---

## API仕様

### 著者API

#### 著者作成
```bash
POST /authors
Content-Type: application/json

{
  "name": "夏目漱石",
  "birthDate": "1867-02-09"
}
```

#### 著者取得
```bash
GET /authors/{id}
```

#### 著者更新
```bash
PUT /authors/{id}
Content-Type: application/json

{
  "name": "夏目金之助",
  "birthDate": "1867-02-09"
}
```

#### 著者の書籍一覧
```bash
GET /authors/{id}/books
```

---

### 書籍API

#### 書籍作成
```bash
POST /books
Content-Type: application/json

{
  "title": "吾輩は猫である",
  "price": 500,
  "authorIds": [1]
}
```

#### 書籍取得
```bash
GET /books/{id}
```

#### 書籍更新
```bash
PUT /books/{id}
Content-Type: application/json

{
  "title": "吾輩は猫である",
  "price": 500,
  "publicationStatus": "PUBLISHED",
  "authorIds": [1, 2]
}
```

---

## 主要機能

### 著者管理
- 著者の登録・更新・取得
- 著者に紐づく書籍一覧の取得

### 書籍管理
- 書籍の登録・更新・取得
- 複数著者の紐付け対応
- 出版状態の管理（UNPUBLISHED / PUBLISHED）

### ⭐ ビジネスルール

**重要**: 出版済み（PUBLISHED）の書籍を未出版（UNPUBLISHED）に変更することはできません。

この制約により、一度出版された書籍の履歴を保護します。

---

## バリデーション

- **著者名**: 必須、空白不可
- **生年月日**: 必須、現在または過去の日付
- **書籍タイトル**: 必須、空白不可
- **価格**: 0以上の整数
- **著者ID**: 最低1人必要
- **出版状態**: UNPUBLISHED → PUBLISHED は可、PUBLISHED → UNPUBLISHED は不可

---

## データベース構造

### authors（著者）
| カラム | 型 | 制約 |
|--------|-------|------|
| id | BIGSERIAL | PRIMARY KEY |
| name | VARCHAR(255) | NOT NULL |
| birth_date | DATE | NOT NULL |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

### books（書籍）
| カラム | 型 | 制約 |
|--------|-------|------|
| id | BIGSERIAL | PRIMARY KEY |
| title | VARCHAR(255) | NOT NULL |
| price | INTEGER | NOT NULL, CHECK(price >= 0) |
| publication_status | VARCHAR(20) | NOT NULL, CHECK IN ('UNPUBLISHED', 'PUBLISHED') |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

### book_authors（書籍-著者 中間テーブル）
| カラム | 型 | 制約 |
|--------|-------|------|
| book_id | BIGINT | FOREIGN KEY → books(id), ON DELETE CASCADE |
| author_id | BIGINT | FOREIGN KEY → authors(id), ON DELETE CASCADE |
| - | - | PRIMARY KEY (book_id, author_id) |

---

## 設計方針

### レイヤードアーキテクチャ
- **Controller層**: HTTPリクエスト/レスポンスの処理
- **Service層**: ビジネスロジックの実装
- **Repository層**: データベースアクセスの隠蔽

### 設計
- Domain層は未導入（状態遷移がシンプルなため）
- 値オブジェクトは未導入（ビジネスルールが限定的なため）
- YAGNI原則に従い、必要最小限の実装

---

## テスト

### テスト実行
```bash
./gradlew test
```

### テストレポート
```bash
open build/reports/tests/test/index.html
```

### テストカバレッジ

- **合計**: 17テスト
- **Service層**: 16テスト（ビジネスロジックの核心）
- **Application起動**: 1テスト

### 重点テスト項目

- ✅ 出版済み→未出版への変更禁止（最重要）
- ✅ 複数著者の書籍作成
- ✅ 存在しないリソースへのアクセス
- ✅ バリデーションエラー

---

## トラブルシューティング

### ⚠️ 起動時の警告について

以下の警告が表示されますが、**動作には影響ありません**：
```
警告 Version mismatch: Database version is older than what dialect POSTGRES supports: 16.10
```

**理由**: jOOQ 3.20 は PostgreSQL 17 を推奨していますが、PostgreSQL 16 でも完全に動作します。PostgreSQL 16 は現在主流のバージョンであり、本番環境での採用実績も豊富です。

---

### ポート競合エラー

ポート 5433 または 8080 が既に使用されている場合：
```bash
# ポート使用状況確認
lsof -i :5433
lsof -i :8080

# 使用中のプロセスを停止後、再実行
./run.sh
```

---

### 起動に失敗する場合
```bash
# 完全リセット
docker-compose down -v

# 再起動
./run.sh
```

---

### データベース接続エラー
```bash
# コンテナの状態確認
docker ps

# ログ確認
docker-compose logs postgres
```

---

## 開発者向け

### jOOQ コード再生成
```bash
./gradlew generateJooq
```

### データベースマイグレーション
```bash
./gradlew flywayMigrate
```

### コードフォーマット
```bash
./gradlew ktlintFormat
```

### アプリケーション単体起動
```bash
# データベースのみ起動
docker-compose up -d

# アプリケーション起動
./gradlew bootRun
```

---

## 停止方法

### アプリケーション停止
```
Ctrl + C
```

### データベース停止
```bash
docker-compose down
```

### 完全削除（データも含む）
```bash
docker-compose down -v
```

---


## ライセンス

MIT

---

## 開発者

コーディングテスト用サンプルプロジェクト
