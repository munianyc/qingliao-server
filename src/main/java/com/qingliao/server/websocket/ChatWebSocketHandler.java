package com.qingliao.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingliao.server.entity.Message;
import com.qingliao.server.entity.SessionMember;
import com.qingliao.server.entity.User;
import com.qingliao.server.entity.UserFcmToken;
import com.qingliao.server.repository.SessionMemberRepository;
import com.qingliao.server.repository.UserFcmTokenRepository;
import com.qingliao.server.security.JwtUtil;
import com.qingliao.server.service.MessageService;
import com.qingliao.server.service.UserService;
import com.qingliao.server.service.FirebaseMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtUtil jwtUtil;
    private final MessageService messageService;
    private final UserService userService;
    private final SessionMemberRepository memberRepo;

    private final Map<Long, List<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> wsUserMap = new ConcurrentHashMap<>();
    private final FirebaseMessagingService firebaseMessagingService;
    private final UserFcmTokenRepository fcmTokenRepo;

    public ChatWebSocketHandler(JwtUtil jwtUtil, MessageService messageService,
                                UserService userService, SessionMemberRepository memberRepo,
                                FirebaseMessagingService firebaseMessagingService,
                                UserFcmTokenRepository fcmTokenRepo) {
        this.jwtUtil = jwtUtil;
        this.messageService = messageService;
        this.userService = userService;
        this.memberRepo = memberRepo;
        this.firebaseMessagingService = firebaseMessagingService;
        this.fcmTokenRepo = fcmTokenRepo;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null || !jwtUtil.validateToken(token)) {
            try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
            return;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        wsUserMap.put(session.getId(), userId);
        userSessions.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);
        userService.updateOnlineStatus(userId, 1);
        broadcastStatus(userId, "user_online");
        log.info("WebSocket connected: userId={}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMsg) {
        try {
            Long userId = wsUserMap.get(session.getId());
            if (userId == null) return;

            @SuppressWarnings("unchecked")
            Map<String, Object> msg = mapper.readValue(textMsg.getPayload(), Map.class);

            String wsType = (String) msg.getOrDefault("type", "chat");
            Number sessionIdNum = (Number) msg.get("sessionId");
            if (sessionIdNum == null) return;
            Long sessionId = sessionIdNum.longValue();

            if ("chat".equals(wsType)) {
                String content = (String) msg.getOrDefault("content", "");
                int msgType = ((Number) msg.getOrDefault("msgType", 0)).intValue();
                String imageUrl = (String) msg.getOrDefault("imageUrl", null);
                String fileName = (String) msg.getOrDefault("fileName", null);
                long fileSize = ((Number) msg.getOrDefault("fileSize", 0)).longValue();

                Message saved = messageService.sendMessage(sessionId, userId, content, msgType,
                        imageUrl, fileName, fileSize);

                User sender = userService.getById(userId);
                Map<String, Object> out = buildMessage(saved, sender);
                broadcastToSession(sessionId, out);
            } else if ("typing".equals(wsType)) {
                User sender = userService.getById(userId);
                Map<String, Object> out = new HashMap<>();
                out.put("type", "typing");
                out.put("sessionId", sessionId);
                out.put("senderId", userId);
                out.put("senderName", sender != null ? sender.getNickname() : "");
                broadcastToSession(sessionId, out);
            } else if ("read".equals(wsType)) {
                messageService.markAsRead(sessionId, userId);
                Map<String, Object> out = new HashMap<>();
                out.put("type", "read");
                out.put("sessionId", sessionId);
                out.put("readerId", userId);
                broadcastToSession(sessionId, out);
            } else if ("recall".equals(wsType)) {
                Number msgIdNum = (Number) msg.get("messageId");
                if (msgIdNum != null) {
                    boolean ok = messageService.recallMessage(msgIdNum.longValue(), userId);
                    if (ok) {
                        Map<String, Object> out = new HashMap<>();
                        out.put("type", "recall");
                        out.put("sessionId", sessionId);
                        out.put("messageId", msgIdNum.longValue());
                        out.put("senderId", userId);
                        broadcastToSession(sessionId, out);
                    }
                }
            }
        } catch (Exception e) {
            log.error("WebSocket message error", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = wsUserMap.remove(session.getId());
        if (userId != null) {
            List<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    userService.updateOnlineStatus(userId, 0);
                    broadcastStatus(userId, "user_offline");
                }
            }
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error", exception);
    }

    private void broadcastStatus(Long userId, String statusType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", statusType);
        payload.put("senderId", userId);
        payload.put("timestamp", System.currentTimeMillis());
        String json;
        try { json = mapper.writeValueAsString(payload); } catch (Exception e) { return; }
        TextMessage msg = new TextMessage(json);

        // Broadcast to all connected users (they filter on client side)
        for (List<WebSocketSession> sessions : userSessions.values()) {
            for (WebSocketSession ws : sessions) {
                if (ws.isOpen()) {
                    try { ws.sendMessage(msg); } catch (IOException ignored) {}
                }
            }
        }
    }

    /** Public method called by REST controllers to push to session members */
    public void broadcastToSession(Long sessionId, Map<String, Object> payload) {
        String json;
        try { json = mapper.writeValueAsString(payload); } catch (Exception e) { return; }
        TextMessage msg = new TextMessage(json);

        List<SessionMember> members = memberRepo.findBySessionId(sessionId);
        for (SessionMember member : members) {
            List<WebSocketSession> sessions = userSessions.get(member.getUserId());
            boolean isOnline = false;

            if (sessions != null) {
                for (WebSocketSession ws : sessions) {
                    if (ws.isOpen()) {
                        try {
                            ws.sendMessage(msg);
                            isOnline = true;
                        } catch (IOException ignored) {}
                    }
                }
            }

            // 如果用户不在线，异步发送Firebase推送（避免阻塞消息发送响应）
            if (!isOnline && "chat".equals(payload.get("type"))) {
                Long senderId = payload.get("senderId") != null ? ((Number) payload.get("senderId")).longValue() : 0;
                String senderName = (String) payload.getOrDefault("senderName", "好友");
                String content = (String) payload.getOrDefault("content", "");
                Long uid = member.getUserId();
                if (!uid.equals(senderId)) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            List<UserFcmToken> fcmTokens = fcmTokenRepo.findByUserId(uid);
                            for (UserFcmToken fcmToken : fcmTokens) {
                                firebaseMessagingService.sendChatPush(
                                        fcmToken.getFcmToken(), senderName, content, sessionId, senderId);
                            }
                            log.info("FCM sent to userId={} for message from {}", uid, senderName);
                        } catch (Exception e) {
                            log.error("Failed to send FCM to userId={}", uid, e);
                        }
                    });
                }
            }
        }
    }

    /** Send a message directly to a user (not via session membership) */
    public void sendToUserDirect(Long userId, Map<String, Object> payload) {
        String json;
        try { json = mapper.writeValueAsString(payload); } catch (Exception e) { return; }
        TextMessage msg = new TextMessage(json);
        List<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            for (WebSocketSession ws : sessions) {
                if (ws.isOpen()) {
                    try { ws.sendMessage(msg); } catch (IOException ignored) {}
                }
            }
        }
    }

    private WebSocketSession findSession(String wsId) {
        for (List<WebSocketSession> list : userSessions.values()) {
            for (WebSocketSession s : list) {
                if (s.getId().equals(wsId)) return s;
            }
        }
        return null;
    }

    private String extractToken(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "token".equals(kv[0])) return kv[1];
            }
        }
        return null;
    }

    private Map<String, Object> buildMessage(Message msg, User sender) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "chat");
        map.put("messageId", msg.getId());
        map.put("sessionId", msg.getSessionId());
        map.put("senderId", msg.getSenderId());
        map.put("content", msg.getContent());
        map.put("msgType", msg.getType());
        map.put("imageUrl", msg.getImageUrl());
        map.put("fileName", msg.getFileName());
        map.put("fileSize", msg.getFileSize());
        map.put("timestamp", msg.getTimestamp());
        map.put("senderName", sender != null ? sender.getNickname() : "");
        return map;
    }
}
