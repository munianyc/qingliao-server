package com.qingliao.server.schedule;

import com.qingliao.server.repository.ChatSessionRepository;
import com.qingliao.server.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(MessageCleanupTask.class);
    private static final int KEEP_READ_COUNT = 20;
    private static final long SEVEN_DAYS_MS = 7 * 24 * 3600 * 1000L;

    private final MessageRepository messageRepo;
    private final ChatSessionRepository sessionRepo;

    public MessageCleanupTask(MessageRepository messageRepo, ChatSessionRepository sessionRepo) {
        this.messageRepo = messageRepo;
        this.sessionRepo = sessionRepo;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldMessages() {
        log.info("Starting message cleanup...");
        long cutoff = System.currentTimeMillis() - SEVEN_DAYS_MS;
        List<Long> sessionIds = sessionRepo.findAllSessionIds();
        int totalDeleted = 0;

        for (Long sessionId : sessionIds) {
            try {
                int deleted = messageRepo.deleteOldReadMessages(sessionId, KEEP_READ_COUNT, cutoff);
                totalDeleted += deleted;
            } catch (Exception e) {
                log.error("Failed to cleanup messages for session {}", sessionId, e);
            }
        }

        log.info("Message cleanup done. Sessions processed: {}, messages deleted: {}", sessionIds.size(), totalDeleted);
    }
}
