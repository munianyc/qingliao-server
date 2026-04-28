package com.qingliao.server.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_members", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"}))
public class SessionMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "unread_count")
    private int unreadCount = 0;

    @Column(name = "joined_at")
    private long joinedAt = Instant.now().toEpochMilli();

    public SessionMember() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
}
