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

### 3.6 提交个人信息修改申请 ⭐

**接口**: `POST /doctor/apply-info-update`

**权限**: 仅医生

**说明**: 医生提交修改个人信息的申请，需要管理员审核通过后才会生效

**请求体**:
```json
{
  "fieldName": "title",
  "newValue": "主任医师",
  "reason": "已获得主任医师资格证书，需要更新职称信息"
}
```

**字段说明**:
- `fieldName` (必填): 要修改的字段名
  - `name`: 姓名
  - `title`: 职称
  - `specialty`: 擅长领域
  - `bio`: 个人简介
- `newValue` (必填): 新的值
- `reason` (可选): 申请原因

**响应示例**:
```json
{
  "code": "200",
  "msg": "申请已提交，请等待管理员审核",
  "data": {
    "id": 1,
    "requestType": "INFO_UPDATE",
    "applicantId": 5,
    "applicantName": "张三",
    "doctorId": 2,
    "doctorName": "张三",
    "fieldName": "title",
    "oldValue": "副主任医师",
    "newValue": "主任医师",
    "reason": "已获得主任医师资格证书，需要更新职称信息",
    "status": "PENDING",
    "createdAt": "2025-11-03T10:30:00"
  }
}
```

---

### 3.7 查看我的信息修改申请

**接口**: `GET /doctor/my-info-applications`

**权限**: 仅医生

**说明**: 医生查看自己提交的所有个人信息修改申请

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "requestType": "INFO_UPDATE",
      "fieldName": "title",
      "oldValue": "副主任医师",
      "newValue": "主任医师",
      "status": "PENDING",
      "reason": "已获得主任医师资格证书，需要更新职称信息",
      "createdAt": "2025-11-03T10:30:00"
    }
  ]
}
```

**状态说明**:
- `PENDING`: 待审核
- `APPROVED`: 已批准（信息已更新）
- `REJECTED`: 已拒绝
- `CANCELLED`: 已取消

---

### 3.8 查看我的个人信息

**接口**: `GET /doctor/my-info`

**权限**: 仅医生

**说明**: 医生查看自己的详细信息

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 2,
    "userId": 5,
    "clinicId": 1,
    "name": "张三",
    "title": "副主任医师",
    "specialty": "心血管疾病",
    "bio": "从医15年，擅长心血管疾病诊疗",
    "user": {
      "id": 5,
      "username": "doctor001",
      "role": "doctor",
      "status": "active"
    },
    "clinic": {
      "id": 1,
      "name": "心内科门诊",
      "departmentId": 1
    }
  }
}
```

---

### 3.9 医生坐诊时间（患者端）

**接口**: `GET /doctor/{id}/schedules`
**权限**: 需要登录（患者/医生/管理员均可）
**说明**: 患者在医生详情页查看该医生在指定日期范围内的坐诊安排。

**路径参数**:
- `id`: 医生ID

**查询参数**:
- `startDate` (可选): 开始日期（格式: yyyy-MM-dd），默认“今天”
- `endDate` (可选): 结束日期（格式: yyyy-MM-dd），默认从 startDate 起未来 30 天
- `timeSlot` (可选): 时间段（可选值：`MORNING`/`AFTERNOON`/`EVENING`，亦兼容中文：`上午`/`下午`/`晚上`）

**规则说明**:
1. 未传 `startDate`/`endDate` 时，默认从“今天”起的未来 30 天；
2. `endDate` 会被规范化为“次日零点”，以确保包含传入的当天；
3. `timeSlot` 兼容英文与中文取值（内部统一为 `morning/afternoon/evening`）；
4. 返回结构与医生端“我的排班”一致，便于前端复用。

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "schedules": [
      {
        "id": 101,
        "scheduleDate": "2025-11-12",
        "timeSlot": "morning",
        "slotType": "expert",
        "totalSlots": 20,
        "availableSlots": 5,
        "bookedSlots": 15,
        "appointmentCount": 15
      },
      {
        "id": 102,
        "scheduleDate": "2025-11-12",
        "timeSlot": "afternoon",
        "slotType": "normal",
        "totalSlots": 20,
        "availableSlots": 12,
        "bookedSlots": 8,
        "appointmentCount": 8
      }
    ],
    "total": 2
  }
}
```

**字段说明**:
- `id`: 排班ID
- `scheduleDate`: 排班日期（yyyy-MM-dd）
- `timeSlot`: 时间段（morning/afternoon/evening）
- `slotType`: 号别（normal/expert/vip）
- `totalSlots`: 总号源数
- `availableSlots`: 剩余可预约号源数
- `bookedSlots`: 已预约号源数（totalSlots - availableSlots）
- `appointmentCount`: 预约记录数（与 bookedSlots 一般一致，保留用于统计）

**关联说明**:
- 通过 `id`（即 `scheduleId`）可用于创建预约（见 7.5 创建预约）。
- 若需跨科室/医生搜索号源，请使用 7.4 “搜索可预约时段”。

---

### 3.10 医生端：当日预约患者列表

- 接口: `GET /doctor/today-patients`
- 权限: 医生（需要 `DOCTOR` 角色）
- 说明: 医生查看“今天”的预约患者清单，可按时间段筛选；自动统计各时段人数。

查询参数:
- `timeSlot` (可选): 时间段；可选值 `MORNING`/`AFTERNOON`/`EVENING`，亦兼容中文 `上午`/`下午`/`晚上`。

响应示例:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "patients": [
      {
        "appointmentId": 1001,
        "patientId": 501,
        "patientName": "张三",
        "phoneNumber": "138****0001",
        "idCardNumber": "**************1234",
        "specificRole": "student",
        "appointmentTime": "2025-11-20 09:20:00",
        "status": "PENDING",
        "statusName": "待就诊",
        "fee": 30.00,
        "actualFee": 1.50,
        "sourceType": "ONLINE",
        "createdAt": "2025-11-19 12:00:00",
        "scheduleId": 3001,
        "scheduleDate": "2025-11-20",
        "timeSlot": "morning",
        "timeSlotName": "上午"
      },
      {
        "appointmentId": 1002,
        "patientId": 502,
        "patientName": "李四",
        "phoneNumber": "138****0002",
        "idCardNumber": "**************5678",
        "specificRole": "teacher",
        "appointmentTime": "2025-11-20 14:10:00",
        "status": "CONFIRMED",
        "statusName": "已确认",
        "fee": 30.00,
        "actualFee": 3.00,
        "sourceType": "ONLINE",
        "createdAt": "2025-11-19 12:10:00",
        "scheduleId": 3002,
        "scheduleDate": "2025-11-20",
        "timeSlot": "afternoon",
        "timeSlotName": "下午"
      }
    ],
    "total": 2,
    "morningCount": 1,
    "afternoonCount": 1,
    "eveningCount": 0
  }
}
```

字段说明:
- `patients`: 当日预约患者列表
  - `appointmentId`: 预约ID
  - `appointmentTime`: 预约时间（yyyy-MM-dd HH:mm:ss）
  - `status`/`statusName`: 预约状态及中文名（PENDING/CONFIRMED/COMPLETED/CANCELLED）
  - `fee`/`actualFee`: 原始费用/实际支付费用
  - `sourceType`: 来源（如 ONLINE/WAITLIST）
  - `patientId`/`patientName`/`phoneNumber`/`idCardNumber`/`specificRole`: 患者信息
  - `scheduleId`/`scheduleDate`/`timeSlot`/`timeSlotName`: 排班信息
- `total`: 当日总预约人数（不含已取消）
- `morningCount`/`afternoonCount`/`eveningCount`: 各时段统计

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

### 4.6 批量创建科室

**接口**: `POST /department/batch`

**权限**: 需要管理员权限

**说明**: 批量创建多个科室

**请求体**:
```json
{
  "departments": [
    {
      "name": "心内科",
      "description": "心血管疾病诊治"
    },
    {
      "name": "骨科",
      "description": "骨骼疾病诊治"
    },
    {
      "name": "神经内科",
      "description": "神经系统疾病诊治"
    }
  ]
}
```

**字段说明**:
- `departments`: 科室列表（必填，至少包含一个科室）
  - `name`: 科室名称（必填）
  - `description`: 科室描述（可选）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "successCount": 3,
    "message": "成功创建 3 个科室"
  }
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "科室列表不能为空",
  "data": null
}
```

**业务逻辑**:
1. 验证科室列表是否为空
2. 使用事务批量插入科室记录
3. 返回成功创建的科室数量

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

## 5.3 按名称查询门诊

**接口**: `GET /clinic/selectByName/{name}`

**权限**: 需要登录（任何角色）

**路径参数**:
- `name`: 门诊名称

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "departmentId": 1,
    "name": "普通门诊",
    "description": "普通门诊服务"
  }
}
```

---

### 5.2 根据ID查询门诊

**接口**: `GET /clinic/selectById/{id}`

**权限**: 需要登录（任何角色）

**路径参数**:
- `id`: 门诊ID

---

### 5.4 创建门诊（管理员）

**接口**: `POST /clinic`

**权限**: 仅管理员

**请求体**:
```json
{
  "departmentId": 1,
  "name": "内科门诊",
  "description": "周一至周五"
}
```

**响应示例**:
```json
{ "code": "200", "msg": "成功", "data": null }
```

---

### 5.5 更新门诊（管理员）

**接口**: `PUT /clinic/{id}`

**权限**: 仅管理员

**路径参数**:
- `id`: 门诊ID

**请求体**:
```json
{
  "departmentId": 1,
  "name": "内科专家门诊",
  "description": "工作日全天"
}
```

**响应示例**:
```json
{ "code": "200", "msg": "成功", "data": null }
```

---

### 5.6 删除门诊（管理员）

**接口**: `DELETE /clinic/{id}`

**权限**: 仅管理员

**路径参数**:
- `id`: 门诊ID

**响应示例**:
```json
{ "code": "200", "msg": "成功", "data": null }
```

---

### 5.7 批量创建门诊（管理员）

**接口**: `POST /clinic/batch`

**权限**: 仅管理员

**说明**: 在已选定科室的基础上批量创建门诊

