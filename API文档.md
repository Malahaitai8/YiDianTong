# YiDianTong 医院预约系统 API 文档

## 📌 基本信息

- **项目名称**: YiDianTong 医院预约系统
- **Base URL**: `http://localhost:8080`
- **认证方式**: JWT Token (Bearer Token)
- **数据格式**: JSON

---

## 🔐 认证说明

### Token 使用方式

除了登录、注册接口外，其他所有接口都需要在请求头中携带 JWT Token：

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
    "userId": 1,
    "username": "patient001",
    "role": "patient",
    "status": "active"
  }
}
```

**字段说明**:
- `token`: JWT认证令牌
- `userId`: 用户ID（User表ID）
- `username`: 用户名
- `role`: 角色类型（patient/doctor/admin）
- `status`: 账户状态（active/disabled）

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

**说明**: 仅支持患者自助注册，系统会自动创建患者记录

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "newpatient",
  "password": "123456",
  "name": "张三",
  "specificRole": "普通患者",
  "phoneNumber": "13800138000",
  "idCardNumber": "110101199001011234"
}
```

**字段说明**:
- `username`: 用户名（必填，唯一）
- `password`: 密码（必填）
- `name`: 真实姓名（可选，默认使用username）
- `specificRole`: 具体角色（可选，默认"普通患者"）
- `phoneNumber`: 手机号（可选）
- `idCardNumber`: 身份证号（可选）

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

**业务逻辑**:
1. 创建User记录（角色强制设为"patient"）
2. 创建Patient记录（包含患者详细信息）
3. 返回成功信息

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

**权限**: 仅管理员

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "userId": 10,
      "name": "张三",
      "specificRole": "普通患者",
      "idStatus": "已认证",
      "phoneNumber": "13800138000",
      "idCardNumber": "110101199001011234",
      "user": {
        "id": 10,
        "username": "patient001",
        "role": "patient",
        "status": "active"
      }
    }
  ]
}
```

**字段说明**:
- `id`: 患者ID（Patient表ID）
- `userId`: 关联的用户ID（User表ID）
- `name`: 患者姓名
- `specificRole`: 具体角色（如"普通患者"）
- `idStatus`: 身份认证状态
- `phoneNumber`: 联系电话
- `idCardNumber`: 身份证号
- `user`: 关联的用户账户信息

---

### 2.2 根据ID查询患者

**接口**: `GET /patient/selectById/{id}`

**权限**: 管理员或患者本人

**路径参数**:
- `id`: 患者ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "userId": 10,
    "name": "张三",
    "specificRole": "普通患者",
    "idStatus": "已认证",
    "phoneNumber": "13800138000",
    "idCardNumber": "110101199001011234",
    "user": {
      "id": 10,
      "username": "patient001",
      "role": "patient",
      "status": "active"
    }
  }
}
```

---

### 2.3 新增患者

**接口**: `POST /patient`

**权限**: 仅管理员

**请求体**:
```json
{
  "userId": 11,
  "name": "李四",
  "specificRole": "普通患者",
  "idStatus": "待认证",
  "phoneNumber": "13900139000",
  "idCardNumber": "110101199002021234"
}
```

**字段说明**:
- `userId`: 关联的用户账户ID（必须先创建User账户）
- `name`: 患者姓名
- `specificRole`: 具体角色
- `idStatus`: 身份认证状态
- `phoneNumber`: 联系电话
- `idCardNumber`: 身份证号

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

**接口**: `PUT /patient/{id}`

**权限**: 管理员或患者本人

**路径参数**:
- `id`: 患者ID

**请求体**:
```json
{
  "name": "张三",
  "specificRole": "VIP患者",
  "phoneNumber": "13800138001",
  "idCardNumber": "110101199001011234"
}
```

**说明**: 可部分更新字段，不传的字段保持不变

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

