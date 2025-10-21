# CORS 跨域配置说明

## ✅ 已完成配置

YiDianTong 后端已经配置好 CORS 跨域支持，前端可以正常调用后端 API。

---

## 📋 配置详情

### 1. 允许的前端地址

开发环境下，后端允许以下前端地址访问：

```
http://localhost:3000      (React 默认端口)
http://localhost:5173      (Vite 默认端口)
http://localhost:8080      (Vue CLI 默认端口)
http://localhost:8081      (备用端口)
http://127.0.0.1:3000
http://127.0.0.1:5173
http://127.0.0.1:8080
http://127.0.0.1:8081
```

### 2. 允许的 HTTP 方法

```
GET, POST, PUT, DELETE, OPTIONS, PATCH
```

### 3. 允许的请求头

```
所有请求头 (*)
```

### 4. 允许携带认证信息

```
✅ 允许携带 Cookies
✅ 允许携带 Authorization 请求头
```

### 5. 预检请求缓存时间

```
3600 秒（1小时）
```

### 6. 暴露的响应头

前端可以访问以下响应头：

```
- Authorization
- Content-Type
- X-Requested-With
```

---

## 🔧 前端配置示例

### Axios 配置

```javascript
import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080',  // 后端地址
  timeout: 5000,
  withCredentials: true  // 允许携带认证信息
});

// 请求拦截器 - 添加 Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误
request.interceptors.response.use(
  response => {
    return response.data;  // 直接返回 data 部分
  },
  error => {
    if (error.response?.status === 401) {
      // Token 过期，跳转登录
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
```

### Fetch API 配置

```javascript
async function request(url, options = {}) {
  const token = localStorage.getItem('token');
  
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers
    },
    credentials: 'include'  // 允许携带认证信息
  };
  
  const response = await fetch(`http://localhost:8080${url}`, config);
  
  if (response.status === 401) {
    localStorage.removeItem('token');
    window.location.href = '/login';
  }
  
  return response.json();
}
```

---

## 🧪 测试 CORS 配置

### 1. 启动后端服务

```bash
cd springboot
mvn spring-boot:run
```

后端将运行在 `http://localhost:8080`

### 2. 测试登录接口

使用浏览器控制台或前端项目测试：

```javascript
fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'patient001',
    password: '123456'
  })
})
.then(res => res.json())
.then(data => console.log(data));
```

如果返回数据而不是 CORS 错误，说明配置成功！

---

## ⚠️ 生产环境注意事项

### 修改允许的域名

生产环境部署时，需要修改 `SecurityConfig.java` 中的 `allowedOrigins`：

```java
// 生产环境配置示例
configuration.setAllowedOrigins(Arrays.asList(
    "https://yourdomain.com",      // 你的生产域名
    "https://www.yourdomain.com"
));
```

### 环境变量配置（推荐）

更好的做法是使用环境变量：

1. 在 `application.yml` 中添加：

```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
```

2. 在 `SecurityConfig.java` 中读取：

```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList(allowedOrigins.split(","))
    );
    // ... 其他配置
}
```

3. 生产环境通过环境变量设置：

```bash
export CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

---

## 📝 常见问题

### Q1: 仍然出现 CORS 错误？

**检查清单**：
- ✅ 后端是否已启动？
- ✅ 前端端口是否在允许列表中？
- ✅ 是否使用了 `http://` 而不是 `https://`？
- ✅ 请求头 `Authorization` 格式是否正确（`Bearer <token>`）？

### Q2: 预检请求（OPTIONS）失败？

确认 `SecurityConfig` 中 `/auth/**` 已设置为 `permitAll()`。

### Q3: Token 无法传递到后端？

检查：
- Axios: 设置 `withCredentials: true`
- Fetch: 设置 `credentials: 'include'`
- 请求头格式: `Authorization: Bearer <token>`

---

## ✅ 总结

**CORS 配置已完成！前端可以：**

1. ✅ 从 `localhost:3000/5173/8080/8081` 访问后端
2. ✅ 使用所有 HTTP 方法（GET/POST/PUT/DELETE等）
3. ✅ 携带 JWT Token 进行认证
4. ✅ 正常接收所有响应数据

**前端开发者可以放心开始开发了！** 🎉

---

**配置文件位置**: `springboot/src/main/java/com/example/springboot/config/SecurityConfig.java`

**最后更新**: 2025-10-15

