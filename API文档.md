# YiDianTong 医院预约系统 API 文档

## 📌 基本信息

- **项目名称**: YiDianTong 医院预约系统
- **Base URL**: `http://localhost:9090`
- **认证方式**: JWT Token (Bearer Token)
- **数据格式**: JSON

---

## 🔐 认证说明

### Token 使用方式

除了登录、注册等公开接口外，其他接口都需要在请求头中携带 JWT Token：

```
Authorization: Bearer <your_token_here>
```

### 统一响应格式

所有接口返回格式统一为：

```json
{
  "code": "200",
  "msg": "成功",
  "data": {}
}
```

- `code`: 状态码，"200"表示成功，其他表示失败
- `msg`: 提示信息
- `data`: 返回的数据对象

---

## 1️⃣ 认证模块

### 1.1 用户登录（统一）

**接口**: `POST /auth/login`

**说明**: 支持患者、医生、管理员三种角色登录

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "patient001",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "username": "patient001",
    "role": "patient",
    "status": "active"
  }
}
```

**角色说明**:
- `patient`: 患者
- `doctor`: 医生
- `admin`: 管理员

---

### 1.2 患者登录（兼容）

**接口**: `POST /auth/patient/login`

**说明**: 患者专用登录接口，实际调用统一登录

**请求参数**: 同 1.1

---

### 1.3 医生登录（兼容）

**接口**: `POST /auth/doctor/login`

**说明**: 医生专用登录接口，实际调用统一登录

**请求参数**: 同 1.1

---

### 1.4 管理员登录（兼容）

**接口**: `POST /auth/admin/login`

**说明**: 管理员专用登录接口，实际调用统一登录

**请求参数**: 同 1.1

---

### 1.5 用户注册

**接口**: `POST /auth/register`

**说明**: 仅支持患者自助注册

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "newpatient",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "注册成功",
  "data": null
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "用户名已存在",
  "data": null
}
```

---

### 1.6 用户登出

**接口**: `POST /auth/logout`

**说明**: 登出系统，前端需删除本地Token

**请求头**: 需要 Token

**响应示例**:
```json
{
  "code": "200",
  "msg": "登出成功",
  "data": null
}
```

---

## 2️⃣ 患者管理模块

### 2.1 查询所有患者

**接口**: `GET /patient/selectAll`

**权限**: 需要登录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "idCard": "110101199001011234",
      "phone": "13800138000",
      "gender": "男",
      "birthDate": "1990-01-01",
      "address": "北京市朝阳区",
      "createTime": "2025-01-01 10:00:00"
    }
  ]
}
```

---

### 2.2 根据ID查询患者

**接口**: `GET /patient/selectById/{id}`

**权限**: 需要登录

**路径参数**:
- `id`: 患者ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "name": "张三",
    "idCard": "110101199001011234",
    "phone": "13800138000",
    "gender": "男",
    "birthDate": "1990-01-01",
    "address": "北京市朝阳区"
  }
}
```

---

### 2.3 新增患者

**接口**: `POST /patient`

**权限**: 需要登录

**请求体**:
```json
{
  "name": "李四",
  "idCard": "110101199002021234",
  "phone": "13900139000",
  "gender": "女",
  "birthDate": "1990-02-02",
  "address": "北京市海淀区"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

---

### 2.4 更新患者信息

**接口**: `PUT /patient`

**权限**: 需要登录

**请求体**:
```json
{
  "id": 1,
  "name": "张三",
  "phone": "13800138001",
  "address": "北京市朝阳区新地址"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

---

### 2.5 删除患者

**接口**: `DELETE /patient/{id}`

**权限**: 需要管理员权限

**路径参数**:
- `id`: 患者ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

---

## 3️⃣ 医生管理模块

### 3.1 查询所有医生

**接口**: `GET /doctor/selectAll`

**权限**: 需要登录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "王医生",
      "departmentId": 1,
      "title": "主任医师",
      "specialty": "心内科",
      "phone": "13700137000",
      "email": "wangdr@hospital.com"
    }
  ]
}
```

---

### 3.2 根据ID查询医生

**接口**: `GET /doctor/selectById/{id}`

**权限**: 需要登录

**路径参数**:
- `id`: 医生ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "name": "王医生",
    "departmentId": 1,
    "title": "主任医师",
    "specialty": "心内科",
    "phone": "13700137000",
    "email": "wangdr@hospital.com"
  }
}
```

