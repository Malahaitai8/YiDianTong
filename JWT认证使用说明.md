# JWT统一认证授权系统使用说明

## 📋 系统概述

本系统采用JWT（JSON Web Token）实现无状态认证，支持患者（patient）、医生（doctor）、管理员（admin）三种角色的统一登录和授权。

## 🔐 核心功能

### 1. 认证流程
1. 用户通过 `/auth/login` 接口使用账号密码登录
2. 后端验证通过后生成JWT token（包含用户ID、角色、过期时间）
3. 前端存储token到本地（如小程序Storage）
4. 后续请求在HTTP请求头的 `Authorization` 字段携带token，格式：`Bearer <token>`
5. JWT过滤器自动验证token并加载用户信息到SecurityContext

### 2. 授权机制
- 基于角色的访问控制（RBAC）
- 患者访问：`/patient/**` 需要 `ROLE_PATIENT`
- 医生访问：`/doctor/**` 需要 `ROLE_DOCTOR`
- 管理员访问：`/admin/**` 需要 `ROLE_ADMIN`
- 支持方法级权限注解：`@PreAuthorize`, `@Secured`

### 3. 数据安全
- 密码加密：使用BCryptPasswordEncoder进行哈希处理
- 传输加密：建议启用HTTPS
- Token过期：默认24小时

## 🚀 API接口

### 登录接口

#### 统一登录（推荐）
```http
POST /auth/login
Content-Type: application/json

{
    "username": "doctor_lwh1",
    "password": "123456"
}
```

**响应示例：**
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "doctor_lwh1",
        "role": "doctor",
        "status": "active"
    }
}
```

#### 角色专用登录（兼容旧接口）
- 患者登录：`POST /auth/patient/login`
- 医生登录：`POST /auth/doctor/login`
- 管理员登录：`POST /auth/admin/login`

### 注册接口

```http
POST /auth/register
Content-Type: application/json

{
    "username": "newuser",
    "password": "password123",
    "role": "patient"  // patient, doctor, admin
}
```

**说明：**
- 患者注册后状态为 `active`，可直接登录
- 医生/管理员注册后状态为 `pending_approval`，需要管理员审批

### 登出接口

```http
POST /auth/logout
Authorization: Bearer <token>
```

## 🔧 前端使用示例

### 1. 登录并保存Token

```javascript
// 登录
async function login(username, password) {
    const response = await fetch('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
        // 保存token到本地存储
        localStorage.setItem('token', result.data.token);
        localStorage.setItem('userInfo', JSON.stringify(result.data));
        return result.data;
    } else {
        throw new Error(result.msg);
    }
}
```

### 2. 携带Token发送请求

```javascript
// 发送请求时携带token
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('token');
    
    const headers = {
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };
    
    const response = await fetch(url, {
        ...options,
        headers
    });
    
    return response.json();
}

// 使用示例
const appointments = await fetchWithAuth('/patient/appointments');
```

### 3. 小程序示例

```javascript
// 登录
wx.request({
    url: 'https://yourdomain.com/auth/login',
    method: 'POST',
    data: {
        username: 'doctor_lwh1',
        password: '123456'
    },
    success(res) {
        if (res.data.code === 200) {
            // 保存token
            wx.setStorageSync('token', res.data.data.token);
            wx.setStorageSync('userInfo', res.data.data);
        }
    }
});

// 发送请求
wx.request({
    url: 'https://yourdomain.com/doctor/schedule',
    method: 'GET',
    header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
    },
    success(res) {
        console.log(res.data);
    }
});
```

## 🛠️ 后端开发指南

### 1. 获取当前登录用户

```java
import com.example.springboot.config.SecurityUtils;

// 获取当前用户
User currentUser = SecurityUtils.getCurrentUser();

// 获取用户ID
Long userId = SecurityUtils.getCurrentUserId();

// 获取用户角色
String role = SecurityUtils.getCurrentUserRole();

// 判断角色
boolean isDoctor = SecurityUtils.isDoctor();
```

### 2. 使用权限注解

```java
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/doctor")
public class DoctorController {
    
    // 只允许医生访问
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/schedule")
    public Result<?> getSchedule() {
        // ...
    }
    
    // 允许医生和管理员访问
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PostMapping("/schedule")
    public Result<?> createSchedule() {
        // ...
    }
}
```

### 3. 在Service层获取用户信息

```java
@Service
public class AppointmentService {
    
    public List<Appointment> getMyAppointments() {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        
        if ("patient".equals(role)) {
            // 查询患者的预约
        } else if ("doctor".equals(role)) {
            // 查询医生的预约
        }
    }
}
```

## 📊 数据库初始化

### 方案1：使用注册接口创建用户

通过 `/auth/register` 接口注册用户，密码会自动加密。

### 方案2：手动插入加密密码

密码"123456"的BCrypt加密示例（每次加密结果不同）：
```
$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**重要提示：** 现有SQL_DATA.txt中的密码是明文，需要加密后才能使用！

### 临时测试账号（仅供开发测试）

可以先通过注册接口创建测试账号：

```bash
# 创建患者账号
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "patient001",
    "password": "123456",
    "role": "patient"
  }'

# 创建医生账号
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor001",
    "password": "123456",
    "role": "doctor"
  }'
```

## ⚠️ 安全配置

### JWT配置（application.yml）

```yaml
jwt:
  secret: YiDianTongSecretKeyForJWTTokenGenerationAndValidation2025
  expiration: 86400000  # 24小时(毫秒)
```

**生产环境建议：**
1. 使用更长更复杂的密钥（至少256位）
2. 密钥存储在环境变量或配置中心，不要提交到代码仓库
3. 定期更换密钥
4. 启用HTTPS
5. 实施token刷新机制

## 🔍 常见问题

### Q1: 提示"用户名或密码错误"
- 检查密码是否已加密
- 确认用户状态为"active"
- 检查用户角色是否正确

### Q2: 请求返回401未授权
- 检查token是否正确携带（Authorization: Bearer <token>）
- 检查token是否过期
- 检查用户角色权限是否匹配

### Q3: 如何批量加密现有密码？
1. 取消注释 `PasswordEncoderUtil.java` 中的 `@Component`
2. 实现 `UserMapper.selectAll()` 方法
3. 启动应用，自动加密所有明文密码
4. 加密完成后重新注释 `@Component`

### Q4: 如何延长token有效期？
修改 `application.yml` 中的 `jwt.expiration` 值（毫秒）

## 📝 开发清单

已完成：
- ✅ JWT工具类（生成、解析、验证token）
- ✅ UserDetails实现（CustomUserDetails）
- ✅ UserDetailsService实现（从数据库加载用户）
- ✅ JWT认证过滤器（拦截请求验证token）
- ✅ Spring Security配置（RBAC权限控制）
- ✅ 登录/注册/登出接口
- ✅ 密码BCrypt加密
- ✅ 工具类（SecurityUtils获取当前用户）

待完成：
- ⏳ 批量加密现有用户密码
- ⏳ Token刷新机制
- ⏳ Token黑名单（用于登出）
- ⏳ 登录日志记录
- ⏳ 防暴力破解（登录限流）

## 📞 联系支持

如有问题，请查看：
- Spring Security官方文档
- JWT (io.jsonwebtoken) 文档
- 项目README.md