**请求体**:
```json
{
  "departmentId": 1,
  "clinics": [
    {
      "name": "普通门诊",
      "description": "周一至周五"
    },
    {
      "name": "专家门诊",
      "description": "周一、周三、周五"
    },
    {
      "name": "特需门诊",
      "description": "预约制"
    }
  ]
}
```

**字段说明**:
- `departmentId`: 所属科室ID（必填）
- `clinics`: 门诊列表（必填，至少包含一个门诊）
  - `name`: 门诊名称（必填）
  - `description`: 门诊描述（可选）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "successCount": 3,
    "departmentId": 1,
    "message": "成功在科室ID 1 下创建 3 个门诊"
  }
}
```

**错误示例**:
```json
{
  "code": "500",
  "msg": "必须指定科室ID",
  "data": null
}
```

```json
{
  "code": "500",
  "msg": "科室ID 999 不存在",
  "data": null
}
```

**业务逻辑**:
1. 验证科室ID是否存在
2. 验证门诊列表是否为空
3. 为所有门诊设置相同的科室ID
4. 使用事务批量插入门诊记录
5. 返回成功创建的门诊数量

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

### 6.1.1 根据ID查询排班详情（通用）

**接口**: `GET /schedule/selectById/{id}`
**权限**: 需要登录（任何角色）
**说明**: 返回指定ID的排班基础信息，供挂号确认页等场景使用。

**路径参数**:
- `id`: 排班ID

**请求头**:
- `Authorization: Bearer <token>`

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 123,
    "doctorId": 1,
    "scheduleDate": "2025-11-20",
    "timeSlot": "morning",
    "slotType": "expert",
    "totalSlots": 20,
    "availableSlots": 5
  }
}
```

**字段说明**:
- `id`: 排班ID
- `doctorId`: 医生ID
- `scheduleDate`: 排班日期（yyyy-MM-dd）
- `timeSlot`: 时间段（morning/afternoon/evening）
- `slotType`: 号别（normal/expert/vip）
- `totalSlots`: 总号源数
- `availableSlots`: 剩余号源数

---


### 6.2 创建单个排班（管理端）

**接口**: `POST /api/admin/schedules`

**权限**: 仅管理员

**说明**: 为指定医生创建单个排班记录

**请求体**:
```json
{
  "doctorId": 1,
  "scheduleDate": "2025-10-25",
  "timeSlot": "morning",
  "slotType": "expert",
  "totalSlots": 20,
  "availableSlots": 20
}
```

**字段说明**:
- `doctorId` (必填): 医生ID
- `scheduleDate` (必填): 排班日期，格式：yyyy-MM-dd
- `timeSlot` (必填): 时间段（morning/afternoon/evening）
- `slotType` (必填): 号别（normal/expert/vip）
- `totalSlots` (必填): 总号源数，必须大于0
- `availableSlots` (可选): 可用号源数，不传则默认等于totalSlots

**响应示例**:
```json
{
  "code": 200,
  "message": "排班创建成功",
  "data": {
    "id": 123,
    "doctorId": 1,
    "scheduleDate": "2025-10-25",
    "timeSlot": "morning",
    "slotType": "expert",
    "totalSlots": 20,
    "availableSlots": 20
  }
}
```

**错误示例**:
```json
{
  "code": 400,
  "message": "创建失败: 该时间段已存在排班，请勿重复创建",
  "data": null
}
```

---

### 6.3 批量创建排班（管理端）

**接口**: `POST /api/admin/schedules/batch`

**权限**: 仅管理员

**说明**: 为指定医生批量创建多个排班记录

**请求体**:
```json
{
  "doctorId": 1,
  "startDate": "2025-10-25",
  "endDate": "2025-10-31",
  "timeSlots": ["morning", "afternoon"],
  "slotType": "expert",
  "totalSlots": 20,
  "skipWeekends": true,
  "excludeDates": ["2025-10-27"]
}
```

**字段说明**:
- `doctorId` (必填): 医生ID
- `startDate` (必填): 开始日期，格式：yyyy-MM-dd
- `endDate` (必填): 结束日期，格式：yyyy-MM-dd
- `timeSlots` (必填): 时间段列表，可包含多个值（morning/afternoon/evening）
- `slotType` (必填): 号别（normal/expert/vip）
- `totalSlots` (必填): 总号源数，必须大于0
- `skipWeekends` (可选): 是否跳过周末，默认false
- `excludeDates` (可选): 要排除的日期列表（如节假日）

**响应示例**:
```json
{
  "code": 200,
  "message": "批量创建完成",
  "data": {
    "successCount": 12,
    "skipCount": 2,
    "errorCount": 0,
    "errors": [],
    "message": "成功创建 12 个排班，跳过 2 个已存在的排班"
  }
}
```

**响应字段说明**:
- `successCount`: 成功创建的排班数量
- `skipCount`: 跳过的已存在排班数量
- `errorCount`: 创建失败的数量
- `errors`: 错误信息列表
- `message`: 汇总信息

---

### 6.4 更新排班（管理端）

**接口**: `PUT /api/admin/schedules/{id}`

**权限**: 仅管理员

**说明**: 更新指定排班的信息

**路径参数**:
- `id`: 排班ID

**请求体**:
```json
{
  "doctorId": 2,
  "scheduleDate": "2025-10-26",
  "timeSlot": "afternoon",
  "slotType": "vip",
  "totalSlots": 15,
  "availableSlots": 10
}
```

**字段说明**:
- 所有字段都是可选的，只传需要修改的字段
- `doctorId`: 医生ID
- `scheduleDate`: 排班日期
- `timeSlot`: 时间段（morning/afternoon/evening）
- `slotType`: 号别（normal/expert/vip）
- `totalSlots`: 总号源数，不能为负数
- `availableSlots`: 可用号源数，不能为负数

**响应示例**:
```json
{
  "code": 200,
  "message": "排班更新成功",
  "data": {
    "id": 123,
    "doctorId": 2,
    "scheduleDate": "2025-10-26",
    "timeSlot": "afternoon",
    "slotType": "vip",
    "totalSlots": 15,
    "availableSlots": 10
  }
}
```

---

### 6.5 删除排班（管理端）

**接口**: `DELETE /api/admin/schedules/{id}`

**权限**: 仅管理员

**说明**: 删除指定的排班记录

**路径参数**:
- `id`: 排班ID

**响应示例**:
```json
{
  "code": 200,
  "message": "排班删除成功",
  "data": null
}
```

**错误示例**:
```json
{
  "code": 400,
  "message": "删除失败: 该排班已有预约，无法删除",
  "data": null
}
```

**业务逻辑**:
- 检查排班是否有已使用的号源
- 如果有预约，拒绝删除
- 如果无预约，执行删除操作

---

### 6.6 查询排班详情（管理端）

**接口**: `GET /api/admin/schedules/{id}`

**权限**: 仅管理员

**说明**: 根据ID查询单个排班的详细信息

**路径参数**:
- `id`: 排班ID

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 123,
    "doctorId": 1,
    "scheduleDate": "2025-10-25",
    "timeSlot": "morning",
    "slotType": "expert",
    "totalSlots": 20,
    "availableSlots": 15
  }
}
```

---

### 6.7 条件查询排班列表（管理端）

**接口**: `GET /api/admin/schedules`

**权限**: 仅管理员

**说明**: 根据多种条件筛选和查询排班列表，支持分页

**查询参数**:
- `doctorId`: 医生ID (可选)
- `departmentId`: 科室ID (可选)
- `startDate`: 开始日期 (可选，格式: yyyy-MM-dd)
- `endDate`: 结束日期 (可选，格式: yyyy-MM-dd)
- `timeSlot`: 时间段 (可选: morning/afternoon/evening)
- `slotType`: 号别 (可选: normal/expert/vip)
- `page`: 页码 (可选，默认1)
- `pageSize`: 每页大小 (可选，默认20)

**示例请求**:
```
GET /api/admin/schedules?doctorId=1&startDate=2025-10-25&endDate=2025-10-31&page=1&pageSize=20
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 123,
        "doctorId": 1,
        "scheduleDate": "2025-10-25",
        "timeSlot": "morning",
        "slotType": "expert",
        "totalSlots": 20,
        "availableSlots": 15
      }
    ],
    "total": 45,
    "page": 1,
    "pageSize": 20,
    "totalPages": 3
  }
}
```

**使用场景**:
- 查询某个医生的所有排班
- 查询某个科室的所有排班
- 查询某个日期范围内的排班
- 查询特定时间段或号别的排班

---

### 6.8 查询医生的所有排班（管理端）

**接口**: `GET /api/admin/schedules/doctor/{doctorId}`

**权限**: 仅管理员

**说明**: 查询指定医生的所有排班记录

**路径参数**:
- `doctorId`: 医生ID

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 123,
      "doctorId": 1,
      "scheduleDate": "2025-10-25",
      "timeSlot": "morning",
      "slotType": "expert",
      "totalSlots": 20,
      "availableSlots": 15
    }
  ],
  "total": 10
}
```

---

### 6.9 加号（增加号源数量）⭐

**接口**: `POST /api/admin/schedules/{id}/add-slots`

**权限**: 仅管理员

**说明**: 为指定排班增加号源数量，同时增加总号源和可用号源

**路径参数**:
- `id`: 排班ID（必填）

**请求体**:
```json
{
  "slotsToAdd": 10,
  "reason": "患者需求量大，临时增加号源"
}
```

**字段说明**:
- `slotsToAdd` (必填): 要增加的号源数量
  - 类型: Integer
  - 范围: 1-50
  - 验证: `@Min(1)` `@Max(50)` `@NotNull`
- `reason` (可选): 加号原因
  - 类型: String
  - 用途: 记录加号理由

