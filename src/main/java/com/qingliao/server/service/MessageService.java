package com.qingliao.server.service;

import com.qingliao.server.entity.*;
import com.qingliao.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepo;
    private final ChatSessionRepository sessionRepo;
    private final SessionMemberRepository memberRepo;

    public MessageService(MessageRepository messageRepo, ChatSessionRepository sessionRepo,
                          SessionMemberRepository memberRepo) {
        this.messageRepo = messageRepo;
        this.sessionRepo = sessionRepo;
        this.memberRepo = memberRepo;
    }

    @Transactional
    public Message sendMessage(Long sessionId, Long senderId, String content, int type,
                               String imageUrl, String fileName, long fileSize) {
        Message msg = new Message();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(content != null ? content : "");
        msg.setType(type);
        msg.setImageUrl(imageUrl != null ? imageUrl : "");
        msg.setFileName(fileName != null ? fileName : "");
        msg.setFileSize(fileSize);

        Message saved = messageRepo.save(msg);

        // Update session last message
        sessionRepo.findById(sessionId).ifPresent(session -> {
            String preview;
            if (type == 1) preview = "[图片]";
            else if (type == 2) preview = "[文件]" + (fileName != null && !fileName.isEmpty() ? " " + fileName : "");
            else preview = (content != null ? content : "");
            session.setLastMessage(preview);
            session.setLastMessageTime(saved.getTimestamp());
            sessionRepo.save(session);
        });

        // Increment unread for other members
        memberRepo.incrementUnread(sessionId, senderId);

        return saved;
    }

    public Message getMessageById(Long id) {
        return messageRepo.findById(id).orElse(null);
    }

    public List<Message> getMessages(Long sessionId) {
        return messageRepo.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    public List<Message> getMessagesAfter(Long sessionId, long afterTimestamp) {
        return messageRepo.findBySessionIdAndTimestampGreaterThanOrderByTimestampAsc(sessionId, afterTimestamp);
    }

    @Transactional
    public void markAsRead(Long sessionId, Long userId) {
        messageRepo.markAsRead(sessionId, userId);
        memberRepo.clearUnread(sessionId, userId);
    }

    @Transactional
    public boolean recallMessage(Long messageId, Long userId) {
        Message msg = messageRepo.findById(messageId).orElse(null);
        if (msg == null || !msg.getSenderId().equals(userId)) return false;
        msg.setRecalled(1);
        msg.setContent("[消息已撤回]");
        messageRepo.save(msg);
        return true;
    }

    public void deleteMessage(Long messageId, Long userId) {
        Message msg = messageRepo.findById(messageId).orElse(null);
        if (msg != null && msg.getSenderId().equals(userId)) {
            messageRepo.delete(msg);
        }
    }
}
