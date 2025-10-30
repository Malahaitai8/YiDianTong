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

## 1️⃣2️⃣ 医生注册审核模块（管理端）

### 12.1 获取待审核医生列表

**接口**: `GET /api/admin/approval/doctors/pending`

**权限**: 管理员

**说明**: 返回状态为 `pending_approval` 的医生账号及其基础资料。

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "userId": 20,
      "username": "doctor001",
      "doctorId": 1,
      "name": "王医生",
      "title": "主任医师",
      "specialty": "心内科",
      "bio": "擅长心血管疾病诊疗，从医20年",
      "clinicId": 1,
      "clinicName": "普通门诊",
      "createdAt": "2025-10-22T14:30:00",
      "status": "pending_approval"
    }
  ],
  "total": 1
}
```

---

### 12.2 获取待审核医生详情

**接口**: `GET /api/admin/approval/doctors/{userId}`

**权限**: 管理员

**路径参数**:
- `userId`: 用户ID

**响应示例**:
```json
{
  "success": true,
  "data": {
    "userId": 20,
    "username": "doctor001",
    "doctorId": 1,
    "name": "王医生",
    "title": "主任医师",
    "specialty": "心内科",
    "bio": "擅长心血管疾病诊疗，从医20年",
    "clinicId": 1,
    "clinicName": "普通门诊",
    "createdAt": "2025-10-22T14:30:00",
    "status": "pending_approval"
  }
}
```

---

### 12.3 审核通过医生注册

**接口**: `POST /api/admin/approval/doctors/approve`

**权限**: 管理员

**请求体**:
```json
{
  "userId": 20,
  "reason": "资料齐全，审核通过"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "审核通过，医生账号已激活"
}
```

**错误示例**:
```json
{
  "success": false,
  "message": "该用户不是待审核状态"
}
```

---

### 12.4 审核拒绝医生注册

**接口**: `POST /api/admin/approval/doctors/reject`

**权限**: 管理员

**请求体**:
```json
{
  "userId": 20,
  "reason": "资料不完整"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "已拒绝该医生的注册申请",
  "reason": "资料不完整"
}
```

---

### 12.5 批量审核通过

**接口**: `POST /api/admin/approval/doctors/approve/batch`

**权限**: 管理员

**请求体**:
```json
[20, 21, 22]
```

**响应示例**:
```json
{
  "success": true,
  "message": "批量审核完成: 成功 2 个, 失败 1 个",
  "successCount": 2,
  "failCount": 1,
  "errors": [
    "用户ID 22 不存在或不是待审核状态"
  ]
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

**文档版本**: v1.2  
**最后更新**: 2025-10-26  
**维护者**: YiDianTong 开发团队

