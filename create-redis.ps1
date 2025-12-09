# PowerShell 脚本：创建 Redis 容器

docker run -d `
  --name my-redis `
  -p 6379:6379 `
  -v my-redis-data:/data `
  redis:7-alpine `
  redis-server --appendonly yes

Write-Host "Redis 容器已创建并启动" -ForegroundColor Green
Write-Host "容器名称: my-redis"
Write-Host "端口映射: 6379:6379"
Write-Host "数据卷: my-redis-data"