---

### 3.3 新增医生

**接口**: `POST /doctor`

**权限**: 需要管理员权限

**请求体**:
```json
{
  "name": "李医生",
  "departmentId": 2,
  "title": "副主任医师",
  "specialty": "骨科",
  "phone": "13700137001",
  "email": "lidr@hospital.com"
}
```

---

### 3.4 更新医生信息

**接口**: `PUT /doctor`

**权限**: 需要管理员权限

**请求体**:
```json
{
  "id": 1,
  "name": "王医生",
  "title": "主任医师",
  "phone": "13700137002"
}
```

---

### 3.5 删除医生

**接口**: `DELETE /doctor/{id}`

**权限**: 需要管理员权限

**路径参数**:
- `id`: 医生ID

---

## 4️⃣ 科室管理模块

### 4.1 查询所有科室

**接口**: `GET /department/selectAll`

**权限**: 需要登录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "心内科",
      "description": "心血管疾病诊治",
      "location": "门诊楼3层",
      "phone": "010-12345678"
    }
  ]
}
```

---

### 4.2 根据ID查询科室

**接口**: `GET /department/selectById/{id}`

**权限**: 需要登录

**路径参数**:
- `id`: 科室ID

---

### 4.3 创建科室

**接口**: `POST /department`

**权限**: 需要管理员权限

**请求体**:
```json
{
  "name": "骨科",
  "description": "骨骼疾病诊治",
  "location": "门诊楼4层",
  "phone": "010-12345679"
}
```

---

### 4.4 更新科室信息

**接口**: `PUT /department`

**权限**: 需要管理员权限

**请求体**:
```json
{
  "id": 1,
  "name": "心内科",
  "location": "门诊楼3层A区",
  "phone": "010-12345680"
}
```

---

### 4.5 删除科室

**接口**: `DELETE /department/{id}`

**权限**: 需要管理员权限

**路径参数**:
- `id`: 科室ID

---

## 5️⃣ 门诊管理模块

### 5.1 查询所有门诊

**接口**: `GET /clinic/selectAll`

**权限**: 需要登录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "普通门诊",
      "type": "GENERAL",
      "description": "普通门诊服务"
    }
  ]
}
```

---

### 5.2 根据ID查询门诊

**接口**: `GET /clinic/selectById/{id}`

**权限**: 需要登录

**路径参数**:
- `id`: 门诊ID

---

## 6️⃣ 排班管理模块

### 6.1 获取本周排班

**接口**: `GET /schedule/week`

**权限**: 需要登录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "doctorId": 1,
      "doctorName": "王医生",
      "departmentId": 1,
      "departmentName": "心内科",
      "clinicId": 1,
      "clinicName": "普通门诊",
      "scheduleDate": "2025-10-15",
      "timeSlot": "上午",
      "startTime": "08:00:00",
      "endTime": "12:00:00",
      "maxAppointments": 20,
      "bookedCount": 15,
      "availableCount": 5,
      "status": "AVAILABLE"
    }
  ]
}
```

**字段说明**:
- `timeSlot`: 时间段（上午/下午/晚上）
- `maxAppointments`: 最大预约数
- `bookedCount`: 已预约数
- `availableCount`: 剩余可预约数
- `status`: 状态（AVAILABLE-可预约/FULL-已满/CANCELLED-已取消）

---

## 7️⃣ 预约管理模块

### 7.1 查询所有预约

**接口**: `GET /appointment/selectAll`

**权限**: 需要登录（管理员）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "scheduleId": 1,
      "appointmentDate": "2025-10-15",
      "timeSlot": "上午",
      "status": "PENDING",
      "createTime": "2025-10-14 10:00:00"
    }
  ]
}
```

**状态说明**:
- `PENDING`: 待就诊
- `CONFIRMED`: 已确认
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

---

### 7.2 根据ID查询预约

