# WebSocket 和自动候补功能测试指南

## 问题修复总结

### 1. 前端修复
- ✅ 修复了候补页面缺少 `webSocketManager` 导入的问题
- ✅ 添加了 WebSocket 事件监听器的注册
- ✅ 修复了 Vue 文件中的语法错误

### 2. 后端修复
- ✅ 修复了 `audit_log` 表缺少 `user_role` 字段导致的错误
- ✅ 确认了自动候补逻辑在取消预约时会被触发
- ✅ 确认了 WebSocket 推送消息的实现

## 测试步骤

### 测试 WebSocket 连接

1. **启动后端服务**
   ```bash
   cd springboot
   mvn spring-boot:run
   ```

2. **启动前端服务**
   ```bash
   cd client-mini-app
   npm run dev:mp-weixin
   ```

3. **检查 WebSocket 连接**
   - 打开微信开发者工具
   - 登录用户账号
   - 查看控制台是否有 "WebSocket连接成功" 的日志
   - 检查后端日志是否有用户连接的记录

### 测试自动候补功能

1. **准备测试数据**
   - 创建一个医生排班（可用号源数量设为1）
   - 创建两个患者账号（患者A和患者B）

2. **测试流程**
   ```
   步骤1: 患者A预约该排班（占用唯一号源）
   步骤2: 患者B加入候补队列并完成预支付
   步骤3: 患者A取消预约
   预期结果: 患者B自动获得预约，收到WebSocket实时通知
   ```

3. **验证点**
   - 患者B的候补页面实时更新为"候补成功"
   - 患者B收到弹窗通知
   - 后端日志显示候补处理成功
   - 数据库中患者B的候补状态变为"GRANTED"
   - 数据库中为患者B创建了新的预约记录

### 测试实时通知

1. **排队位次更新测试**
   - 多个患者加入同一排班的候补队列
   - 观察各患者的排队位次是否实时更新

2. **候补成功通知测试**
   - 按照上述自动候补测试流程
   - 验证WebSocket推送和前端处理

## 调试技巧

### 前端调试
```javascript
// 在候补页面的 onLoad 方法中添加调试代码
console.log('WebSocket连接状态:', webSocketManager.getReadyState());
console.log('WebSocket是否连接:', webSocketManager.isConnected());

// 在 handleWaitlistSuccessEvent 方法中添加调试
console.log('收到候补成功事件:', data);
```

### 后端调试
```java
// 在 WaitlistWebSocketServer.pushWaitlistSuccess 方法中添加日志
logger.info("推送候补成功消息: userId={}, doctorName={}", userId, doctorName);

// 在 AppointmentService.processNextInWaitlist 方法中添加日志
logger.info("开始处理候补队列: scheduleId={}", scheduleId);
```

## 常见问题排查

### WebSocket 连接失败
1. 检查后端 WebSocket 配置是否正确
2. 检查前端 WebSocket URL 是否正确
3. 检查用户是否已登录
4. 检查网络连接和防火墙设置

### 自动候补不工作
1. 检查候补队列中是否有有效的预支付订单
2. 检查取消预约的逻辑是否调用了 `processNextInWaitlist`
3. 检查候补记录的状态是否正确
4. 检查 Redis 队列中的数据是否正确

### 实时通知不显示
1. 检查 WebSocket 连接是否正常
2. 检查前端事件监听器是否正确注册
3. 检查后端是否正确推送了消息
4. 检查消息格式是否正确

## 预期日志输出

### 成功的 WebSocket 连接
```
前端: WebSocket连接成功
后端: 用户123连接WebSocket，当前在线人数为: 1
```

### 成功的自动候补
```
后端: 候补队列自动创建预约成功: 患者ID=456, 排班ID=789, 订单号=ORDER123, waitlistId=101
前端: 收到候补成功通知: {doctorName: "张医生", appointmentDate: "2025-11-27", timeSlot: "morning"}
```

