package com.example.springboot.websocket;

import com.example.springboot.config.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 候补队列WebSocket服务器
 * 用于实时推送候补状态变化
 */
@ServerEndpoint("/ws/waitlist/{userId}")
@Component
public class WaitlistWebSocketServer {
    
    private static final Logger logger = LoggerFactory.getLogger(WaitlistWebSocketServer.class);
    
    // 静态变量，用来记录当前在线连接数
    private static int onlineCount = 0;
    
    // concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象
    private static CopyOnWriteArraySet<WaitlistWebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    
    // 用户ID到WebSocket的映射
    private static ConcurrentHashMap<Long, WaitlistWebSocketServer> userWebSocketMap = new ConcurrentHashMap<>();
    
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    // 接收userId
    private Long userId;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        try {
            this.session = session;
            this.userId = Long.parseLong(userIdStr);
            
            // 添加到set中
            webSocketSet.add(this);
            userWebSocketMap.put(this.userId, this);
            
            addOnlineCount(); // 在线数加1
            logger.info("用户{}连接WebSocket，当前在线人数为: {}", userId, getOnlineCount());
            
            // 发送连接成功消息
            sendMessage(createMessage("CONNECTED", "WebSocket连接成功", null));
            
        } catch (Exception e) {
            logger.error("WebSocket连接异常: userId={}, error={}", userIdStr, e.getMessage(), e);
        }
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        // 从set中删除
        webSocketSet.remove(this);
        if (userId != null) {
            userWebSocketMap.remove(userId);
        }
        
        subOnlineCount(); // 在线数减1
        logger.info("用户{}断开WebSocket连接，当前在线人数为: {}", userId, getOnlineCount());
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("收到用户{}的消息: {}", userId, message);
        
        try {
            // 解析客户端消息
            Map<String, Object> clientMessage = objectMapper.readValue(message, Map.class);
            String type = (String) clientMessage.get("type");
            
            if ("PING".equals(type)) {
                // 心跳检测
                sendMessage(createMessage("PONG", "心跳响应", null));
            } else if ("SUBSCRIBE_WAITLIST".equals(type)) {
                // 订阅候补状态
                Long scheduleId = Long.valueOf(clientMessage.get("scheduleId").toString());
                logger.info("用户{}订阅排班{}的候补状态", userId, scheduleId);
                sendMessage(createMessage("SUBSCRIBED", "已订阅候补状态", Map.of("scheduleId", scheduleId)));
            }
            
        } catch (Exception e) {
            logger.error("处理WebSocket消息异常: userId={}, message={}, error={}", 
                userId, message, e.getMessage(), e);
        }
    }
    
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("用户{}的WebSocket发生错误: {}", userId, error.getMessage(), error);
    }
    
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        if (this.session != null && this.session.isOpen()) {
            this.session.getBasicRemote().sendText(message);
        }
    }
    
    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, Long userId) throws IOException {
        logger.info("推送消息到用户{}, 推送内容: {}", userId, message);
        
        if (userId != null) {
            WaitlistWebSocketServer webSocket = userWebSocketMap.get(userId);
            if (webSocket != null) {
                webSocket.sendMessage(message);
            } else {
                logger.warn("用户{}不在线，无法推送消息", userId);
            }
        } else {
            // 群发消息
            for (WaitlistWebSocketServer item : webSocketSet) {
                try {
                    item.sendMessage(message);
                } catch (IOException e) {
                    logger.error("推送消息失败: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * 推送候补成功消息
     */
    public static void pushWaitlistSuccess(Long userId, Long scheduleId, Long appointmentId, String doctorName, String appointmentDate, String timeSlot) {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("scheduleId", scheduleId);
            data.put("appointmentId", appointmentId);
            data.put("doctorName", doctorName);
            data.put("appointmentDate", appointmentDate);
            data.put("timeSlot", timeSlot);
            data.put("timestamp", System.currentTimeMillis());
            
            String message = createMessage("WAITLIST_SUCCESS", "候补成功！您的候补已转为预约", data);
            sendInfo(message, userId);
            logger.info("推送候补成功消息: userId={}, scheduleId={}, appointmentId={}", userId, scheduleId, appointmentId);
            
        } catch (Exception e) {
            logger.error("推送候补成功消息失败: userId={}, scheduleId={}, error={}", userId, scheduleId, e.getMessage(), e);
        }
    }
    
    /**
     * 推送候补排队位次更新
     */
    public static void pushWaitlistRankUpdate(Long userId, int newRank, int queueSize) {
        try {
            Map<String, Object> data = Map.of(
                "rank", newRank,
                "queueSize", queueSize,
                "timestamp", System.currentTimeMillis()
            );
            
            String message = createMessage("RANK_UPDATE", "排队位次更新", data);
            sendInfo(message, userId);
            
        } catch (Exception e) {
            logger.error("推送排队位次更新失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 推送号源释放通知
     */
    public static void pushSlotAvailable(Long scheduleId, String doctorName, String appointmentDate, String timeSlot) {
        try {
            Map<String, Object> data = Map.of(
                "scheduleId", scheduleId,
                "doctorName", doctorName,
                "appointmentDate", appointmentDate,
                "timeSlot", timeSlot,
                "timestamp", System.currentTimeMillis()
            );
            
            String message = createMessage("SLOT_AVAILABLE", "号源释放通知", data);
            
            // 广播给所有在线用户（可以根据需要优化为只推送给相关用户）
            sendInfo(message, null);
            
        } catch (Exception e) {
            logger.error("推送号源释放通知失败: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
        }
    }
    
    /**
     * 创建标准消息格式
     */
    private static String createMessage(String type, String message, Object data) {
        try {
            Map<String, Object> messageMap = Map.of(
                "type", type,
                "message", message,
                "data", data != null ? data : Map.of(),
                "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(messageMap);
        } catch (Exception e) {
            logger.error("创建消息失败: {}", e.getMessage(), e);
            return "{\"type\":\"ERROR\",\"message\":\"消息创建失败\"}";
        }
    }
    
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
    
    public static synchronized void addOnlineCount() {
        WaitlistWebSocketServer.onlineCount++;
    }
    
    public static synchronized void subOnlineCount() {
        WaitlistWebSocketServer.onlineCount--;
    }
    
    /**
     * 获取在线用户列表
     */
    public static Map<Long, Boolean> getOnlineUsers() {
        Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();
        for (Long userId : userWebSocketMap.keySet()) {
            onlineUsers.put(userId, true);
        }
        return onlineUsers;
    }
}