**成功响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 123,
    "doctorId": 5,
    "scheduleDate": "2025-11-20",
    "timeSlot": "morning",
    "slotType": "normal",
    "totalSlots": 30,
    "availableSlots": 18
  }
}
```

**错误响应**:

1. 排班不存在:
```json
{
  "code": "500",
  "msg": "加号失败: 排班不存在",
  "data": null
}
```

2. 参数验证失败:
```json
{
  "code": "400",
  "msg": "增加的号源数量至少为1",
  "data": null
}
```

**业务逻辑**:
1. 验证排班是否存在
2. 计算新的总号源 = 原总号源 + slotsToAdd
3. 计算新的可用号源 = 原可用号源 + slotsToAdd
4. 更新数据库（事务性保证）
5. 返回更新后的排班信息

**使用场景**:
- 患者需求量突然增加，需要临时增加号源
- 医生临时可以加班，增加接诊名额
- 特殊情况下的紧急加号需求

**注意事项**:
- 此操作不可逆，请确认后操作
- 增加的号源会同时增加总号源和可用号源
- 单次最多增加 50 个号源
- 建议填写加号原因，便于后续追溯

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

**业务校验与逻辑**:
1. 系统自动获取当前登录患者ID；
2. 校验排班存在，预约时间必须是未来且与排班日期一致；
3. 校验同日次数上限（默认3次，可通过 `APPOINTMENT_DAILY_LIMIT` 配置）；
4. 校验同一排班禁止重复预约（排除已取消记录）；
5. 原子扣减排班的可用号源数（availableSlots - 1，号源不足报错）；
6. 按排班 `slotType` 计算挂号费（`FEE_NORMAL/FEE_EXPERT/FEE_VIP`，失败回退0），同步设置 `actualFee`；
7. 创建预约记录。

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

**常见错误**:
```json
{
  "code": "500",
  "msg": "号源不足",
  "data": null
}
```
```json
{
  "code": "500",
  "msg": "当天预约次数已达上限",
  "data": null
}
```
```json
{
  "code": "500",
  "msg": "请勿重复预约该排班",
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

**说明**: 将预约状态更新为 CANCELLED。系统将优先用候补队列自动填充；若无候补则归还号源。

**路径参数**:
- `id`: 预约ID

**业务校验与逻辑**:
1. 验证预约是否属于当前患者或管理员操作；
2. 校验退号时限：距就诊时间不足配置时长不可退号（默认120分钟，可通过 `CANCEL_LIMIT_MINUTES` 配置）；
3. 将预约状态更新为 CANCELLED；
4. 取消成功后：尝试从候补队列弹出队首并自动创建预约；若无候补再归还号源（availableSlots + 1）。

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**常见错误**:
```json
{
  "code": "500",
  "msg": "预约不存在或已取消",
  "data": null
}
```
```json
{
  "code": "500",
  "msg": "距离就诊不足120分钟，不可退号",
  "data": null
}
```

---

### 7.8 删除预约

**接口**: `DELETE /appointment/{id}`

**权限**: 患者本人或管理员

**说明**: 物理删除预约记录。删除成功后将尝试候补填充；若无候补则归还号源。

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

### 📌 模块概述

候补队列是本系统的核心特色功能，当某个排班的号源已满时，患者可以加入候补队列。当有患者取消预约或删除预约时，系统会**自动**将候补队列中的首位患者转为正式预约，实现号源的高效利用。

#### 技术架构

**存储方案**: 使用 **Redis ZSET**（有序集合）数据结构

**数据结构设计**:
1. **候补队列** (ZSET):
   - Key: `waitlist:schedule:{scheduleId}`
   - 成员: `patientId`
   - 分数: 加入时间戳（毫秒）
   - 特性: 自动按时间戳排序，保证公平性

2. **患者反向索引** (SET):
   - Key: `waitlist:patient:{patientId}`
   - 成员: `scheduleId` 列表
   - 用途: 快速查询患者的所有候补记录

#### 业务规则

1. **加入条件**:
   - 号源必须已满（`availableSlots = 0`）
   - 不可重复加入同一排班的候补队列
   - 排班必须存在且有效

2. **排队规则**:
   - 严格按加入时间先后排序（FIFO）
   - 排名从 0 开始（0 表示第1位）
   - 支持实时查询当前排名和队列人数

3. **自动转预约机制** 🤖:
   - **触发条件**: 当预约被取消或删除时
   - **处理流程**:
     1. 从候补队列弹出队首患者
     2. 自动为该患者创建预约
     3. 扣减号源（`availableSlots - 1`）
     4. 自动计算费用
     5. 设置预约来源为 `WAITLIST`
   - **兜底机制**: 若候补队列为空，则归还号源

4. **数据一致性保证**:
   - 使用 Redis 原子操作
   - 双重索引自动维护
   - 异常情况自动回滚

---

### 8.1 加入候补队列

**接口**: `POST /waitlist`

**权限**: 患者（需要 `PATIENT` 角色）

**说明**: 当预约已满时，患者可加入候补队列

**请求体**:
```json
{
  "scheduleId": 1
}
```

**字段说明**:
- `scheduleId` (必填): 排班ID

**成功响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": "加入候补成功"
}
```

**错误响应**:

1. 号源未满:
```json
{
  "code": "500",
  "msg": "当前仍有号源，可直接预约",
  "data": null
}
```

2. 重复加入:
```json
{
  "code": "500",
  "msg": "已在候补队列中，请勿重复提交",
  "data": null
}
```

3. 排班不存在:
```json
{
  "code": "500",
  "msg": "排班不存在",
  "data": null
}
```

**业务逻辑**:
1. 验证排班是否存在
2. 检查号源是否已满
3. 检查是否已在候补队列中
4. 加入 Redis ZSET，分数为当前时间戳
5. 更新患者反向索引

---

### 8.2 查看我的候补

**接口**: `GET /waitlist/me`

**权限**: 患者（需要 `PATIENT` 角色）

**说明**: 查看当前患者的所有候补记录，包含实时排名和队列人数

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "scheduleId": 1,
      "rank": 0,
      "queueSize": 3
    },
    {
      "scheduleId": 5,
      "rank": 10,
      "queueSize": 25
    }
  ]
}
```

**字段说明**:
- `scheduleId`: 排班ID
- `rank`: 您的候补排名
  - 从 0 开始（0 表示第1位，即下一个转为预约的位置）
  - 实时计算，基于加入时间戳排序
- `queueSize`: 该排班当前总候补人数
  - 包含您自己在内的所有候补人数

**空队列响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": []
}
```

**业务逻辑**:
1. 从患者反向索引获取所有 `scheduleId`
2. 遍历每个 `scheduleId`，查询 Redis ZSET
3. 使用 `ZRANK` 命令获取排名
4. 使用 `ZCARD` 命令获取队列总人数
5. 过滤掉已失效的记录

---

### 8.3 弹出队首（管理员手动操作）

**接口**: `POST /waitlist/next/{scheduleId}`

**权限**: 仅管理员（需要 `ADMIN` 角色）

**说明**: 管理员手动弹出队首患者ID，不自动创建预约（用于特殊场景）

**路径参数**:
- `scheduleId`: 排班ID

**成功响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": 102
}
```

**字段说明**:
- `data`: 被弹出的患者ID（`patientId`）

**队列为空响应**:
```json
{
  "code": "500",
  "msg": "队列为空",
  "data": null
}
```

**业务逻辑**:
1. 从 Redis ZSET 中弹出分数最低（最早加入）的元素
2. 从患者反向索引中移除对应记录
3. 返回被弹出的患者ID

**注意事项**:
- 此接口仅返回患者ID，不创建预约
- 通常用于管理员手动处理特殊情况
- 大多数情况下，系统会在预约取消/删除时**自动**处理候补队列

---

### 8.4 自动候补处理机制（系统自动）

**触发场景**:
1. 患者取消预约（`PUT /appointment/{id}/cancel`）
2. 删除预约记录（`DELETE /appointment/{id}`）

**自动处理流程**:

```
预约取消/删除
    |
    ↓
检查候补队列是否为空
    |
    ├─→ 队列不为空
    |       |
    |       ↓
    |   弹出队首患者
    |       |
    |       ↓
    |   自动创建预约
    |       |
    |       ├─→ 设置 patientId
    |       ├─→ 设置 scheduleId
    |       ├─→ 设置 doctorId
    |       ├─→ 设置 sourceType = "WAITLIST"
    |       ├─→ 计算费用
    |       └─→ 扣减号源
    |       |
    |       ↓
    |   候补转预约成功 ✓
    |
    └─→ 队列为空
            |
            ↓
        归还号源 (availableSlots + 1)
```

**实现细节**:
- 方法: `AppointmentService.processWaitlistAfterDeletion()`
- 返回值: `boolean`（是否成功填充）
- 异常处理: 失败时不影响主流程，仅记录日志

**费用计算规则**:
- 根据排班的 `slotType` 自动计算费用
- 从系统配置读取费用标准：
  - `FEE_NORMAL`: 普通号费用
  - `FEE_EXPERT`: 专家号费用
  - `FEE_VIP`: 特需号费用

**日志记录**:
```
候补队列自动创建预约成功: 患者ID=102, 排班ID=1
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

## 1️⃣0️⃣ 医生信息变更申请模块

说明：医生提交信息变更申请，系统以 `system_config` JSON 存储，管理员审核后可更新医生资料。

### 10.1 医生提交变更申请

**接口**: `POST /api/doctor-change/submit`

**权限**: 医生

**说明**:
- 系统自动使用当前登录用户的 `userId` 作为 `requestedByUserId`
- 若未传 `doctorId`，后端将根据当前用户反查其 `doctorId`
- 若未传 `clinicId` 但提供了 `clinicName`，后端会用名称解析出 `clinicId`

**请求体**:
```json
{
  "doctorId": 1,
  "clinicId": 2,
  "clinicName": "消化内科门诊",
  "name": "王医生",
  "title": "主任医师",
  "specialty": "心血管内科",
  "bio": "从医20年，擅长……"
}
```

字段说明：
- `doctorId` (可选): 不传则后端按当前用户反查医生ID
- `clinicId` (可选): 与 `clinicName` 二选一，若都传则以 `clinicId` 为准
- `clinicName` (可选): 当未提供 `clinicId` 时可用名称解析门诊
- `name`/`title`/`specialty`/`bio` (可选): 申请变更的目标值

**成功响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": "e8b8b4a7-...",
    "doctorId": 1,
    "requestedByUserId": 20,
    "clinicId": 2,
    "name": "王医生",
    "title": "主任医师",
    "specialty": "心血管内科",
    "bio": "从医20年，擅长……",
    "status": "PENDING",
    "createdAt": 1730000000000,
    "updatedAt": 1730000000000
  }
}
```