**权限**: 需要登录（任何角色）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "userId": 20,
      "clinicId": 1,
      "name": "王医生",
      "title": "主任医师",
      "specialty": "心内科",
      "bio": "擅长心血管疾病诊疗，从医20年",
      "user": {
        "id": 20,
        "username": "doctor001",
        "role": "doctor",
        "status": "active"
      },
      "clinic": {
        "id": 1,
        "departmentId": 1,
        "name": "普通门诊",
        "description": "普通门诊服务"
      }
    }
  ]
}
```

**字段说明**:
- `id`: 医生ID（Doctor表ID）
- `userId`: 关联的用户ID
- `clinicId`: 所属门诊ID
- `name`: 医生姓名
- `title`: 职称
- `specialty`: 专长
- `bio`: 个人简介
- `user`: 关联的用户账户信息
- `clinic`: 所属门诊信息（包含departmentId）

---

### 3.2 根据ID查询医生

**接口**: `GET /doctor/selectById/{id}`

**权限**: 需要登录（任何角色）

**路径参数**:
- `id`: 医生ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "userId": 20,
    "clinicId": 1,
    "name": "王医生",
    "title": "主任医师",
    "specialty": "心内科",
    "bio": "擅长心血管疾病诊疗，从医20年",
    "user": {
      "id": 20,
      "username": "doctor001",
      "role": "doctor",
      "status": "active"
    },
    "clinic": {
      "id": 1,
      "departmentId": 1,
      "name": "普通门诊",
      "description": "普通门诊服务"
    }
  }
}
```

---

### 3.3 新增医生

**接口**: `POST /doctor`

**权限**: 仅管理员

**请求体**:
```json
{
  "userId": 21,
  "clinicId": 1,
  "name": "李医生",
  "title": "副主任医师",
  "specialty": "骨科",
  "bio": "专注骨科疾病治疗"
}
```

**字段说明**:
- `userId`: 关联的用户账户ID（必须先创建User账户）
- `clinicId`: 所属门诊ID
- `name`: 医生姓名
- `title`: 职称
- `specialty`: 专长
- `bio`: 个人简介

---

### 3.4 更新医生信息

**接口**: `PUT /doctor/{id}`

**权限**: 管理员或医生本人

**路径参数**:
- `id`: 医生ID

**请求体**:
```json
{
  "name": "王医生",
  "title": "主任医师",
  "specialty": "心血管内科",
  "bio": "更新后的个人简介"
}
```

**说明**: 可部分更新字段，不传的字段保持不变

---

### 3.5 删除医生

**接口**: `DELETE /doctor/{id}`

**权限**: 仅管理员

**路径参数**:
- `id`: 医生ID

---

## 4️⃣ 科室管理模块

### 4.1 查询所有科室

**接口**: `GET /department/selectAll`

**权限**: 需要登录（任何角色）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "心内科",
      "description": "心血管疾病诊治"
    }
  ]
}
```

**字段说明**:
- `id`: 科室ID
- `name`: 科室名称
- `description`: 科室描述

---

### 4.2 根据ID查询科室

**接口**: `GET /department/selectById/{id}`

**权限**: 需要登录（任何角色）

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
  "description": "骨骼疾病诊治"
}
```

---

### 4.4 更新科室信息

**接口**: `PUT /department/{id}`

**权限**: 需要管理员权限

**路径参数**:
- `id`: 科室ID

**请求体**:
```json
{
  "name": "心内科",
  "description": "心血管疾病诊治与预防"
}
```

**说明**: 可部分更新字段，不传的字段保持不变

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

**权限**: 需要登录（任何角色）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "departmentId": 1,
      "name": "普通门诊",
      "description": "普通门诊服务"
    }
  ]
}
```

**字段说明**:
- `id`: 门诊ID
- `departmentId`: 所属科室ID
- `name`: 门诊名称
- `description`: 门诊描述

---

### 5.2 根据ID查询门诊

**接口**: `GET /clinic/selectById/{id}`

**权限**: 需要登录（任何角色）

**路径参数**:
- `id`: 门诊ID

---

## 6️⃣ 排班管理模块

### 6.1 获取本周排班

**接口**: `GET /schedule/week`

**权限**: 需要登录（任何角色）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "doctorId": 1,
      "scheduleDate": "2025-10-23",
      "timeSlot": "上午",
      "slotType": "MORNING",
      "totalSlots": 20,
      "availableSlots": 5,
      "dayOfWeek": "星期三"
    }
  ]
}
```

**字段说明**:
- `id`: 排班ID
- `doctorId`: 医生ID
- `scheduleDate`: 排班日期（yyyy-MM-dd）
- `timeSlot`: 时间段描述（如"上午"、"下午"、"晚上"）
- `slotType`: 时间段类型（MORNING/AFTERNOON/EVENING）
- `totalSlots`: 总号源数
- `availableSlots`: 剩余可预约号源数
- `dayOfWeek`: 星期几

