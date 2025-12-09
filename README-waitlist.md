# 添加候补记录操作指南

## 1. 创建 Redis 容器

### Windows PowerShell:
```powershell
.\create-redis.ps1
```

### Linux/Mac:
```bash
chmod +x create-redis.sh
./create-redis.sh
```

### 或者直接使用 Docker 命令:
```bash
docker run -d --name my-redis -p 6379:6379 -v my-redis-data:/data redis:7-alpine redis-server --appendonly yes
```

## 2. 添加候补记录到数据库

### 步骤 1: 查询可用的患者ID
```sql
SELECT id, name FROM patient LIMIT 10;
```

### 步骤 2: 修改 add-waitlist.sql
将文件中的患者ID（示例中使用的是 1 和 2）替换为实际的患者ID。

### 步骤 3: 执行 SQL 脚本
```bash
mysql -u root -p your_database < add-waitlist.sql
```

或者在 MySQL 客户端中执行：
```sql
source add-waitlist.sql;
```

## 3. 将候补记录添加到 Redis

### 方法 1: 使用 Python 脚本（推荐）

1. 安装依赖：
```bash
pip install redis pymysql
```

2. 修改 `add-waitlist-to-redis.py` 中的数据库和 Redis 配置

3. 运行脚本：
```bash
python add-waitlist-to-redis.py
```

### 方法 2: 手动使用 Redis 命令

1. 先查询候补记录的ID和join_time：
```sql
SELECT id, schedule_id, patient_id, join_time 
FROM waitlist 
WHERE schedule_id BETWEEN 183 AND 190 
ORDER BY schedule_id, id;
```

2. 使用 Redis CLI 添加：
```bash
# 连接 Redis
redis-cli

# 为每个候补记录添加到队列
# 格式：ZADD waitlist:schedule:{scheduleId} {timestamp_ms} {waitlistId}
# 例如：
ZADD waitlist:schedule:183 1704067200000 1001
ZADD waitlist:schedule:183 1704067201000 1002
# ... 依此类推
```

注意：timestamp_ms 是 join_time 的毫秒时间戳，可以使用在线工具转换。

## 4. 验证

### 检查数据库记录：
```sql
SELECT COUNT(*) FROM waitlist WHERE schedule_id BETWEEN 183 AND 190;
-- 应该返回 16 条记录（8个排班 × 2个候补）
```

### 检查 Redis 队列：
```bash
redis-cli

# 查看某个排班的候补队列
ZRANGE waitlist:schedule:183 0 -1 WITHSCORES

# 查看队列长度
ZCARD waitlist:schedule:183
```

## 注意事项

1. **患者ID**: 确保使用的患者ID在数据库中存在
2. **唯一约束**: 一个患者不能为同一个排班重复加入候补（数据库有唯一约束）
3. **时间戳**: Redis 中的 score 必须是 join_time 的毫秒时间戳
4. **状态**: 候补记录的状态应该是 'WAITING'
5. **Redis 连接**: 确保 Redis 服务正在运行