**错误示例**:
```json
{ "code": "500", "msg": "clinicName not found", "data": null }
```

---

### 10.2 医生查看本人变更申请列表

**接口**: `GET /api/doctor-change/my`

**权限**: 医生

**说明**: 后端使用当前登录用户反查其医生身份并返回申请列表。

**成功响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": "e8b8b4a7-...",
      "doctorId": 1,
      "requestedByUserId": 20,
      "status": "PENDING",
      "createdAt": 1730000000000,
      "updatedAt": 1730000000000,
      "clinicId": 2,
      "name": "王医生",
      "title": "主任医师",
      "specialty": "心血管内科",
      "bio": "……"
    }
  ]
}
```

---

### 10.3 按医生ID查询变更申请列表

**接口**: `GET /api/doctor-change/by-doctor/{doctorId}`

**权限**: 医生或管理员

**路径参数**:
- `doctorId`: 医生ID

---

### 10.4 管理员查询变更申请（可按状态过滤）

**接口**: `GET /api/doctor-change/admin/list`

**权限**: 管理员

**查询参数**:
- `status` (可选): `PENDING`/`APPROVED`/`REJECTED`

---

### 10.5 管理员审核变更申请

**接口**: `POST /api/doctor-change/admin/review`

**权限**: 管理员

**说明**: 审核通过时会更新医生资料；拒绝时记录原因。

**请求体**:
```json
{
  "id": "e8b8b4a7-...",
  "action": "APPROVE", // 或 "REJECT"
  "reason": "资料不完整"
}
```

**成功响应**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": "e8b8b4a7-...",
    "status": "APPROVED",
    "approvedBy": "admin001",
    "approvedAt": 1730000005000,
    "reason": null
  }
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

### 9.4 白名单管理（统一） ⭐

说明：统一使用 `Whitelist` 白名单表，按 `roleType` 区分学生/教师/外部用户，通过学工号统一字段 `identityNumber` 管理。

- 角色类型取值：
  - `student` 学生
  - `teacher` 教师
  - `outsider` 外部人员
- 状态取值：
  - `active` 启用
  - `inactive` 停用

#### 9.4.1 查询所有白名单
- 接口: `GET /admin/whitelist/selectAll`
- 权限: 管理员
- 响应示例:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "identityNumber": "20230001",
      "roleType": "student",
      "status": "active",
      "createdAt": "2025-11-01 10:00:00",
      "updatedAt": "2025-11-01 10:00:00"
    }
  ]
}
```

#### 9.4.2 根据ID查询白名单
- 接口: `GET /admin/whitelist/selectById/{id}`
- 权限: 管理员
- 路径参数:
  - `id`: 白名单ID
- 响应示例:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "identityNumber": "T0001",
    "roleType": "teacher",
    "status": "active",
    "createdAt": "2025-11-01 10:00:00",
    "updatedAt": "2025-11-01 10:00:00"
  }
}
```

#### 9.4.3 按角色类型查询白名单
- 接口: `GET /admin/whitelist/selectByRoleType/{roleType}`
- 权限: 管理员
- 路径参数:
  - `roleType`: `student`/`teacher`/`outsider`
- 响应: 同 9.4.1

#### 9.4.4 新增白名单
- 接口: `POST /admin/whitelist`
- 权限: 管理员
- 请求体:
```json
{
  "identityNumber": "20230002",
  "roleType": "student",
  "status": "active"
}
```
- 字段说明:
  - `identityNumber` (必填): 学号/工号/证件号
  - `roleType` (必填): 角色类型
  - `status` (可选): 默认 `active`
- 响应示例:
```json
{ "code": "200", "msg": "白名单添加成功", "data": null }
```
- 错误示例:
```json
{ "code": "500", "msg": "该 identityNumber 已存在", "data": null }
```

#### 9.4.5 更新白名单
- 接口: `PUT /admin/whitelist/{id}`
- 权限: 管理员
- 路径参数:
  - `id`: 白名单ID
- 请求体（部分字段可选更新）:
```json
{
  "identityNumber": "20230002",
  "roleType": "student",
  "status": "disabled"
}
```
- 响应示例:
```json
{ "code": "200", "msg": "白名单更新成功", "data": null }
```

#### 9.4.6 删除白名单
- 接口: `DELETE /admin/whitelist/{id}`
- 权限: 管理员
- 路径参数:
  - `id`: 白名单ID
- 响应示例:
```json
{ "code": "200", "msg": "白名单删除成功", "data": null }
```

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

## 1️⃣1️⃣ 个人信息管理模块 ⭐

### 11.1 患者个人信息

- 接口: `GET /patient/profile`
- 权限: 患者
- 说明: 获取当前登录患者的个人信息（合并用户与患者表关键字段）
- 响应示例:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "userId": 10,
    "name": "张三",
    "specificRole": "student",
    "idStatus": "verified",
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

- 接口: `PUT /patient/profile`
- 权限: 患者
- 说明: 更新当前登录患者的个人资料（仅允许更新非认证类字段，如手机号）
- 请求体:
```json
{
  "phoneNumber": "13800138001"
}
```
- 响应: `{ "code": "200", "msg": "个人信息更新成功", "data": null }`

- 接口: `PUT /patient/password`
- 权限: 患者
- 说明: 修改当前登录患者的登录密码，需要校验旧密码
- 请求体:
```json
{
  "oldPassword": "123456",
  "newPassword": "newpass123"
}
```
- 响应: `{ "code": "200", "msg": "密码修改成功", "data": null }`

---

### 11.2 身份认证

- 接口: `POST /auth/verify/identity`
- 权限: 患者
- 说明: 患者提交姓名、学号/工号（统一字段 identityNumber）、身份证号进行身份认证。后端将据此检索白名单（`Whitelist`），确定 `specificRole` 为 `student`/`teacher`/`outsider` 并更新患者信息与认证状态。
- 请求体:
```json
{
  "name": "张三",
  "identityNumber": "20230001",
  "idCardNumber": "110101199001011234"
}
```
- 成功响应（示例）:
```json
{
  "code": "200",
  "msg": "身份认证成功",
  "data": {
    "id": 1,
    "userId": 10,
    "name": "张三",
    "specificRole": "student",
    "idStatus": "verified",
    "phoneNumber": "13800138000",
    "idCardNumber": "110101199001011234"
  }
}
```
- 错误示例:
```json
{ "code": "500", "msg": "白名单不存在或信息不匹配", "data": null }
```

- 接口: `GET /auth/verify/status`
- 权限: 患者
- 说明: 查询当前登录患者的认证状态与信息（含 `idStatus`、`specificRole` 等）

---

### 11.3 医生个人信息

- 接口: `GET /doctor/my-info`
- 权限: 医生
- 说明: 获取当前登录医生的详细信息

- 接口: `POST /doctor/apply-info-update`
- 权限: 医生
- 说明: 医生提交个人信息修改申请，需管理员审核通过后生效
- 请求体:
```json
{
  "fieldName": "title",
  "newValue": "主任医师",
  "reason": "已获得主任医师资格"
}
```
- 响应: 返回创建的申请详情（见下）

- 接口: `GET /doctor/my-info-applications`
- 权限: 医生
- 说明: 查看当前医生提交的所有个人信息修改申请及状态

提示：
- 管理端可通过“申请管理模块”统一审核信息修改申请（`/api/application-requests/review`）。

---

## 1️⃣1️⃣ 测试接口

### 11.1 Hello World

**接口**: `GET /hello`

**权限**: 无需认证

**响应**: `"hello"`

---

## 1️⃣2️⃣ 医生调班申请审核模块

### 12.1 医生提交调班申请

**接口**: `POST /api/schedule-change/submit`

**权限**: 医生

**说明**: 医生申请修改自己的排班信息，需要管理员审核通过后才能生效。

**请求体**:
```json
{
  "scheduleId": 1,
  "newScheduleDate": "2025-11-10",
  "newTimeSlot": "AFTERNOON",
  "newSlotType": "expert",
  "newTotalSlots": 15,
  "reason": "临时有会议安排，需要调整排班"
}
```

**字段说明**:
- `scheduleId`: 要调整的排班ID（必填）
- `newScheduleDate`: 新的排班日期（可选，不传则保持原日期）
- `newTimeSlot`: 新的时间段（可选，MORNING/AFTERNOON/EVENING）
- `newSlotType`: 新的号别（可选，normal/expert/vip）
- `newTotalSlots`: 新的总号源数（可选）
- `reason`: 申请原因（可选）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "scheduleId": 1,
    "requestedByUserId": 10,
    "doctorId": 5,
    "originalScheduleDate": "2025-11-08",
    "originalTimeSlot": "MORNING",
    "originalSlotType": "normal",
    "originalTotalSlots": 10,
    "newScheduleDate": "2025-11-10",
    "newTimeSlot": "AFTERNOON",
    "newSlotType": "expert",
    "newTotalSlots": 15,
    "reason": "临时有会议安排，需要调整排班",
    "status": "PENDING",
    "createdAt": 1698825600000,
    "updatedAt": 1698825600000
  }
}
```

---

### 12.2 医生查看自己的调班申请

**接口**: `GET /api/schedule-change/my`

**权限**: 医生

**说明**: 查看自己提交的所有调班申请及审核状态。

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "scheduleId": 1,
      "requestedByUserId": 10,
      "doctorId": 5,
      "originalScheduleDate": "2025-11-08",
      "originalTimeSlot": "MORNING",
      "newScheduleDate": "2025-11-10",
      "newTimeSlot": "AFTERNOON",
      "reason": "临时有会议安排",
      "status": "APPROVED",
      "approvedBy": "admin",
      "approvedAt": 1698829200000,
      "createdAt": 1698825600000
    }
  ]
}
```

---

### 12.3 管理员查询调班申请列表

**接口**: `GET /api/schedule-change/admin/list?status=PENDING`

**权限**: 管理员

**查询参数**:
- `status`: 过滤状态（可选）
  - `PENDING`: 待审核
  - `APPROVED`: 已通过
  - `REJECTED`: 已拒绝

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "scheduleId": 1,
      "requestedByUserId": 10,
      "doctorId": 5,
      "originalScheduleDate": "2025-11-08",
      "originalTimeSlot": "MORNING",
      "originalSlotType": "normal",
      "originalTotalSlots": 10,
      "newScheduleDate": "2025-11-10",
      "newTimeSlot": "AFTERNOON",
      "newSlotType": "expert",
      "newTotalSlots": 15,
      "reason": "临时有会议安排，需要调整排班",
      "status": "PENDING",
      "createdAt": 1698825600000,
      "updatedAt": 1698825600000
    }
  ]
}
```

