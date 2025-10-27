echo "🚀 Starting Book Management Application..."
echo ""

# データベース起動
echo "📦 Starting database containers..."
docker-compose up -d

# ヘルスチェックで待機
echo "⏳ Waiting for database to be healthy..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    # Docker Composeのサービス名を使用
    if docker compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo "✅ Database is ready!"
        sleep 2
        break
    fi
    attempt=$((attempt + 1))
    echo "Waiting... (${attempt}/${max_attempts})"
    sleep 2
done

if [ $attempt -ge $max_attempts ]; then
    echo "❌ Database did not become ready in time"
    echo "📋 Database logs:"
    docker-compose logs postgres
    exit 1
fi

# jOOQコード生成のためにFlywayマイグレーションを先に実行
echo ""
echo "📋 Running Flyway migrations..."
./gradlew flywayMigrate

# Flywayが失敗したら終了
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Flyway migration failed!"
    exit 1
fi

echo "✅ Flyway migration successful!"

# ビルド
echo ""
echo "🔨 Building application..."
./gradlew clean build

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""
echo "✨ Starting application..."
echo "📍 API will be available at http://localhost:8080"
echo ""

# アプリケーションをバックグラウンドで起動
./gradlew bootRun > /dev/null 2>&1 &
GRADLE_PID=$!

echo "⏳ Waiting for application to start..."
max_wait=60
elapsed=0

while [ $elapsed -lt $max_wait ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Application is ready!"
        break
    fi
    sleep 2
    elapsed=$((elapsed + 2))
    echo "Waiting... (${elapsed}s/${max_wait}s)"
done

if [ $elapsed -ge $max_wait ]; then
    echo "❌ Application did not start in time"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# スモークテスト実行
echo ""
echo "🧪 Running smoke tests..."
echo ""

# テスト1: ヘルスチェック
echo "1️⃣  Health check..."
health_response=$(curl -s http://localhost:8080/actuator/health)
if echo "$health_response" | grep -q '"status":"UP"'; then
    echo "   ✅ Health check passed"
else
    echo "   ❌ Health check failed"
    echo "   Response: $health_response"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# テスト2: 著者作成
echo ""
echo "2️⃣  Creating author..."
author_response=$(curl -s -X POST http://localhost:8080/authors \
  -H "Content-Type: application/json" \
  -d '{"name":"夏目漱石","birthDate":"1867-02-09"}')
if echo "$author_response" | grep -q '"id":1'; then
    echo "   ✅ Author creation passed"
    echo "   Response: $author_response"
else
    echo "   ❌ Author creation failed"
    echo "   Response: $author_response"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# テスト3: 書籍作成
echo ""
echo "3️⃣  Creating book..."
book_response=$(curl -s -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"吾輩は猫である","price":500,"authorIds":[1]}')
if echo "$book_response" | grep -q '"id":1'; then
    echo "   ✅ Book creation passed"
    echo "   Response: $book_response"
else
    echo "   ❌ Book creation failed"
    echo "   Response: $book_response"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# テスト4: 書籍取得
echo ""
echo "4️⃣  Getting book..."
get_response=$(curl -s http://localhost:8080/books/1)
if echo "$get_response" | grep -q '"title":"吾輩は猫である"'; then
    echo "   ✅ Book retrieval passed"
else
    echo "   ❌ Book retrieval failed"
    echo "   Response: $get_response"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# テスト5: 書籍を出版済みに変更
echo ""
echo "5️⃣  Publishing book..."
publish_response=$(curl -s -X PUT http://localhost:8080/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"吾輩は猫である","price":500,"publicationStatus":"PUBLISHED","authorIds":[1]}')
if echo "$publish_response" | grep -q '"publicationStatus":"PUBLISHED"'; then
    echo "   ✅ Book publishing passed"
else
    echo "   ❌ Book publishing failed"
    echo "   Response: $publish_response"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# テスト6: 出版済み→未出版への変更（エラーになるべき）
echo ""
echo "6️⃣  Attempting to unpublish (should fail)..."
unpublish_response=$(curl -s -X PUT http://localhost:8080/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"吾輩は猫である","price":500,"publicationStatus":"UNPUBLISHED","authorIds":[1]}')
if echo "$unpublish_response" | grep -q "出版済みの書籍を未出版に変更することはできません"; then
    echo "   ✅ Business rule validation passed"
    echo "   Error message: 出版済みの書籍を未出版に変更することはできません"
else
    echo "   ❌ Business rule validation failed"
    echo "   Response: $unpublish_response"
    kill $GRADLE_PID 2>/dev/null
    exit 1
fi

# 全テスト成功
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 All smoke tests passed!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📍 Application is running at http://localhost:8080"
echo ""
echo "📖 API Documentation:"
echo "   - POST   /authors           Create author"
echo "   - GET    /authors/{id}      Get author"
echo "   - PUT    /authors/{id}      Update author"
echo "   - GET    /authors/{id}/books Get author's books"
echo "   - POST   /books             Create book"
echo "   - GET    /books/{id}        Get book"
echo "   - PUT    /books/{id}        Update book"
echo ""
echo "💡 To test the API manually, restart with:"
echo "   docker-compose up -d    # Start database"
echo "   ./gradlew bootRun       # Start application"
echo ""
echo "🛑 To stop database:"
echo "   docker-compose down"
echo ""

# アプリケーション停止
kill $GRADLE_PID 2>/dev/null
sleep 1
lsof -ti :8080 | xargs kill -9 2>/dev/null

echo "✅ Smoke tests completed successfully"
