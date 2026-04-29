package com.qingliao.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingliao.server.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallSignalingHandler extends TextWebSocketHandler {

    private final Logger log = LoggerFactory.getLogger(CallSignalingHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtUtil jwtUtil;

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final Set<Long> busyUsers = ConcurrentHashMap.newKeySet();

    public CallSignalingHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null || !jwtUtil.validateToken(token)) {
            try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
            return;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("Call WS connected: userId={}, total sessions={}", userId,
            userSessions.getOrDefault(userId, Set.of()).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdForSession(session);
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    busyUsers.remove(userId);
                }
            }
            log.info("Call WS disconnected: userId={}, remaining sessions={}", userId,
                userSessions.getOrDefault(userId, Set.of()).size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMsg) {
        try {
            Long userId = getUserId(session);
            if (userId == null) return;

            @SuppressWarnings("unchecked")
            Map<String, Object> msg = mapper.readValue(textMsg.getPayload(), Map.class);
            String callType = (String) msg.get("type");
            if (callType == null) return;

            switch (callType) {
                case "call_offer": {
                    Number targetNum = (Number) msg.get("targetId");
                    if (targetNum == null) return;
                    long targetId = targetNum.longValue();

                    if (busyUsers.contains(targetId)) {
                        // Verify target actually has an active call signaling connection
                        if (isUserOnline(targetId)) {
                            sendToUser(userId, Map.of("type", "call_busy", "targetId", targetId));
                            return;
                        } else {
                            // Stale busy — clear it
                            busyUsers.remove(targetId);
                        }
                    }

                    busyUsers.add(userId);
                    busyUsers.add(targetId);

                    Map<String, Object> forward = new HashMap<>();
                    forward.put("type", "call_offer");
                    forward.put("callerId", userId);
                    forward.put("callerName", msg.getOrDefault("callerName", ""));
                    forward.put("sdp", msg.get("sdp"));
                    forward.put("videoMode", msg.getOrDefault("videoMode", true));
                    sendToUser(targetId, forward);
                    break;
                }
                case "call_answer": {
                    Number targetNum = (Number) msg.get("targetId");
                    if (targetNum == null) return;
                    Map<String, Object> forward = new HashMap<>();
                    forward.put("type", "call_answer");
                    forward.put("callerId", userId);
                    forward.put("sdp", msg.get("sdp"));
                    sendToUser(targetNum.longValue(), forward);
                    break;
                }
                case "ice_candidate": {
                    Number targetNum = (Number) msg.get("targetId");
                    if (targetNum == null) return;
                    Map<String, Object> forward = new HashMap<>();
                    forward.put("type", "ice_candidate");
                    forward.put("from", userId);
                    forward.put("candidate", msg.get("candidate"));
                    forward.put("sdpMid", msg.get("sdpMid"));
                    forward.put("sdpMLineIndex", msg.get("sdpMLineIndex"));
                    sendToUser(targetNum.longValue(), forward);
                    break;
                }
                case "call_hangup": {
                    Number targetNum = (Number) msg.get("targetId");
                    busyUsers.remove(userId);
                    if (targetNum != null) {
                        long tid = targetNum.longValue();
                        busyUsers.remove(tid);
                        Map<String, Object> forward = new HashMap<>();
                        forward.put("type", "call_hangup");
                        forward.put("from", userId);
                        sendToUser(tid, forward);
                    }
                    break;
                }
                case "call_reject": {
                    Number targetNum = (Number) msg.get("targetId");
                    busyUsers.remove(userId);
                    if (targetNum != null) {
                        long tid = targetNum.longValue();
                        busyUsers.remove(tid);
                        Map<String, Object> forward = new HashMap<>();
                        forward.put("type", "call_reject");
                        forward.put("from", userId);
                        sendToUser(tid, forward);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Call signaling error", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Call WS transport error", exception);
    }

    public boolean isUserOnline(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return false;
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) return true;
        }
        return false;
    }

    private void sendToUser(Long userId, Map<String, Object> payload) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) return;
        String json;
        try { json = mapper.writeValueAsString(payload); } catch (IOException e) { return; }
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try { s.sendMessage(msg); } catch (IOException ignored) {}
            }
        }
    }

    private Long getUserId(WebSocketSession session) { return getUserIdForSession(session); }

    private Long getUserIdForSession(WebSocketSession session) {
        for (Map.Entry<Long, Set<WebSocketSession>> e : userSessions.entrySet()) {
            for (WebSocketSession s : e.getValue()) {
                if (s.getId().equals(session.getId())) return e.getKey();
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
}