---

### 12.4 管理员审核调班申请

**接口**: `POST /api/schedule-change/admin/review`

**权限**: 管理员

**说明**: 审核通过时自动更新排班信息，拒绝时记录原因。

**请求体（审核通过）**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "action": "APPROVE"
}
```

**请求体（审核拒绝）**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "action": "REJECT",
  "reason": "当前时间段已有其他医生排班"
}
```

**响应示例（通过）**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "APPROVED",
    "approvedBy": "admin",
    "approvedAt": 1698829200000
  }
}
```

**响应示例（拒绝）**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "REJECTED",
    "approvedBy": "admin",
    "approvedAt": 1698829200000,
    "rejectionReason": "当前时间段已有其他医生排班"
  }
}
```

---

## 1️⃣3️⃣ 排班规则管理模块 ⭐

### 13.1 创建排班规则

**接口**: `POST /api/admin/schedule-rules`

**权限**: 仅管理员

**说明**: 创建排班规则模板，规则存储在 `system_config` 表中（JSON格式），可用于批量生成排班。

**请求体**:
```json
{
  "ruleName": "心内科主任医师固定排班",
  "ruleType": "FIXED_WEEKLY",
  "doctorId": 1,
  "departmentId": 1,
  "clinicId": 1,
  "weekDays": [1, 3, 5],
  "timeSlots": ["morning", "afternoon"],
  "slotType": "expert",
  "totalSlots": 20,
  "maxDailySchedules": 2,
  "maxContinuousDays": 5,
  "skipWeekends": true,
  "skipHolidays": true,
  "startDate": "2025-11-01",
  "endDate": "2025-12-31",
  "priority": 10,
  "description": "心内科主任医师每周一、三、五上午和下午出诊"
}
```

**字段说明**:
- `ruleName` (必填): 规则名称
- `ruleType` (必填): 规则类型
  - `FIXED_WEEKLY`: 固定周排班（每周固定某几天）
  - `ROTATION`: 轮班制
  - `CUSTOM`: 自定义规则
- `doctorId` (可选): 关联的医生ID，不传表示通用规则
- `departmentId` (可选): 关联的科室ID
- `clinicId` (可选): 关联的门诊ID
- `weekDays` (可选): 生效的星期几，1=周一, 2=周二, ..., 7=周日
  - 例如: `[1, 3, 5]` 表示周一、三、五
- `timeSlots` (必填): 时间段列表，如 `["morning", "afternoon", "evening"]`
- `slotType` (必填): 号别 `normal`/`expert`/`vip`
- `totalSlots` (必填): 默认总号源数（1-100）
- `maxDailySchedules` (可选): 每天最多排班次数
- `maxContinuousDays` (可选): 最多连续排班天数
- `skipWeekends` (可选): 是否跳过周末，默认 false
- `skipHolidays` (可选): 是否跳过节假日，默认 false
- `startDate` (必填): 规则生效开始日期（yyyy-MM-dd）
- `endDate` (可选): 规则生效结束日期
- `priority` (可选): 优先级，数字越大优先级越高，默认 0
- `description` (可选): 规则描述

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": "SCHEDULE_RULE_A1B2C3D4",
    "ruleName": "心内科主任医师固定排班",
    "ruleType": "FIXED_WEEKLY",
    "doctorId": 1,
    "departmentId": 1,
    "clinicId": 1,
    "weekDays": [1, 3, 5],
    "timeSlots": ["morning", "afternoon"],
    "slotType": "expert",
    "totalSlots": 20,
    "status": "ACTIVE",
    "priority": 10,
    "createdBy": "admin",
    "createdAt": "2025-10-31 10:00:00"
  }
}
```

---

### 13.2 查询所有排班规则

**接口**: `GET /api/admin/schedule-rules`

**权限**: 仅管理员

**说明**: 获取系统中所有的排班规则列表，按优先级降序排列。

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "id": "SCHEDULE_RULE_A1B2C3D4",
      "ruleName": "心内科主任医师固定排班",
      "ruleType": "FIXED_WEEKLY",
      "doctorId": 1,
      "weekDays": [1, 3, 5],
      "timeSlots": ["morning", "afternoon"],
      "slotType": "expert",
      "totalSlots": 20,
      "status": "ACTIVE",
      "priority": 10,
      "createdAt": "2025-10-31 10:00:00"
    }
  ]
}
```

---

### 13.3 查询规则详情

**接口**: `GET /api/admin/schedule-rules/{ruleId}`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID（数字ID，如：1）

**响应示例**: 同 13.1

---

### 13.3.1 查询规则完整详情（包含关联信息）⭐

**接口**: `GET /api/admin/schedule-rules/detail/{id}`

**权限**: 仅管理员

**说明**: 通过数据库联查直接返回包含医生、科室、门诊等完整关联信息的详情，自动转换枚举值为中文。

**路径参数**:
- `id`: 规则ID（数字ID）

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "ruleName": "心内科主任医师固定排班",
    "ruleType": "FIXED",
    "ruleTypeName": "固定排班",
    "doctorId": 1,
    "doctorName": "王医生",
    "doctorTitle": "主任医师",
    "departmentId": 1,
    "departmentName": "心内科",
    "clinicId": 1,
    "clinicName": "普通门诊",
    "weekDays": "1,3,5",
    "weekDaysList": [1, 3, 5],
    "weekDaysDisplay": "周一、周三、周五",
    "timeSlots": "MORNING,AFTERNOON",
    "timeSlotsList": ["MORNING", "AFTERNOON"],
    "timeSlotsDisplay": "上午、下午",
    "slotType": "EXPERT",
    "slotTypeName": "专家号",
    "totalSlots": 20,
    "maxDailySchedules": 2,
    "maxContinuousDays": 5,
    "skipWeekends": true,
    "skipHolidays": true,
    "startDate": "2025-11-01",
    "endDate": "2025-12-31",
    "status": "ACTIVE",
    "statusName": "启用",
    "priority": 10,
    "description": "心内科主任医师每周一、三、五上午和下午出诊",
    "createdBy": "admin",
    "createdAt": "2025-10-31 10:00:00",
    "updatedAt": "2025-10-31 10:00:00"
  }
}
```

**字段说明**:
- **基本信息**:
  - `id`: 规则ID
  - `ruleName`: 规则名称
  - `ruleType`: 规则类型（英文代码）
  - `ruleTypeName`: 规则类型（中文名称）
- **关联信息**（自动联查）:
  - `doctorId/doctorName/doctorTitle`: 医生信息
  - `departmentId/departmentName`: 科室信息
  - `clinicId/clinicName`: 门诊信息
- **时间配置**（多格式）:
  - `weekDays`: 星期字符串（如："1,3,5"）
  - `weekDaysList`: 星期数组（如：[1, 3, 5]）
  - `weekDaysDisplay`: 中文显示（如："周一、周三、周五"）
  - `timeSlots/timeSlotsList/timeSlotsDisplay`: 时段的三种格式
- **号源配置**:
  - `slotType/slotTypeName`: 号别类型及中文名称
  - `totalSlots`: 总号源数
- **高级规则**:
  - `maxDailySchedules`: 每日最大排班数
  - `maxContinuousDays`: 连续天数限制
  - `skipWeekends/skipHolidays`: 是否跳过周末/节假日
- **状态信息**:
  - `status/statusName`: 状态及中文名称
  - `priority`: 优先级
- **审计信息**:
  - `createdBy`: 创建人
  - `createdAt/updatedAt`: 创建/更新时间

**与普通详情接口的区别**:
- ✅ 一次查询返回所有关联信息（医生、科室、门诊）
- ✅ 自动转换枚举值为中文（ACTIVE → "启用"）
- ✅ 提供多种格式的时间配置（字符串、数组、中文显示）
- ✅ 性能更好（使用 SQL JOIN 而非多次查询）

**使用场景**:
- 详情页面展示完整规则信息
- 导出规则数据
- 需要展示中文名称的场景

---

### 13.4 查询医生的排班规则

**接口**: `GET /api/admin/schedule-rules/doctor/{doctorId}`

**权限**: 仅管理员

**路径参数**:
- `doctorId`: 医生ID

**说明**: 获取指定医生的所有排班规则。

---

### 13.5 按状态查询规则

**接口**: `GET /api/admin/schedule-rules/status/{status}`

**权限**: 仅管理员

**路径参数**:
- `status`: 状态（ACTIVE 或 INACTIVE）

**说明**: 查询指定状态的排班规则。

---

### 13.6 更新排班规则

**接口**: `PUT /api/admin/schedule-rules/{ruleId}`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID

**请求体**:
```json
{
  "ruleName": "心内科主任医师更新后的排班",
  "totalSlots": 25,
  "status": "ACTIVE",
  "description": "更新后的描述"
}
```

**说明**: 可部分更新字段，不传的字段保持不变。

**响应示例**: 返回更新后的完整规则信息。

---

### 13.7 删除排班规则

**接口**: `DELETE /api/admin/schedule-rules/{ruleId}`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": "规则删除成功"
}
```

**注意**: 删除规则不会影响已生成的排班记录。

---

### 13.8 启用排班规则

**接口**: `POST /api/admin/schedule-rules/{ruleId}/enable`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID

