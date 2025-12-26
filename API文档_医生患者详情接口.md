# 医生患者详情接口文档

## 接口概述

医生可提前了解患者预约信息，点击患者可了解患者挂号状态（是否曾经就诊，曾就诊时间）。

## 接口信息

- **接口路径**: `GET /appointment/doctor/patients`
- **请求方法**: GET
- **权限要求**: 医生角色 (`DOCTOR`)
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`

## 请求信息

### 请求头
```
Authorization: Bearer <JWT_TOKEN>
```

### 请求参数
无查询参数，通过JWT Token中的用户信息识别当前医生。

## 响应信息

### 成功响应 (200)

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "patientId": 123,
      "name": "张三",
      "phoneNumber": "13800138000",
      "specificRole": "student",
      "hasVisited": true,
      "lastVisitDate": "2025-12-01T09:00:00.000+00:00",
      "lastVisitStatus": "COMPLETED",
      "lastStatusName": "已完成",
      "nextAppointmentId": 456,
      "nextAppointmentDate": "2025-12-30T14:00:00.000+00:00",
      "nextAppointmentTimeSlot": "AFTERNOON",
      "totalAppointments": 5,
      "completedAppointments": 3
    },
    {
      "patientId": 124,
      "name": "李四",
      "phoneNumber": "13800138001",
      "specificRole": "teacher",
      "hasVisited": false,
      "lastVisitDate": "2025-12-15T10:30:00.000+00:00",
      "lastVisitStatus": "SCHEDULED",
      "lastStatusName": "已预约",
      "nextAppointmentId": null,
      "nextAppointmentDate": null,
      "nextAppointmentTimeSlot": null,
      "totalAppointments": 1,
      "completedAppointments": 0
    }
  ]
}
```

### 响应字段说明

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| patientId | Long | 是 | 患者ID |
| name | String | 是 | 患者姓名 |
| phoneNumber | String | 是 | 患者手机号 |
| specificRole | String | 是 | 患者身份角色（student/teacher/staff/outsider等） |
| hasVisited | Boolean | 是 | 是否曾经在该医生处就诊过 |
| lastVisitDate | Date | 否 | 最近一次就诊/预约日期（ISO 8601格式） |
| lastVisitStatus | String | 否 | 最近一次预约状态（英文） |
| lastStatusName | String | 否 | 最近一次预约状态（中文） |
| nextAppointmentId | Long | 否 | 即将到诊的预约ID（未来预约） |
| nextAppointmentDate | Date | 否 | 即将到诊的时间（ISO 8601格式） |
| nextAppointmentTimeSlot | String | 否 | 即将到诊的时间段（MORNING/AFTERNOON） |
| totalAppointments | Integer | 是 | 在该医生处的总预约次数 |
| completedAppointments | Integer | 是 | 在该医生处已完成的就诊次数 |

### 预约状态枚举

| 状态值 | 中文说明 | 说明 |
|--------|----------|------|
| SCHEDULED | 已预约 | 患者已预约但未到诊 |
| CONFIRMED | 已确认 | 预约已确认 |
| COMPLETED | 已完成 | 就诊已完成 |
| CANCELLED | 已取消 | 预约已被取消 |
| NOSHOW | 未到诊 | 患者未按时到诊 |

### 时间段枚举

| 状态值 | 中文说明 |
|--------|----------|
| MORNING | 上午 |
| AFTERNOON | 下午 |

## 错误响应

### 401 Unauthorized
```json
{
  "code": 401,
  "message": "未登录"
}
```

### 403 Forbidden
```json
{
  "code": 403,
  "message": "无权限访问"
}
```

### 500 Internal Server Error
```json
{
  "code": 500,
  "message": "获取患者详情失败: <错误信息>"
}
```

## 使用示例

### cURL 示例
```bash
curl -X GET \
  "http://localhost:8080/appointment/doctor/patients" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### JavaScript (Axios) 示例
```javascript
import axios from 'axios';

// 假设已经获取到JWT token
const token = localStorage.getItem('token');

const response = await axios.get('/appointment/doctor/patients', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

console.log(response.data.data); // 患者详情列表
```

## 业务逻辑说明

1. **数据范围**: 仅返回当前登录医生名下所有患者的预约信息
2. **排序规则**: 按最近预约时间倒序排列（最新的预约在前）
3. **权限控制**: 只有医生角色的用户才能调用此接口
4. **数据安全**: 通过JWT Token验证用户身份，确保医生只能查看自己的患者
5. **性能优化**: 查询包含统计计算，建议前端适当缓存数据

## 相关接口

- `GET /appointment/me` - 患者查看自己的预约记录
- `GET /appointment/selectById/{id}` - 查看具体预约详情
- `GET /patient/profile` - 患者查看个人信息

## 注意事项

1. **身份验证**: 调用前确保用户已登录且具有医生角色
2. **网络超时**: 建议设置合理的请求超时时间
3. **数据更新**: 患者预约状态变化时，统计数据会实时更新
4. **空数据处理**: 当医生没有预约患者时，返回空数组
5. **日期格式**: 所有日期字段均使用ISO 8601标准格式

## 更新历史

- **v1.0** (2025-01-XX): 初始版本，实现医生查看患者详情功能