**关联说明**:
- 通过 `doctorId` 可关联查询医生详情（包含clinicId和departmentId）
- Doctor → Clinic → Department 形成三级关联

---

## 7️⃣ 预约管理模块

### 7.1 查询所有预约

**接口**: `GET /appointment/selectAll`

**权限**: 仅管理员

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "doctorId": 1,
      "scheduleId": 1,
      "appointmentTime": "2025-10-23 09:30:00",
      "status": "PENDING",
      "fee": 50.00,
      "actualFee": 45.00,
      "createdAt": "2025-10-22 14:30:00",
      "sourceType": "ONLINE"
    }
  ]
}
```

**字段说明**:
- `id`: 预约ID
- `patientId`: 患者ID
- `doctorId`: 医生ID（冗余字段，方便查询）
- `scheduleId`: 排班ID（核心关联字段）
- `appointmentTime`: 预约时间（yyyy-MM-dd HH:mm:ss）
- `status`: 预约状态
- `fee`: 挂号费
- `actualFee`: 实际费用（优惠后）
- `createdAt`: 创建时间
- `sourceType`: 预约来源（ONLINE-线上/OFFLINE-线下）

**状态说明**:
- `PENDING`: 待就诊
- `CONFIRMED`: 已确认
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

**关联说明**:
- 通过 `scheduleId` 关联排班信息
- 通过 `patientId` 关联患者信息
- 通过 `doctorId` 关联医生信息

---

### 7.2 根据ID查询预约

**接口**: `GET /appointment/selectById/{id}`

**权限**: 管理员、患者本人或该预约关联医生

**路径参数**:
- `id`: 预约ID

---

### 7.3 查询我的预约

**接口**: `GET /appointment/me`

**权限**: 患者

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
      "doctorId": 1,
      "scheduleId": 1,
      "appointmentTime": "2025-10-23 09:30:00",
      "status": "PENDING",
      "fee": 50.00,
      "actualFee": 45.00,
      "createdAt": "2025-10-22 14:30:00",
      "sourceType": "ONLINE"
    }
  ]
}
```

---

### 7.4 搜索可预约时段

**接口**: `GET /appointment/search`

**权限**: 患者

**说明**: 按条件搜索可预约的时间段

**查询参数**:
- `departmentId`: 科室ID (可选)
- `doctorId`: 医生ID (可选)
- `startDate`: 开始日期 (必填，格式: yyyy-MM-dd)
- `endDate`: 结束日期 (必填，格式: yyyy-MM-dd)
- `timeSlot`: 时间段 (可选: 上午/下午/晚上)

**示例请求**:
```
GET /appointment/search?startDate=2025-10-23&endDate=2025-10-30&doctorId=1&timeSlot=上午
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "scheduleId": 1,
      "doctorName": "王医生",
      "departmentName": "心内科",
      "date": "2025-10-23",
      "timeSlot": "上午",
      "startTime": "08:00",
      "endTime": "12:00",
      "availableSlots": 5,
      "totalSlots": 20,
      "fee": 50.0,
      "discountedFee": 45.0
    }
  ]
}
```

**字段说明**:
- `scheduleId`: 排班ID（用于创建预约）
- `doctorName`: 医生姓名
- `departmentName`: 科室名称
- `date`: 排班日期
- `timeSlot`: 时间段
- `startTime`: 开始时间
- `endTime`: 结束时间
- `availableSlots`: 剩余号源
- `totalSlots`: 总号源
- `fee`: 挂号费
- `discountedFee`: 优惠后费用

---

### 7.5 创建预约

**接口**: `POST /appointment`

**权限**: 患者

**说明**: 患者身份创建预约，系统自动获取当前患者ID

**请求体**:
```json
{
  "scheduleId": 1,
  "appointmentTime": "2025-10-23 09:30:00"
}
```

**字段说明**:
- `scheduleId`: 排班ID（必填，从搜索接口获取）
- `appointmentTime`: 预约时间（必填，格式: yyyy-MM-dd HH:mm:ss）