**说明**: 将规则状态设置为 ACTIVE。

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": "规则已启用"
}
```

---

### 13.9 禁用排班规则

**接口**: `POST /api/admin/schedule-rules/{ruleId}/disable`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID

**说明**: 将规则状态设置为 INACTIVE，禁用后无法应用生成排班。

---

### 13.10 应用规则生成排班 ⭐

**接口**: `POST /api/admin/schedule-rules/{ruleId}/apply`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID

**说明**: 根据规则自动批量创建排班记录，这是排班规则的核心功能。

**请求体（可选）**:
```json
{
  "applyStartDate": "2025-11-01",
  "applyEndDate": "2025-11-30",
  "overwriteExisting": false,
  "excludeDates": ["2025-11-05", "2025-11-12"]
}
```

**字段说明**:
- `applyStartDate` (可选): 应用开始日期，不传则使用规则的开始日期
- `applyEndDate` (可选): 应用结束日期，不传则使用规则的结束日期或默认生成30天
- `overwriteExisting` (可选): 是否覆盖已存在的排班，默认 false（跳过）
- `excludeDates` (可选): 排除的日期列表，在这些日期不生成排班

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "successCount": 24,
    "skipCount": 3,
    "errorCount": 0,
    "errors": [],
    "message": "成功创建 24 个排班，跳过 3 个已存在的排班，失败 0 个"
  }
}
```

**业务逻辑**:
1. 检查规则是否启用（ACTIVE）
2. 根据规则的 `weekDays`、`skipWeekends` 等条件生成日期列表
3. 排除 `excludeDates` 中的日期
4. 对每个日期和时间段检查是否已存在排班
5. 根据 `overwriteExisting` 参数决定是覆盖还是跳过
6. 批量创建排班记录

**使用场景**:
- 新医生入职，快速生成一个月的排班
- 调整某医生的长期排班规则
- 批量生成节假日后的排班

---

### 13.11 检测规则冲突

**接口**: `GET /api/admin/schedule-rules/{ruleId}/conflicts`

**权限**: 仅管理员

**路径参数**:
- `ruleId`: 规则ID

**说明**: 检查指定规则与其他规则是否存在冲突（同一医生、重叠时间）。

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "hasConflicts": true,
    "conflictCount": 1,
    "conflicts": [
      {
        "conflictRuleId": "SCHEDULE_RULE_B2C3D4E5",
        "conflictRuleName": "心内科医师轮班规则",
        "reason": "规则在医生ID=1、日期范围和时间段上存在重叠"
      }
    ]
  }
}
```

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

### 流程4: 管理员制定排班规则并生成排班 ⭐

1. **创建排班规则**: `POST /api/admin/schedule-rules`
   ```json
   {
     "ruleName": "心内科主任医师固定排班",
     "ruleType": "FIXED_WEEKLY",
     "doctorId": 1,
     "weekDays": [1, 3, 5],
     "timeSlots": ["morning", "afternoon"],
     "slotType": "expert",
     "totalSlots": 20,
     "skipWeekends": true,
     "startDate": "2025-11-01",
     "endDate": "2025-12-31",
     "description": "每周一、三、五上午和下午出诊"
   }
   ```
   - 返回规则ID（数字ID，如：`1`）

2. **检测规则冲突**: `GET /api/admin/schedule-rules/{ruleId}/conflicts`
   - 确保规则不会与其他规则冲突

3. **应用规则生成排班**: `POST /api/admin/schedule-rules/{ruleId}/apply`
   ```json
   {
     "applyStartDate": "2025-11-01",
     "applyEndDate": "2025-11-30",
     "overwriteExisting": false,
     "excludeDates": ["2025-11-05"]
   }
   ```
   - 系统自动生成整月的排班
   - 返回成功、跳过、失败的数量

4. **查看生成的排班**: `GET /api/admin/schedules?doctorId=1&startDate=2025-11-01&endDate=2025-11-30`

5. **管理规则**:
   - 启用规则: `POST /api/admin/schedule-rules/{ruleId}/enable`
   - 禁用规则: `POST /api/admin/schedule-rules/{ruleId}/disable`
   - 更新规则: `PUT /api/admin/schedule-rules/{ruleId}`
   - 删除规则: `DELETE /api/admin/schedule-rules/{ruleId}`

**使用场景**:
- 新医生入职：创建规则 → 应用规则生成未来一个月排班
- 调整排班：修改规则 → 覆盖模式重新生成排班
- 临时调整：禁用规则 → 手动创建单个排班
- 批量管理：查询所有规则 → 按需启用/禁用

---

### 流程5: 管理员管理医生

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

## 1️⃣4️⃣ 申请管理模块 ⭐

说明：统一的申请管理模块，支持调班申请和医生信息修改申请。

### 14.1 创建申请

**接口**: `POST /api/application-requests`

**权限**: 医生或管理员

**说明**: 创建调班申请或信息修改申请

**请求体（调班申请示例）**:
```json
{
  "requestType": "SCHEDULE_CHANGE",
  "scheduleId": 1,
  "changeType": "RESCHEDULE",
  "originalDate": "2025-11-08",
  "originalTimeSlot": "MORNING",
  "newDate": "2025-11-10",
  "newTimeSlot": "AFTERNOON",
  "slotsAdjustment": 0,
  "reason": "临时有会议，需要调整排班"
}
```

**请求体（信息修改申请示例）**:
```json
{
  "requestType": "INFO_UPDATE",
  "doctorId": 1,
  "fieldName": "title",
  "oldValue": "主治医师",
  "newValue": "副主任医师",
  "reason": "职称晋升"
}
```

**字段说明**:

**通用字段**:
- `requestType` (必填): 申请类型
  - `SCHEDULE_CHANGE`: 调班申请
  - `INFO_UPDATE`: 信息修改申请
- `reason` (可选): 申请原因

**调班申请字段**:
- `scheduleId` (必填): 原排班ID
- `changeType` (必填): 变更类型
  - `RESCHEDULE`: 改期
  - `CANCEL`: 取消排班
  - `SLOTS_ADJUST`: 号源调整
- `originalDate/originalTimeSlot`: 原日期/时段
- `newDate/newTimeSlot`: 新日期/时段（改期时必填）
- `slotsAdjustment`: 号源调整数量（调整号源时必填）

**信息修改申请字段**:
- `doctorId` (必填): 医生ID
- `fieldName` (必填): 要修改的字段名（如：title, specialty, bio 等）
- `oldValue`: 旧值
- `newValue` (必填): 新值

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "requestType": "SCHEDULE_CHANGE",
    "requestTypeName": "调班申请",
    "applicantId": 2,
    "scheduleId": 1,
    "changeType": "RESCHEDULE",
    "changeTypeName": "改期",
    "originalDate": "2025-11-08",
    "originalTimeSlot": "MORNING",
    "newDate": "2025-11-10",
    "newTimeSlot": "AFTERNOON",
    "status": "PENDING",
    "statusName": "待审核",
    "reason": "临时有会议，需要调整排班",
    "createdAt": "2025-10-31 14:30:00"
  }
}
```

---

### 14.2 查询所有申请

**接口**: `GET /api/application-requests`

**权限**: 仅管理员

**说明**: 管理员查询所有申请记录

---

### 14.3 查询我的申请

**接口**: `GET /api/application-requests/my`

**权限**: 医生或管理员

**说明**: 查询当前用户提交的所有申请

---

### 14.4 查询申请详情

**接口**: `GET /api/application-requests/{requestId}`

**权限**: 医生或管理员

**路径参数**:
- `requestId`: 申请ID

---

### 14.4.1 查询申请完整详情（包含关联信息）⭐

**接口**: `GET /api/application-requests/detail/{requestId}`

**权限**: 医生或管理员

**说明**: 通过数据库联查直接返回包含申请人、审核人、医生等完整关联信息的详情，自动转换枚举值为中文。

**路径参数**:
- `requestId`: 申请ID

**响应示例（调班申请）**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "requestType": "SCHEDULE_CHANGE",
    "requestTypeName": "调班申请",
    "applicantId": 2,
    "applicantUsername": "doctor1",
    "applicantRole": "DOCTOR",
    "scheduleId": 1,
    "changeType": "RESCHEDULE",
    "changeTypeName": "改期",
    "originalDate": "2025-11-08",
    "originalTimeSlot": "MORNING",
    "originalTimeSlotName": "上午",
    "newDate": "2025-11-10",
    "newTimeSlot": "AFTERNOON",
    "newTimeSlotName": "下午",
    "slotsAdjustment": 0,
    "status": "PENDING",
    "statusName": "待审核",
    "reason": "临时有会议，需要调整排班",
    "rejectionReason": null,
    "reviewerId": null,
    "reviewerUsername": null,
    "reviewedAt": null,
    "createdAt": "2025-10-31 14:30:00",
    "updatedAt": "2025-10-31 14:30:00"
  }
}
```

**响应示例（信息修改申请）**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 2,
    "requestType": "INFO_UPDATE",
    "requestTypeName": "信息修改申请",
    "applicantId": 3,
    "applicantUsername": "doctor2",
    "applicantRole": "DOCTOR",
    "doctorId": 1,
    "doctorName": "王医生",
    "fieldName": "title",
    "fieldNameChinese": "职称",
    "oldValue": "主治医师",
    "newValue": "副主任医师",
    "status": "APPROVED",
    "statusName": "已批准",
    "reason": "职称晋升",
    "rejectionReason": null,
    "reviewerId": 1,
    "reviewerUsername": "admin",
    "reviewedAt": "2025-10-31 15:00:00",
    "createdAt": "2025-10-31 14:45:00",
    "updatedAt": "2025-10-31 15:00:00"
  }
}
```

**字段说明**:
- **基本信息**:
  - `id`: 申请ID
  - `requestType/requestTypeName`: 申请类型及中文名称
  - `status/statusName`: 状态及中文名称（待审核/已批准/已拒绝/已取消）
- **申请人信息**（自动联查）:
  - `applicantId/applicantUsername/applicantRole`: 申请人信息
- **调班申请字段**:
  - `scheduleId`: 排班ID
  - `changeType/changeTypeName`: 变更类型及中文名称
  - `originalDate/originalTimeSlot/originalTimeSlotName`: 原日期/时段（含中文）
  - `newDate/newTimeSlot/newTimeSlotName`: 新日期/时段（含中文）
  - `slotsAdjustment`: 号源调整数量
- **信息修改字段**:
  - `doctorId/doctorName`: 医生信息
  - `fieldName/fieldNameChinese`: 字段名及中文名称
  - `oldValue/newValue`: 旧值/新值
- **审核信息**（自动联查）:
  - `reviewerId/reviewerUsername`: 审核人信息
  - `reviewedAt`: 审核时间
  - `reason`: 申请原因
  - `rejectionReason`: 拒绝原因
