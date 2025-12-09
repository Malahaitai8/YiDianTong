# PowerShell 脚本：将候补记录添加到 Redis
# 需要先安装 Redis.NET 或使用 redis-cli

# Redis 连接信息
$redisHost = "localhost"
$redisPort = 6379

# 数据库连接信息（用于查询候补记录）
$dbHost = "localhost"
$dbUser = "root"
$dbPassword = "your_password"  # 请修改
$dbName = "your_database"      # 请修改

Write-Host "开始将候补记录添加到 Redis..." -ForegroundColor Cyan

# 方法1: 使用 redis-cli（如果已安装）
# 注意：需要先安装 Redis for Windows 或使用 WSL

# 方法2: 使用 Python 脚本（推荐）
Write-Host ""
Write-Host "推荐使用 Python 脚本: python add-waitlist-to-redis.py" -ForegroundColor Yellow
Write-Host ""
Write-Host "或者手动执行以下步骤：" -ForegroundColor Cyan
Write-Host "1. 查询候补记录ID和join_time:"
Write-Host "   SELECT id, schedule_id, patient_id, join_time FROM waitlist WHERE schedule_id BETWEEN 183 AND 190;"
Write-Host ""
Write-Host "2. 使用 redis-cli 添加（需要将 join_time 转换为时间戳）:"
Write-Host "   redis-cli"
Write-Host "   ZADD waitlist:schedule:183 {timestamp_ms} {waitlistId}"
Write-Host "   SADD waitlist:patient:{patientId} {waitlistId}"
Write-Host ""
Write-Host "3. 或者直接运行 Python 脚本自动处理"