**接口**: `GET /appointment/selectById/{id}`

**权限**: 需要登录

**路径参数**:
- `id`: 预约ID

---

### 7.3 查询我的预约

**接口**: `GET /appointment/me`

**权限**: 需要登录（患者）

**说明**: 自动获取当前登录患者的所有预约记录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "scheduleId": 1,
      "appointmentDate": "2025-10-15",
      "timeSlot": "上午",
      "status": "PENDING",
      "createTime": "2025-10-14 10:00:00"
    }
  ]
}
```

---

### 7.4 创建预约

**接口**: `POST /appointment`

**权限**: 需要登录（患者）

**说明**: 患者身份创建预约，系统自动获取当前患者ID

**请求体**:
```json
{
  "scheduleId": 1,
  "appointmentDate": "2025-10-15",
  "timeSlot": "上午"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "scheduleId": 1,
    "appointmentDate": "2025-10-15",
    "timeSlot": "上午",
    "status": "PENDING",
    "createTime": "2025-10-14 10:00:00"
  }
}
```

---

### 7.5 取消预约

**接口**: `PUT /appointment/{id}/cancel`

**权限**: 需要登录（患者本人）

**说明**: 将预约状态更新为 CANCELLED

**路径参数**:
- `id`: 预约ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

---

### 7.6 删除预约

**接口**: `DELETE /appointment/{id}`

**权限**: 需要登录（患者本人或管理员）

**说明**: 物理删除预约记录

**路径参数**:
- `id`: 预约ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

---

## 8️⃣ 候补队列模块 ⭐

### 8.1 加入候补队列

**接口**: `POST /waitlist`

**权限**: 需要登录（患者）

**说明**: 当预约已满时，患者可加入候补队列

**请求体**:
```json
{
  "scheduleId": 1
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "scheduleId": 1,
    "queuePosition": 3,
    "status": "WAITING",
    "createTime": "2025-10-14 10:00:00"
  }
}
```

**字段说明**:
- `queuePosition`: 队列位置（第几位）
- `status`: 状态（WAITING-等待中/NOTIFIED-已通知/EXPIRED-已过期）

---

### 8.2 查看我的候补

**接口**: `GET /waitlist/me`

**权限**: 需要登录（患者）

**说明**: 查看当前患者的所有候补记录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "scheduleId": 1,
      "queuePosition": 3,
      "status": "WAITING",
      "createTime": "2025-10-14 10:00:00"
    }
  ]
}
```

---

### 8.3 弹出队首并创建预约

**接口**: `POST /waitlist/next/{scheduleId}`

**权限**: 需要管理员权限

**说明**: 当有空位时，弹出队首患者并自动创建预约

**路径参数**:
- `scheduleId`: 排班ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "scheduleId": 1,
    "queuePosition": 1,
    "status": "NOTIFIED",
    "createTime": "2025-10-14 10:00:00"
  }
}
```

---

## 9️⃣ 管理员功能模块

### 9.1 查询所有管理员

**接口**: `GET /admin/selectAll`

**权限**: 需要管理员权限

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "系统管理员",
      "phone": "13600136000",
      "email": "admin@hospital.com"
    }
  ]
}
```

---

### 9.2 根据ID查询管理员

**接口**: `GET /admin/selectById/{id}`

**权限**: 需要管理员权限

**路径参数**:
- `id`: 管理员ID

---

### 9.3 创建医生账号

**接口**: `POST /admin/doctor/create`

**权限**: 需要管理员权限

**说明**: 管理员为医生创建登录账号

**请求体**:
```json
{
  "username": "doctor001",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "医生账号创建成功",
  "data": null
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "用户名已存在",
  "data": null
}
```

---

### 9.4 停用医生账号

**接口**: `POST /admin/doctor/{id}/disable`

**权限**: 需要管理员权限

**说明**: 将医生账号状态设置为 disabled

**路径参数**:
- `id`: 用户ID（User表的ID）

**响应示例**:
```json
{
  "code": "200",
  "msg": "医生账号已停用",
  "data": null
}
```

---

### 9.5 重置医生密码

**接口**: `POST /admin/doctor/{id}/reset-password`

**权限**: 需要管理员权限

