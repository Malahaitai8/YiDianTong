#!/bin/bash

# 易点通挂号系统 - 快速部署脚本
# 使用方法: ./deploy.sh

set -e

echo "=========================================="
echo "  易点通挂号系统 - Docker部署脚本"
echo "=========================================="
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 检查环境变量文件
if [ ! -f .env ]; then
    echo "⚠️  未找到.env文件，正在从.env.example创建..."
    if [ -f .env.example ]; then
        cp .env.example .env
        echo "✅ 已创建.env文件，请编辑配置后重新运行此脚本"
        echo "   重要: 请修改数据库密码、JWT密钥和微信小程序配置"
        exit 1
    else
        echo "❌ 未找到.env.example文件"
        exit 1
    fi
fi

# 创建日志目录
mkdir -p springboot/logs

echo "📦 开始构建Docker镜像..."
docker compose -f docker-compose.prod.yml build

echo ""
echo "🚀 启动服务..."
docker compose -f docker-compose.prod.yml up -d

echo ""
echo "⏳ 等待服务启动..."
sleep 10

echo ""
echo "📊 服务状态:"
docker compose -f docker-compose.prod.yml ps

echo ""
echo "🔍 检查服务健康状态..."

# 检查后端
if curl -f http://localhost:8080/hello &> /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "⚠️  后端服务可能未就绪，请查看日志: docker logs ydt-backend"
fi

# 检查前端
if curl -f http://localhost &> /dev/null; then
    echo "✅ 前端服务运行正常"
else
    echo "⚠️  前端服务可能未就绪，请查看日志: docker logs ydt-frontend"
fi

# 检查Redis
if docker exec ydt-redis redis-cli ping &> /dev/null; then
    echo "✅ Redis服务运行正常"
else
    echo "⚠️  Redis服务可能未就绪，请查看日志: docker logs ydt-redis"
fi

echo ""
echo "=========================================="
echo "✅ 部署完成！"
echo ""
echo "📝 常用命令:"
echo "   查看日志: docker compose -f docker-compose.prod.yml logs -f"
echo "   停止服务: docker compose -f docker-compose.prod.yml down"
echo "   重启服务: docker compose -f docker-compose.prod.yml restart"
echo ""
echo "🌐 访问地址:"
echo "   前端管理界面: http://你的服务器IP"
echo "   后端API: http://你的服务器IP:8080"
echo "   API文档: http://你的服务器IP:8080/swagger-ui.html"
echo ""
echo "📱 小程序配置:"
echo "   1. 在微信公众平台配置服务器域名"
echo "   2. 修改client-mini-app/src/config/index.js中的API地址"
echo "   3. 构建小程序体验版"
echo "=========================================="

