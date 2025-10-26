echo "🔍 Running smoke tests..."
echo ""

# ヘルスチェック
echo "1. Health check..."
response=$(curl -s http://localhost:8080/actuator/health)
if echo "$response" | grep -q '"status":"UP"'; then
    echo "✅ Health check passed"
else
    echo "❌ Health check failed"
    exit 1
fi

# 著者作成
echo ""
echo "2. Creating author..."
author=$(curl -s -X POST http://localhost:8080/authors \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Author","birthDate":"2000-01-01"}')
if echo "$author" | grep -q '"id":'; then
    echo "✅ Author creation passed"
else
    echo "❌ Author creation failed"
    exit 1
fi

# 書籍作成
echo ""
echo "3. Creating book..."
book=$(curl -s -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Book","price":1000,"authorIds":[1]}')
if echo "$book" | grep -q '"id":'; then
    echo "✅ Book creation passed"
else
    echo "❌ Book creation failed"
    exit 1
fi

echo ""
echo "🎉 All smoke tests passed!"