- **审计信息**:
  - `createdAt/updatedAt`: 创建/更新时间

**与普通详情接口的区别**:
- ✅ 一次查询返回所有关联信息（申请人、审核人、医生）
- ✅ 自动转换枚举值为中文（PENDING → "待审核"）
- ✅ 自动转换字段名为中文（title → "职称"）
- ✅ 性能更好（使用 SQL JOIN 而非多次查询）

**使用场景**:
- 申请详情页面展示完整信息
- 审核页面展示申请人和相关信息
- 需要展示中文名称的场景

---

### 14.5 按状态查询申请

**接口**: `GET /api/application-requests/status/{status}`

**权限**: 仅管理员

**路径参数**:
- `status`: 状态（PENDING/APPROVED/REJECTED/CANCELLED）

**说明**: 管理员根据状态筛选申请

---

### 14.6 查询待审核申请

**接口**: `GET /api/application-requests/pending`

**权限**: 仅管理员

**说明**: 管理员查询所有待审核的申请

---

### 14.7 审核申请 ⭐

**接口**: `POST /api/application-requests/review`

**权限**: 仅管理员

**说明**: 管理员批准或拒绝申请，支持调班申请和信息修改申请

**请求体（批准）**:
```json
{
  "requestId": 1,
  "action": "APPROVED"
}
```

**请求体（拒绝）**:
```json
{
  "requestId": 1,
  "action": "REJECTED",
  "rejectReason": "当前时间段已有其他医生排班"
}
```

**字段说明**:
- `requestId` (必填): 申请ID
- `action` (必填): 审核操作
  - `APPROVED`: 批准
  - `REJECTED`: 拒绝
- `rejectReason` (拒绝时必填): 拒绝原因

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "statusName": "已批准",
    "reviewerId": 1,
    "reviewedAt": "2025-10-31 15:00:00"
  }
}
```

**业务逻辑**:
- **调班申请批准**时：
  - 根据 `changeType` 执行相应操作
  - `RESCHEDULE`: 更新排班日期/时段
  - `CANCEL`: 取消排班
  - `SLOTS_ADJUST`: 调整号源数量
- **信息修改申请批准**时：
  - 更新医生的对应字段信息

---

### 14.8 取消申请

**接口**: `POST /api/application-requests/{requestId}/cancel`

**权限**: 申请人本人

**路径参数**:
- `requestId`: 申请ID

**说明**: 申请人取消自己的待审核申请

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": "申请已取消"
}
```

---

### 14.9 删除申请

**接口**: `DELETE /api/application-requests/{requestId}`

**权限**: 仅管理员

**路径参数**:
- `requestId`: 申请ID

**说明**: 管理员删除申请记录（谨慎操作）

---

### 14.10 获取申请统计

**接口**: `GET /api/application-requests/statistics`

**权限**: 仅管理员

**说明**: 管理员查看申请统计数据

**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "total": 50,
    "pending": 12,
    "approved": 30,
    "rejected": 6,
    "cancelled": 2,
    "scheduleChange": 35,
    "infoUpdate": 15
  }
}
```

**字段说明**:
- `total`: 总申请数
- `pending`: 待审核数
- `approved`: 已批准数
- `rejected`: 已拒绝数
- `cancelled`: 已取消数
- `scheduleChange`: 调班申请数
- `infoUpdate`: 信息修改申请数

---

## 1️⃣5️⃣ 号别与号源上限管理（管理员） ⭐

说明：统一管理不同层级（全局/医生/门诊）的排班与预约容量策略，解耦具体排班记录中的 `slotType`、`totalSlots` 与平台治理策略，支持覆盖继承。

### 15.1 查询全局上限配置
**接口**: `GET /api/admin/schedule-settings`
**权限**: 管理员
**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "allowedSlotTypes": ["normal", "expert", "vip"],
    "defaultTotalSlots": 20,
    "maxSlotsPerSchedule": 50,
    "maxAppointmentsPerDayPerDoctor": 60,
    "maxAppointmentsPerDayPerPatient": 3,
    "vipDailyLimitPerDoctor": 10,
    "enforceWeekendLimits": true,
    "cancelPolicy": {
      "latestCancelHours": 2,
      "penaltyEnabled": false
    },
    "overrideStrategy": "INHERIT"
  }
}
```

字段说明：
- `allowedSlotTypes`: 允许的号别集合（`normal/expert/vip`）
- `defaultTotalSlots`: 未配置时默认每条排班的总号源
- `maxSlotsPerSchedule`: 单条排班最大可配置总号源上限
- `maxAppointmentsPerDayPerDoctor`: 单医生单日预约总上限（跨排班累计）
- `maxAppointmentsPerDayPerPatient`: 单患者单日预约上限（与系统配置保持一致，允许在此冗余管理）
- `vipDailyLimitPerDoctor`: 单医生单日 VIP 号上限
- `enforceWeekendLimits`: 是否对周末启用更严格的上限策略
- `cancelPolicy.latestCancelHours`: 就诊前最晚允许取消的小时数
- `cancelPolicy.penaltyEnabled`: 逾期取消是否触发惩罚（由业务决定处理方式）
- `overrideStrategy`: 下级（医生/门诊）策略与上级的关系（`INHERIT`/`OVERRIDE`）

---

### 15.2 更新全局上限配置
**接口**: `PUT /api/admin/schedule-settings`
**权限**: 管理员
**请求体（部分字段可选）**:
```json
{
  "allowedSlotTypes": ["normal", "expert", "vip"],
  "defaultTotalSlots": 25,
  "maxSlotsPerSchedule": 60,
  "maxAppointmentsPerDayPerDoctor": 80,
  "maxAppointmentsPerDayPerPatient": 3,
  "vipDailyLimitPerDoctor": 12,
  "enforceWeekendLimits": true,
  "cancelPolicy": {
    "latestCancelHours": 4,
    "penaltyEnabled": true
  },
  "overrideStrategy": "INHERIT",
  "effectiveStartDate": "2025-11-15",
  "effectiveEndDate": null
}
```
说明：
- 支持设置生效期（`effectiveStartDate`/`effectiveEndDate`），不传则立即生效且不限期
- 只传需要调整的字段，未传字段保持不变

---

### 15.3 查询医生级上限
**接口**: `GET /api/admin/schedule-settings/doctor/{doctorId}`
**权限**: 管理员
**路径参数**:
- `doctorId`: 医生ID
**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "doctorId": 1,
    "allowedSlotTypes": ["normal", "expert"],
    "defaultTotalSlots": 30,
    "maxSlotsPerSchedule": 40,
    "vipDailyLimitPerDoctor": 8,
    "overrideStrategy": "OVERRIDE",
    "effectiveStartDate": "2025-11-10",
    "effectiveEndDate": null
  }
}
```

---

### 15.4 更新医生级上限
**接口**: `PUT /api/admin/schedule-settings/doctor/{doctorId}`
**权限**: 管理员
**路径参数**:
- `doctorId`: 医生ID
**请求体（部分字段可选）**:
```json
{
  "allowedSlotTypes": ["normal", "expert"],
  "defaultTotalSlots": 30,
  "maxSlotsPerSchedule": 40,
  "vipDailyLimitPerDoctor": 8,
  "overrideStrategy": "OVERRIDE",
  "effectiveStartDate": "2025-11-10",
  "effectiveEndDate": null,
  "description": "主任医师特需"
}
```
说明：
- 当 `overrideStrategy=OVERRIDE` 时，未显式给出的字段将不继承全局，建议显式给全量字段
- 当设为 `INHERIT` 时，下级未配置字段从全局继承

---

### 15.5 查询门诊级上限
**接口**: `GET /api/admin/schedule-settings/clinic/{clinicId}`
**权限**: 管理员
**路径参数**:
- `clinicId`: 门诊ID
**响应示例**:
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "clinicId": 1,
    "allowedSlotTypes": ["normal", "expert", "vip"],
    "defaultTotalSlots": 20,
    "maxSlotsPerSchedule": 50,
    "overrideStrategy": "INHERIT",
    "effectiveStartDate": null,
    "effectiveEndDate": null
  }
}
```

---

### 15.6 更新门诊级上限
**接口**: `PUT /api/admin/schedule-settings/clinic/{clinicId}`
**权限**: 管理员
**路径参数**:
- `clinicId`: 门诊ID
**请求体（部分字段可选）**:
```json
{
  "allowedSlotTypes": ["normal", "expert", "vip"],
  "defaultTotalSlots": 22,
  "maxSlotsPerSchedule": 55,
  "overrideStrategy": "INHERIT",
  "effectiveStartDate": "2025-11-20",
  "effectiveEndDate": null
}
```

---

使用说明：
- 排班创建/更新与预约创建时，可参考上述配置进行约束校验：
  1) 校验 `slotType` 是否在 `allowedSlotTypes` 内
  2) 校验 `totalSlots` 不超过 `maxSlotsPerSchedule`
  3) 跨排班累计校验医生单日预约量不超过 `maxAppointmentsPerDayPerDoctor`
  4) 患者侧校验单日预约次数不超过 `maxAppointmentsPerDayPerPatient`
  5) 取消时根据 `cancelPolicy` 执行时间窗与惩罚策略
- 覆盖优先级：医生级 > 门诊级 > 全局；同层级使用最新的有效期配置

---

## 1️⃣6️⃣ 患者端 - 智能问答助手

说明：为小程序/患者端提供问答助手能力，支持“猜你想问”（高频问题）与“关键词匹配”两类接口。数据来源于系统配置中的 FAQ/统计条目，后续可对接专门知识库表。

### 16.1 高频问题统计
接口：`GET /api/patient/qa/top-questions`
权限：患者（`PATIENT`）
请求参数：
- `limit` 可选，返回条数，默认 10

响应示例：
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    { "question": "如何预约挂号？", "count": 128 },
    { "question": "可以取消预约吗？", "count": 97 }
  ]
}
```

说明：
- 统计数据暂从系统配置 `qa.stats` 读取，格式：`{ "问题A": 100, "问题B": 50 }`
- 未来可接驳日志埋点与离线/实时统计

---

### 16.2 关键词匹配接口
接口：`POST /api/patient/qa/keyword-match`
权限：患者（`PATIENT`）
请求体：
```json
{ "query": "退号 时限", "topK": 5 }
```

响应示例：
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "question": "可以取消预约吗？",
      "answer": "就诊前2小时可在个人中心取消，逾期将记一次违约。",
      "score": 0.9
    },
    {
      "question": "退号有时间限制吗？",
      "answer": "距就诊时间不足2小时不可退号。",
      "score": 0.7
    }
  ]
}
```

