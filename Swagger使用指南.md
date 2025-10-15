# 医点通 Swagger API 文档使用指南

## 访问地址

启动项目后，访问以下地址：

```
http://localhost:8080/swagger-ui.html
```

或者：

```
http://localhost:8080/swagger-ui/index.html
```

## 使用步骤

### 1. 启动项目

```bash
cd springboot
mvn spring-boot:run
```

### 2. 访问 Swagger UI

在浏览器中打开 `http://localhost:8080/swagger-ui.html`

### 3. 测试接口（带JWT认证）

#### 步骤1：注册或登录获取Token

1. 找到 **认证管理** 分组
2. 点击 `POST /auth/register` 注册新用户（患者）
   ```json
   {
     "username": "testpatient",
     "password": "123456"
   }
   ```

3. 或者点击 `POST /auth/login` 登录
   ```json
   {
     "username": "testpatient",
     "password": "123456"
   }
   ```

4. 复制返回的 `token` 值

#### 步骤2：配置JWT Token

1. 点击页面右上角的 **Authorize** 按钮（或锁形图标🔒）
2. 在弹出的对话框中输入：`Bearer {你的token}`
   - 注意：`Bearer` 和 token 之间有一个空格
   - 例如：`Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMiLCJ1c2VybmFtZSI6InRlc3QiLCJyb2xlIjoicGF0aWVudCIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.xxxxx`
3. 点击 **Authorize** 按钮
4. 关闭对话框

#### 步骤3：测试需要认证的接口

现在您可以测试需要JWT认证的接口了，例如：

- **预约管理**
  - `POST /appointment` - 创建预约
  - `GET /appointment/me` - 查看我的预约
  - `PUT /appointment/{id}/cancel` - 取消预约

- **患者管理**
  - `GET /patient/selectAll` - 查询所有患者
  - `PUT /patient` - 更新患者信息

## API 分组说明

### 1. 认证管理 (Authentication)
- 登录、注册、登出接口
- **不需要Token**，可以直接测试

### 2. 预约管理 (Appointment)
- 预约挂号相关操作
- 部分接口需要JWT Token

### 3. 患者管理 (Patient)
- 患者信息管理
- **需要PATIENT角色**

### 4. 医生管理 (Doctor)
- 医生信息管理
- **需要DOCTOR角色**

### 5. 测试接口 (Test)
- 系统测试接口
- 不需要认证

## 测试账号

### 患者账号
```
用户名: patient1
密码: 123456
角色: PATIENT
```

### 医生账号
```
用户名: doctor1
密码: 123456
角色: DOCTOR
```

### 管理员账号
```
用户名: admin
密码: 123456
角色: ADMIN
```

## 常见问题

### Q1: 提示 "401 Unauthorized"
**原因**：Token未配置或已过期

**解决**：
1. 重新登录获取新的Token
2. 点击右上角 **Authorize** 按钮重新配置Token

### Q2: 提示 "403 Forbidden"
**原因**：当前用户角色没有权限访问该接口

**解决**：
1. 使用具有相应权限的账号登录
2. 例如访问 `/doctor/**` 接口需要DOCTOR角色

### Q3: Swagger UI 页面打不开
**原因**：项目未启动或端口配置错误

**解决**：
1. 确认项目已启动
2. 检查 `application.yml` 中的端口配置（默认8080）
3. 访问 `http://localhost:8080/swagger-ui.html`

### Q4: 如何查看接口返回的数据结构？
在 Swagger UI 中：
1. 点击接口展开
2. 查看 **Responses** 部分
3. 点击 **Schema** 查看数据结构

## OpenAPI 文档地址

除了 Swagger UI，您还可以访问原始的 OpenAPI JSON 文档：

```
http://localhost:8080/v3/api-docs
```

这个JSON文档可以导入到 Postman、Apifox 等API测试工具中使用。

## 提示

1. **Token有效期**：默认24小时，过期后需要重新登录
2. **批量测试**：可以使用 Swagger UI 的 "Try it out" 功能快速测试接口
3. **导出文档**：访问 `/v3/api-docs` 可以获取JSON格式的API文档
4. **自定义配置**：修改 `SwaggerConfig.java` 可以自定义文档信息

## 技术栈

- SpringDoc OpenAPI 2.7.0
- Spring Boot 3.5.5
- JWT Authentication
- Spring Security

---

**祝您使用愉快！** 🎉

