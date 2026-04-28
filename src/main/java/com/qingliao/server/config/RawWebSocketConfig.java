package com.qingliao.server.config;

import com.qingliao.server.websocket.CallSignalingHandler;
import com.qingliao.server.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final CallSignalingHandler callSignalingHandler;

    public RawWebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                              CallSignalingHandler callSignalingHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.callSignalingHandler = callSignalingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(callSignalingHandler, "/ws/call")
                .setAllowedOriginPatterns("*");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}
