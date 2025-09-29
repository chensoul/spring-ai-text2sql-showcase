#!/bin/bash

# 快速测试脚本
# 使用方法: ./quick_test.sh "查询内容"

if [ $# -eq 0 ]; then
    echo "使用方法: ./quick_test.sh \"查询内容\""
    echo "示例: ./quick_test.sh \"查询所有员工信息\""
    exit 1
fi

QUERY="$1"
BASE_URL="http://localhost:8080"
API_ENDPOINT="/api/steps/query"

echo "=========================================="
echo "快速测试步骤化 Text2SQL API"
echo "=========================================="
echo "查询内容: $QUERY"
echo ""

curl -X POST "${BASE_URL}${API_ENDPOINT}" \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"$QUERY\"}" \
  -w "\n\nHTTP状态码: %{http_code}\n响应时间: %{time_total}s\n" \
  -s | jq '.' 2>/dev/null || echo "响应不是有效的JSON格式"

echo ""
echo "=========================================="
