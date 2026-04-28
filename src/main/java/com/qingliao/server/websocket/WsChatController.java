package com.qingliao.server.websocket;

import com.qingliao.server.dto.WsMessage;
import com.qingliao.server.entity.Message;
import com.qingliao.server.entity.User;
import com.qingliao.server.service.MessageService;
import com.qingliao.server.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WsChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;

    public WsChatController(SimpMessagingTemplate messagingTemplate,
                            MessageService messageService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.userService = userService;
    }

    @MessageMapping("/chat.send")
    public void handleChat(@Payload WsMessage wsMsg) {
        User sender = userService.getById(wsMsg.getSenderId());
        wsMsg.setSenderName(sender != null ? sender.getNickname() : "");
        wsMsg.setSenderAvatar(sender != null ? sender.getAvatar() : "");

        // Save to database
        Message msg = messageService.sendMessage(wsMsg.getSessionId(), wsMsg.getSenderId(),
                wsMsg.getContent(), 0, wsMsg.getImageUrl(), null, 0);
        wsMsg.setMessageId(msg.getId());
        wsMsg.setTimestamp(msg.getTimestamp());

        // Broadcast to all session members
        messagingTemplate.convertAndSend("/topic/session/" + wsMsg.getSessionId(), wsMsg);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload WsMessage wsMsg) {
        User sender = userService.getById(wsMsg.getSenderId());
        wsMsg.setSenderName(sender != null ? sender.getNickname() : "");
        messagingTemplate.convertAndSend("/topic/session/" + wsMsg.getSessionId(), wsMsg);
    }

    @MessageMapping("/chat.read")
    public void handleRead(@Payload WsMessage wsMsg) {
        messageService.markAsRead(wsMsg.getSessionId(), wsMsg.getSenderId());
        messagingTemplate.convertAndSend("/topic/session/" + wsMsg.getSessionId(), wsMsg);
    }
}