**路径参数**:
- `id`: 用户ID（User表的ID）

**请求体**:
```json
{
  "newPassword": "newpass123"
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "密码已重置",
  "data": null
}
```

---

## 🔟 系统配置模块

### 10.1 查询所有配置

**接口**: `GET /systemConfig/selectAll`

**权限**: 需要登录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "configKey": "max_appointments_per_day",
      "configValue": "3",
      "description": "每日最大预约次数"
    }
  ]
}
```

---

### 10.2 根据ID查询配置

**接口**: `GET /systemConfig/selectById/{id}`

**权限**: 需要登录

**路径参数**:
- `id`: 配置ID

---

### 10.3 根据Key查询配置

**接口**: `GET /systemConfig/selectByKey/{key}`

**权限**: 需要登录

**路径参数**:
- `key`: 配置键名

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "configKey": "max_appointments_per_day",
    "configValue": "3",
    "description": "每日最大预约次数"
  }
}
```

---

## 1️⃣1️⃣ 测试接口

### 11.1 Hello World

**接口**: `GET /hello`

**权限**: 无需认证

**响应**: `"hello"`

---

## 📋 常见业务流程

### 流程1: 患者注册登录

1. **注册账号**: `POST /auth/register`
   ```json
   {
     "username": "patient001",
     "password": "123456"
   }
   ```

2. **登录获取Token**: `POST /auth/login`
   ```json
   {
     "username": "patient001",
     "password": "123456"
   }
   ```
   
3. **保存Token**: 将返回的 `token` 保存到本地，后续请求都需要携带

---

### 流程2: 患者预约就诊

1. **查看本周排班**: `GET /schedule/week`
   - 查看可用的医生排班
   - 选择合适的时间段

2. **创建预约**: `POST /appointment`
   ```json
   {
     "scheduleId": 1,
     "appointmentDate": "2025-10-15",
     "timeSlot": "上午"
   }
   ```

3. **查看我的预约**: `GET /appointment/me`
   - 确认预约是否成功

4. **取消预约**（如需要）: `PUT /appointment/{id}/cancel`

---

### 流程3: 候补队列使用

1. **尝试预约**: `POST /appointment`
   - 如果返回"预约已满"错误

2. **加入候补队列**: `POST /waitlist`
   ```json
   {
     "scheduleId": 1
   }
   ```

3. **查看候补状态**: `GET /waitlist/me`
   - 查看自己在队列中的位置

4. **等待通知**: 
   - 当有空位时，管理员调用 `POST /waitlist/next/{scheduleId}`
   - 系统自动为队首患者创建预约

---

### 流程4: 管理员管理医生

1. **创建医生账号**: `POST /admin/doctor/create`
   ```json
   {
     "username": "doctor001",
     "password": "123456"
   }
   ```

2. **停用医生账号**: `POST /admin/doctor/{id}/disable`

3. **重置医生密码**: `POST /admin/doctor/{id}/reset-password`
   ```json
   {
     "newPassword": "newpass123"
   }
   ```

---

## ⚠️ 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未授权，Token无效或过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 📝 注意事项

1. **Token有效期**: JWT Token默认有效期为24小时，过期后需要重新登录

2. **密码加密**: 所有密码在存储时都经过BCrypt加密，不可逆

3. **角色权限**:
   - `patient`: 只能操作自己的预约和候补
   - `doctor`: 可以查看自己的排班和预约
   - `admin`: 拥有所有权限

4. **预约限制**: 
   - 每个患者每天最多预约3次（可通过系统配置调整）
   - 预约时间必须是未来时间

5. **候补队列规则**:
   - 先进先出（FIFO）
   - 队首患者优先获得空位
   - 候补记录有效期为7天

---

## 🔧 开发环境配置

### 本地运行

1. 启动后端服务
   ```bash
   cd springboot
   mvn spring-boot:run
   ```

2. 默认端口: `8080`

3. 数据库配置: 查看 `application.yml`

---

## 📞 联系方式

如有问题，请联系开发团队。

---

**文档版本**: v1.0  
**最后更新**: 2025-10-14  
**维护者**: YiDianTong 开发团队

