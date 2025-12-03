# 易点通挂号系统 - 后端服务

> 基于 Spring Boot 的医院预约挂号管理平台后端系统

## 项目简介

易点通挂号系统是一个功能完善的医院预约挂号管理平台，支持患者在线预约挂号、医生排班管理、管理员系统配置等功能。系统采用前后端分离架构，本文档主要介绍后端服务部分。

## 技术栈

### 核心框架
- **Spring Boot 3.5.5** - 核心框架
- **Java 21** - 开发语言
- **Maven** - 项目管理工具

### 数据持久层
- **MyBatis 3.0.5** - ORM 框架
- **MySQL 8.4.0** - 关系型数据库

### 安全认证
- **Spring Security** - 安全框架
- **JWT (jjwt 0.12.5)** - 无状态认证

### 缓存
- **Spring Data Redis** - Redis 缓存支持

### 工具库
- **Lombok 1.18.34** - 代码简化
- **Bean Validation** - 参数验证
- **SpringDoc OpenAPI 2.7.0** - API 文档生成（Swagger）

## 项目结构

```
springboot/
├── src/main/java/com/example/springboot/
│   ├── common/              # 通用类
│   │   └── Result.java      # 统一响应结果封装
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java           # Spring Security 配置
│   │   ├── JwtUtil.java                  # JWT 工具类
│   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   ├── CustomUserDetailsService.java # 自定义用户详情服务
│   │   ├── RedisConfig.java              # Redis 配置
│   │   ├── SwaggerConfig.java            # Swagger 配置
│   │   └── ...
│   ├── constants/           # 常量类
│   │   └── RoleConstants.java  # 角色常量定义
│   ├── controller/          # 控制器层（19个）
│   │   ├── AuthController.java              # 认证控制器
│   │   ├── PatientController.java           # 患者控制器
│   │   ├── DoctorController.java            # 医生控制器
│   │   ├── AdminController.java             # 管理员控制器
│   │   ├── AppointmentController.java       # 预约控制器
│   │   ├── ScheduleController.java          # 排班控制器
│   │   ├── ScheduleRuleController.java      # 排班规则控制器
│   │   ├── ClinicController.java            # 诊所控制器
│   │   ├── DepartmentController.java        # 科室控制器
│   │   ├── WaitlistController.java          # 候补队列控制器
│   │   ├── QaController.java                # QA 问答控制器
│   │   └── ...
│   ├── service/             # 服务层（17个）
│   │   ├── UserService.java
│   │   ├── PatientService.java
│   │   ├── DoctorService.java
│   │   ├── AppointmentService.java
│   │   ├── ScheduleService.java
│   │   └── ...
│   ├── mapper/              # MyBatis 映射接口（13个）
│   │   ├── UserMapper.java
│   │   ├── PatientMapper.java
│   │   ├── DoctorMapper.java
│   │   └── ...
│   ├── entity/              # 实体类（13个）
│   │   ├── User.java              # 用户实体
│   │   ├── Patient.java           # 患者实体
│   │   ├── Doctor.java            # 医生实体
│   │   ├── Admin.java             # 管理员实体
│   │   ├── Appointment.java       # 预约实体
│   │   ├── Schedule.java          # 排班实体
│   │   ├── ScheduleRule.java      # 排班规则实体
│   │   ├── Clinic.java            # 诊所实体
│   │   ├── Department.java        # 科室实体
│   │   ├── Waitlist.java          # 候补队列实体
│   │   ├── ApplicationRequest.java # 申请记录实体
│   │   └── ...
│   ├── dto/                 # 数据传输对象（47个）
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── CreateAppointmentRequest.java
│   │   └── ...
│   ├── exception/           # 异常处理
│   │   ├── CustomerException.java      # 自定义异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   └── SpringbootApplication.java  # 启动类
│
├── src/main/resources/
│   ├── application.yml      # 应用配置文件
│   └── mapper/              # MyBatis XML 映射文件（12个）
│       ├── UserMapper.xml
│       ├── PatientMapper.xml
│       ├── DoctorMapper.xml
│       └── ...
│
├── SQL.sql                  # 数据库建表脚本
├── SQL_DATA.sql             # 数据初始化脚本
└── pom.xml                  # Maven 配置文件
```

## 核心功能模块

### 1. 用户认证与授权
- **统一登录**：支持患者、医生、管理员三种角色登录
- **JWT 认证**：基于 Token 的无状态认证机制
- **角色权限控制**：基于 Spring Security 的细粒度权限管理
- **密码加密**：BCrypt 密码加密存储

### 2. 患者管理
- 患者注册与身份验证
- 患者信息管理
- 预约挂号功能
- 预约记录查询
- 候补队列加入与退出

### 3. 医生管理
- 医生信息管理
- 医生排班管理
- 医生申请管理（调班、信息修改）
- 医生工作台（查看预约、患者信息）

### 4. 排班管理
- **排班规则管理**：
  - 周期性排班规则（如：每周一三五上午）
  - 自定义排班规则
  - 模板化排班规则
- **排班记录管理**：
  - 手动创建排班
  - 批量生成排班
  - 排班查询与统计
- **号源管理**：
  - 号源类型（普通号、专家号、VIP号）
  - 号源数量调整

