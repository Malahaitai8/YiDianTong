#!/bin/bash

# 高并发预约测试脚本
# 使用Apache Bench进行压力测试

echo "开始高并发预约压力测试..."

# 测试参数
CONCURRENT_USERS=100
TOTAL_REQUESTS=1000
URL="http://localhost:8080/appointment"
AUTH_TOKEN="your-jwt-token-here"  # 需要替换为实际的JWT token

# 预约请求体
APPOINTMENT_DATA='{
  "scheduleId": 1,
  "appointmentTime": "2025-01-01T09:00:00"
}'

echo "测试参数:"
echo "- 并发用户数: $CONCURRENT_USERS"
echo "- 总请求数: $TOTAL_REQUESTS"
echo "- 目标URL: $URL"
echo ""

# 执行压力测试
echo "执行压力测试..."
ab -n $TOTAL_REQUESTS -c $CONCURRENT_USERS \
   -H "Authorization: Bearer $AUTH_TOKEN" \
   -H "Content-Type: application/json" \
   -p <(echo "$APPOINTMENT_DATA") \
   -T "application/json" \
   $URL

echo ""
echo "压力测试完成。"

# 检查系统资源使用情况
echo "检查系统资源使用情况..."
echo "数据库连接数:"
mysql -u root -p123456 -e "SHOW PROCESSLIST;" yi_dian_tong_2 | wc -l

echo "Redis连接数:"
redis-cli -h localhost -p 6379 info clients | grep connected_clients

echo "JVM内存使用:"
ps aux | grep java | grep -v grep | awk '{print $2}' | xargs -I {} jstat -gc {} | tail -1
