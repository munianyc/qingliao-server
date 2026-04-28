package com.qingliao.server.repository;

import com.qingliao.server.entity.SessionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface SessionMemberRepository extends JpaRepository<SessionMember, Long> {

    List<SessionMember> findByUserId(Long userId);

    Optional<SessionMember> findBySessionIdAndUserId(Long sessionId, Long userId);

    List<SessionMember> findBySessionId(Long sessionId);

    @Query("SELECT sm FROM SessionMember sm WHERE sm.sessionId = :sessionId AND sm.userId != :userId")
    List<SessionMember> findOtherMembers(Long sessionId, Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE SessionMember sm SET sm.unreadCount = sm.unreadCount + 1 WHERE sm.sessionId = :sessionId AND sm.userId != :userId")
    void incrementUnread(Long sessionId, Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE SessionMember sm SET sm.unreadCount = 0 WHERE sm.sessionId = :sessionId AND sm.userId = :userId")
    void clearUnread(Long sessionId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SessionMember sm WHERE sm.userId = :userId")
    void deleteByUserId(Long userId);
}