### 5. 预约管理
- 在线预约挂号
- 预约取消与改期
- 预约状态管理（待就诊、已完成、已取消）
- 预约费用管理

### 6. 候补队列
- 号源已满时加入候补
- 号源释放时自动通知候补患者
- 候补队列管理

### 7. 申请管理
- **调班申请**：医生申请取消或改期排班
- **信息修改申请**：医生申请修改个人信息
- 申请审批流程（管理员审批）

### 8. 机构管理
- **科室管理**：科室信息维护
- **诊所管理**：诊所信息维护
- 批量操作支持

### 9. 问答系统（QA）
- 常见问题管理
- 智能问答匹配
- 热门问题统计

### 10. 系统配置
- 系统参数配置
- 费用配置
- 系统初始化

## 系统角色

### 患者（PATIENT）
- 注册与登录
- 预约挂号
- 查看预约记录
- 加入候补队列

### 医生（DOCTOR）
- 登录认证
- 查看排班信息
- 提交调班申请
- 提交信息修改申请
- 查看预约患者信息

### 管理员（ADMIN）
- 系统全权限管理
- 用户管理
- 排班规则配置
- 申请审批
- 系统配置管理
- 数据统计

## 数据库设计

### 核心数据表

1. **user** - 用户表
2. **patient** - 患者表
3. **doctor** - 医生表
4. **admin** - 管理员表
5. **department** - 科室表
6. **clinic** - 诊所表
7. **schedule** - 排班表
8. **schedule_rule** - 排班规则表
9. **appointment** - 预约表
10. **waitlist** - 候补队列表
11. **application_request** - 申请记录表
12. **system_config** - 系统配置表

## 配置说明

### application.yml 配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456
    url: jdbc:mysql://localhost:3306/yi_dian_tong_2?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true
  
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your_redis_password_here

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.springboot.entity
  configuration:
    map-underscore-to-camel-case: true

server:
  port: 8080

jwt:
  secret: YiDianTongSecretKeyForJWTTokenGenerationAndValidation2025
  expiration: 86400000  # 24小时
```

### 环境要求

- **JDK 21+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis 6.0+** (可选，用于缓存)

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd springboot
```

### 2. 配置数据库

- 创建数据库：`yi_dian_tong_2`
- 执行 `SQL.sql` 创建表结构
- 执行 `SQL_DATA.sql` 初始化数据（可选）

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息。

### 4. 启动项目

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或使用 IDE 直接运行 SpringbootApplication.java
```

### 5. 访问 API 文档

启动后访问 Swagger UI：
- http://localhost:8080/swagger-ui/index.html

## API 文档

系统集成了 SpringDoc OpenAPI，启动后可通过 Swagger UI 查看完整的 API 文档。

### 主要 API 端点

#### 认证相关
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/logout` - 用户登出

#### 患者相关
- `GET /patient/profile` - 获取患者信息
- `POST /patient/appointments` - 创建预约
- `GET /patient/appointments` - 查询预约记录

#### 医生相关
- `GET /doctor/dashboard` - 医生工作台
- `GET /doctor/schedules` - 查询排班
- `POST /doctor/change-request` - 提交调班申请

#### 管理员相关
- `GET /admin/statistics` - 系统统计
- `POST /admin/schedule-rules` - 创建排班规则
- `POST /admin/approve-request` - 审批申请

详细 API 文档请查看 Swagger UI。

## 统一响应格式

所有 API 接口返回统一的响应格式：

```json
{
  "code": "200",
  "msg": "success",
  "data": {}
}
```

- `code`: 状态码（200 表示成功，500 表示失败）
- `msg`: 响应消息
- `data`: 响应数据

## 安全机制

1. **JWT 认证**：所有需要认证的接口都需要在请求头中携带 JWT Token
   ```
   Authorization: Bearer <token>
   ```

2. **角色权限**：基于 Spring Security 的角色权限控制
   - 使用 `@PreAuthorize` 注解进行方法级权限控制
   - 角色常量定义在 `RoleConstants` 类中

3. **密码加密**：使用 BCrypt 对用户密码进行加密存储

4. **CORS 配置**：支持跨域请求，已配置常见前端开发端口

## 异常处理

系统采用全局异常处理机制：

- `CustomerException` - 自定义业务异常
- `GlobalExceptionHandler` - 全局异常处理器，统一处理异常并返回标准响应格式

## 开发规范

1. **代码风格**：使用 Lombok 简化代码，遵循 Java 命名规范
2. **分层架构**：Controller → Service → Mapper → Database
3. **DTO 模式**：使用 DTO 进行数据传输，避免直接暴露实体类
4. **统一响应**：所有接口返回 `Result` 类型
5. **异常处理**：业务异常统一抛出 `CustomerException`

## 待优化功能

- [ ] 增加单元测试覆盖率
- [ ] 完善 API 文档注释
- [ ] 增加日志记录和监控
- [ ] 优化数据库查询性能
- [ ] 增加接口限流和防刷机制
- [ ] 完善缓存策略

## 许可证

本项目为内部项目，未经授权不得使用。

## 联系方式

如有问题或建议，请联系项目维护团队。

---

**最后更新**：2025年


