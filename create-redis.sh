#!/bin/bash
# 创建 Redis 容器的命令

docker run -d \
  --name my-redis \
  -p 6379:6379 \
  -v my-redis-data:/data \
  redis:7-alpine \
  redis-server --appendonly yes

echo "Redis 容器已创建并启动"
echo "容器名称: my-redis"
echo "端口映射: 6379:6379"
echo "数据卷: my-redis-data"


