package com.qingliao.server.repository;

import com.qingliao.server.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    @Query("SELECT cs FROM ChatSession cs INNER JOIN SessionMember sm ON cs.id = sm.sessionId WHERE sm.userId = :userId ORDER BY cs.lastMessageTime DESC")
    List<ChatSession> findSessionsByUserId(Long userId);

    @Query("SELECT cs.id FROM ChatSession cs")
    List<Long> findAllSessionIds();
}
