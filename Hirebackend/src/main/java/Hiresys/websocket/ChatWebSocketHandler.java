package Hiresys.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import Hiresys.entity.ChatMessage;
import Hiresys.entity.User;
import Hiresys.mapper.UserMapper;
import Hiresys.service.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private IChatService chatService;

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> sessionKeys = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            log.info("WebSocket连接请求: {}", query);
            if (query == null || query.isEmpty()) {
                sendMessage(session, createResponseMessage("error", "缺少连接参数", null));
                session.close(CloseStatus.BAD_DATA);
                return;
            }
            Map<String, String> queryParams = parseQueryParams(query);
            String userId = queryParams.get("userId");
            String userType = queryParams.get("type");
            String companyId = queryParams.get("companyId");

            if (userId == null || userType == null) {
                sendMessage(session, createResponseMessage("error", "连接参数不完整", null));
                session.close(CloseStatus.BAD_DATA);
                return;
            }

            Set<String> keys = new HashSet<>();
            String userKey = "user_" + userId;
            keys.add(userKey);

            if (companyId != null && !companyId.isEmpty()) {
                keys.add("company_" + companyId);
            } else if ("company".equals(userType)) {
                keys.add("company_" + userId);
            }

            for (String key : keys) {
                onlineUsers.put(key, session);
            }
            sessionKeys.put(session.getId(), keys);
            log.info("用户上线 keys={}, 当前在线映射数: {}", keys, onlineUsers.size());

            Map<String, Object> connectData = new HashMap<>();
            connectData.put("keys", keys);
            sendMessage(session, createResponseMessage("connected", "连接成功", connectData));
        } catch (Exception e) {
            log.error("处理WebSocket连接时出错", e);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            Map<String, Object> msgData = objectMapper.readValue(payload, Map.class);

            String type = (String) msgData.get("type");

            if ("chat".equals(type)) {
                handleChatMessage(session, msgData);
            } else if ("ping".equals(type)) {
                sendMessage(session, createResponseMessage("pong", "pong", null));
            } else {
                sendMessage(session, createResponseMessage("error", "不支持的消息类型: " + type, null));
            }
        } catch (Exception e) {
            log.error("消息处理失败", e);
            sendMessage(session, createResponseMessage("error", "消息处理失败: " + e.getMessage(), null));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        removeSession(session);
        log.info("用户下线, 当前在线映射数: {}", onlineUsers.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        removeSession(session);
    }

    private void handleChatMessage(WebSocketSession session, Map<String, Object> msgData) throws IOException {
        Integer senderId = parseInteger(msgData.get("senderId"));
        Long receiverId = parseLong(msgData.get("receiverId"));
        String receiverType = (String) msgData.get("receiverType");
        String content = (String) msgData.get("content");
        String messageType = (String) msgData.get("messageType");
        String clientMsgId = (String) msgData.get("clientMsgId");

        if (senderId == null || receiverId == null || receiverType == null || content == null || content.trim().isEmpty()) {
            sendMessage(session, createResponseMessage("error", "消息参数不完整", null));
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setReceiverType(receiverType);
        chatMessage.setContent(content);
        chatMessage.setMessageType(messageType != null ? messageType : "text");

        chatMessage = chatService.saveMessage(chatMessage);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", chatMessage.getId());
        responseData.put("senderId", chatMessage.getSenderId());
        responseData.put("receiverId", chatMessage.getReceiverId());
        responseData.put("receiverType", chatMessage.getReceiverType());
        responseData.put("content", chatMessage.getContent());
        responseData.put("messageType", chatMessage.getMessageType());
        responseData.put("sendTime", chatMessage.getSendTime().toString());
        responseData.put("clientMsgId", clientMsgId);

        WebSocketSession receiverSession = null;

        if ("company".equals(receiverType)) {
            log.info("消息发送给公司 ID: {}", receiverId);
            String companyKey = "company_" + receiverId;
            receiverSession = onlineUsers.get(companyKey);
            if (receiverSession != null && receiverSession.isOpen()) {
                log.info("命中公司在线映射: {}", companyKey);
            }

            if (receiverSession == null || !receiverSession.isOpen()) {
                User hrUser = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getRole, 1)
                        .eq(User::getCompanyId, receiverId)
                        .last("LIMIT 1")
                );
                if (hrUser != null) {
                    String hrKey = "user_" + hrUser.getId();
                    receiverSession = onlineUsers.get(hrKey);
                    log.info("找到 HR 用户: {} (ID: {})", hrUser.getUsername(), hrUser.getId());
                } else {
                    log.info("公司 ID: {} 没有对应的 HR 用户", receiverId);
                }
            }
        } else {
            String receiverKey = "user_" + receiverId;
            receiverSession = onlineUsers.get(receiverKey);
        }

        if (receiverSession != null && receiverSession.isOpen()) {
            sendMessage(receiverSession, createResponseMessage("chat", "收到新消息", responseData));
        } else {
            log.info("接收者离线: {}", "company".equals(receiverType) ? "公司_" + receiverId : receiverType + "_" + receiverId);
        }

        sendMessage(session, createResponseMessage("sent", "消息已发送", responseData));
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        return Integer.parseInt(value.toString());
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        return Long.parseLong(value.toString());
    }

    private void removeSession(WebSocketSession session) {
        if (session == null) return;
        Set<String> keys = sessionKeys.remove(session.getId());
        if (keys != null) {
            for (String key : keys) {
                WebSocketSession onlineSession = onlineUsers.get(key);
                if (onlineSession == session) {
                    onlineUsers.remove(key);
                }
            }
        } else {
            onlineUsers.values().remove(session);
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) throws IOException {
        if (session != null && session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    private Map<String, Object> createResponseMessage(String type, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    public void sendMessageToUser(String userType, String userId, Map<String, Object> message) {
        String key = userType + "_" + userId;
        WebSocketSession session = onlineUsers.get(key);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("发送消息到用户失败: key={}", key, e);
            }
        }
    }
}
