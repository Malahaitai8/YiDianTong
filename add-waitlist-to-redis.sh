#!/bin/bash
# 将候补记录添加到 Redis 队列
# 注意：需要先执行 add-waitlist.sql 插入数据库记录，然后获取 waitlistId

# Redis 连接信息（根据实际情况修改）
REDIS_HOST="localhost"
REDIS_PORT="6379"

# 函数：添加候补到 Redis
add_to_redis() {
    local schedule_id=$1
    local waitlist_id=$2
    local join_time=$3
    
    # Redis key 格式：waitlist:schedule:{scheduleId}
    local queue_key="waitlist:schedule:${schedule_id}"
    
    # 使用 ZADD 添加到有序集合，score 为 join_time 的时间戳（毫秒）
    # 注意：join_time 需要转换为时间戳
    local score=$(date -d "$join_time" +%s)000  # 转换为毫秒时间戳
    
    redis-cli -h $REDIS_HOST -p $REDIS_PORT ZADD "$queue_key" $score "$waitlist_id"
    
    echo "已添加候补到 Redis: scheduleId=$schedule_id, waitlistId=$waitlist_id"
}

# 从数据库查询候补记录并添加到 Redis
# 注意：这里需要先执行 SQL 查询获取 waitlistId 和 join_time
# 示例：假设我们已经知道 waitlistId，可以直接调用

# 示例：为 scheduleId 183 添加两个候补（需要替换为实际的 waitlistId）
# add_to_redis 183 <waitlist_id_1> "<join_time_1>"
# add_to_redis 183 <waitlist_id_2> "<join_time_2>"

echo "请先执行 add-waitlist.sql，然后根据查询结果手动调用 add_to_redis 函数"
echo "或者使用下面的 Python 脚本自动处理"


