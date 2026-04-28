package com.qingliao.server.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(length = 512)
    private String avatar = "";

    @Column(length = 255)
    private String signature = "";

    @Column
    private int gender = 0;

    @Column(length = 64)
    private String region = "";

    @Column(name = "online_status")
    private int onlineStatus = 0;

    @Column(name = "last_online")
    private long lastOnline = 0;

    @Column(name = "created_at")
    private long createdAt = Instant.now().toEpochMilli();

    @Column(name = "qid_modified_at")
    private Long qidModifiedAt = 0L;

    public User() {}

    public User(String username, String passwordHash, String nickname) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.createdAt = Instant.now().toEpochMilli();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public int getGender() { return gender; }
    public void setGender(int gender) { this.gender = gender; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public int getOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(int onlineStatus) { this.onlineStatus = onlineStatus; }
    public long getLastOnline() { return lastOnline; }
    public void setLastOnline(long lastOnline) { this.lastOnline = lastOnline; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getQidModifiedAt() { return qidModifiedAt; }
    public void setQidModifiedAt(Long qidModifiedAt) { this.qidModifiedAt = qidModifiedAt; }
}
