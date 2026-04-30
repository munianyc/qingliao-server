package com.qingliao.server.controller;

import com.qingliao.server.dto.*;
import com.qingliao.server.entity.Message;
import com.qingliao.server.entity.User;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.MessageService;
import com.qingliao.server.service.UserService;
import com.qingliao.server.websocket.ChatWebSocketHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.qingliao.server.service.FileStorageService;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtil securityUtil;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileStorageService fileStorage;
    private final ChatWebSocketHandler chatWsHandler;
    private final UserService userService;

    public MessageController(MessageService messageService, SecurityUtil securityUtil,
                             SimpMessagingTemplate messagingTemplate, FileStorageService fileStorage,
                             ChatWebSocketHandler chatWsHandler, UserService userService) {
        this.messageService = messageService;
        this.securityUtil = securityUtil;
        this.messagingTemplate = messagingTemplate;
        this.fileStorage = fileStorage;
        this.chatWsHandler = chatWsHandler;
        this.userService = userService;
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<List<Map<String, Object>>> getMessages(@PathVariable Long sessionId) {
        List<Message> messages = messageService.getMessages(sessionId);
        return ApiResponse.ok(buildMessageList(messages));
    }

    @GetMapping("/{sessionId}/after")
    public ApiResponse<List<Map<String, Object>>> getMessagesAfter(
            @PathVariable Long sessionId,
            @RequestParam("after") long afterTimestamp) {
        List<Message> messages = messageService.getMessagesAfter(sessionId, afterTimestamp);
        return ApiResponse.ok(buildMessageList(messages));
    }

    private List<Map<String, Object>> buildMessageList(List<Message> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message m : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("sessionId", m.getSessionId());
            map.put("senderId", m.getSenderId());
            map.put("content", m.getContent());
            map.put("type", m.getType());
            map.put("imageUrl", m.getImageUrl());
            map.put("fileName", m.getFileName());
            map.put("fileSize", m.getFileSize());
            map.put("isRead", m.getIsRead());
            map.put("recalled", m.getRecalled());
            map.put("timestamp", m.getTimestamp());
            User sender = userService.getById(m.getSenderId());
            if (sender != null) {
                map.put("senderName", sender.getNickname());
                map.put("senderAvatar", sender.getAvatar());
            }
            result.add(map);
        }
        return result;
    }

    @PostMapping("/{sessionId}")
    public ApiResponse<Message> sendMessage(@PathVariable Long sessionId,
                                            @RequestBody SendMessageRequest req) {
        Long userId = securityUtil.getCurrentUserId();
        int type = req.getType() != 0 ? req.getType() : 0;
        Message msg = messageService.sendMessage(sessionId, userId,
                req.getContent(), type, null, null, 0);

        pushWsMessage(sessionId, userId, msg);
        return ApiResponse.ok(msg);
    }

    @PostMapping("/{sessionId}/image")
    public ApiResponse<Message> sendImage(@PathVariable Long sessionId,
                                          @RequestParam("file") MultipartFile file) {
        Long userId = securityUtil.getCurrentUserId();
        String url = fileStorage.storeImage(file);
        Message msg = messageService.sendMessage(sessionId, userId, "", 1, url, null, 0);

        pushWsMessage(sessionId, userId, msg);
        return ApiResponse.ok(msg);
    }

    @PostMapping("/{sessionId}/file")
    public ApiResponse<Message> sendFile(@PathVariable Long sessionId,
                                          @RequestParam("file") MultipartFile file) {
        Long userId = securityUtil.getCurrentUserId();
        String originalName = file.getOriginalFilename();
        long fileSize = file.getSize();
        String url = fileStorage.storeFile(file, originalName);
        Message msg = messageService.sendMessage(sessionId, userId, "", 2, url, originalName, fileSize);

        pushWsMessage(sessionId, userId, msg);
        return ApiResponse.ok(msg);
    }

    @PostMapping("/{sessionId}/read")
    public ApiResponse<?> markRead(@PathVariable Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();
        messageService.markAsRead(sessionId, userId);

        // Broadcast read status to all session members
        Map<String, Object> readPayload = new HashMap<>();
        readPayload.put("type", "read");
        readPayload.put("sessionId", sessionId);
        readPayload.put("readerId", userId);
        chatWsHandler.broadcastToSession(sessionId, readPayload);
        return ApiResponse.ok();
    }

    @PostMapping("/{messageId}/recall")
    public ApiResponse<?> recall(@PathVariable Long messageId) {
        boolean ok = messageService.recallMessage(messageId, securityUtil.getCurrentUserId());
        if (!ok) return ApiResponse.error(400, "无法撤回（非本人发送）");

        // Broadcast recall via raw WebSocket
        Message msg = messageService.getMessageById(messageId);
        if (msg != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "recall");
            payload.put("sessionId", msg.getSessionId());
            payload.put("messageId", messageId);
            payload.put("senderId", securityUtil.getCurrentUserId());
            chatWsHandler.broadcastToSession(msg.getSessionId(), payload);
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<?> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId, securityUtil.getCurrentUserId());
        return ApiResponse.ok();
    }

    private void pushWsMessage(Long sessionId, Long userId, Message msg) {
        User sender = userService.getById(userId);

        // Push via STOMP (for web clients)
        WsMessage wsMsg = new WsMessage();
        wsMsg.setType("new_message");
        wsMsg.setSessionId(sessionId);
        wsMsg.setSenderId(userId);
        wsMsg.setContent(msg.getContent());
        wsMsg.setImageUrl(msg.getImageUrl());
        wsMsg.setMessageId(msg.getId());
        wsMsg.setTimestamp(msg.getTimestamp());
        wsMsg.setSenderName(sender != null ? sender.getNickname() : "");
        wsMsg.setSenderAvatar(sender != null ? sender.getAvatar() : "");
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, wsMsg);

        // Also push via raw WebSocket (for Android clients)
        Map<String, Object> rawPayload = new HashMap<>();
        rawPayload.put("type", "chat");
        rawPayload.put("messageId", msg.getId());
        rawPayload.put("sessionId", sessionId);
        rawPayload.put("senderId", userId);
        rawPayload.put("content", msg.getContent());
        rawPayload.put("msgType", msg.getType());
        rawPayload.put("imageUrl", msg.getImageUrl());
        rawPayload.put("fileName", msg.getFileName());
        rawPayload.put("fileSize", msg.getFileSize());
        rawPayload.put("timestamp", msg.getTimestamp());
        rawPayload.put("senderName", sender != null ? sender.getNickname() : "");
        chatWsHandler.broadcastToSession(sessionId, rawPayload);
    }
}
