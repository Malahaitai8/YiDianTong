# PowerShell 脚本：为 scheduleId 183-190 添加候补记录
# 使用方法：先修改数据库连接信息，然后运行此脚本

# 数据库连接信息（请修改为实际值）
$dbHost = "localhost"
$dbPort = 3306
$dbUser = "root"
$dbPassword = "your_password"  # 请修改
$dbName = "your_database"      # 请修改

# 查询可用的患者ID（使用前两个）
$queryPatients = "SELECT id FROM patient LIMIT 2"
# 假设查询结果为 patientId1=1, patientId2=2（请根据实际情况修改）
$patientId1 = 1
$patientId2 = 2

# 生成 SQL 语句
$sqlStatements = @()

for ($scheduleId = 183; $scheduleId -le 190; $scheduleId++) {
    $sqlStatements += "INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES"
    $sqlStatements += "($scheduleId, $patientId1, NOW(), 'WAITING'),"
    $sqlStatements += "($scheduleId, $patientId2, NOW(), 'WAITING');"
    $sqlStatements += ""
}

# 输出 SQL 到文件
$sqlStatements | Out-File -FilePath "add-waitlist-generated.sql" -Encoding UTF8

Write-Host "SQL 文件已生成: add-waitlist-generated.sql" -ForegroundColor Green
Write-Host "请先查询患者ID，然后修改 SQL 文件中的 patient_id 值" -ForegroundColor Yellow
Write-Host ""
Write-Host "执行 SQL 的方法：" -ForegroundColor Cyan
Write-Host "1. 使用 MySQL 客户端: mysql -u $dbUser -p $dbName < add-waitlist-generated.sql"
Write-Host "2. 或在 MySQL Workbench 中打开并执行该文件"


