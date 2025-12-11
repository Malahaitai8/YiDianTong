# 升级Docker和安装Docker Compose

## 当前状态
- Docker版本：18.09.0（较旧）
- Docker Compose：未安装

## 升级步骤（CentOS系统）

### 1. 停止旧版Docker服务

```bash
systemctl stop docker
```

### 2. 卸载旧版Docker（可选，推荐保留）

如果不想卸载，可以直接安装新版本覆盖。

### 3. 安装新版本Docker

```bash
# 安装必要的工具
yum install -y yum-utils

# 添加Docker官方仓库
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装最新版Docker Engine和Docker Compose插件
yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 启动Docker服务
systemctl start docker
systemctl enable docker
```

### 4. 验证安装

```bash
# 检查Docker版本（应该是20.x或更高）
docker --version

# 检查Docker Compose版本
docker compose version

# 测试Docker是否正常工作
docker run hello-world
```

### 5. 配置Docker镜像加速（推荐，国内服务器）

```bash
# 创建配置文件
mkdir -p /etc/docker
vi /etc/docker/daemon.json
```

添加以下内容：

```json
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

保存后重启Docker：

```bash
systemctl daemon-reload
systemctl restart docker
```

## 如果遇到问题

### 问题1：yum安装失败

```bash
# 清理yum缓存
yum clean all
yum makecache

# 再次尝试安装
yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 问题2：仓库连接失败

```bash
# 使用阿里云镜像（如果官方仓库无法访问）
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

