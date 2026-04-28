package com.qingliao.server.repository;

import com.qingliao.server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySessionIdOrderByTimestampAsc(Long sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.sessionId = :sessionId AND m.senderId != :userId")
    void markAsRead(Long sessionId, Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.sessionId = :sessionId AND m.senderId != :userId AND m.isRead = 0")
    int countUnread(Long sessionId, Long userId);

    @Query("SELECT m FROM Message m WHERE m.sessionId IN :sessionIds AND m.isRead = 0 AND m.senderId != :userId")
    List<Message> findUnreadBySessions(List<Long> sessionIds, Long userId);
}
