package com.qingliao.server.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moment_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"moment_id", "user_id"}))
public class MomentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moment_id", nullable = false)
    private Long momentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at")
    private long createdAt = Instant.now().toEpochMilli();

    public MomentLike() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMomentId() { return momentId; }
    public void setMomentId(Long momentId) { this.momentId = momentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
