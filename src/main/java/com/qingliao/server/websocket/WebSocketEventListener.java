package com.qingliao.server.websocket;

import com.qingliao.server.dto.WsMessage;
import com.qingliao.server.security.JwtUtil;
import com.qingliao.server.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate,
                                   JwtUtil jwtUtil, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        // Extract user from headers and set online
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();
        if (destination != null && destination.startsWith("/user/")) {
            Principal principal = headers.getUser();
            if (principal != null) {
                sessionUserMap.put(headers.getSessionId(), Long.parseLong(principal.getName()));
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Long userId = sessionUserMap.remove(event.getSessionId());
        if (userId != null) {
            userService.updateOnlineStatus(userId, 0);

            WsMessage statusMsg = new WsMessage();
            statusMsg.setType("user_offline");
            statusMsg.setSenderId(userId);
            messagingTemplate.convertAndSend("/topic/status", statusMsg);
        }
    }
}