说明：
- FAQ 数据暂从系统配置 `qa.faqs` 读取，格式：`[{ "question": "...", "answer": "..." }, ...]`
- 匹配规则为朴素的关键字相似度打分，未来可替换为向量检索或更强检索器

---

### 16.3 管理端 FAQ 管理（CRUD）
说明：管理员维护问答助手的知识库与统计，支持导入/导出。

- 接口：`GET /api/admin/qa/faqs`
  权限：管理员
  说明：分页/全量获取 FAQ 列表
  查询参数：`page`、`pageSize`（可选）

- 接口：`POST /api/admin/qa/faqs`
  权限：管理员
  请求体（单条）：
  ```json
  { "question": "可以取消预约吗？", "answer": "就诊前2小时可在个人中心取消。" }
  ```

- 接口：`PUT /api/admin/qa/faqs/{id}`
  权限：管理员
  请求体（部分字段可选）：
  ```json
  { "question": "可以取消预约吗？", "answer": "就诊前2小时可在个人中心取消，逾期计违约。" }
  ```

- 接口：`DELETE /api/admin/qa/faqs/{id}`
  权限：管理员
  说明：删除 FAQ

- 接口：`POST /api/admin/qa/faqs/import`
  权限：管理员
  说明：批量导入 FAQ
  请求体：
  ```json
  {
    "faqs": [
      { "question": "如何预约挂号？", "answer": "进入预约页选择科室/医生/时段提交即可。" },
      { "question": "退号有时间限制吗？", "answer": "就诊前2小时内不可退号。" }
    ]
  }
  ```

- 接口：`GET /api/admin/qa/faqs/export`
  权限：管理员
  说明：导出当前 FAQ 列表（JSON）

- 接口：`PUT /api/admin/qa/stats`
  权限：管理员
  说明：维护高频问题统计（可人工校正或清零）
  请求体：
  ```json
  { "stats": { "如何预约挂号？": 128, "可以取消预约吗？": 97 } }
  ```

数据存储说明：
- 可先存放于 `system_config`（键：`qa.faqs`、`qa.stats`），后续平滑迁移至专门表 `qa_faq`、`qa_stats`。

---

### 16.4 面向未来的大模型集成端点（预留）
接口：`POST /api/patient/qa/ask`
权限：患者
用途：统一对话入口，优先使用大模型（如已启用且有配额），否则回退关键词匹配。

请求体：
```json
{
  "query": "如何退号？",
  "context": {
    "patientId": 1,
    "preferredLanguage": "zh-CN"
  }
}
```

响应（使用 LLM 时）：
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "answer": "就诊前2小时可在个人中心取消预约...",
    "source": "LLM",
    "traceId": "req_20251111_abc123"
  }
}
```

响应（回退关键词匹配时）：
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "answer": "就诊前2小时可在个人中心取消预约...",
    "source": "FAQ",
    "matches": [
      { "question": "可以取消预约吗？", "score": 0.88 }
    ]
  }
}
```

固定身份回答规则（强制）：
- 若用户问题涉及“模型是什么/是谁”等元问题（例如：你是什么模型？你是谁？由谁提供支持？），必须直接返回以下固定回答，不走 LLM 推理或 FAQ 匹配：
  - “您好，我是由claude-4.5-sonnet-thinking模型提供支持，作为Cursor IDE的核心功能之一，可协助完成各类开发任务，只要是编程相关的问题，都可以问我！你现在有什么想做的吗？”

开关与配置（`system_config` 或 `application.yml`）：
- `qa.llm.enabled`：是否启用 LLM（默认 false）
- `qa.llm.provider`：`anthropic`/`openai`/`azure`/`local` 等（默认空）
- `qa.llm.apiKey`：大模型 API Key（建议通过安全配置注入）
- `qa.llm.model`：模型名称（如 `claude-4.5-sonnet-thinking`）
- `qa.answer.language`：默认语言，`zh-CN`
- `qa.safety.maxTokens`、`qa.safety.timeoutMs`、`qa.safety.blocklist`：安全与风控配置

回退策略：
1) 当 `qa.llm.enabled=false` 或调用异常/超时，回退至关键词匹配
2) 当命中“固定身份回答”规则时，直接返回固定文本

审计与埋点（建议）：
- 记录 `query`、`source`（LLM/FAQ/IDENTITY_RULE）、`latencyMs`、`traceId`
- 匿名化采样统计命中与点击数据以更新 `qa.stats`

---

## 📋 常见业务流程更新

### 流程6: 医生调班申请审核流程

1. **医生提交调班申请**: `POST /api/application-requests`
   ```json
   {
     "requestType": "SCHEDULE_CHANGE",
     "scheduleId": 1,
     "changeType": "RESCHEDULE",
     "originalDate": "2025-11-08",
     "originalTimeSlot": "MORNING",
     "newDate": "2025-11-10",
     "newTimeSlot": "AFTERNOON",
     "reason": "临时有会议"
   }
   ```

2. **医生查看自己的申请**: `GET /api/application-requests/my`

3. **管理员查看待审核申请**: `GET /api/application-requests/pending`

4. **管理员查看申请详情**: `GET /api/application-requests/detail/{requestId}`
   - 查看申请人、医生、原排班、新排班等完整信息

5. **管理员审核申请**: `POST /api/application-requests/review`
   ```json
   {
     "requestId": 1,
     "approved": true,
     "reviewComment": "同意调班"
   }
   ```
   - 系统自动更新排班信息

---

### 流程7: 医生信息修改申请流程（方式一：通用申请接口）

1. **医生提交信息修改申请**: `POST /api/application-requests`
   ```json
   {
     "requestType": "INFO_UPDATE",
     "doctorId": 1,
     "fieldName": "specialty",
     "oldValue": "心血管内科",
     "newValue": "心血管内科、介入治疗",
     "reason": "专业技能拓展"
   }
   ```

2. **管理员查看待审核申请**: `GET /api/application-requests/pending`

3. **管理员审核**: `POST /api/application-requests/review`
   ```json
   {
     "requestId": 1,
     "action": "APPROVED"
   }
   ```
   - 批准后系统自动更新医生信息

---

### 流程8: 医生信息修改申请流程（方式二：医生端专用接口）⭐

1. **医生提交修改申请**: `POST /doctor/apply-info-update`
   ```json
   {
     "fieldName": "title",
     "newValue": "主任医师",
     "reason": "已获得主任医师资格证书"
   }
   ```
   - 系统自动获取当前医生身份
   - 自动获取字段的旧值

2. **医生查看申请列表**: `GET /doctor/my-info-applications`
   - 查看所有自己提交的申请及状态

3. **医生查看个人信息**: `GET /doctor/my-info`
   - 查看当前的个人信息

4. **管理员查看待审核申请**: `GET /api/application-requests/pending`
   - 查看所有待审核的申请

5. **管理员查看申请详情**: `GET /api/application-requests/detail/{requestId}`
   - 查看包含申请人、医生、字段变更等完整信息

6. **管理员审核申请**: `POST /api/application-requests/review`
   ```json
   {
     "requestId": 1,
     "action": "APPROVED"
   }
   ```
   - 批准后系统自动更新医生的对应字段

7. **医生确认更新**: `GET /doctor/my-info`
   - 查看信息是否已更新

**两种方式的区别**:
- **方式一（通用接口）**: 需要手动指定 doctorId 和 oldValue，适合管理员代理提交
- **方式二（医生端接口）**: 自动获取医生身份和旧值，更简单安全，推荐医生使用

---

**文档版本**: v1.7
**最后更新**: 2025-11-11
**维护者**: YiDianTong 开发团队

---

## 📝 更新日志
### v1.7 (2025-11-11)
- ✅ 更新预约业务规则与文档细节
  - 创建预约新增：同日次数上限（`APPOINTMENT_DAILY_LIMIT`）、同排班防重复、未来时间与排班日期一致校验、号源原子扣减、费用按 `slotType` 配置
  - 取消/删除预约：优先候补自动填充；无候补再归还号源；新增退号时限（`CANCEL_LIMIT_MINUTES`）
  - 错误示例补充与文案统一（“号源不足/当天预约次数已达上限/请勿重复预约该排班/距离就诊不足X分钟，不可退号”）

### v1.6 (2025-11-11)
- ✅ 新增“号别与号源上限管理”模块（管理员）
  - 全局/医生/门诊三级上限配置查询与更新
  - 字段覆盖策略、默认号源、号别白名单、取消政策等
  - 与排班与预约相关接口解耦，便于统一治理
- ✅ 新增患者端“智能问答助手”接口
  - `GET /api/patient/qa/top-questions` 高频问题统计
  - `POST /api/patient/qa/keyword-match` 关键词匹配FAQ
  - 管理端 FAQ 管理 CRUD：`/api/admin/qa/faqs*`、`/api/admin/qa/stats`
  - 预留大模型集成端点：`POST /api/patient/qa/ask`，含固定身份回答规则与回退策略

### v1.5 (2025-11-11)
- ✅ 新增患者端“医生坐诊时间”接口文档
  - `GET /doctor/{id}/schedules`：支持 `startDate`/`endDate`/`timeSlot` 参数
  - 说明 `endDate` 规范化为“次日零点”，`timeSlot` 兼容中英文
  - 返回结构包含 `bookedSlots`、`appointmentCount`

### v1.4 (2025-11-04)
- ✅ 新增医生端个人信息修改申请接口
  - `POST /doctor/apply-info-update` - 提交个人信息修改申请
  - `GET /doctor/my-info-applications` - 查看我的信息修改申请
  - `GET /doctor/my-info` - 查看我的个人信息
- ✅ 更新申请管理模块审核接口参数格式
- ✅ 新增医生信息修改申请流程文档（两种方式）

### v1.3 (2025-10-31)
- 新增申请管理统一模块
- 新增排班规则管理功能