**业务逻辑**:
1. 系统自动获取当前登录患者ID
2. 从排班中获取医生ID
3. 扣减排班的可用号源数（availableSlots - 1）
4. 计算挂号费和优惠费用
5. 创建预约记录

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "doctorId": 1,
    "scheduleId": 1,
    "appointmentTime": "2025-10-23 09:30:00",
    "status": "PENDING",
    "fee": 50.00,
    "actualFee": 45.00,
    "createdAt": "2025-10-22 14:30:00",
    "sourceType": "ONLINE"
  }
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "号源已满，无法预约",
  "data": null
}
```

---

### 7.6 改约

**接口**: `PUT /appointment/{id}/reschedule`

**权限**: 患者本人

**说明**: 修改预约到新的时间段

**路径参数**:
- `id`: 预约ID

**请求体**:
```json
{
  "newScheduleId": 123
}
```

**字段说明**:
- `newScheduleId`: 新的排班ID（必填）

**业务逻辑**:
1. 验证预约是否属于当前患者
2. 恢复原排班的号源数（availableSlots + 1）
3. 扣减新排班的号源数（availableSlots - 1）
4. 更新预约的scheduleId和doctorId
5. 保持预约状态不变

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "doctorId": 2,
    "scheduleId": 123,
    "appointmentTime": "2025-10-25 09:00:00",
    "status": "PENDING",
    "fee": 50.00,
    "actualFee": 45.00,
    "createdAt": "2025-10-22 14:30:00",
    "sourceType": "ONLINE"
  }
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "无权限操作此预约",
  "data": null
}
```

---

### 7.7 取消预约

**接口**: `PUT /appointment/{id}/cancel`

**权限**: 患者本人

**说明**: 将预约状态更新为 CANCELLED，并恢复号源

**路径参数**:
- `id`: 预约ID

**业务逻辑**:
1. 验证预约是否属于当前患者
2. 将预约状态更新为 CANCELLED
3. 恢复排班的号源数（availableSlots + 1）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "预约不存在或已取消",
  "data": null
}
```

---

### 7.8 删除预约

**接口**: `DELETE /appointment/{id}`

**权限**: 患者本人或管理员

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

**权限**: 患者

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

**权限**: 患者

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

**权限**: 仅管理员

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

**权限**: 仅管理员

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

**权限**: 仅管理员

**路径参数**:
- `id`: 管理员ID

---

### 9.3 创建医生账号

**接口**: `POST /admin/doctor/create`

**权限**: 仅管理员

**说明**: 管理员为医生创建登录账号和医生档案

**请求体**:
```json
{
  "username": "doctor001",
  "password": "123456",
  "name": "王医生",
  "title": "主任医师",
  "specialty": "心内科",
  "bio": "擅长心血管疾病诊疗，从医20年",
  "clinicId": 1
}
```

**字段说明**:
- `username`: 用户名（必填，唯一）
- `password`: 密码（必填）
- `name`: 医生姓名（必填）
- `title`: 职称（必填）
- `specialty`: 专长（必填）
- `bio`: 个人简介（可选）
- `clinicId`: 所属门诊ID（必填）

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

**业务逻辑**:
1. 创建User记录（角色设为"doctor"）
2. 创建Doctor记录（包含医生详细信息）
3. 返回成功信息

---

---

### 9.5 停用医生账号

**接口**: `POST /admin/doctor/{id}/disable`

**权限**: 仅管理员

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

**权限**: 仅管理员

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

### 9.7 重置所有用户密码

**接口**: `POST /admin/users/reset-all-passwords`

**权限**: 管理员

**说明**: 管理员一键重置所有用户密码为123456

**请求体**: 无

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": "已重置密码的用户数量: 15"
}
```

---

## 🔟 系统配置模块

### 10.1 查询所有配置

**接口**: `GET /systemConfig/selectAll`

**权限**: 需要登录（任何角色）

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

**权限**: 需要登录（任何角色）

**路径参数**:
- `id`: 配置ID

---

### 10.3 根据Key查询配置

**接口**: `GET /systemConfig/selectByKey/{key}`

**权限**: 需要登录（任何角色）

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
     "password": "123456",
     "name": "张三",
     "specificRole": "普通患者",
     "phoneNumber": "13800138000",
     "idCardNumber": "110101199001011234"
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
     "appointmentTime": "2025-10-15 09:30:00"
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
     "password": "123456",
     "name": "王医生",
     "title": "主任医师",
     "specialty": "心内科",
     "bio": "擅长心血管疾病诊疗，从医20年",
     "clinicId": 1
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

